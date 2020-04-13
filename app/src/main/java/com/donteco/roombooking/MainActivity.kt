package com.donteco.roombooking

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.donteco.roombookingfragment.BaseEventRepository
import com.donteco.roombookingfragment.MainViewModel
import com.donteco.roombookingfragment.MainViewModelFactory
import com.donteco.roombookingfragment.RoomBookingFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var frag: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val orientation = when (resources.configuration.orientation) {
            1 -> RoomBookingFragment.Orientation.VERTICAL
            2 -> RoomBookingFragment.Orientation.HORIZONTAL
            else -> RoomBookingFragment.Orientation.HORIZONTAL
        }

        val repo = BaseEventRepository()
        val model =
            ViewModelProvider(this, MainViewModelFactory(repo, null)).get(
                MainViewModel::class.java
            )
        model.errorText = "Ошибочка"

        frag = RoomBookingFragment.newInstance(orientation, true, root)

        supportFragmentManager.beginTransaction().replace(R.id.container, frag!!, "room").commit()

        seekBarHoriz.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    if (p1 > 10) {
                        val lp = container.layoutParams as ConstraintLayout.LayoutParams
                        lp.matchConstraintPercentWidth = p1.toFloat() / 100
                        container.layoutParams = lp
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }

            }
        )

        seekBarVert.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    if (p1 > 10) {
                        val lp = container.layoutParams as ConstraintLayout.LayoutParams
                        lp.matchConstraintPercentHeight = p1.toFloat() / 100
                        container.layoutParams = lp
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }

            }
        )

        mCheckbox.setOnCheckedChangeListener { _, b ->
            repo.error = b
        }

        interaction_layer.setOnTouchListener { _, _ ->

            Log.d("RoomBooking activity", "Interaction layer touched")
            return@setOnTouchListener false
        }

        hideSystemUI()
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        supportFragmentManager.beginTransaction().remove(frag!!).commit()
//    }


}

fun Activity.hideSystemUI() {
    val decorView: View = window.decorView
    decorView.setOnSystemUiVisibilityChangeListener {
        hideSystemUI()
    }
    decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)

}