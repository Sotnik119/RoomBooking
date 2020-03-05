package com.donteco.roombookingfragment

import android.content.Context
import android.util.AttributeSet
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
    private val drawedEvents = ArrayList<View>()
    private var currentTimeLine = LayoutInflater.from(context).inflate(R.layout.time_line, null)
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


    fun setEvents(events: Array<Event>) {
        this.events.clear()
        this.events.addAll(events)
        drawEvents()
    }

    private fun clearDrawledEvents() {
        drawedEvents.forEach {
            this.removeView(it)
        }
        drawedEvents.clear()

    }

    private fun drawEvents() {
        clearDrawledEvents()
        events.forEach {
            //get time
            val k = Calendar.getInstance().apply { time = it.startDate }
            val sh = k.get(Calendar.HOUR_OF_DAY)
            val sm = k.get(Calendar.MINUTE)

            k.time = it.endDate
            val eh = k.get(Calendar.HOUR_OF_DAY)
            val em = k.get(Calendar.MINUTE)

            //get start position
            val startPos = (sh * _rowSize + _rowSize.toFloat() / 60 * sm) + _rowSize / 2
            val endPos = (eh * _rowSize + _rowSize.toFloat() / 60 * em) + _rowSize / 2

            val height = endPos - startPos

            val item = LayoutInflater.from(context).inflate(R.layout.day_view_event, null)
            drawedEvents.add(item)
            frame.addView(item, LayoutParams(LayoutParams.MATCH_PARENT, height.toInt()).apply {
                this.topMargin = startPos.toInt()
            })

            item.event_card.setCardBackgroundColor(resources.getColor(R.color.roomOccupied))
            item.event_text.text = it.getEventDescription(format)
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
            scrollTo(0, topMargin.toInt() - measuredHeight/2)
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