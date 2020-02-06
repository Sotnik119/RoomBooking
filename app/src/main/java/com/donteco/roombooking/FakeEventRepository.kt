package com.donteco.roombooking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat

class FakeEventRepository : IEventsRepository {


    val rep: MutableLiveData<Array<Event>> = MutableLiveData()

    val format = SimpleDateFormat("yyyy, MM, dd, HH, mm")
    private val evens = arrayListOf(
        Event(
            format.parse("2020, 01, 29, 11, 40"),
            format.parse("2020, 01, 29, 13, 00"),
            "First event",
            "Oleg Sotnik"
        ),
        Event(
            format.parse("2020, 01, 29, 13, 52"),
            format.parse("2020, 01, 29, 13, 59"),
            "Second event",
            "Oleg Sotnik"
        ),
        Event(
            format.parse("2020, 01, 29, 14, 40"),
            format.parse("2020, 01, 29, 15, 40"),
            "Thrid event",
            "Oleg Sotnik"
        ),
        Event(
            format.parse("2020, 02, 03, 17, 30"),
            format.parse("2020, 02, 03, 19, 00"),
            "Fourth event",
            "Oleg Sotnik"
        )
    )

    init {
        rep.postValue(
            evens.toTypedArray()
        )
    }

    override fun getEventsLive(): LiveData<Array<Event>> {
        return rep
    }

    override fun getEvents(): Array<Event> {
        return evens.toTypedArray()
    }

    override fun addEvent(event: Event): Boolean {
        evens.add(event)
        return true
    }

    override fun updateEvent(event: Event): Boolean {
        return true
    }

    override fun update() {
        rep.postValue(
            evens.toTypedArray()
        )
    }
}