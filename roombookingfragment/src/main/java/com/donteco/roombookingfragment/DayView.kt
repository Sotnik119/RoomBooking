package com.donteco.roombookingfragment

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ScrollView
import kotlinx.android.synthetic.main.day_view_event.view.*
import java.util.*
import kotlin.collections.ArrayList

class DayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    val frame = FrameLayout(context)
    private val dayViewLayout = DayViewLayout(context)
    private var _rowSize = 160
    private val events = ArrayList<Event>()
    private var currentDate = Date()
    private val drawedEvents = ArrayList<View>()
    private var currentTimeLine = LayoutInflater.from(context).inflate(R.layout.time_line, null)
    var roomName = ""

    init {
        this.addView(
            frame, ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        )
        frame.addView(
            dayViewLayout,
            ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        )
        dayViewLayout.setRowSize(_rowSize)
    }

    var format: Format =
        Format.FORMAT_24H
        set(value) {
            dayViewLayout.setFormat(value)
            field = value
            drawEvents()
        }

    fun setRowSize(value: Int) {
        dayViewLayout.setRowSize(value)
        _rowSize = value
        drawEvents()
    }


    fun setEvents(events: Array<Event>, currentDate: Date) {
        this.events.clear()
        this.events.addAll(events)
        this.currentDate = currentDate
        drawEvents()
    }

    private fun clearDrawledEvents() {
        drawedEvents.forEach {
            frame.removeView(it)
        }
        drawedEvents.clear()

    }

    private fun drawEvents() {
        clearDrawledEvents()
        val k = Calendar.getInstance()
        events.forEach {
            //get time
            k.time = it.startDate
            val sh = k.get(Calendar.HOUR_OF_DAY)
            val sm = k.get(Calendar.MINUTE)

            k.time = it.endDate
            val eh = k.get(Calendar.HOUR_OF_DAY)
            val em = k.get(Calendar.MINUTE)

            //get start position
            var startPos = (sh * _rowSize + _rowSize.toFloat() / 60 * sm) + _rowSize / 2
            var endPos = (eh * _rowSize + _rowSize.toFloat() / 60 * em) + _rowSize / 2

            if (currentDate.atStartOfDay().after(it.startDate.atStartOfDay())) {
                startPos = 0f
            }
            if (currentDate.atStartOfDay().before(it.endDate.atStartOfDay())) {
                endPos = dayViewLayout.measuredHeight.toFloat()
            }

            val height = endPos - startPos

            val item = LayoutInflater.from(context).inflate(R.layout.day_view_event, null)
            drawedEvents.add(item)
            frame.addView(item, LayoutParams(LayoutParams.MATCH_PARENT, height.toInt()).apply {
                this.topMargin = startPos.toInt()
            })


            item.event_card.setCardBackgroundColor(
                if (it.endDate.time < Date().time) resources.getColor(
                    R.color.eventEnded
                ) else resources.getColor(R.color.roomOccupied)
            )

            val textsize = _rowSize.toFloat() / 4.5f
            if (height >= _rowSize) {
                item.event_room.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize)
                item.event_room.text = roomName
            }
            if (height >= _rowSize / 2) {
                item.event_name.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize)
                item.event_time.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize)
                item.event_name.text = it.name
                item.event_time.text = it.getFormattedTime(format)

            }
        }
    }

    fun drawCurrentTimeLine() {
        removeCurrentTimeLine()
        val calendar = Calendar.getInstance()
        val h = calendar.get(Calendar.HOUR_OF_DAY)
        val m = calendar.get(Calendar.MINUTE)
        val topMargin = (h * _rowSize + _rowSize.toFloat() / 60 * m) + _rowSize / 2
        frame.addView(currentTimeLine, LayoutParams(LayoutParams.MATCH_PARENT, 3).apply {
            this.topMargin = topMargin.toInt()
        })
    }

    fun scrollToCurrentTime() {
        val calendar = Calendar.getInstance()
        val h = calendar.get(Calendar.HOUR_OF_DAY)
        val m = calendar.get(Calendar.MINUTE)
        val topMargin = ((h * _rowSize + _rowSize.toFloat() / 60 * m) + _rowSize / 2)
        this.post {
            scrollTo(0, topMargin.toInt() - measuredHeight / 2)
        }
    }

    fun removeCurrentTimeLine() {
        try {
            frame.removeView(currentTimeLine)
        } catch (e: Exception) {
            android.util.Log.d("DayView", "CurrentTimeLine doesn't have parent")
        }
    }
}