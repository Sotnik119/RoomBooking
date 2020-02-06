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

class RoomBookingFragment : Fragment(), IClosable {


    companion object {
        fun newInstance(orientation: Orientation, timeFormat: Format, customDialogs: Boolean) =
            RoomBookingFragment().apply {
                this.orientation = orientation
                this.useCustomDialogs = customDialogs
                this.timeFormat = timeFormat
            }
    }

    var orientation = Orientation.HORIZONTAL
    var useCustomDialogs = true
    var timeFormat = Format.FORMAT_24H

    lateinit var binding: ActivityMainLandBinding
    var currentDialogFragment: Fragment? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = ActivityMainLandBinding.inflate(layoutInflater)
        val repo = FakeEventRepository()
        val viewModel = ViewModelProviders.of(activity!!, MainViewModelFactory(repo))
            .get(MainViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.timeFormat.postValue(timeFormat)

        binding.mainFrame.orientation = when (orientation) {
            Orientation.VERTICAL -> LinearLayout.VERTICAL
            Orientation.HORIZONTAL -> LinearLayout.HORIZONTAL
        }

        binding.dialog.setOnTouchListener { _, _ ->
            close()
            false
        }

        binding.roomStatus.btnCalendar.setOnClickListener {
            val dialog = DayViewDialog.newInstance(150)
            if (useCustomDialogs) {
                binding.dialog.visibility = View.VISIBLE
                currentDialogFragment = dialog
                activity!!.supportFragmentManager.beginTransaction().apply {
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    replace(R.id.dialog_frame, currentDialogFragment!!)
                    commit()
                }
            } else {
                dialog.show(activity!!.supportFragmentManager, "DayViewDialog")
            }
        }


        binding.roomTime.btnBookRoom.setOnClickListener {
            val mode =
                if (viewModel.status.value == MainViewModel.Status.STATUS_AVAILABLE) BookingDialog.Mode.BOOK else BookingDialog.Mode.MANAGE

            val dialog = BookingDialog.newInstance(mode, this)
            if (useCustomDialogs) {
                binding.dialog.visibility = View.VISIBLE
                currentDialogFragment = dialog
                activity!!.supportFragmentManager.beginTransaction().apply {
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    replace(R.id.dialog_frame, currentDialogFragment!!)
                    commit()
                }
            } else {
                dialog.show(activity!!.supportFragmentManager, "BookDialog")
            }
        }
        return binding.root
    }

    /**
     * Close dialog
     */
    override fun close() {
        binding.dialog.visibility = View.GONE
        if (currentDialogFragment != null) {
            activity!!.supportFragmentManager.beginTransaction().apply {
                remove(currentDialogFragment!!)
                commit()
            }
        }
    }

    enum class Orientation {
        VERTICAL,
        HORIZONTAL
    }
}