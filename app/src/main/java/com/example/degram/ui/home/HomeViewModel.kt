package com.example.degram.ui.home

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.degram.util.Constants
import com.example.degram.util.formatDate
import com.example.degram.util.formatTime
import com.example.degram.util.getDayFromTimeStamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _noDataError = MutableLiveData<Boolean>()
    val noDataError: LiveData<Boolean>
        get() = _noDataError

    private val _askPermission = MutableLiveData<Boolean>()
    val askPermission : LiveData<Boolean>
        get() = _askPermission

    //Average of last 21 days
    private val _dailyAverage = MutableLiveData<Metrics<Long>>()
    val dailyAverage: LiveData<Metrics<Long>>
        get() = _dailyAverage

    //For the global usage comparison indicator
    private val _currentUsage = MutableLiveData<Metrics<Long>>()
    val currentUsage: LiveData<Metrics<Long>>
        get() = _currentUsage

    //If a user uses the app less than yesterday it is counted as a streak
    private val _streakCount = MutableLiveData<Metrics<Long>>()
    val streakCount: LiveData<Metrics<Long>>
        get() = _streakCount

    //The metric for which the stats have to be displayed
    //If currently none is selected , it is null
    private val _info = MutableLiveData<Metrics<Long>>()
    val info: LiveData<Metrics<Long>>
        get() = _info

    private val _degramScore = MutableLiveData<Int>()
    val degramScore: LiveData<Int>
        get() = _degramScore

    //Graph Data
    private val _graphData = MutableLiveData<List<UsageStats>>()
    val graphData: LiveData<List<UsageStats>>
        get() = _graphData

    private val _usage = MutableLiveData<String>()
    val usage: LiveData<String>
        get() = _usage

    fun getUsageData(usageStatsManager: UsageStatsManager, context: Context) {

        //Check for permission first
        if (checkForPermission(context)) {
            viewModelScope.launch {
                //For calculating streaks and average , we get the past 10 days data
                //Streaks can go only till 10, after which they remain constant
                //Average is also computed based on last 10 days of data
                val calendarStreaks = Calendar.getInstance()
                calendarStreaks.add(Calendar.DAY_OF_MONTH, -10)
                val startTimeStreaks = calendarStreaks.timeInMillis

                //Get the stats , and filter the data for only instagram app
                val stats =
                        usageStatsManager.queryUsageStats(
                                UsageStatsManager.INTERVAL_DAILY,
                                startTimeStreaks,
                                System.currentTimeMillis()
                        ).filter { stats -> stats.packageName == Constants.INSTAGRAM_PACKAGE_NAME }

                //TODO : REMOVE
                stats.forEach {
                    Log.e(TAG,
                            "Day : ${getDayFromTimeStamp(it.firstTimeStamp)}, Usage : ${formatTime(it.totalTimeInForeground)}"
                    )
                }

                Log.e(TAG, "Average : ${(stats.sumOf { it.totalTimeInForeground }) / stats.size}, Current : ${stats.last().totalTimeInForeground}")

                if (stats.isNotEmpty()) {

                    val dailyAverage = (stats.sumOf { it.totalTimeInForeground }) / stats.size
                    val currentUsage = stats.last().totalTimeInForeground
                    val streakCount = calculateStreakCount(stats)

                    _dailyAverage.value = Metrics.DailyAverage(dailyAverage, currentUsage)
                    _currentUsage.value = Metrics.GlobalAverage(currentUsage)
                    _streakCount.value = Metrics.StreakCount(streakCount)
                    _degramScore.value = calculateDegramScore(dailyAverage >= currentUsage, currentUsage <= Constants.GLOBAL_USAGE_AVERAGE, streakCount.toInt())

                    _graphData.value = stats
                    setUsage(stats.last())

                } else {
                    _noDataError.value = true
                }
            }
        } else _askPermission.value = true
    }

    private fun calculateDegramScore(isLessThanDailyAverage: Boolean, isLessThanGlobalAverage: Boolean, streakCount: Int): Int {
        Log.e(TAG, "${isLessThanDailyAverage}, ${isLessThanGlobalAverage}, $streakCount")
        var score = 0
        if (isLessThanDailyAverage) score += 30
        if (isLessThanGlobalAverage) score += 30
        score += streakCount * 4
        return score
    }

    private fun calculateStreakCount(streakStats: List<UsageStats>): Long {
        var index = 9
        var continueToCheck = true
        var count: Long = 0

        while (continueToCheck) {
            if (index > 0) {
                if (streakStats[index].totalTimeInForeground <= streakStats[index - 1].totalTimeInForeground) {
                    count++
                    index--
                } else continueToCheck = false
            } else break

        }

        return count
    }

    //Set the data for the bottom sheet, and make it visible
    fun showInfoDialog(data: LiveData<Metrics<Long>>) {
        _info.value = data.value
    }

    fun setUsage(usageStats: UsageStats) { _usage.value = "${formatDate(usageStats.firstTimeStamp)} : ${formatTime(usageStats.totalTimeInForeground)}"}

    private fun checkForPermission(context: Context): Boolean {
        return try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
            val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid,
                    applicationInfo.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    companion object {
        private const val TAG = "MetricsViewModel"
    }
}

sealed class Metrics<Long> {
    data class DailyAverage(val data: Long, val currentUsage: Long) : Metrics<Long>()
    data class GlobalAverage(val data: Long) : Metrics<Long>()
    data class StreakCount(val data: Long) : Metrics<Long>()
    object ScoreInfo  : Metrics<Long>()
}
