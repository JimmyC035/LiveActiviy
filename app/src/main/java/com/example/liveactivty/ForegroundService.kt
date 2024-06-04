package com.example.liveactivty

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ForegroundService: Service() {

    private var processNotification: Notification? = null
    private val process = ProcessCounter()
    private val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(this)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when(intent?.action){
            ForegroundAction.START.name -> start()
            ForegroundAction.STOP.name -> stop()
        }


        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        CoroutineScope(Dispatchers.Default).launch{
            notification()
            process.start().collect{processValue ->
                Log.i("Jimmy","$processValue")
                updateNotification(processValue = processValue)
            }
        }
    }

    private fun buildNotification(processValue: Int): Notification {
        val remoteViews = createBigRemoteView()
        remoteViews.setProgressBar(R.id.progress_bar, 100, processValue, false)
        val remoteViewSmall = createSmallRemoteView()
        remoteViewSmall.setProgressBar(R.id.progress_bar, 100, processValue, false)

        return NotificationCompat.Builder(this, "delivery_status_channel")
            .setSmallIcon(R.drawable.uber)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteViewSmall)
            .setCustomBigContentView(remoteViews)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .build()
    }

    private fun notification(){
        val processNotification = buildNotification(0)
        startForeground(1,processNotification)
    }

    private fun updateNotification(processValue: Int) {
        if(processValue >= 100) {
            sendArrivalNotification()
            stop()
            return
        }

        processNotification = buildNotification(processValue)
        // 更新通知
        processNotification?.let {
            notificationManager.notify(1, it)
        }
    }

    private fun sendArrivalNotification() {
        val arrivalNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.uber)
            .setContentTitle("司機已抵達")
            .setContentText("您的司機已經到達目的地，請準備上車。")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2, arrivalNotification)
    }



    private fun createBigRemoteView(): RemoteViews {
        return RemoteViews(packageName, R.layout.big_notification)
    }

    private fun createSmallRemoteView(): RemoteViews {
        return RemoteViews(packageName, R.layout.small_notification)
    }


    private fun stop() {
        process.stop()
        stopSelf()
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    enum class ForegroundAction{
        START, STOP
    }
}