package com.donteco.roombookingfragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main_land.view.*
import kotlinx.android.synthetic.main.info_message.view.*
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
    private lateinit var loading: View
    private lateinit var viewModel :MainViewModel

    var currentDialogFragment: Fragment? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val myInflater = inflater.cloneInContext(ContextThemeWrapper(activity, R.style.AppTheme123))
        layout = myInflater.inflate(R.layout.activity_main_land, container, false)

        loading = LayoutInflater.from(context).inflate(R.layout.info_message, null).apply {
            progressBar.visibility = View.VISIBLE
            message_icon.visibility = View.INVISIBLE
            visibility = View.GONE
        }

        viewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
//            ViewModelProviders.of(activity!!, MainViewModelFactory(repo)).get(MainViewModel::class.java)

        viewModel.widgetOrientation = orientation

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

        viewModel.fontColor.observe(this, Observer {
            layout.btn_book_room.setTextColor(it)
            layout.status.setTextColor(it)
            layout.time.setTextColor(it)
            layout.room_name.setTextColor(it)
            layout.btn_calendar.setColorFilter(it)
        })

        viewModel.messages.observe(this, Observer {
            if (it != null) {
                showMessage(it.type, it.header, it.text)
                viewModel.messages.postValue(null)
            }
        })

        layout.dialog.setOnTouchListener { _, _ ->
            close()
            false
        }

        layout.btn_calendar.setOnClickListener {
            val rowsize = (if(layout.measuredHeight > layout.measuredWidth) layout.measuredWidth else layout.measuredHeight) / 9
            val dialogFragment =
                DayViewDialog.newInstance(rowsize)
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

        (layout as ViewGroup).addView(
            loading, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        viewModel.loading.observe(this, Observer {
            showLoading(it.type, it.header, it.text)
        })
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
        viewModel.keyboardWrapper?.onBookDialogClose()
    }

//    fun sendColorBroadcast(color: Int) {
//        activity?.sendBroadcast(
//            Intent("com.cubicmedia.action.LED_BAR_COLOR").setPackage("com.donteco.elcledcontroller")
//                .putExtra("COLOR", color)
//                .putExtra("BRIGHTNESS", 100)
//        )
//    }


    private fun showMessage(type: Boolean, headText: String, messageText: String) {
        val layout: View = LayoutInflater.from(context).inflate(R.layout.info_message, null)
        layout.apply {
            message_icon.setImageResource(if (type) R.drawable.ic_tick else R.drawable.ic_cancel)
            message_status.text = headText
            message_text.text = messageText

            alpha = 0f
            visibility = View.VISIBLE
        }

        (this.layout as ViewGroup).addView(
            layout,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        val animationShow = ObjectAnimator.ofFloat(layout, View.ALPHA, 0f, 1f).apply {
            duration = 500
        }
        val animationHide = ObjectAnimator.ofFloat(layout, View.ALPHA, 1f, 0f).apply {
            duration = 500
            startDelay = 2000L
        }

        val set = AnimatorSet()
        set.playSequentially(animationShow, animationHide)
        set.doOnEnd { (this.layout as ViewGroup).removeView(layout) }
        set.start()

    }

    private var currentAnimation: ObjectAnimator? = null
    private fun showLoading(show: Boolean, headText: String = "", text: String = "") {
        currentAnimation?.cancel()
        if (show) {
            loading.apply {
                message_status.text = headText
                message_text.text = text
                alpha = 0f
                visibility = View.VISIBLE
            }
            currentAnimation = ObjectAnimator.ofFloat(loading, View.ALPHA, 0f, 1f).apply {
                duration = 500
            }
            currentAnimation?.start()
        } else {
            currentAnimation = ObjectAnimator.ofFloat(loading, View.ALPHA, 1f, 0f).apply {
                duration = 500
            }
            currentAnimation?.start()
        }
    }


    enum class Orientation {
        VERTICAL,
        HORIZONTAL
    }
}