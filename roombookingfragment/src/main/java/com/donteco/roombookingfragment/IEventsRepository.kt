package com.donteco.roombookingfragment

import androidx.lifecycle.LiveData

interface IEventsRepository {
    fun getMessages(): LiveData<Message>
    fun getEventsLive(): LiveData<Array<Event>>
    fun getEvents(): Array<Event>
    fun addEvent(event: Event): Boolean {
        return true
    }

    fun updateEvent(event: Event): Boolean {
        return true
    }

    fun update()
}