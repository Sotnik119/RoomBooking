package com.donteco.roombookingfragment

import android.graphics.Color
import java.text.SimpleDateFormat
import java.util.*

fun Date.toFormattedString(format: SimpleDateFormat): String {
    return format.format(this)
}

fun Date.atEndOfDay(): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.time
}

fun Date.atStartOfDay(): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

fun Log(mes: String) {
    android.util.Log.d("MyLog", mes)
}

fun Int.setAlpha(alphaPercent: Int): Int {
    return Color.argb(
        (255.toFloat() / 100 * alphaPercent).toInt(),
        Color.red(this),
        Color.green(this),
        Color.blue(this)
    )
}