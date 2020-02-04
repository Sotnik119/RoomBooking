package com.donteco.roombooking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat
import java.util.*

interface IEventsRepository {
    fun getEventsLive(): LiveData<Array<Event>>
    fun getEvents(): Array<Event>
    fun addEvent():Boolean {return true}
    fun updateEvent() {}
}