package com.donteco.roombooking

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TableLayout
import kotlinx.android.synthetic.main.day_view_row.view.*


class DayViewLayout @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) :
    TableLayout(context, attrs) {

    init {
        setRowSize(60)
    }

    private var rowSize = 50
    private var format = Format.FORMAT_24H

    fun setRowSize(rowSize: Int) {
        this.rowSize = rowSize
        draw()
    }

    fun setFormat(format: Format) {
        this.format = format
        draw()
    }

    private fun draw() {
        clear()
        for (i in 0..24) {
            val row = LayoutInflater.from(context).inflate(R.layout.day_view_row, null)
            row.minimumHeight = rowSize
            row.time_text.text = getTextForHour(i)
            this.addView(row, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, rowSize))
        }
    }

    private fun getTextForHour(h: Int): String {
        return if (format == Format.FORMAT_24H) {
            String.format("%02d:00", h)
        } else {
            if (h == 0 || h == 24) "12 am" else if (h == 12) "12 pm" else if (h < 12) "$h am" else "${h % 12}pm"
        }
    }

    private fun clear() {
        this.removeAllViews()
    }
}