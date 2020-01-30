package com.donteco.roombooking

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.dialog_events.*


class Main2Activity : AppCompatActivity() {
    var size = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_events)

        val repo = EventRepo()

        day_view.setEvents(repo.getEvents())

        day_view.setFormat(DayViewLayout.Format.FORMAT_24H)

        prev_day.setOnClickListener {
            size -= 10
            go()
        }

        next_day.setOnClickListener {
            size += 10
            go()
        }
    }

    fun go() {
        day_view.setRowSize(size)
        date_text.text = size.toString()
    }
}
