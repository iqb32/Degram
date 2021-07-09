package com.example.degram.util

import android.app.usage.UsageStats
import android.content.Context
import androidx.core.content.ContextCompat
import com.example.degram.R
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

//Extension function to transform list of [UsageStat] objects into bar graph data set
fun List<UsageStats>.asGraphData(context: Context): BarDataSet {
    val data = BarDataSet(this.map { stats ->
        BarEntry(
            (indexOf(stats) + 1).toFloat(),
            stats.totalTimeInForeground.toFloat()
        )
    }, "Weekly Usage")
    data.color = ContextCompat.getColor(context, R.color.blueish_black)
    data.setDrawValues(false)
    return data
}

//Extension function to transform list of [UsageStat] objects into the x-axis data for graph
fun List<UsageStats>.getXAxisData(): ArrayList<String> {
    val list = ArrayList<String>()
    list.add("0")
    this.forEach { list.add(getDayFromTimeStamp(it.firstTimeStamp)) }
    return list
}

//Format milliseconds into more readable form
class YAxisFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return formatTime(value.toLong())
    }
}

fun formatTime(time: Long): String {
    return when {
        time >= 3600000 -> "${((time / (1000*60*60)) % 24).toInt()} h ${((time / (1000*60)) % 60).toInt()} m"
        time in 60000..3599999 -> "${((time / (1000*60)) % 60).toInt()} m ${((time / 1000) % 60).toInt()} s"
        else -> "${TimeUnit.MILLISECONDS.toSeconds(time)} s"
    }
}

fun getDayFromTimeStamp(lastTimeStamp: Long): String {
    val date = Date(lastTimeStamp)
    return  SimpleDateFormat("EE", Locale.getDefault()).format(date)
}

fun formatDate(time : Long): String = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(time))