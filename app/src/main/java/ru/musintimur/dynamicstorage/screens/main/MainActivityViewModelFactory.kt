package ru.musintimur.dynamicstorage.screens.main

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.musintimur.dynamicstorage.objects.graphic.ScreenHelper

class MainActivityViewModelFactory(private val app: Application, private val screenHelper: ScreenHelper): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        MainActivityViewModel(app, screenHelper) as T
}