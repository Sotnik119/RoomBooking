package com.donteco.roombooking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.donteco.roombooking.databinding.DialogBookingBinding

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
            LayoutInflater.from(activity).inflate(R.layout.manage_current, binding.frame)

    }


    fun prepareManage() {
        binding.headText.text = "Управление"
        binding.tabLayout.addTab(binding.tabLayout.newTab().apply { text = "Текущая встреча" })
        binding.tabLayout.addTab(binding.tabLayout.newTab().apply { text = "Новая встеча" })
    }

    enum class Mode {
        BOOK,
        MANAGE
    }
}