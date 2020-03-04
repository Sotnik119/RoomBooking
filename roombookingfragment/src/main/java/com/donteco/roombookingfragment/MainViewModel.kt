package com.donteco.roombookingfragment

import android.graphics.Color
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.fixedRateTimer


class MainViewModel(
    private val repo: IEventsRepository,
    private val config: Config
) : ViewModel() {

    val roomName = MutableLiveData<String>().apply { postValue(config.roomName) }

    private val currentEvent: MutableLiveData<Event?> = MutableLiveData()

    private val _status: MutableLiveData<Status> = MutableLiveData()
    val status: LiveData<Status>
        get() = _status

    private val _mainColor: MutableLiveData<Int> = MutableLiveData()
    val mainColor: LiveData<Int>
        get() = _mainColor
    val mainColorLeft: LiveData<Int>
        get() = Transformations.map(_mainColor) {
            it.setAlpha(100 - config.leftTransparent)
        }
    val mainColorRight: LiveData<Int>
        get() = Transformations.map(_mainColor) {
            it.setAlpha(100 - config.rightTransparent)
        }

    private val _time: MutableLiveData<String> = MutableLiveData()
    val time: LiveData<String>
        get() = _time

    private val _fontColor: MutableLiveData<Int> = MutableLiveData()
    val fontColor: LiveData<Int>
        get() = _fontColor

    private val _roomText: MutableLiveData<String> = MutableLiveData()
    val roomText: LiveData<String>
        get() = _roomText


    val bookButtonText: LiveData<String>
        get() = Transformations.map(status) {
            if (it != Status.STATUS_OCCUPIED) {
                "Забронироваь зал"
            } else {
                "Управление"
            }
        }

    val timeFormat = MutableLiveData<Format>().apply { postValue(config.timeFormat) }

    val filterDate = MutableLiveData<Date>().apply { postValue(Date().atStartOfDay()) }
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

    val messages = repo.getMessages()

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
        _fontColor.postValue(config.fontColor)
    }

    //Updating state
    private fun update() {
        val closest = findClosestEvent()
        if (closest != null) {
            currentEvent.postValue(closest)
            if (closest.isEventTakesPlaceNow()) {
                setStatus(Status.STATUS_OCCUPIED)
            } else {
                if (closest.getRemainedTime() < 15) {
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
        return events.sortedBy { it.startDate }.firstOrNull {
            (Date().after(it.startDate) && Date().before(it.endDate)) || it.startDate.after(Date())
        }
    }

    private fun setStatus(newStatus: Status) {
        _status.postValue(newStatus)
        _mainColor.postValue(
            when (newStatus) {
                Status.STATUS_AVAILABLE -> config.freeColor
                Status.STATUS_OCCUPIED -> config.busyColor
                Status.STATUS_WAIT -> config.willBusyColor
                else -> config.connectError
            }
        )
        _roomText.postValue(
            when (newStatus) {
                Status.STATUS_AVAILABLE -> "\nДоступно\n"
                Status.STATUS_OCCUPIED -> currentEvent.value?.toString()
                Status.STATUS_WAIT -> "Доступно на ${currentEvent.value?.getRemainedTime()} минут"
                else -> "\nНет связи с сервером\n"
            }
        )
    }

    override fun onCleared() {
        eventList.removeObserver(repoObserver)
        super.onCleared()
    }


    fun createEvent(length: Int) {
        createEvent(Date(), length, "Прямое бронирование")
    }

    fun createEvent(date: Date, length: Int, name: String = "Раннее бронирование") {
        val timeEnd = Calendar.getInstance().apply {
            time = date
            add(Calendar.MINUTE, length)
        }
        val timeStart = Calendar.getInstance().apply {
            time = date
            set(Calendar.MILLISECOND, 0)
        }
        val event = Event(timeStart.time, timeEnd.time, name, "")
        if (repo.addEvent(event)) {
            //todo: show success
        } else {
            //todo: show error?
        }
    }

    fun closeEvent() {
        val event = currentEvent.value?.apply { endDate = Date() }
        event?.also {
            if (repo.updateEvent(event)) {

            }
        }

    }

    fun extendEvent(length: Int) {
        val event = currentEvent.value
        event?.apply {
            val timeEnd = Calendar.getInstance()
            timeEnd.time = this.endDate
            timeEnd.add(Calendar.MINUTE, length)
            endDate = timeEnd.time
        }
        event?.also {
            if (repo.updateEvent(event)) {
                //todo
            } else {
                //todo
            }
        }
    }

    enum class Status(val num: Int) {
        STATUS_AVAILABLE(1),
        STATUS_OCCUPIED(2),
        STATUS_WAIT(3),
        STATUS_UNKNOWN(0)
    }

    data class Config(
        val roomName: String,
        val freeColor: Int,
        val busyColor: Int,
        val willBusyColor: Int,
        val connectError: Int,
        val fontColor: Int,
        val leftTransparent: Int,
        val rightTransparent: Int,
        val timeFormat: Format
    ) {
        companion object {
            fun getDefault() = Config(
                "",
                Color.GREEN,
                Color.RED,
                Color.YELLOW,
                Color.BLUE,
                Color.YELLOW,
                40,
                60,
                Format.FORMAT_24H
            )
        }
    }
}


class MainViewModelFactory(
    private val repo: IEventsRepository?,
    private val config: MainViewModel.Config?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(
            repo ?: BaseEventRepository(),
            config ?: MainViewModel.Config.getDefault()
        ) as T
    }

}