package com.donteco.roombookingfragment

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.dialog_events.view.*
import java.text.SimpleDateFormat
import java.util.*


class DayViewDialog : DialogFragment() {

    companion object {
        fun newInstance(rowSize: Int) = DayViewDialog().apply {
            this.rowSize = rowSize
        }
    }

    var rowSize: Int = 100

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val myInflater = inflater.cloneInContext(ContextThemeWrapper(activity, R.style.AppTheme123))
        val layout = myInflater.inflate(R.layout.dialog_events, container, false)
        val viewModel = ViewModelProvider(activity!!).get(MainViewModel::class.java)


        layout.day_view.setRowSize(rowSize)
        layout.day_view.scrollToCurrentTime()

        viewModel.filterDate.postValue(Date())

        viewModel.mainColor.observe(viewLifecycleOwner, Observer {
            layout.header.setBackgroundColor(it)
        })

        viewModel.timeFormat.observe(viewLifecycleOwner, Observer {
            layout.day_view.format = it
        })

        viewModel.fontColor.observe(viewLifecycleOwner, Observer {
            layout.date_text.setTextColor(it)
            layout.prev_day.setColorFilter(it)
            layout.next_day.setColorFilter(it)
        })

        viewModel.filterDate.observe(viewLifecycleOwner, Observer {
            layout.date_text.text =
                it.toFormattedString(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()))
            if (it.atStartOfDay().time == Date().atStartOfDay().time) layout.day_view.drawCurrentTimeLine() else layout.day_view.removeCurrentTimeLine()
        })

        viewModel.filteredEvents.observe(viewLifecycleOwner, Observer {
            layout.day_view.setEvents(it)
        })

        viewModel.roomName.observe(viewLifecycleOwner, Observer {
            layout.day_view.roomName = it
        })

        layout.prev_day.setOnClickListener {
            val date = viewModel.filterDate.value!!
            val c = Calendar.getInstance().apply { time = date; add(Calendar.DAY_OF_MONTH, -1) }
            if (!c.time.before(Date().atStartOfDay()))
                viewModel.filterDate.postValue(c.time)
        }

        layout.next_day.setOnClickListener {
            val date = viewModel.filterDate.value!!
            val c = Calendar.getInstance().apply { time = date; add(Calendar.DAY_OF_MONTH, 1) }
            viewModel.filterDate.postValue(c.time)
        }
        return layout
    }
}
