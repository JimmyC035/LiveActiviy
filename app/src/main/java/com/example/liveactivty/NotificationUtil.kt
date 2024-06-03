package com.example.liveactivty

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

class NotificationUtil(val context: Context) {

    // 通知 渠道信息
    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "channel_silencefly96_notification_id"
        const val CHANNEL_NAME = "channel_silencefly96_notification_name"
    }

    /** 消息管理器 */
    private val mManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    /** 内部保存一个默认的通知创建器 */
    private var mNotificationBuilder: Notification.Builder? = null

    /**
     * 创建通知的builder，可以统一设置各个通知
     *
     * @param channelId 当前类使用的channelId
     * @param channelName 前类使用的channelName
     */
    fun createNotificationBuilder(
        channelId: String = CHANNEL_ID,
        channelName: String = CHANNEL_NAME
    ): Notification.Builder {
        if (mNotificationBuilder != null) {
            return mNotificationBuilder!!
        }

        // Android8 需要创建通知渠道
        if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannel(channelId, channelName)
        }

        // 创建NotificationBuilder，通知由service发送
        mNotificationBuilder = Notification.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_more)             // 必须设置，否则会奔溃
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, android.R.drawable.stat_notify_more))
            // .setCustomContentView(remoteViews)                          // 折叠后通知显示的布局
            // .setCustomHeadsUpContentView(remoteViews)                   // 横幅样式显示的布局
            // .setCustomBigContentView(remoteViews)                       // 展开后通知显示的布局
            // .setContent(remoteViews)                                    // 兼容低版本
            // .setColor(ContextCompat.getColor(context, R.color.blue))    // 小图标的颜色
            .setPriority(Notification.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)                // 默认配置,通知的提示音、震动等
        // .setAutoCancel(true)                                     // 允许点击后清除通知
        // .setContentIntent(pendingIntent)                         // 调集跳转

        return mNotificationBuilder!!
    }

    /**
     * 创建通知渠道，高版本要求，不然无法发送通知
     *
     * @param importance 重要程度
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    fun createNotificationChannel(
        channelId: String = CHANNEL_ID,
        channelName: String = CHANNEL_NAME,
        importance: Int = NotificationManager.IMPORTANCE_DEFAULT
    ) {
        val channel = NotificationChannel(
            channelId,
            channelName,
            importance
        ).apply {
            // 是否在应用图标的右上角展示小红点
            setShowBadge(false)
            // 推送消息时是否让呼吸灯闪烁。
            enableLights(true)
            // 推送消息时是否让手机震动。
            enableVibration(true)
            // 呼吸灯颜色
            lightColor = Color.BLUE
        }
        mManager.createNotificationChannel(channel)
    }

    /**
     * 创建简单的通知
     *
     * @param title 通知标题
     * @param content 通知内容
     * @param resID 图标
     * @param pendingIntent 点击跳转到指定页面的intent
     * @param notificationBuilder 通知builder
     */
    fun createNotification(
        title: String,
        content: String,
        resID: Int,
        pendingIntent: PendingIntent? = null,
        notificationBuilder: Notification.Builder? = null
    ): Notification {
        // 创建默认builder
        val builder = notificationBuilder ?: createNotificationBuilder()

        // 设置pendingIntent，为null的时候不要去覆盖之前的
        if(pendingIntent != null) {
            builder.setContentIntent(pendingIntent)
        }

        // 传入标题、内容、图标、跳转，创建标题
        return builder
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(resID)
            // .setContentIntent(pendingIntent)
            .build()
    }

    /**
     * 发送或更新(notificationId要保持一致)简单通知到通知栏
     *
     * @param title 通知标题
     * @param content 通知内容
     * @param resID 图标
     * @param pendingIntent 点击跳转到指定页面的intent
     * @param notificationBuilder 通知builder
     * @param notificationId 通知ID
     */
    fun sendOrUpdateNotification(
        title: String,
        content: String,
        resID: Int,
        pendingIntent: PendingIntent? = null,
        notificationBuilder: Notification.Builder? = null,
        notificationId: Int = NOTIFICATION_ID
    ) {
        val notification = createNotification(title, content, resID, pendingIntent, notificationBuilder)

        // 通过NOTIFICATION_ID发送，可借此关闭
        mManager.notify(notificationId, notification)
    }

    /**
     * 创建自定义通知
     *
     * @param remoteViews 显示的自定义view
     * @param pendingIntent 点击跳转到指定页面的intent
     * @param notificationBuilder 通知builder
     */
    fun createCustomNotification(
        remoteViews: RemoteViews,
        pendingIntent: PendingIntent? = null,
        notificationBuilder: Notification.Builder? = null
    ): Notification {
        // 创建默认builder
        val builder = notificationBuilder ?: createNotificationBuilder()

        // 设置pendingIntent，为null的时候不要去覆盖之前的
        if(pendingIntent != null) {
            builder.setContentIntent(pendingIntent)
        }

        // 创建通知
        return builder
            .setCustomContentView(remoteViews)               // 折叠后通知显示的布局
            .setCustomHeadsUpContentView(remoteViews)        // 横幅样式显示的布局
            .setCustomBigContentView(remoteViews)            // 展开后通知显示的布局
            .setContent(remoteViews)                         // 兼容低版本
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)  // 默认配置,通知的提示音、震动等
            // .setContentIntent(pendingIntent)                 // 通知点击跳转
            .build()
    }

    /**
     * 创建或更新(notificationId要保持一致)自定义通知并发送
     *
     * @param remoteViews 显示的自定义view
     * @param pendingIntent 点击跳转到指定页面的intent
     * @param notificationBuilder 通知builder
     * @param notificationId 通知ID
     */
    fun sendOrUpdateCustomNotification(
        remoteViews: RemoteViews,
        pendingIntent: PendingIntent? = null,
        notificationBuilder: Notification.Builder? = null,
        notificationId: Int = NOTIFICATION_ID
    ) {
        val notification =
            createCustomNotification(remoteViews, pendingIntent, notificationBuilder)

        // 通过NOTIFICATION_ID发送，可借此关闭
        mManager.notify(notificationId, notification)
    }



    /**
     * 关闭通知栏上对应notificationId的通知
     *
     * @param notificationId 通知ID
     */
    fun cancelNotification(
        notificationId: Int = NOTIFICATION_ID
    ) {
        mManager.cancel(notificationId)
    }
}
