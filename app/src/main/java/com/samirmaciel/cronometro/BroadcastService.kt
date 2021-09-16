package com.samirmaciel.cronometro

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.*
import kotlin.math.roundToInt

class BroadcastService : Service() {

    private val timer = Timer()



    private var time : Double = 0.0
    private var timeLimit : Double = 0.0
    private var currentProgress : Long = 0
    private var currentTimeSet : String = "00:00"

    val CHANNEL_ID : String = "COUNTDOWN_CHANNEL"
    val NOTIFICATION_ID : Int = 4

    companion object {
        const val TIMER_UPDATE = "TIMEUPDATE"
        const val TIME_LIMIT = "TIMERLIMIT"
        const val CURRENT_TIME_SET = "CURRENT_TIME_SET"
        const val CURRENT_PROGRESS = "CURRENTPROGRESS"
        const val CURRENT_TIME = "CURRENTTIME"
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        timeLimit = intent.getDoubleExtra(TIME_LIMIT, 0.0)
        time = intent.getDoubleExtra(CURRENT_TIME, 0.0)
        currentTimeSet = intent.getStringExtra(CURRENT_TIME_SET).toString()
        currentProgress = intent.getLongExtra(CURRENT_PROGRESS, 0)
        timer.scheduleAtFixedRate(TimerTask(time), 0, 1000)
        createNotificationChannel()

        return START_NOT_STICKY
    }

    private inner class TimerTask(private var time : Double) : java.util.TimerTask()
    {
        override fun run() {
            time--
            currentProgress++

            if(time == 0.0){
                val intent = Intent(TIMER_UPDATE)
                intent.putExtra(TIMER_UPDATE, time)
                intent.putExtra(CURRENT_PROGRESS, currentProgress)
                intent.putExtra(CURRENT_TIME_SET, currentTimeSet)
                intent.putExtra(TIME_LIMIT, true)
                sendBroadcast(intent)

            }else{
                val intent = Intent(TIMER_UPDATE)
                intent.putExtra(TIMER_UPDATE, time)
                intent.putExtra(TIME_LIMIT, timeLimit)
                intent.putExtra(CURRENT_TIME_SET, currentTimeSet)
                intent.putExtra(CURRENT_PROGRESS, currentProgress)
                sendBroadcast(intent)
                callNotification(getTimeStringFromDouble(time))
            }
        }
    }

    private fun callNotification(notificationString : String){

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        val pedingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)

        var notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_sports_soccer_24)
            .setContentTitle("Cronometro")
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pedingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentText(notificationString)
        val notificationManager : NotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        startForeground(NOTIFICATION_ID, notification.build())
    }

    @SuppressLint("WrongConstant")
    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "NotificationChannel"
            val descriptionText = "Canal de notificações"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager : NotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getTimeStringFromDouble(time : Double) : String{
        val resultInt = time.roundToInt()
        val minutes = resultInt %  86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60

        return makeTimeString( minutes, seconds)

    }

    private fun makeTimeString( minutes: Int, seconds: Int) : String{

        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("SERVIVE", "onDestroy: SERVICE")
        stopForeground(true)
        timer.cancel()


    }

    override fun onBind(p0: Intent?): IBinder? {

        return null

    }


}