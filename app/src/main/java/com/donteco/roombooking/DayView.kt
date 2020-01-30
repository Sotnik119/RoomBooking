package com.donteco.roombooking

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import java.util.*
import kotlin.collections.ArrayList

class DayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) :
    FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    private val dayViewLayout = DayViewLayout(context)
    private var _rowSize = 60

    init {
        this.addView(
            dayViewLayout,
            ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        )
        dayViewLayout.setRowSize(_rowSize)

    }

    private val events = ArrayList<Event>()
    private val drawedEvents = ArrayList<View>()

    fun setRowSize(value: Int) {
        dayViewLayout.setRowSize(value)
        _rowSize = value
        drawEvents()
    }

    fun setFormat(format: DayViewLayout.Format) {
        dayViewLayout.setFormat(format)
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
            val startPos = (sh * _rowSize + _rowSize / 60 * sm) + _rowSize/2
            val endPos = (eh * _rowSize + _rowSize / 60 * em) + _rowSize/2

            val height = endPos - startPos

            val item = View(context)

            this.addView(item, LayoutParams(LayoutParams.MATCH_PARENT,height).apply {
                this.topMargin = startPos
            })
            drawedEvents.add(item)

            item.setBackgroundColor(resources.getColor(R.color.roomOccupied))
        }
    }
}