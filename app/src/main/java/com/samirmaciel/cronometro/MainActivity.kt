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
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.samirmaciel.cronometro.databinding.ActivityMainBinding
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    var timerStarted : Boolean = false
    var timerIsCurrent : Boolean = false
    lateinit var serviceIntent : Intent
    private var time = 0.0
    private var timeLimit : Double = 0.0
    private var current_progress : Long = 0


    private val MAINLOG = "MAINLOG"
    private val viewModel: MainViewModel by viewModels()
    private var _binding : ActivityMainBinding? = null
    private val binding : ActivityMainBinding get() = _binding!!

    companion object {
        const val PROGRESS_MAIN = "PROGRESS_MAIN"
        const val TIMER_MAIN = "TIMER_MAIN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(MAINLOG, "onCreate: ")
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        serviceIntent = Intent(applicationContext, BroadcastService::class.java)
        registerReceiver(updateTime, IntentFilter(BroadcastService.TIMER_UPDATE))

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStart() {
        super.onStart()

        binding.buttonPlayPause.setOnClickListener{
            if(time > 0.0 || timeLimit > 0.0){
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
        if(!timerIsCurrent && time > 0.0){
            time = time - 10
            timeLimit = timeLimit - 10
            viewModel.timelimit.value = timeLimit
            viewModel.currentTimeSet.value = getTimeStringFromDouble(time)
            binding.inputTime.setText(getTimeStringFromDouble(time))
            binding.progressBar.setMax(time.roundToInt())
        }
    }

    private fun addTimeToLimit() {
        if(!timerIsCurrent){
            time = time + 10
            timeLimit = timeLimit + 10
            viewModel.timelimit.value = timeLimit
            viewModel.currentTimeSet.value = getTimeStringFromDouble(time)
            Log.d("LIFECYCLE", "addTimeToLimit: " + viewModel.currentTimeSet.value.toString())
            binding.inputTime.setText(getTimeStringFromDouble(time))
            binding.progressBar.setMax(time.roundToInt())
        }
    }

    private val updateTime : BroadcastReceiver = object : BroadcastReceiver(){
        @RequiresApi(Build.VERSION_CODES.N)
        override fun onReceive(context: Context, intent : Intent) {
            binding.inputTime.setText(intent.getStringExtra(BroadcastService.CURRENT_TIME_SET))
            timeLimit = intent.getDoubleExtra(BroadcastService.TIME_LIMIT, 0.0)
            time = intent.getDoubleExtra(BroadcastService.TIMER_UPDATE, 0.0)
            binding.textCount.text = getTimeStringFromDouble(time)
            current_progress = intent.getLongExtra(BroadcastService.CURRENT_PROGRESS, 0)
            binding.progressBar.setMax(timeLimit.roundToInt())
            binding.progressBar.setProgress(current_progress.toInt(), true)
            if(intent.getBooleanExtra(BroadcastService.TIME_LIMIT, false)){
                resetTime()
                timerIsCurrent = false
            }
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

    private fun startPauseTime() {
        if(timerStarted){
            stopTimer()
        }else{
            startTimer()
        }
    }

    private fun startTimer() {
        if(time == 0.0){
            serviceIntent.putExtra(BroadcastService.CURRENT_TIME, timeLimit)
        }else{
            serviceIntent.putExtra(BroadcastService.CURRENT_TIME, time)
        }
        serviceIntent.putExtra(BroadcastService.CURRENT_TIME_SET, getTimeStringFromDouble(timeLimit))
        serviceIntent.putExtra(BroadcastService.TIME_LIMIT, timeLimit)
        serviceIntent.putExtra(BroadcastService.CURRENT_PROGRESS, current_progress)
        startService(serviceIntent)
        timerStarted = true
        timerIsCurrent = true

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
        timerStarted = false
        timerIsCurrent = false
        binding.progressBar.setProgress(0, true)
        binding.textCount.setText("00:00")
        binding.inputTime.setText("00:00")

    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(updateTime)
        Log.d(MAINLOG, "onStop: ")
    }


    override fun onResume() {
        super.onResume()
        Log.d(MAINLOG, "onResume: ")
        registerReceiver(updateTime, IntentFilter(BroadcastService.TIMER_UPDATE))
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(MAINLOG, "onDestroy: ")
        stopService(serviceIntent)
        unregisterReceiver(updateTime)
        _binding = null

    }


}