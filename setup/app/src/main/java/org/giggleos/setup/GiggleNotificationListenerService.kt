package org.giggleos.setup

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/** Opt-in notification listener. It intentionally discards all notification content. */
class GiggleNotificationListenerService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        GiggleNotifications.add(
            applicationContext,
            sbn.key,
            sbn.packageName,
            sbn.postTime
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        GiggleNotifications.remove(applicationContext, sbn.key)
    }
}
