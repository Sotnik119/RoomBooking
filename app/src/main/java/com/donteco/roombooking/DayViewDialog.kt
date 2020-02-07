package com.donteco.roombooking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.donteco.roombooking.databinding.DialogEventsBinding
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

        val binding = DialogEventsBinding.inflate(layoutInflater, container, false)
        val viewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.dayView.setRowSize(rowSize)

        viewModel.filterDate.postValue(Date())

        viewModel.timeFormat.observe(this, Observer {
            binding.dayView.format = it
        })

        viewModel.filterDate.observe(this, Observer {
            binding.dateText.text =
                it.toFormattedString(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()))
            if (it.atStartOfDay().time ==  Date().atStartOfDay().time) binding.dayView.drawCurrentTimeLine() else binding.dayView.removeCurrentTimeLine()
        })

        viewModel.filteredEvents.observe(this, Observer {
            binding.dayView.setEvents(it)
        })

        binding.prevDay.setOnClickListener {
            val date = viewModel.filterDate.value!!
            val c = Calendar.getInstance().apply { time = date; add(Calendar.DAY_OF_MONTH, -1) }
            if (!c.time.before(Date().atStartOfDay()))
                viewModel.filterDate.postValue(c.time)
        }

        binding.nextDay.setOnClickListener {
            val date = viewModel.filterDate.value!!
            val c = Calendar.getInstance().apply { time = date; add(Calendar.DAY_OF_MONTH, 1) }
            viewModel.filterDate.postValue(c.time)
        }
        return binding.root
    }
}
