package com.example.testlocation.viewmodel

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testlocation.db.Loc
import com.example.testlocation.helper.ACTION_PAUSE_SERVICE
import com.example.testlocation.helper.ACTION_START_OR_RESUME_SERVICE
import com.example.testlocation.helper.ACTION_STOP_SERVICE
import com.example.testlocation.respositories.MainRepository
import com.example.testlocation.respositories.TrackingRepository
import com.example.testlocation.service.TrackingService
import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.round

@HiltViewModel
class TrackingViewModel @Inject constructor(private val mainRepository: MainRepository): ViewModel() {

    val isTracking = TrackingRepository.isTracking
    val pathPoints = TrackingRepository.pathPoints
    val currentTimeInMillis = TrackingRepository.timeRunInMillis

    val locations = mainRepository.getAllLocation()

    fun getDBLocations(): LiveData<List<Loc>> {
        return locations;
    }


    fun sendCommandToService(context: Context) {
        val action =
            if (isTracking.value!!) ACTION_PAUSE_SERVICE
            else ACTION_START_OR_RESUME_SERVICE
        Intent(context, TrackingService::class.java).also {
            it.action = action
            context.startService(it)
        }
    }

    fun setCancelCommand(context: Context) {
        Intent(context, TrackingService::class.java).also {
            it.action = ACTION_STOP_SERVICE
            context.startService(it)
        }
    }

    fun processRun(context: Context) {
        viewModelScope.launch {
            setCancelCommand(context)
            TrackingRepository.pathPoints.value?.forEach {
                mainRepository.insertRun(Loc());
            }
        }
    }

    fun deleteRun(run: Loc) {
        viewModelScope.launch {
        // mainRepository.deleteRun(run)
        }
    }

    fun restoreDeletedRun(run: Loc) {
        viewModelScope.launch {
            //mainRepository.insertRun(run)
        }
    }
}
