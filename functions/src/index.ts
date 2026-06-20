import * as admin from "firebase-admin";
import {setGlobalOptions} from "firebase-functions/v2";
import {onDocumentCreated} from "firebase-functions/v2/firestore";
import * as logger from "firebase-functions/logger";

setGlobalOptions({maxInstances: 10});

admin.initializeApp();

export const notifyNearbyUsersOnNewAlert = onDocumentCreated(
  "alerts/{alertId}",
  async (event) => {
    const snapshot = event.data;
    logger.info("Trigger recibido", {alertId: event.params.alertId});

    if (!snapshot) {
      logger.info("Snapshot nulo");
      return;
    }

    const alert = snapshot.data();
    logger.info("Datos del aviso", {alert});

    if (
      !alert ||
      typeof alert.lat !== "number" ||
      typeof alert.lng !== "number"
    ) {
      logger.error("Aviso inválido: faltan lat/lng numéricos", {alert});
      return;
    }

    const usersSnap = await admin.firestore().collection("users").get();
    logger.info("Usuarios cargados", {count: usersSnap.size});

    const tokensToSend: string[] = [];

    for (const userDoc of usersSnap.docs) {
      const uid = userDoc.id;
      const user = userDoc.data();

      if (uid === alert.userId) continue;
      if (user.notificationsEnabled === false) continue;

      const userLat = user.lastLocation?.lat;
      const userLng = user.lastLocation?.lng;

      if (typeof userLat !== "number" || typeof userLng !== "number") {
        logger.info("Usuario sin localización válida", {uid});
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
        const tokenDoc = await admin.firestore()
          .collection("fcmTokens")
          .doc(uid)
          .get();

        const token = tokenDoc.data()?.token;

        if (typeof token === "string" && token.trim().length > 0) {
          tokensToSend.push(token);
        } else {
          logger.info("Usuario dentro del radio pero sin token", {uid});
        }
      }
    }

    logger.info("Tokens finales", {count: tokensToSend.length});

    if (tokensToSend.length === 0) {
      logger.info("No hay tokens a los que enviar");
      return;
    }

    const response = await admin.messaging().sendEachForMulticast({
      tokens: tokensToSend,
      notification: {
        title: "Nuevo aviso cerca de ti",
        body:
          `${alert.nombreMascota ?? "Mascota"} · ` +
          `${alert.tipoAviso ?? "Nuevo aviso"}`,
      },
      data: {
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
  },
);
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