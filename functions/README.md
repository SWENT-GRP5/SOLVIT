# Solvit Cloud Functions

This directory contains the Firebase Cloud Functions for the Solvit app, including the notification system.

## Notification System Setup

### Prerequisites
- Node.js version 18 (required for Firebase Functions)
- Firebase CLI (`npm install -g firebase-tools@11.30.0`)
- Firebase project with Blaze (pay-as-you-go) plan enabled
- Firebase Admin SDK initialized in your project

### Installation Steps

1. Install dependencies:
```bash
cd functions
npm install
```

2. Deploy the functions:
```bash
firebase deploy --only functions
```

### Cloud Function Configuration

The `sendNotification` function handles FCM (Firebase Cloud Messaging) notifications with the following features:
- Authentication verification
- Input validation
- Platform-specific configurations for Android and iOS
- Comprehensive error handling and logging

#### Function Input Format
```javascript
{
  recipientToken: string,    // FCM token of the recipient
  notification: {
    title: string,          // Notification title
    body: string           // Notification message
  },
  data: {                  // Optional additional data
    [key: string]: string
  }
}
```

### Android Integration

1. Ensure your `app/build.gradle.kts` includes the required dependencies:
```kotlin
dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.x.x"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-functions-ktx")
}
```

2. Configure the notification channel in your Application class:
```kotlin
class YourApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "service_requests",
                "Service Requests",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for service request updates"
                enableVibration(true)
            }
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
```

### Usage Examples

#### Kotlin (Android)
```kotlin
// Send a basic notification
notificationManager.sendNotification(
    recipientUserId = "userId",
    title = "New Message",
    body = "You have a new message",
    data = mapOf("type" to "message")
)

// Send a service request update
notificationManager.sendServiceRequestUpdateNotification(
    recipientUserId = "userId",
    requestId = "requestId",
    status = "completed",
    message = "Your service request has been completed"
)
```

### Troubleshooting

1. FCM Token Issues:
   - Ensure the FCM token is properly registered and updated
   - Check FcmTokenManager logs with tag "FCM_DEBUG"
   - Verify token is stored in Firestore

2. Notification Not Received:
   - Check Firebase Console for function execution logs
   - Verify the notification channel is created
   - Ensure app has notification permissions

3. Common Errors:
   - "Recipient token not found": User's FCM token is missing
   - "No authenticated user": Sender is not logged in
   - "Error sending notification": Check Firebase Console for detailed error logs

### Security Considerations

- All notification requests must be authenticated
- Sensitive information should not be included in notifications
- FCM tokens should be regularly updated and validated
- Use data messages for sensitive updates that shouldn't show when app is in background

### Testing

1. Local Testing:
```bash
cd functions
npm run serve
```

2. Production Testing:
- Monitor Firebase Console for function execution
- Check logcat with filter "FCM_DEBUG"
- Use Firebase Test Lab for device compatibility

### Best Practices

1. Token Management:
   - Update FCM token on app launch
   - Handle token refresh events
   - Clean up old tokens

2. Notification Content:
   - Keep messages concise
   - Include relevant action data
   - Use proper notification channels

3. Error Handling:
   - Implement retry logic for failed notifications
   - Log all errors with appropriate context
   - Handle edge cases (token expired, user logged out)
