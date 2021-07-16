package com.example.degram.ui.insights

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.degram.data.DegramRepository
import com.example.degram.util.formatDate
import com.example.degram.util.formatTime
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
        @ApplicationContext private val context: Context,
        private val repository: DegramRepository
) : ViewModel() {

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

    init {
        getStats()
    }

     fun getStats() {
        //Check for permission first
        if (checkForPermission(context)) {
            viewModelScope.launch {
                val stats = repository.getStats()
                if (stats != null) {
                    _dailyAverage.value = Metrics.DailyAverage(stats.dailyAverage, stats.currentUsage)
                    _currentUsage.value = Metrics.GlobalAverage(stats.currentUsage)
                    _streakCount.value = Metrics.StreakCount(stats.streaksCount)
                    _degramScore.value = stats.degramScore

                    _graphData.value = stats.stats
                    setUsage(stats.stats.last())
                } else {
                    _noDataError.value = true
                }
            }
        } else _askPermission.value = true
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
