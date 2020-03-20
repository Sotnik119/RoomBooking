package com.donteco.roombookingfragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface IEventsRepository {
    fun getRoomName(): LiveData<String>
    fun getMessages(): MutableLiveData<Message?>
    fun getEventsLive(): LiveData<Array<Event>?>
    fun addEvent(event: Event): Boolean {
        return true
    }
    fun getLoadingState(): LiveData<Message>
    fun updateEvent(event: Event): Boolean {
        return true
    }
    fun update()
}