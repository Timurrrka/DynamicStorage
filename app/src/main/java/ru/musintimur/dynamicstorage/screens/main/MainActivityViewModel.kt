package ru.musintimur.dynamicstorage.screens.main

import android.app.Activity
import android.app.Application
import android.graphics.Rect
import android.util.DisplayMetrics
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import ru.musintimur.dynamicstorage.objects.Figure.Good
import ru.musintimur.dynamicstorage.objects.Figure.Worker

private const val DEFAULT_SCREEN_SIZE = 100

class MainActivityViewModel(private val app: Application, private var metrics: DisplayMetrics) : AndroidViewModel(app) {

    private val _goods = MutableLiveData<List<Good>>()
    private val _workers = MutableLiveData<List<Worker>>()
    private lateinit var producerJob: Job
    private var isProducing = false

    fun getGoods(): LiveData<List<Good>> = _goods
    fun getWorkers(): LiveData<List<Worker>> = _workers

    fun startProducingGoods() {
        if (!isProducing) {
            isProducing = true

            producerJob = CoroutineScope(Dispatchers.Default).launch {
                while (isProducing) {
                    val newGoods = mutableListOf<Good>()

                    repeat(10) {
                        newGoods.add(Good(app.applicationContext, calculateViewSize(), getRandomX(), getRandomY()))
                    }

                    _goods.postValue(newGoods)
                    delay(10_000)
                }
            }
        }
    }

    fun stopProduceGoods() {
        isProducing = false
        if (::producerJob.isInitialized) {
            producerJob.cancel()
        }
    }

    fun setupWorkers() {
        val newWorkers = mutableListOf<Worker>()

        enumValues<Worker.Companion.WorkerSkin>().forEach {
            newWorkers.add(Worker(app.applicationContext, it, calculateViewSize(), getRandomX(), getRandomY()))
        }

        _workers.postValue(newWorkers)
    }

    fun updateMetrics(newMetrics: DisplayMetrics) {
        metrics = newMetrics
    }

    fun getRandomX(min: Int = 0, max: Int = getMaxX()): Float = (min..max).random().toFloat()

    fun getRandomY(min: Int = 0, max: Int = getMaxY()): Float = (min..max).random().toFloat()

    fun getMaxX(): Int = metrics.widthPixels - calculateViewSize()

    fun getMaxY(): Int = metrics.heightPixels - getStatusBarHeight() - calculateViewSize()

    fun calculateViewSize(): Int =
        (setOf(metrics.widthPixels, metrics.heightPixels).min() ?: DEFAULT_SCREEN_SIZE) / 10

    private fun getStatusBarHeight(): Int {
        val resourceId = app.applicationContext.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) app.applicationContext.resources.getDimensionPixelSize(resourceId)
        else Rect().apply { (app.applicationContext as Activity).window.decorView.getWindowVisibleDisplayFrame(this) }.top
    }

}