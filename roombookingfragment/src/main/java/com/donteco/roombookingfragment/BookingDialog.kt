package com.donteco.roombookingfragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.dialog_booking.view.*
import kotlinx.android.synthetic.main.manage_current_landscape.view.*
import kotlinx.android.synthetic.main.manage_future.view.*
import java.util.*
import kotlin.math.min

class BookingDialog : DialogFragment() {
    companion object {
        fun newInstance(mode: Mode, IClosable: IClosable) = BookingDialog().apply {
            this.mode = mode
            this.callback = IClosable
        }
    }

    var mode = Mode.BOOK
    lateinit var viewModel: MainViewModel
    lateinit var layout: View
    lateinit var callback: IClosable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = ViewModelProvider(activity!!).get(MainViewModel::class.java)
        val myInflater = inflater.cloneInContext(ContextThemeWrapper(activity, R.style.AppTheme123))
        layout = myInflater.inflate(R.layout.dialog_booking, container, false)

        viewModel.mainColor.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            layout.head_layout.setBackgroundColor(it)
        })
        viewModel.fontColor.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            layout.head_text.setTextColor(it)
        })

        when (mode) {
            Mode.BOOK -> {
                prepareBook()
            }
            Mode.MANAGE -> {
                prepareManage()
            }
        }

        return layout
    }

    override fun onStart() {
        super.onStart()
        val x = min(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels) * 0.9f
        dialog?.window?.setLayout(
            x.toInt(),
            x.toInt()
        )
    }

    private fun prepareBook() {
        layout.head_text.text = "Назначить встречу"
        val tab1 = layout.tab_layout.newTab().apply { text = "Сейчас" }
        val tab2 = layout.tab_layout.newTab().apply { text = "Позже" }
        layout.tab_layout.addTab(tab1)
        layout.tab_layout.addTab(tab2)
        inflateFirstTabPrepare()
        layout.tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                layout.frame.removeAllViews()
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
        viewModel.keyboardWrapper?.onBookDialogClose()
        val myInflater = LayoutInflater.from(activity)
            .cloneInContext(ContextThemeWrapper(activity, R.style.AppTheme123))
        val buttonLayout = myInflater.inflate(
            R.layout.manage_current_portrait,
            layout.frame,
            true
        )
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
        val myInflater = LayoutInflater.from(activity)
            .cloneInContext(ContextThemeWrapper(activity, R.style.AppTheme123))
        val layout =
            myInflater.inflate(R.layout.manage_future, layout.frame, true)

        if (viewModel.dontShowAndroidKeyboard) {
            layout.event_name.isFocusable = false
        }

        layout.event_name.setOnTouchListener { view, event ->
            if (event.getAction() == MotionEvent.ACTION_UP) {
                var fromTopToFieldBottom = 0F
                try {
                    fromTopToFieldBottom =
                        layout.event_name.getScreenPositionY().toFloat() + layout.event_name.height
                } catch (e: Exception) {

                } finally {
                    viewModel.keyboardWrapper?.onEnterEventNameClicked(
                        fromTopToFieldBottom,
                        layout.event_name
                    )
                }

            }
            false
        }

        val numberPickersTextSize = this@BookingDialog.layout.measuredHeight.toFloat() / 22
        val numberPickerDividerColor =
            ResourcesCompat.getColor(resources, R.color.numberpicker_divider, null)

        layout.chooser_day.apply {
            val names = arrayOf("Сегодня", "Завтра")
            dividerColor = numberPickerDividerColor
            minValue = 1
            maxValue = names.size
            displayedValues = names
            textSize = numberPickersTextSize
            selectedTextSize = numberPickersTextSize
        }

        layout.chooser_hour.apply {
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
            dividerColor = numberPickerDividerColor
            minValue = 0
            maxValue = 23
            displayedValues = names.toTypedArray()
            value = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1
            textSize = numberPickersTextSize
            selectedTextSize = numberPickersTextSize
        }

        layout.chooser_minute.apply {
            val names = arrayOf("00", "15", "30", "45")
            dividerColor = numberPickerDividerColor
            minValue = 0
            maxValue = names.size - 1
            displayedValues = names
            textSize = numberPickersTextSize
            selectedTextSize = numberPickersTextSize
        }

        layout.chooser_length.apply {
            val names = arrayOf("15 мин", "30 мин", "45 мин", "1 час")
            dividerColor = numberPickerDividerColor
            minValue = 1
            maxValue = names.size
            displayedValues = names
            textSize = numberPickersTextSize
            selectedTextSize = numberPickersTextSize
        }

        layout.appCompatButton.setOnClickListener {
            val day =
                if (layout.chooser_day.value == 1) Date() else Date().apply { time += 24 * 60 * 60 * 1000 }

            val calendar = Calendar.getInstance().apply {
                time = day
                set(Calendar.HOUR_OF_DAY, layout.chooser_hour.value)
                set(Calendar.MINUTE, layout.chooser_minute.value * 15)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val date = calendar.time
            val length = layout.chooser_length.value * 15
            val name =
                if (layout.event_name.text.toString().isEmpty()) "Раннее бронирование" else layout.event_name.text.toString()

            bookLater(date, length, name)
        }
    }

    private fun prepareManage() {
        layout.head_text.text = "Управление"
        val tab1 = layout.tab_layout.newTab().apply { text = "Текущая встреча" }
        val tab2 = layout.tab_layout.newTab().apply { text = "Новая встреча" }
        layout.tab_layout.addTab(tab1)
        layout.tab_layout.addTab(tab2)
        inflateFirstTabManage()
        layout.tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                layout.frame.removeAllViews()
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
        viewModel.keyboardWrapper?.onBookDialogClose()
        val myInflater = LayoutInflater.from(activity)
            .cloneInContext(ContextThemeWrapper(activity, R.style.AppTheme123))
        val buttonLayout = myInflater.inflate(
            R.layout.manage_current_portrait,
            layout.frame,
            true
        )
        buttonLayout.button1.apply {
            text = "Завершить"
            setOnClickListener { close() }
        }
        buttonLayout.button2.apply {
            text = "+ 15 мин."
            setOnClickListener { extend(15) }
        }
        buttonLayout.button3.apply {
            text = "+ 30 мин."
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
