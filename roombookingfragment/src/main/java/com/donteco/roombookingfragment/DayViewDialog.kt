package com.donteco.roombookingfragment

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.dialog_events.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min


class DayViewDialog : DialogFragment() {

    companion object {
        fun newInstance(rowSize: Int) = DayViewDialog().apply {
            this.rowSize = rowSize
        }
    }

    var rowSize: Int = 100
    var resumeCallback: Iresumed? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val myInflater = inflater.cloneInContext(ContextThemeWrapper(activity, R.style.AppTheme123))
        val layout = myInflater.inflate(R.layout.dialog_events, container, false)

        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel = ViewModelProvider(activity!!).get(MainViewModel::class.java)


        day_view.setRowSize(rowSize)
        day_view.scrollToCurrentTime()

        viewModel.filterDate.postValue(Date())

        viewModel.mainColor.observe(viewLifecycleOwner, Observer {
            header.setBackgroundColor(it)
        })

        viewModel.timeFormat.observe(viewLifecycleOwner, Observer {
            day_view.format = it
        })

        viewModel.fontColor.observe(viewLifecycleOwner, Observer {
            date_text.setTextColor(it)
            prev_day.setColorFilter(it)
            next_day.setColorFilter(it)
        })

        viewModel.filterDate.observe(viewLifecycleOwner, Observer {
            date_text.text =
                it.toFormattedString(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()))
            if (it.atStartOfDay().time == Date().atStartOfDay().time) {
                day_view.drawCurrentTimeLine()
                prev_day.visibility = View.GONE
            } else {
                day_view.removeCurrentTimeLine()
                prev_day.visibility = View.VISIBLE
            }
        })

        viewModel.filteredEvents.observe(viewLifecycleOwner, Observer {
            day_view.setEvents(it, viewModel.filterDate.value ?: Date())
        })

        viewModel.roomName.observe(viewLifecycleOwner, Observer {
            day_view.roomName = it
        })

        prev_day.setOnClickListener {
            val date = viewModel.filterDate.value!!
            val c = Calendar.getInstance().apply { time = date; add(Calendar.DAY_OF_MONTH, -1) }
            if (!c.time.before(Date().atStartOfDay()))
                viewModel.filterDate.postValue(c.time)
        }

        next_day.setOnClickListener {
            val date = viewModel.filterDate.value!!
            val c = Calendar.getInstance().apply { time = date; add(Calendar.DAY_OF_MONTH, 1) }
            viewModel.filterDate.postValue(c.time)
        }
    }

    override fun onStart() {
        super.onStart()
        val x =
            min(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels) * 0.9f
        dialog?.window?.setLayout(
            x.toInt(),
            x.toInt()
        )
    }

    override fun onResume() {
        super.onResume()
        resumeCallback?.onResumed()
    }

    interface Iresumed {
        fun onResumed();
    }
}
