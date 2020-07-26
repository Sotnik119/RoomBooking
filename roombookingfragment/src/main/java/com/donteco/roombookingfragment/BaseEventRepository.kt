package com.donteco.roombookingfragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class BaseEventRepository : IEventsRepository {

    private val rep: MutableLiveData<Array<Event>?> = MutableLiveData()

    private val messageSender = MutableLiveData<Message?>()

    private val evens = arrayListOf<Event>()

    override fun getMessages(): MutableLiveData<Message?> {
        return messageSender
    }

    private val _roomName: MutableLiveData<String> = MutableLiveData("test room")

    override fun getRoomName(): LiveData<String> {
        return _roomName
    }

    override fun getAdditionalText(): LiveData<String> {
        return MutableLiveData("")
    }

    override fun getEventsLive(): LiveData<Array<Event>?> {
        return rep
    }

    override fun addEvent(event: Event): Boolean {
        return if (canAddEvent(event)) {
            _loading.postValue(Message(true, "Операция выполняется", "Пожалуйса, подождите..."))

            _loading.postValue(Message(false))
            evens.add(event)
            update()
            messageSender.postValue(Message(true, "Забронировано!", "Зал забронирован"))

            true
        } else {
            messageSender.postValue(Message(false, "Ошибка", "Проверьте данные"))
            false
        }
    }

    private val _loading = MutableLiveData<Message>()
    override fun getLoadingState(): LiveData<Message> {
        return _loading
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
            if (error) null else evens.toTypedArray()
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

    var error: Boolean = false
        set(value) {
            field = value
            update()
        }
}