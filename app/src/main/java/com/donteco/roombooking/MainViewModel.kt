package com.donteco.roombooking

import androidx.lifecycle.*
import androidx.lifecycle.Observer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.fixedRateTimer


class MainViewModel(repo: IEventsRepository) : ViewModel() {

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

    val timeFormat = MutableLiveData<Format>().apply { postValue(Format.FORMAT_24H) }

    val filterDate = MutableLiveData<Date>().apply { postValue(Date()) }
    val filteredEvents: LiveData<Array<Event>>
        get() = Transformations.switchMap(filterDate) { date ->
            Transformations.switchMap(eventList) {
                val filtered = MutableLiveData<Array<Event>>()
                filtered.postValue(it.filter {
                    date.atStartOfDay().before(it.startDate) && date.atEndOfDay().after(
                        it.endDate
                    )
                }
                    .toTypedArray())
                filtered
            }
        }

    val eventList = repo.getEventsLive()
    private val repoObserver = Observer<Array<Event>> {
        setEvents(it)
    }

    init {
        setStatus(Status.STATUS_UNKNOWN)

        eventList.observeForever(repoObserver)

        fixedRateTimer("Time", true, 2000, 5000) {
            _time.postValue(
                SimpleDateFormat(
                    if (timeFormat.value == Format.FORMAT_24H) "HH:mm" else "hh:mm a",
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
                setStatus(Status.STATUS_OCCUPIED)
            } else {
                if (closest.getRemainedTime() < 30) {
                    setStatus(Status.STATUS_WAIT)
                } else {
                    setStatus(Status.STATUS_AVAILABLE)
                }
            }
        } else {
            setStatus(Status.STATUS_AVAILABLE)
        }


    }

    private var events = repo.getEvents()

    private fun setEvents(eventsq: Array<Event>) {
        events = eventsq
        update()
    }

    private fun findClosestEvent(): Event? {
        return events.firstOrNull {
            (Date().after(it.startDate) && Date().before(it.endDate)) || it.startDate.after(Date())
        }
    }

    private fun setStatus(newStatus: Status) {
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


class MainViewModelFactory(private val repo: IEventsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(repo) as T
    }

}