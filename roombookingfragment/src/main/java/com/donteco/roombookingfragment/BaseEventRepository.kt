package com.donteco.roombookingfragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat

class BaseEventRepository : IEventsRepository {


    val rep: MutableLiveData<Array<Event>> = MutableLiveData()

    val messageSender = MutableLiveData<Message>()

    val format = SimpleDateFormat("yyyy, MM, dd, HH, mm")
    private val evens = arrayListOf<Event>()

    override fun getMessages(): LiveData<Message> {
        return messageSender
    }
//        Event(
//            format.parse("2020, 01, 29, 11, 40"),
//            format.parse("2020, 01, 29, 13, 00"),
//            "First event",
//            "Oleg Sotnik"
//        ),
//        Event(
//            format.parse("2020, 01, 29, 13, 52"),
//            format.parse("2020, 01, 29, 13, 59"),
//            "Second event",
//            "Oleg Sotnik"
//        ),
//        Event(
//            format.parse("2020, 01, 29, 14, 40"),
//            format.parse("2020, 01, 29, 15, 40"),
//            "Thrid event",
//            "Oleg Sotnik"
//        ),
//        Event(
//            format.parse("2020, 02, 03, 17, 30"),
//            format.parse("2020, 02, 03, 19, 00"),
//            "Fourth event",
//            "Oleg Sotnik"
//        )
//    )

    override fun getEventsLive(): LiveData<Array<Event>> {
        return rep
    }

    override fun getEvents(): Array<Event> {
        return evens.toTypedArray()
    }

    override fun addEvent(event: Event): Boolean {
        return if (canAddEvent(event)) {
            evens.add(event)
            update()
            messageSender.postValue(Message(true, "Забронировано!", "Зал забронирован"))
            true
        } else {
            false
        }
    }

    override fun updateEvent(event: Event): Boolean {
        return if (canUpdateEvent(event)) {
            evens.remove(evens.find { it.id == event.id })
            evens.add(event)
            messageSender.postValue(Message(true, "Забронировано!", "Зал забронирован"))
            update()
            true
        } else
            false
    }

    //fixme: update with copy
    override fun update() {
        rep.postValue(
            evens.toTypedArray()
        )
    }

    private fun canAddEvent(event: Event): Boolean {
        val daylyEvents =
            evens.filter { it.startDate.atStartOfDay().time == event.startDate.atStartOfDay().time }

        val crossed = daylyEvents.firstOrNull { event.crossAnotherEvent(it) }

        return crossed == null
    }

    private fun canUpdateEvent(event: Event): Boolean {
        val filtered = evens.filter { it.id != event.id }

        val crossed = filtered.firstOrNull { event.crossAnotherEvent(it) }

        return crossed == null
    }
}