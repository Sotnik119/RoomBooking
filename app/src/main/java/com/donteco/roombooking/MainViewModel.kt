package com.donteco.roombooking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.fixedRateTimer

class MainViewModel : ViewModel() {

    private val currentEvent: MutableLiveData<Event?> = MutableLiveData()

    private val status: MutableLiveData<Status> = MutableLiveData()

    private val _mainColor: MutableLiveData<Int> = MutableLiveData()
    val mainColor: LiveData<Int>
        get() = _mainColor

    private val _time: MutableLiveData<String> = MutableLiveData()
    val time: LiveData<String>
        get() = _time

    private val _roomText: MutableLiveData<String> = MutableLiveData()
    val roomText: LiveData<String>
        get() = _roomText

    private val repo: EventRepo = EventRepo()

    private val eventList = repo.getEventsLive()
    private val repoObserver = Observer<Array<Event>> {
        setEvents(it)
    }

    init {
        setstatus(Status.STATUS_UNKNOWN)

        eventList.observeForever(repoObserver)

        fixedRateTimer("Time", true, 2000, 5000) {
            _time.postValue(
                SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                ).format(Calendar.getInstance().time)
            )
            update()
        }
    }

    //Updating state
    private fun update() {
        val closest = findClosestEvent()
        if (closest != null) {
            currentEvent.postValue(closest)
            if (closest.isEventTakesPlaceNow()) {
                setstatus(Status.STATUS_OCCUPIED)
            } else {
                if (closest.getRemainedTime() < 30) {
                    setstatus(Status.STATUS_WAIT)
                } else {
                    setstatus(Status.STATUS_AVAILABLE)
                }
            }
        } else {
            setstatus(Status.STATUS_AVAILABLE)
        }


    }

    private var events = repo.getEvents()

    private fun setEvents(eventsq: Array<Event>) {
        Log("Setting event list $eventsq")
        events = eventsq
        update()
    }

    private fun findClosestEvent(): Event? {
        return events.firstOrNull {
            (Date().after(it.startDate) && Date().before(it.endDate)) || it.startDate.after(Date())
        }
    }

    private fun setstatus(newStatus: Status) {
        status.postValue(newStatus)
        _mainColor.postValue(
            when (newStatus) {
                Status.STATUS_AVAILABLE -> R.color.roomAvailable
                Status.STATUS_OCCUPIED -> R.color.roomOccupied
                Status.STATUS_WAIT -> R.color.roomWait
                else -> R.color.roomUnknow
            }
        )
        _roomText.postValue(
            when (newStatus) {
                Status.STATUS_AVAILABLE -> "\nRoom available\n"
                Status.STATUS_OCCUPIED -> currentEvent.value?.toString()
                Status.STATUS_WAIT -> "Available for ${currentEvent.value?.getRemainedTime()} min"
                else -> "\nUnknown\n"
            }
        )
    }

    override fun onCleared() {
        eventList.removeObserver(repoObserver)
        super.onCleared()
    }

    enum class Status(val num: Int) {
        STATUS_AVAILABLE(1),
        STATUS_OCCUPIED(2),
        STATUS_WAIT(3),
        STATUS_UNKNOWN(0)
    }
}