package com.donteco.roombookingfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main_land.view.*
import kotlinx.android.synthetic.main.room_status.view.*
import kotlinx.android.synthetic.main.room_time.view.*

class RoomBookingFragment : Fragment(), IClosable {


    var orientation = Orientation.HORIZONTAL
    var useCustomDialogs = true

    companion object {
        fun newInstance(
            orientation: Orientation,
            customDialogs: Boolean
        ) =
            RoomBookingFragment().apply {
                this.orientation = orientation
                this.useCustomDialogs = customDialogs
            }
    }

    private lateinit var layout: View

    var currentDialogFragment: Fragment? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        layout = inflater.inflate(R.layout.activity_main_land, container, false)

        val viewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
//            ViewModelProviders.of(activity!!, MainViewModelFactory(repo)).get(MainViewModel::class.java)


        layout.main_frame.orientation = when (orientation) {
            Orientation.VERTICAL -> LinearLayout.VERTICAL
            Orientation.HORIZONTAL -> LinearLayout.HORIZONTAL
        }

        viewModel.mainColorLeft.observe(this, Observer {
            layout.room_status.setBackgroundColor(it)
        })

        viewModel.mainColorRight.observe(this, Observer {
            layout.room_time.setBackgroundColor(it)
        })

        viewModel.roomName.observe(this, Observer {
            layout.room_name.text = it
        })

        viewModel.time.observe(this, Observer {
            layout.time.text = it
        })

        viewModel.roomText.observe(this, Observer {
            layout.status.text = it
        })

        viewModel.bookButtonText.observe(this, Observer {
            layout.btn_book_room.text = it
        })

        viewModel.mainColor.observe(this, Observer {
            layout.btn_book_room.setBackgroundColor(it)
        })

        layout.dialog.setOnTouchListener { _, _ ->
            close()
            false
        }

        layout.btn_calendar.setOnClickListener {
            val dialogFragment =
                DayViewDialog.newInstance(layout.measuredHeight / 8)
            if (useCustomDialogs) {
                layout.dialog.visibility = View.VISIBLE
                currentDialogFragment = dialogFragment
                activity!!.supportFragmentManager.beginTransaction().apply {
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    replace(R.id.dialog_frame, currentDialogFragment!!)
                    commit()
                }
            } else {
                dialogFragment.show(activity!!.supportFragmentManager, "DayViewDialog")
            }
        }


        layout.btn_book_room.setOnClickListener {
            val mode =
                if (viewModel.status.value == MainViewModel.Status.STATUS_OCCUPIED) BookingDialog.Mode.MANAGE else BookingDialog.Mode.BOOK

            val dialogFragment =
                BookingDialog.newInstance(mode, this)
            if (useCustomDialogs) {
                layout.dialog.visibility = View.VISIBLE
                currentDialogFragment = dialogFragment
                activity!!.supportFragmentManager.beginTransaction().apply {
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    replace(R.id.dialog_frame, currentDialogFragment!!)
                    commit()
                }
            } else {
                dialogFragment.show(activity!!.supportFragmentManager, "BookDialog")
            }
        }
        return layout
    }

    /**
     * Close dialog
     */
    override fun close() {
        layout.dialog.visibility = View.GONE
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