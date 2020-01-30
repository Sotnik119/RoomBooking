package com.donteco.roombooking

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView


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
            val row = TableRow(this.context).apply {
                this.weightSum = 10f
            }
            row.minimumHeight = rowSize
            this.addView(row, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, rowSize))

            //hour text
            val text = TextView(context).apply {
                this.text = getTextForHour(i)
                this.gravity = Gravity.CENTER
            }
            row.addView(
                text,
                TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f).apply {
                    gravity = Gravity.CENTER
                })

            //line - divider
            val divider = View(context).apply {
                this.setBackgroundColor(resources.getColor(R.color.black_trans))
            }
            row.addView(
                divider,
                TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1, 8f).apply {
                    gravity = Gravity.CENTER
                })
        }
    }

    private fun getTextForHour(h: Int): String {
        return h.toString() //todo!!!
    }

    private fun clear() {
        this.removeAllViews()
    }

    enum class Format {
        FORMAT_24H,
        FORMAT_12H
    }
}