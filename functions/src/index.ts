import * as admin from "firebase-admin";
import {setGlobalOptions} from "firebase-functions/v2";
import {onDocumentCreated} from "firebase-functions/v2/firestore";
import * as logger from "firebase-functions/logger";

setGlobalOptions({maxInstances: 10});

admin.initializeApp();

/**
 * Calcula la distancia en metros entre dos coordenadas geográficas.
 * @param {number} lat1 Latitud del punto 1.
 * @param {number} lng1 Longitud del punto 1.
 * @param {number} lat2 Latitud del punto 2.
 * @param {number} lng2 Longitud del punto 2.
 * @return {number} Distancia en metros.
 */
function distanceMeters(
  lat1: number,
  lng1: number,
  lat2: number,
  lng2: number
): number {
  const toRad = (value: number) => (value * Math.PI) / 180;
  const earthRadius = 6371000;

  const dLat = toRad(lat2 - lat1);
  const dLng = toRad(lng2 - lng1);

  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(lat1)) *
      Math.cos(toRad(lat2)) *
      Math.sin(dLng / 2) *
      Math.sin(dLng / 2);

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return earthRadius * c;
}

export const notifyNearbyUsersOnNewAlert = onDocumentCreated(
  "alerts/{alertId}",
  async (event) => {
    const snapshot = event.data;

    logger.info("Trigger recibido", {
      alertId: event.params.alertId,
    });

    if (!snapshot) {
      return;
    }

    const alert = snapshot.data();

    if (
      !alert ||
      typeof alert.lat !== "number" ||
      typeof alert.lng !== "number"
    ) {
      logger.error("Aviso inválido", {alert});
      return;
    }

    const db = admin.firestore();
    const usersSnap = await db.collection("users").get();
    const recipients: Array<{uid: string; token: string}> = [];

    for (const userDoc of usersSnap.docs) {
      const uid = userDoc.id;
      const user = userDoc.data();

      if (uid === alert.userId) {
        continue;
      }

      if (user.notificationsEnabled === false) {
        continue;
      }

      const userLat = user.lastLocation?.lat;
      const userLng = user.lastLocation?.lng;

      if (typeof userLat !== "number" || typeof userLng !== "number") {
        continue;
      }

      const distance = distanceMeters(
        alert.lat,
        alert.lng,
        userLat,
        userLng
      );

      const userRadius = user.notificationRadius ?? 1500;
      const alertRadius = alert.radioMetros ?? 1500;
      const effectiveRadius = Math.min(userRadius, alertRadius);

      logger.info("Usuario evaluado", {
        uid,
        distance,
        userRadius,
        alertRadius,
        effectiveRadius,
      });

      if (distance <= effectiveRadius) {
        const tokenDoc = await db.collection("fcmTokens").doc(uid).get();
        const token = tokenDoc.data()?.token;

        if (typeof token === "string" && token.trim().length > 0) {
          recipients.push({uid, token});
        } else {
          logger.info("Usuario dentro del radio pero sin token", {uid});
        }
      }
    }

    logger.info("Destinatarios finales", {
      count: recipients.length,
      recipients: recipients.map((recipient) => ({
        uid: recipient.uid,
      })),
    });

    if (recipients.length === 0) {
      logger.info("No hay destinatarios");
      return;
    }

    const title = "Nuevo aviso cerca de ti";
    const body =
      `${alert.nombreMascota ?? "Mascota"} · ` +
      `${alert.tipoAviso ?? "Nuevo aviso"}`;

    const batch = db.batch();

    for (const recipient of recipients) {
      const notificationRef = db
        .collection("users")
        .doc(recipient.uid)
        .collection("notifications")
        .doc();

      batch.set(notificationRef, {
        title,
        body,
        type: "NEARBY_ALERT",
        alertId: snapshot.id,
        petId: alert.petId ?? "",
        senderId: alert.userId ?? "",
        isRead: false,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
      });
    }

    await batch.commit();

    logger.info("Notificaciones de avisos guardadas", {
      count: recipients.length,
    });

    const response = await admin.messaging().sendEachForMulticast({
      tokens: recipients.map((recipient) => recipient.token),
      notification: {
        title,
        body,
      },
      data: {
        type: "NEARBY_ALERT",
        alertId: snapshot.id,
        petId: alert.petId ?? "",
        userId: alert.userId ?? "",
        tipoAviso: alert.tipoAviso ?? "",
      },
      android: {
        priority: "high",
        notification: {
          channelId: "petscue_alerts",
        },
      },
    });

    logger.info("Resultado FCM de aviso cercano", {
      successCount: response.successCount,
      failureCount: response.failureCount,
      responses: response.responses.map((item) => ({
        success: item.success,
        error: item.error?.message ?? null,
      })),
    });

    const invalidRecipientIds: string[] = [];

    response.responses.forEach((item, index) => {
      const errorCode = item.error?.code;

      if (
        errorCode === "messaging/registration-token-not-registered" ||
        errorCode === "messaging/invalid-registration-token"
      ) {
        invalidRecipientIds.push(recipients[index].uid);
      }
    });

    await Promise.all(
      invalidRecipientIds.map((uid) =>
        db.collection("fcmTokens").doc(uid).delete()
      )
    );
  }
);

export const notifyNewChatMessage = onDocumentCreated(
  "conversations/{conversationId}/messages/{messageId}",
  async (event) => {
    const messageSnapshot = event.data;
    const conversationId = event.params.conversationId;
    const messageId = event.params.messageId;

    if (!messageSnapshot) {
      logger.warn("Mensaje sin snapshot", {
        conversationId,
        messageId,
      });
      return;
    }

    const message = messageSnapshot.data();
    const senderId = String(message.senderId ?? "");
    const senderName = String(message.senderName ?? "Alguien");
    const messageText = String(message.text ?? "");

    if (!senderId || !messageText) {
      logger.warn("Mensaje inválido para notificación", {
        conversationId,
        messageId,
        senderId,
      });
      return;
    }

    const db = admin.firestore();

    const conversationSnapshot = await db
      .collection("conversations")
      .doc(conversationId)
      .get();

    if (!conversationSnapshot.exists) {
      logger.warn("Conversación no encontrada", {
        conversationId,
      });
      return;
    }

    const conversation = conversationSnapshot.data();
    const participantIds = conversation?.participantIds;

    if (!Array.isArray(participantIds)) {
      logger.warn("Conversación sin participantes válidos", {
        conversationId,
      });
      return;
    }

    const recipientIds = participantIds.filter(
      (participantId: unknown): participantId is string =>
        typeof participantId === "string" &&
        participantId !== senderId
    );

    if (recipientIds.length === 0) {
      logger.info("No hay destinatario para el mensaje", {
        conversationId,
        senderId,
      });
      return;
    }

    const title = `Nuevo mensaje de ${senderName}`;
    const body = messageText.length > 120 ?
      `${messageText.substring(0, 117)}...` :
      messageText;

    for (const recipientId of recipientIds) {
      const notificationRef = db
        .collection("users")
        .doc(recipientId)
        .collection("notifications")
        .doc();

      await notificationRef.set({
        title,
        body,
        type: "CHAT_MESSAGE",
        conversationId,
        senderId,
        isRead: false,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      const tokenSnapshot = await db
        .collection("fcmTokens")
        .doc(recipientId)
        .get();

      const token = tokenSnapshot.get("token");

      if (typeof token !== "string" || token.trim().length === 0) {
        logger.info("Destinatario sin token FCM", {
          recipientId,
          conversationId,
        });
        continue;
      }

      try {
        await admin.messaging().send({
          token,
          notification: {
            title,
            body,
          },
          data: {
            type: "CHAT_MESSAGE",
            conversationId,
            senderId,
            title,
            body,
          },
          android: {
            priority: "high",
            notification: {
              channelId: "petscue_alerts",
            },
          },
        });

        logger.info("Notificación de chat enviada", {
          recipientId,
          conversationId,
        });
      } catch (error) {
        logger.error("Error enviando notificación de chat", {
          recipientId,
          conversationId,
          error,
        });

        const errorCode = (error as {code?: string})?.code;

        if (
          errorCode === "messaging/registration-token-not-registered" ||
          errorCode === "messaging/invalid-registration-token"
        ) {
          await db.collection("fcmTokens").doc(recipientId).delete();
        }
      }
    }
  }
);
