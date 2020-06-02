package ru.musintimur.dynamicstorage.screens.main

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import ru.musintimur.dynamicstorage.objects.Figure.Good
import ru.musintimur.dynamicstorage.objects.Figure.Worker
import ru.musintimur.dynamicstorage.objects.graphic.ScreenHelper

class MainActivityViewModel(private val app: Application, private val screenHelper: ScreenHelper) : AndroidViewModel(app) {

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
                        newGoods.add(
                            Good(app.applicationContext,
                                screenHelper.calculateViewSize(),
                                screenHelper.getRandomX(),
                                screenHelper.getRandomY())
                        )
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
            newWorkers.add(
                Worker(app.applicationContext,
                    it,
                    screenHelper.calculateViewSize(),
                    screenHelper.getRandomX(),
                    screenHelper.getRandomY())
            )
        }

        _workers.postValue(newWorkers)
    }

    fun refreshScreenHelper(activity: Activity) {
        screenHelper.updateActivity(activity)
    }

}