package com.donteco.roombooking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.donteco.roombooking.databinding.DialogBookingBinding
import com.donteco.roombooking.databinding.ManageCurrentBinding

class BookingDialog : DialogFragment() {
    companion object {
        fun newInstance(mode: Mode) = BookingDialog().apply { this.mode = mode }
    }

    var mode = Mode.BOOK
    lateinit var viewModel: MainViewModel
    lateinit var binding: DialogBookingBinding

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

    fun prepareBook() {
        binding.headText.text = "Назначить встречу"
        binding.tabLayout.addTab(binding.tabLayout.newTab().apply { text = "Сейчас" })
        binding.tabLayout.addTab(binding.tabLayout.newTab().apply { text = "Позже" })
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
            text = "1 час"
            setOnClickListener { bookNow(60) }
        }
    }

    fun bookNow(length: Int) {
        viewModel.createEvent(length)
    }

    fun extend(length: Int) {
        viewModel.extendEvent(length)
    }

    fun close() {
        viewModel.closeEvent()
    }


    fun prepareManage() {
        binding.headText.text = "Управление"
        binding.tabLayout.addTab(binding.tabLayout.newTab().apply { text = "Текущая встреча" })
        binding.tabLayout.addTab(binding.tabLayout.newTab().apply { text = "Новая встеча" })
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

    enum class Mode {
        BOOK,
        MANAGE
    }
}