package com.samirmaciel.cronometro

import android.annotation.SuppressLint
import android.app.Notification.VISIBILITY_PUBLIC
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.lifecycle.ViewModelProvider
import java.util.*
import kotlin.math.roundToInt

class BroadcastService : Service() {


    private val timer = Timer()
    private var time : Double = 0.0
    private var timeLimit : Double = 10.0
    val CHANNEL_ID : String = "22"
    val NOTIFICATION_ID : Int = 4

    companion object {
        const val TIMER_UPDATE = "timerUpdate"
        const val TIMER_EXTRA = "timeExtra"
        const val TIMER_LIMIT = "timerLimit"
        const val SET_TIME = "setTime"
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        timeLimit = intent.getDoubleExtra(SET_TIME, 0.0)
        timer.scheduleAtFixedRate(TimerTask(time), 0, 1000)

        return START_NOT_STICKY
    }

    private val setTime : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            timeLimit = intent.getDoubleExtra("TIME", 0.0)
            Log.d("TIMERAPP", "onReceive: " + timeLimit)
        }

    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        registerReceiver(setTime, IntentFilter(MainActivity.TIMER_MAIN))
    }

    private inner class TimerTask(private var time : Double) : java.util.TimerTask()
    {
        override fun run() {
            time++
            if(time == timeLimit){
                val intent = Intent(TIMER_UPDATE)
                intent.putExtra(TIMER_UPDATE, time)
                sendBroadcast(intent)
                callNotification(getTimeStringFromDouble(time))
                Log.d("TIMERAPP", "TimerTask" + time)
            }else{
                val intent = Intent(TIMER_UPDATE)
                intent.putExtra("time", "timerLimit")
                sendBroadcast(intent)
                //stopSelf()
            }
        }
    }

    private fun callNotification(time : String){

        val intent = Intent(this, MainActivity::class.java)
        val pedingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        var notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_sports_soccer_24)
            .setContentTitle("Cronometro")
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pedingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentText("Tempo : " + time)
        val notificationManager : NotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        startForeground(NOTIFICATION_ID, notification.build())
    }


    @SuppressLint("WrongConstant")
    private fun createNotificationChannel(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "NotificationChannel"
            val descriptionText = "Canal de notificações"
            val importance = NotificationManager.IMPORTANCE_MAX
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
        stopForeground(true)
        timer.cancel()
        super.onDestroy()

    }

    override fun onBind(p0: Intent?): IBinder? {

        return null

    }


}