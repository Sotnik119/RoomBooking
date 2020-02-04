package com.donteco.roombooking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
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

        binding.roomStatus.btnCalendar.setOnClickListener {
            //            supportFragmentManager.beginTransaction().apply {
//                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                add(R.id.fragment_day, DayViewDialog())
//                addToBackStack(null)
//                commit()
//            }
            DayViewDialog.newInstance().show(activity!!.supportFragmentManager, "DayViewDialog")
        }

        binding.roomTime.btnBookRoom.setOnClickListener {

        }

        return binding.root
    }
}