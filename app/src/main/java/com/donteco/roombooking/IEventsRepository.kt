package com.donteco.roombooking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat
import java.util.*

interface IEventsRepository {
    fun getEventsLive(): LiveData<Array<Event>>
    fun getEvents(): Array<Event>
    fun addEvent(event: Event):Boolean {return true}
    fun updateEvent(event: Event):Boolean {return true}
    fun update()
}