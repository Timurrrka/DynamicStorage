package ru.musintimur.dynamicstorage.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

fun Context.getMyDrawable(id: Int): Drawable? = ContextCompat.getDrawable(this, id)