package com.donteco.roombookingfragment

import java.text.SimpleDateFormat
import java.util.*

class Event(
    val startDate: Date,
    var endDate: Date,
    val name: String,
    val owner: String,
    val description: String = "",
    val id: String = UUID.randomUUID().toString()
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

    fun crossAnotherEvent(other: Event): Boolean {

        val o1 = this.endDate.after(other.startDate) && this.endDate.before(other.endDate)
        val o2 = this.startDate.after(other.startDate) && this.startDate.before(other.endDate)
        val o3 = this.startDate.before(other.startDate) && this.endDate.after(other.endDate)
        val o4 = this.startDate.after(other.startDate) && this.endDate.before(other.endDate)

        return o1 || o2 || o3 || o4
    }
}