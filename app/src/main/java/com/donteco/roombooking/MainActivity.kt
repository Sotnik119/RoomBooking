package com.donteco.roombooking

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProviders
import com.donteco.roombooking.databinding.ActivityMainLandBinding
import kotlinx.android.synthetic.main.activity_main_land.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainLandBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        main_frame.orientation = when (resources.configuration.orientation) {
            1 -> LinearLayout.VERTICAL
            2 -> LinearLayout.HORIZONTAL
            else -> LinearLayout.VERTICAL
        }

    }
}
