package com.donteco.roombookingfragment

import android.graphics.Color
import android.widget.EditText
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.fixedRateTimer


class MainViewModel(
    private val repo: IEventsRepository,
    private val config: Config
) : ViewModel() {

    lateinit var widgetOrientation: RoomBookingFragment.Orientation
    var dontShowAndroidKeyboard = false

    private var isErrorState = false

    val roomName = repo.getRoomName()
    val additionalText = repo.getAdditionalText()

    private val currentEvent: MutableLiveData<Event?> = MutableLiveData()

    val status: LiveData<Status> = currentEvent.map {
        if (isErrorState) {
            Status.STATUS_UNKNOWN
        } else if (it != null) {
            if (it.isEventTakesPlaceNow()) {
                Status.STATUS_OCCUPIED
            } else {
                if (it.getRemainedTime() < 15) {
                    Status.STATUS_WAIT
                } else {
                    Status.STATUS_AVAILABLE
                }
            }
        } else {
            Status.STATUS_AVAILABLE
        }
    }

    val mainColor: LiveData<Int> = status.map {
        when (it) {
            Status.STATUS_AVAILABLE -> config.freeColor
            Status.STATUS_OCCUPIED -> config.busyColor
            Status.STATUS_WAIT -> config.willBusyColor
            else -> config.connectError
        }
    }

    val mainColorLeft: LiveData<Int> = mainColor.map {
        it.setAlpha(100 - config.leftTransparent)
    }
    val mainColorRight: LiveData<Int> = mainColor.map {
        it.setAlpha(100 - config.rightTransparent)
    }

    private val _time: MutableLiveData<String> = MutableLiveData()
    val time: LiveData<String>
        get() = _time

    private val _fontColor: MutableLiveData<Int> = MutableLiveData()
    val fontColor: LiveData<Int>
        get() = _fontColor

    var errorText = ""

    val roomText: LiveData<String> = status.map {
        when (it) {
            Status.STATUS_AVAILABLE -> "\nДоступно\n"
            Status.STATUS_OCCUPIED -> currentEvent.value?.toString() ?: ""
            Status.STATUS_WAIT -> "Доступно на ${
                getTextForMinutes(currentEvent.value?.getRemainedTime() ?: 0)
            }"
            else -> "\n$errorText\n"
        }
    }

    val nextEventText: LiveData<String> = currentEvent.map {
        val noEventsText = "\nНет ближайших событий\n"
        val eventList = eventList.value?.sortedBy { it.startDate }

        if (it == null || eventList.isNullOrEmpty()) {
            noEventsText
        } else {
            val nextevent = if (!it.isEventTakesPlaceNow())
                it
            else
                eventList.getOrNull(eventList.indexOf(it) + 1)

            if (nextevent == null)
               noEventsText
            else
                "Следующее событие:\n$nextevent"
        }
    }


    val bookButtonText: LiveData<String> = status.map {
        if (it != Status.STATUS_OCCUPIED) {
            "Забронировать зал"
        } else {
            "Управление"
        }
    }

    val timeFormat = MutableLiveData<Format>().apply { postValue(config.timeFormat) }

    val filterDate = MutableLiveData<Date>().apply { postValue(Date().atStartOfDay()) }
    val filteredEvents: LiveData<Array<Event>> = filterDate.switchMap { date ->
        eventList.switchMap {
            val filtered = MutableLiveData<Array<Event>>()
            filtered.postValue(it?.filter {
                //                date.atStartOfDay().before(it.startDate) && date.atEndOfDay().after(it.endDate)
                it.startDate.after(date.atStartOfDay()) && it.startDate.before(date.atEndOfDay()) ||
                        it.endDate.after(date.atStartOfDay()) && it.endDate.before(date.atEndOfDay())
            }
                ?.toTypedArray())
            filtered
        }
    }

    val eventList = repo.getEventsLive()
    private val repoObserver = Observer<Array<Event>?> {
        setEvents(it)
    }

    val messages = repo.getMessages()
    val loading = repo.getLoadingState()

    init {
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
        dontShowAndroidKeyboard = config.dontShowAndroidKeyboard
    }

    //Updating state
    private fun update() {
        val closest = findClosestEvent()
        currentEvent.postValue(closest)
    }

    private var events = eventList.value

    private fun setEvents(events: Array<Event>?) {
        if (events != null) {
            this.events = events
            isErrorState = false
            update()
        } else {
            isErrorState = true
        }

    }

    private fun findClosestEvent(): Event? {
        return eventList.value?.sortedBy { it.startDate }?.firstOrNull {
            (Date().after(it.startDate) && Date().before(it.endDate)) || it.startDate.after(Date())
        }
    }

    private fun getTextForMinutes(minutes: Int): String {
        return try {
            if (minutes in 11..19) {
                "$minutes минут"
            } else {
                val word = when (minutes % 10) {
                    1 -> "минуту"
                    in 2..4 -> "минуты"
                    else -> "минут"
                }
                "$minutes $word"
            }
        } catch (e: Exception) {
            "$minutes минут"
        }

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
        repo.addEvent(event)
    }

    fun closeEvent() {
        val event = currentEvent.value?.copy(endDate = Date())
        event?.also {
            repo.updateEvent(event)
        }

    }

    fun extendEvent(length: Int) {
        val event = currentEvent.value?.copy()
        event?.apply {
            val timeEnd = Calendar.getInstance()
            timeEnd.time = this.endDate
            timeEnd.add(Calendar.MINUTE, length)
            endDate = timeEnd.time
        }
        event?.also {
            repo.updateEvent(event)
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
        val timeFormat: Format,
        val dontShowAndroidKeyboard: Boolean
    ) {
        companion object {
            fun getDefault() = Config(
                "",
                Color.GREEN,
                Color.RED,
                Color.YELLOW,
                Color.BLUE,
                Color.WHITE,
                40,
                60,
                Format.FORMAT_24H,
                false
            )
        }
    }


    //keyboard features
    var keyboardWrapper: IKeyboardWrapper? = null

    interface IKeyboardWrapper {
        fun onEnterEventNameClicked(fromTopToFieldBottom: Float, editText: EditText)
        fun onBookDialogClose()
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