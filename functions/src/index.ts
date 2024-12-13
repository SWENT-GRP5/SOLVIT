import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import { notificationHandler } from './handlers/notificationHandler';

// Initialize Firebase Admin
admin.initializeApp();

// Export functions
export const sendNotification = functions.https.onCall(notificationHandler);
