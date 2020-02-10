package com.donteco.roombookingfragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.donteco.roombookingfragment.databinding.DialogBookingBinding
import com.donteco.roombookingfragment.databinding.ManageCurrentBinding
import com.donteco.roombookingfragment.databinding.ManageFutureBinding
import com.google.android.material.tabs.TabLayout
import java.util.*

class BookingDialog : DialogFragment() {
    companion object {
        fun newInstance(mode: Mode, IClosable: IClosable) = BookingDialog().apply {
            this.mode = mode
            this.callback = IClosable
        }
    }

    var mode = Mode.BOOK
    lateinit var viewModel: MainViewModel
    lateinit var binding: DialogBookingBinding
    lateinit var callback: IClosable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
        binding = DialogBookingBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = activity!!

        binding.model = viewModel

        when (mode) {
            Mode.BOOK -> {
                prepareBook()
            }
            Mode.MANAGE -> {
                prepareManage()
            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.6f).toInt(),
            (resources.displayMetrics.heightPixels * 0.8f).toInt()
        )
    }

    private fun prepareBook() {
        binding.headText.text = "Назначить встречу"
        val tab1 = binding.tabLayout.newTab().apply { text = "Сейчас" }
        val tab2 = binding.tabLayout.newTab().apply { text = "Позже" }
        binding.tabLayout.addTab(tab1)
        binding.tabLayout.addTab(tab2)
        inflateFirstTabPrepare()
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.frame.removeAllViews()
                if (tab == tab1) {
                    inflateFirstTabPrepare()
                } else {
                    inflateSecondTab()
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun inflateFirstTabPrepare() {
        val buttonLayout =
            ManageCurrentBinding.inflate(LayoutInflater.from(activity), binding.frame, true)
        buttonLayout.button1.apply {
            text = "15 мин"
            setOnClickListener { bookNow(15) }
        }
        buttonLayout.button2.apply {
            text = "30 мин"
            setOnClickListener { bookNow(30) }
        }
        buttonLayout.button3.apply {
            text = " 1 час "
            setOnClickListener { bookNow(60) }
        }
    }

    private fun inflateSecondTab() {
        val layout = ManageFutureBinding.inflate(LayoutInflater.from(activity), binding.frame, true)
        layout.chooserDay.apply {
            val names = arrayOf("Сегодня", "Завтра")
            minValue = 1
            maxValue = names.size
            displayedValues = names
        }

        layout.chooserHour.apply {
            val names = arrayListOf<String>()
            for (i in 0..23) {
                names.add(
                    DayViewLayout.getTextForHour(
                        i,
                        viewModel.timeFormat.value!!,
                        true
                    )
                )
            }
            minValue = 0
            maxValue = 23
            displayedValues = names.toTypedArray()
            value = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1
        }

        layout.chooserMinute.apply {
            val names = arrayOf("00", "15", "30", "45")
            minValue = 1
            maxValue = 4
            displayedValues = names
        }

        layout.chooserLength.apply {
            val names = arrayOf("15 мин", "30 мин", "45 мин", "1 час")
            minValue = 1
            maxValue = names.size
            displayedValues = names
        }

        layout.appCompatButton.setOnClickListener {
            val day =
                if (layout.chooserDay.value == 1) Date() else Date().apply { time += 24 * 60 * 60 * 1000 }

            val calendar = Calendar.getInstance().apply {
                time = day
                set(Calendar.HOUR_OF_DAY, layout.chooserHour.value)
                set(Calendar.MINUTE, layout.chooserMinute.value * 15)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val date = calendar.time
            val length = layout.chooserLength.value * 15
            val name =
                if (layout.eventName.text.toString().isEmpty()) "Раннее бронировани" else layout.eventName.text.toString()

            bookLater(date, length, name)
        }
    }

    private fun prepareManage() {
        binding.headText.text = "Управление"
        val tab1 = binding.tabLayout.newTab().apply { text = "Текущая встреча" }
        val tab2 = binding.tabLayout.newTab().apply { text = "Новая встеча" }
        binding.tabLayout.addTab(tab1)
        binding.tabLayout.addTab(tab2)
        inflateFirstTabManage()
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.frame.removeAllViews()
                if (tab == tab1) {
                    inflateFirstTabManage()
                } else {
                    inflateSecondTab()
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun inflateFirstTabManage() {
        val buttonLayout =
            ManageCurrentBinding.inflate(LayoutInflater.from(activity), binding.frame, true)
        buttonLayout.button1.apply {
            text = "Завершить"
            setOnClickListener { close() }
        }
        buttonLayout.button2.apply {
            text = "+15 мин"
            setOnClickListener { extend(15) }
        }
        buttonLayout.button3.apply {
            text = "+30 мин"
            setOnClickListener { extend(30) }
        }
    }


    private fun bookNow(length: Int) {
        viewModel.createEvent(length)
        callback.close()
        dismiss()
    }

    private fun bookLater(startDate: Date, length: Int, name: String) {
        viewModel.createEvent(startDate, length, name)
        callback.close()
        dismiss()
    }

    private fun extend(length: Int) {
        viewModel.extendEvent(length)
        callback.close()
        dismiss()
    }

    private fun close() {
        viewModel.closeEvent()
        callback.close()
        dismiss()
    }


    enum class Mode {
        BOOK,
        MANAGE
    }
}