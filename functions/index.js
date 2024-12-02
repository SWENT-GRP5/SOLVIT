const functions = require('firebase-functions');
const admin = require('firebase-admin');

// Initialize Firebase Admin
admin.initializeApp();

/**
 * Cloud Function to send FCM notifications
 * Expects data in the following format:
 * {
 *   recipientToken: string,
 *   notification: {
 *     title: string,
 *     body: string
 *   },
 *   data: {
 *     [key: string]: string
 *   }
 * }
 */
exports.sendNotification = functions.https.onCall((data, context) => {
    console.log('Received notification request:', JSON.stringify(data));

    // Verify authentication
    if (!context.auth) {
        console.error('Unauthenticated request');
        throw new functions.https.HttpsError(
            'unauthenticated',
            'The function must be called while authenticated.'
        );
    }

    const { recipientToken, notification, data: messageData } = data;

    if (!recipientToken) {
        console.error('Missing recipient token');
        throw new functions.https.HttpsError(
            'invalid-argument',
            'Recipient token is required'
        );
    }

    if (!notification || !notification.title) {
        console.error('Invalid notification format');
        throw new functions.https.HttpsError(
            'invalid-argument',
            'Notification must include a title'
        );
    }

    // Construct message
    const message = {
        token: recipientToken,
        notification: {
            title: notification.title,
            body: notification.body || '',
        },
        data: messageData || {},
        android: {
            priority: 'high',
            notification: {
                channel_id: 'service_requests',
                priority: 'max',
                default_sound: true,
                default_vibrate_timings: true,
            }
        },
        apns: {
            payload: {
                aps: {
                    sound: 'default',
                    badge: 1
                }
            }
        }
    };

    console.log('Sending message:', JSON.stringify(message));

    // Send message
    return admin.messaging().send(message)
        .then((response) => {
            console.log('Successfully sent message:', response);
            return { success: true, messageId: response };
        })
        .catch((error) => {
            console.error('Error sending notification:', error);
            throw new functions.https.HttpsError(
                'internal',
                'Error sending notification',
                error
            );
        });
});
