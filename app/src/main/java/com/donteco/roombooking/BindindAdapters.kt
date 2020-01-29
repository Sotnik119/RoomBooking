package com.donteco.roombooking

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("color")
fun colorBinding(view: View, color: Int) {
    if (color != 0) view.setBackgroundColor(view.context.resources.getColor(color))
}

fun Log(mes: String) {
    android.util.Log.d("MyLog", mes)
}