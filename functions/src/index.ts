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
    logger.info("Trigger recibido", {alertId: event.params.alertId});

    if (!snapshot) return;

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

      if (uid === alert.userId) continue;
      if (user.notificationsEnabled === false) continue;

      const userLat = user.lastLocation?.lat;
      const userLng = user.lastLocation?.lng;

      if (typeof userLat !== "number" || typeof userLng !== "number") {
        continue;
      }

      const distance = distanceMeters(alert.lat, alert.lng, userLat, userLng);
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
      recipients: recipients.map((r) => ({uid: r.uid})),
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
      const notifRef = db
        .collection("users")
        .doc(recipient.uid)
        .collection("notifications")
        .doc();

      batch.set(notifRef, {
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

    logger.info("Guardando notificaciones en Firestore", {
      count: recipients.length,
    });

    await batch.commit();

    logger.info("Notificaciones guardadas en Firestore");

    const response = await admin.messaging().sendEachForMulticast({
      tokens: recipients.map((r) => r.token),
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

    logger.info("Resultado FCM", {
      successCount: response.successCount,
      failureCount: response.failureCount,
      responses: response.responses.map((r) => ({
        success: r.success,
        error: r.error?.message ?? null,
      })),
    });
  }
);