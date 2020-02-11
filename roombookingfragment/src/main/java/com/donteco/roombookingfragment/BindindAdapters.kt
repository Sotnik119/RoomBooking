package com.donteco.roombookingfragment

import android.view.View
import androidx.databinding.BindingAdapter

//@BindingAdapter("color")
//fun colorBinding(view: View, color: Int) {
//    if (color != 0) view.setBackgroundColor(view.context.resources.getColor(color))
//}

@BindingAdapter("color")
fun colorBinding(view: View, color: Int) {
    if (color != 0) view.setBackgroundColor(color)
}