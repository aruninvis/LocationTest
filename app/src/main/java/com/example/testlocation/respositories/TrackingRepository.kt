package com.example.testlocation.respositories

import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.example.testlocation.db.Loc
import com.example.testlocation.db.LocationDao
import com.example.testlocation.helper.TIMER_UPDATE_INTERVAL
import com.example.testlocation.helper.TargetType
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*

import javax.inject.Inject
import javax.inject.Singleton

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@Singleton
class TrackingRepository @Inject constructor(private val locationDao: LocationDao) {

    var isFirstRun = true
        private set
    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L
    var isCancelled = false
    private var lastCounted = 0

    fun initStartingValues() {
        pathPoints.value = mutableListOf()
        timeRunInMillis.postValue(0L)
        timeRunInSeconds.postValue(0L)
        lapTime = 0L
        timeRun = 0L
        timeStarted = 0L
        lastSecondTimestamp = 0L
    }

    private fun startTracking() {
        addEmptyPolyline()
        isTracking.value = true
        isCancelled = false

       // targetType.value = userInfo.targetType
    }

    fun startRun(firstRun: Boolean = false) {
        startTracking()
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        isFirstRun = firstRun

        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
//                val progressValue = when (userInfo.targetType) {
//                    TargetType.TIME -> timeRunInSeconds.value!! / 60f
//                    TargetType.DISTANCE -> distanceInMeters.value!!.toFloat()
//                    TargetType.CALORIES -> caloriesBurned.value!!.toFloat()
//                    else -> 0f
//                }
//                val percentage = progressValue / userInfo.targetType.value * 100f
//                isTargetReached.postValue(percentage >= 100)
//                progress.postValue(percentage.toInt())

                lapTime = System.currentTimeMillis() - timeStarted
                timeRunInMillis.postValue(timeRun + lapTime)

                if (timeRunInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    val lastPointIndex = pathPoints.value!!.last().lastIndex
                    if (lastPointIndex > lastCounted) {
                        var distance = 0f
                        for (i in lastCounted until lastPointIndex) {
                            val pos1 = pathPoints.value!!.last()[i]
                            val pos2 = pathPoints.value!!.last()[i + 1]

                            val result = FloatArray(1)
                            Location.distanceBetween(
                                pos1.latitude,
                                pos1.longitude,
                                pos2.latitude,
                                pos2.longitude,
                                result
                            )
                            distance += result[0]
                        }

                        lastCounted = lastPointIndex
                        val newDistance = distanceInMeters.value!! + distance.toInt()
                        distanceInMeters.postValue(newDistance)
                    }

                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }

                delay(TIMER_UPDATE_INTERVAL)
            }

            timeRun += lapTime
        }
    }

    fun pauseRun() {
        isTracking.value = false
        isTimerEnabled = false
        lastCounted = 0
    }

    fun cancelRun() {
        isCancelled = true
        isFirstRun = true
        timeRunInMillis.value = 0L // reset value for correct fragment observers income values
        distanceInMeters.value = 0
        caloriesBurned.value = 0
        progress.value = 0
        isTargetReached.value = false
        pauseRun()
        initStartingValues()
    }

    fun addPoint(latLng: LatLng) {
        pathPoints.value?.last()?.add(latLng)

        pathPoints.postValue(pathPoints.value)

    }

    private fun addEmptyPolyline() {
        pathPoints.value?.add(mutableListOf())
    }

    companion object {
        val isTracking = MutableLiveData(false)
        val pathPoints = MutableLiveData<Polylines>()
        val timeRunInMillis = MutableLiveData(0L)
        val timeRunInSeconds = MutableLiveData(0L)
        val distanceInMeters = MutableLiveData(0)
        val caloriesBurned = MutableLiveData(0)
        val progress = MutableLiveData(0)
        var targetType = MutableLiveData(TargetType.NONE)
        var isTargetReached = MutableLiveData(false)
    }
}
