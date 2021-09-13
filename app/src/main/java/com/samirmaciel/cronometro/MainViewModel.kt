package com.samirmaciel.cronometro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    var count : MutableLiveData<Int> = MutableLiveData()
    var time : MutableLiveData<Double> = MutableLiveData()



}