package com.donteco.roombookingfragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_main_land.view.*
import kotlinx.android.synthetic.main.dialog_frame_layout.view.*
import kotlinx.android.synthetic.main.info_message.view.*
import kotlinx.android.synthetic.main.room_status.view.*
import kotlinx.android.synthetic.main.room_time.view.*

class RoomBookingFragment : Fragment(), IClosable {


    var orientation = Orientation.HORIZONTAL
    var useCustomDialogs = true

    companion object {
        fun newInstance(
            orientation: Orientation,
            customDialogs: Boolean,
            customDialogRoot: ViewGroup
        ) =
            RoomBookingFragment().apply {
                this.orientation = orientation
                this.useCustomDialogs = customDialogs
                this.customDialogRoot = customDialogRoot
            }
    }

    private lateinit var layout: View
    private lateinit var loading: View
    private lateinit var dialogView: View
    private lateinit var viewModel: MainViewModel
    lateinit var customDialogRoot: ViewGroup

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

        viewModel = ViewModelProvider(activity!!).get(MainViewModel::class.java)
//            ViewModelProviders.of(activity!!, MainViewModelFactory(repo)).get(MainViewModel::class.java)

        viewModel.widgetOrientation = orientation

        layout.main_frame.orientation = when (orientation) {
            Orientation.VERTICAL -> LinearLayout.VERTICAL
            Orientation.HORIZONTAL -> LinearLayout.HORIZONTAL
        }

        viewModel.status.observe(viewLifecycleOwner, Observer {
            if (it == MainViewModel.Status.STATUS_UNKNOWN) {
                layout.btn_calendar.visibility = View.GONE
                layout.btn_book_room.visibility = View.GONE
            } else {
                layout.btn_calendar.visibility = View.VISIBLE
                layout.btn_book_room.visibility = View.VISIBLE
            }
        })

        viewModel.mainColorLeft.observe(viewLifecycleOwner, Observer {
            layout.room_status.setBackgroundColor(it)
        })

        viewModel.mainColorRight.observe(viewLifecycleOwner, Observer {
            layout.room_time.setBackgroundColor(it)
        })

        viewModel.roomName.observe(viewLifecycleOwner, Observer {
            layout.room_name.text = it
        })

        viewModel.additionalText.observe(viewLifecycleOwner, Observer {
            layout.room_additional_message.text = it
        })

        viewModel.time.observe(viewLifecycleOwner, Observer {
            layout.time.text = it
        })

        viewModel.roomText.observe(viewLifecycleOwner, Observer {
            layout.status.text = it
        })

        viewModel.bookButtonText.observe(viewLifecycleOwner, Observer {
            layout.btn_book_room.text = it
        })

        viewModel.mainColor.observe(viewLifecycleOwner, Observer {
            layout.btn_book_room.setBackgroundColor(it)
        })

        viewModel.fontColor.observe(viewLifecycleOwner, Observer {
            layout.btn_book_room.setTextColor(it)
            layout.status.setTextColor(it)
            layout.time.setTextColor(it)
            layout.room_name.setTextColor(it)
            layout.btn_calendar.setColorFilter(it)
            layout.room_additional_message.setTextColor(it)
        })

        viewModel.messages.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                showMessage(it.type, it.header, it.text)
                viewModel.messages.postValue(null)
            }
        })

        dialogView = myInflater.inflate(R.layout.dialog_frame_layout, null)

        dialogView.dialog.setOnTouchListener { _, event ->
            if (closeOnTouch && event.action == MotionEvent.ACTION_UP) close()
            false
        }


        layout.btn_calendar.setOnSingleClickListener {
            closeOnTouch = false
            val h = customDialogRoot.measuredHeight
            val w = customDialogRoot.measuredWidth
            val rowsize = (if (h > w) w else h) / 9
            val dialogFragment =
                DayViewDialog.newInstance(rowsize)
            if (useCustomDialogs) {
                dialogView.visibility = View.VISIBLE
                currentDialogFragment = dialogFragment
                (dialogView.parent as ViewGroup?)?.removeView(
                    dialogView
                )
                dialogFragment.resumeCallback = object : DayViewDialog.Iresumed {
                    override fun onResumed() {
                        Log("fragment calendar resumed")
                        closeOnTouch = true
                    }

                }
                customDialogRoot.addView(
                    dialogView,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
                Log("Start transaction for open fragment")
                try {
                    activity!!.supportFragmentManager.let {
                        it.beginTransaction().apply {
                            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            replace(R.id.dialog_frame, currentDialogFragment!!)
                            commit()
                        }
                        Log("End transaction for open fragment")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log("Error transaction for open fragment")
                }

            } else {
                dialogFragment.show(activity!!.supportFragmentManager, "DayViewDialog")
            }
        }


        layout.btn_book_room.setOnSingleClickListener {
            closeOnTouch = false
            val mode =
                if (viewModel.status.value == MainViewModel.Status.STATUS_OCCUPIED) BookingDialog.Mode.MANAGE else BookingDialog.Mode.BOOK

            val dialogFragment =
                BookingDialog.newInstance(mode, this)
            if (useCustomDialogs) {
                dialogView.visibility = View.VISIBLE
                currentDialogFragment = dialogFragment
                customDialogRoot.addView(
                    dialogView,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
                activity!!.supportFragmentManager.beginTransaction().apply {
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    replace(R.id.dialog_frame, currentDialogFragment!!)
                    commit()
                    closeOnTouch = true
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

        viewModel.loading.observe(viewLifecycleOwner, Observer {
            showLoading(it.type, it.header, it.text)
        })
        return layout
    }


    /**
     * Close dialog
     */
    override fun close() {
        Log("Start close dialog")
        if (currentDialogFragment != null) {
            activity!!.supportFragmentManager.beginTransaction().apply {
                remove(currentDialogFragment!!)
                commit()
            }
            dialogView.visibility = View.GONE
            try {
                dialogView.parent?.let {
                    (it as ViewGroup).removeView(dialogView)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        viewModel.keyboardWrapper?.onBookDialogClose()
    }

    private var closeOnTouch: Boolean = false
        set(value) {

            field = value
        }

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
        layout.isFocusable = true
        layout.isClickable = true

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
                loading.isFocusable = true
                loading.isClickable = true
            }
            currentAnimation = ObjectAnimator.ofFloat(loading, View.ALPHA, 0f, 1f).apply {
                duration = 500
            }
            currentAnimation?.start()
        } else {
            currentAnimation = ObjectAnimator.ofFloat(loading, View.ALPHA, 1f, 0f).apply {
                duration = 500
            }
            loading.isFocusable = false
            loading.isClickable = false
            currentAnimation?.start()
        }
    }

    enum class Orientation {
        VERTICAL,
        HORIZONTAL
    }
}