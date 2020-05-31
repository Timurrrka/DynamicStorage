package ru.musintimur.dynamicstorage.screens.main

import android.app.Application
import android.util.DisplayMetrics
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivityViewModelFactory(private val app: Application, private val metrics: DisplayMetrics): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        MainActivityViewModel(app, metrics) as T
}