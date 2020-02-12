package com.donteco.roombooking

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.donteco.roombookingfragment.MainViewModel
import com.donteco.roombookingfragment.MainViewModelFactory
import com.donteco.roombookingfragment.RoomBookingFragment

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
        val model =
            ViewModelProviders.of(this, MainViewModelFactory(null, null)).get(
                MainViewModel::class.java
            )

        model.roomName.postValue("Переговорка 2")

        frag = RoomBookingFragment.newInstance(orientation, true)

        supportFragmentManager.beginTransaction().replace(R.id.container, frag!!, "room").commit()
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        supportFragmentManager.beginTransaction().remove(frag!!).commit()
//    }
}
