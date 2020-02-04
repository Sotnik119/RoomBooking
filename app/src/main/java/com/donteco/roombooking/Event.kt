package com.donteco.roombooking

import java.text.SimpleDateFormat
import java.util.*

class Event(
    val startDate: Date,
    val endDate: Date,
    val name: String,
    val owner: String,
    val description: String = ""
) {
    private val format24 = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val format12 = SimpleDateFormat("hh:mm a", Locale.getDefault())

    override fun toString(): String {
        return "$name\n" +
                "${format24.format(startDate)} - ${format24.format(endDate)}\n" +
                owner
    }

    fun isEventTakesPlaceNow(): Boolean {
        return Date() in startDate..endDate
    }

    fun getRemainedTime(): Int { //Todo
        return (startDate.time - Date().time).toInt() / 60000
    }

    fun getEventDescription(timeFormat: Format): String {
        val format = if (timeFormat == Format.FORMAT_24H) format24 else format12
        return "$name\n" +
                "${startDate.toFormattedString(format)} - ${endDate.toFormattedString(format)}\n" +
                owner
    }
}