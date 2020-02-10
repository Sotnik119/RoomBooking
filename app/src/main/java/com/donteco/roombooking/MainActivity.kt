package com.donteco.roombooking

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import com.donteco.roombooking.databinding.ActivityMainLandBinding
import kotlinx.android.synthetic.main.activity_main_land.*
import kotlinx.android.synthetic.main.dialog_events.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val orientation = when (resources.configuration.orientation) {
            1 -> RoomBookingFragment.Orientation.VERTICAL
            2 -> RoomBookingFragment.Orientation.HORIZONTAL
            else -> RoomBookingFragment.Orientation.HORIZONTAL
        }
        val roomFragment =
            RoomBookingFragment.newInstance(orientation, Format.FORMAT_24H, true, null)

        supportFragmentManager.beginTransaction().add(R.id.container, roomFragment, "room").commit()
    }
}
