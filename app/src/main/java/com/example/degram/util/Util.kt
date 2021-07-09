package com.example.degram.util

import java.util.*

//The time from where to get the usage stats
 fun getStart(index: Int): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.WEEK_OF_YEAR, index)
    return cal.timeInMillis
}

//The time till when we need to get the usage stats
 fun getEnd(index: Int): Long {
    val cal = Calendar.getInstance()
    if (index < -1) cal.add(Calendar.WEEK_OF_YEAR, index + 1)
    return cal.timeInMillis
}
