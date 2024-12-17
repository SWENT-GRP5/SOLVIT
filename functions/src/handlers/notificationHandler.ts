import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

export const notificationHandler = async (data: any, context: functions.https.CallableContext) => {
  // Verify authentication
  if (!context.auth) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'The function must be called while authenticated.'
    );
  }

  const { recipientToken, notification, data: messageData } = data;

  try {
    const message = {
      token: recipientToken,
      notification: notification,
      data: messageData,
    };

    const response = await admin.messaging().send(message);
    console.log('Successfully sent message:', response);
    return { success: true, messageId: response };
  } catch (error) {
    console.error('Error sending message:', error);
    throw new functions.https.HttpsError('internal', 'Error sending notification');
  }
};
