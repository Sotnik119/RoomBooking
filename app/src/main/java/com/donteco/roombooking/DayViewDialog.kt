package com.donteco.roombooking

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.donteco.roombooking.databinding.DialogEventsBinding
import kotlinx.android.synthetic.main.dialog_events.view.*
import java.text.SimpleDateFormat
import java.util.*


class DayViewDialog : DialogFragment() {

    companion object {
        fun newInstance() = DayViewDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = DialogEventsBinding.inflate(layoutInflater, container, false)
        val viewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.filterDate.postValue(Date())

        viewModel.timeFormat.observe(this, Observer {
            binding.frame.day_view.format = it
        })

        viewModel.filterDate.observe(this, Observer {
            binding.frame.date_text.text =
                it.toFormattedString(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()))
        })

        viewModel.filteredEvents.observe(this, Observer {
            binding.frame.day_view.setEvents(it)
        })

        binding.frame.prev_day.setOnClickListener {
            val date = viewModel.filterDate.value!!
            val c = Calendar.getInstance().apply { time = date; add(Calendar.DAY_OF_MONTH, -1) }
            viewModel.filterDate.postValue(c.time)
        }

        binding.frame.next_day.setOnClickListener {
            val date = viewModel.filterDate.value!!
            val c = Calendar.getInstance().apply { time = date; add(Calendar.DAY_OF_MONTH, 1) }
            viewModel.filterDate.postValue(c.time)
        }
        return binding.root
    }
}
