package com.samirmaciel.cronometro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.samirmaciel.cronometro.databinding.ActivityMainBinding
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    var timerStarted : Boolean = false
    var timerCountDown : Boolean = false
    lateinit var serviceIntent : Intent
    private var time = 0.0
    private var timeLimit : Double = 0.0
    private var current_progress : Long = 0


    lateinit var viewModel: MainViewModel
    private var _binding : ActivityMainBinding? = null
    private val binding : ActivityMainBinding get() = _binding!!

    companion object {
        const val PROGRESS_MAIN = "PROGRESS_MAIN"
        const val TIMER_MAIN = "TIMER_MAIN"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        serviceIntent = Intent(applicationContext, BroadcastService::class.java)
        registerReceiver(updateTime, IntentFilter(BroadcastService.TIMER_UPDATE))

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStart() {
        super.onStart()

        binding.buttonPlayPause.setOnClickListener{
            if(timeLimit > 0.0){
                startPauseTime()
            }else{
                Toast.makeText(applicationContext, "Adicione tempo limite para a contagem", Toast.LENGTH_SHORT).show()
            }

        }

        binding.buttonReset.setOnClickListener{
            resetTime()
        }

        binding.buttonAddTime.setOnClickListener{
            addTimeToLimit()
        }

        binding.buttonRemoveTime.setOnClickListener{
            removeTimeToLimit()
        }

    }

    private fun removeTimeToLimit() {
        timeLimit = timeLimit - 10
        binding.inputTime.setText(getTimeStringFromDouble(timeLimit))
        val intent = Intent(TIMER_MAIN)
        intent.putExtra(BroadcastService.TIME_LIMIT, timeLimit)
        sendBroadcast(intent)
        binding.progressBar.setMax(timeLimit.roundToInt())
    }

    private fun addTimeToLimit() {
        timeLimit = timeLimit + 10
        binding.inputTime.setText(getTimeStringFromDouble(timeLimit))
        val intent = Intent(TIMER_MAIN)
        intent.putExtra(BroadcastService.TIME_LIMIT, timeLimit)
        sendBroadcast(intent)
        binding.progressBar.setMax(timeLimit.roundToInt())
    }

    private val updateTime : BroadcastReceiver = object : BroadcastReceiver(){
        @RequiresApi(Build.VERSION_CODES.N)
        override fun onReceive(context: Context, intent : Intent) {


            time = intent.getDoubleExtra(BroadcastService.TIMER_UPDATE, 0.0)

            binding.textCount.text = getTimeStringFromDouble(time)


            current_progress = intent.getLongExtra(BroadcastService.CURRENT_PROGRESS, 0)


            binding.progressBar.setProgress(current_progress.toInt(), true)
            if(intent.getBooleanExtra(BroadcastService.TIME_LIMIT, false)){
                stopService(serviceIntent)
                Log.d("STOPINGSERVICE", "onReceive: StopService")
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
        serviceIntent.putExtra(BroadcastService.TIME_LIMIT, timeLimit)
        serviceIntent.putExtra(BroadcastService.SET_TIME, time)
        serviceIntent.putExtra(BroadcastService.CURRENT_TIME, time)
        serviceIntent.putExtra(BroadcastService.CURRENT_PROGRESS, current_progress)
        startService(serviceIntent)
        timerStarted = true

    }

    private fun stopTimer() {
        stopService(serviceIntent)
        timerStarted = false

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun resetTime(){
        stopTimer()
        time = 0.0
        current_progress = 0
        timeLimit = 0.0
        binding.progressBar.setProgress(0, true)
        binding.textCount.setText("00:00")
        binding.inputTime.setText("00:00")

    }

    override fun onDestroy() {
        unregisterReceiver(updateTime)
        _binding = null
        super.onDestroy()

    }


}