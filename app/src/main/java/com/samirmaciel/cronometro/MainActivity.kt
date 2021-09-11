package com.samirmaciel.cronometro

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.samirmaciel.cronometro.databinding.ActivityMainBinding
import java.lang.Exception
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    var timerStarted : Boolean = false
    lateinit var serviceIntent : Intent
    private var time = 0.0



    lateinit var viewModel: MainViewModel
    private var _binding : ActivityMainBinding? = null
    private val binding : ActivityMainBinding get() = _binding!!

    companion object {
        const val TIMER_MAIN = "TIMER_MAIN"
    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        serviceIntent = Intent(applicationContext, BroadcastService::class.java)
        registerReceiver(updateTime, IntentFilter(BroadcastService.TIMER_UPDATE))

    }

    override fun onStart() {
        super.onStart()

        binding.buttonPlayPause.setOnClickListener{
            startPauseTime()
            binding.inputTime.setText("")
        }

        binding.buttonReset.setOnClickListener{
            resetTime()
        }
        binding.inputTime.doOnTextChanged { text, start, before, count ->
            binding.buttonPlayPause.isEnabled = binding.inputTime.text.isNotEmpty()
        }
    }
    private val updateTime : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent : Intent) {
            time = intent.getDoubleExtra(BroadcastService.TIMER_UPDATE, 0.0)
            binding.textCount.text = getTimeStringFromDouble(time)
            if(intent.getStringExtra("time") != null){
                binding.textCount.text = "TEMPO LIMITE"
                Log.d("TIMERAPP", "onReceive: Tempo limite")
            }
        }
    }

    override fun onStop() {
        unregisterReceiver(updateTime)
        super.onStop()
    }


    override fun onResume() {
        super.onResume()
        registerReceiver(updateTime, IntentFilter(BroadcastService.TIMER_UPDATE))

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

    private fun startPauseTime() {
        if(timerStarted){
            stopTimer()
        }else{
            startTimer()
        }
    }

    private fun startTimer() {
        serviceIntent.putExtra(BroadcastService.SET_TIME, binding.inputTime.text.toString().toDouble())
        startService(serviceIntent)
        timerStarted = true

    }

    private fun stopTimer() {

        stopService(serviceIntent)
        timerStarted = false

    }

    private fun resetTime(){
        stopTimer()
        time = 0.0
        binding.textCount.setText("00:00")

    }

    override fun onDestroy() {
        unregisterReceiver(updateTime)
        _binding = null
        super.onDestroy()

    }


}