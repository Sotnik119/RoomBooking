package com.donteco.roombooking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import com.donteco.roombooking.databinding.ActivityMainLandBinding

class RoomBookingFragment : Fragment() {
    companion object {
        fun newInstance() = RoomBookingFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = ActivityMainLandBinding.inflate(layoutInflater)
        val repo = FakeEventRepository()
        val viewModel = ViewModelProviders.of(activity!!, MainViewModelFactory(repo))
            .get(MainViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.mainFrame.orientation = when (resources.configuration.orientation) {
            1 -> LinearLayout.VERTICAL
            2 -> LinearLayout.HORIZONTAL
            else -> LinearLayout.VERTICAL
        }

        binding.dialog.setOnTouchListener { _, _ ->
            binding.dialog.visibility = View.GONE
            false
        }

        binding.roomStatus.btnCalendar.setOnClickListener {
            activity!!.supportFragmentManager.beginTransaction().apply {
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                replace(R.id.dialog_frame, DayViewDialog.newInstance(150))
                commit()
            }
            binding.dialog.visibility = View.VISIBLE
            // DayViewDialog.newInstance(150).show(activity!!.supportFragmentManager, "DayViewDialog")
        }

        binding.roomTime.btnBookRoom.setOnClickListener {

            activity!!.supportFragmentManager.beginTransaction().apply {
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                replace(R.id.dialog_frame, BookingDialog.newInstance(BookingDialog.Mode.BOOK))
                commit()
            }
            binding.dialog.visibility = View.VISIBLE

//            BookingDialog.newInstance(BookingDialog.Mode.BOOK).show(activity!!.supportFragmentManager, "BookDialog")
        }

        return binding.root
    }
}