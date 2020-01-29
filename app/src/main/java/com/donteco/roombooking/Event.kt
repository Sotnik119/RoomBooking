package com.donteco.roombooking

import java.text.SimpleDateFormat
import java.util.*

class Event(
    val startDate: Date,
    val endDate: Date,
    val name: String,
    val owner: String
) {
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun toString(): String {
        return "$name\n" +
                "${format.format(startDate)} - ${format.format(endDate)}\n" +
                owner
    }

    fun isEventTakesPlaceNow(): Boolean {
        return Date() in startDate..endDate
    }

    fun getRemainedTime(): Int { //Todo
        return (startDate.time - Date().time).toInt()/60000
    }
}