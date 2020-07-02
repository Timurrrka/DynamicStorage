package ru.musintimur.dynamicstorage.screens.main

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import ru.musintimur.dynamicstorage.objects.Figure.Good
import ru.musintimur.dynamicstorage.objects.Figure.Worker
import ru.musintimur.dynamicstorage.objects.graphic.ScreenHelper

private const val GOODS_COUNT = 10
private const val REFRESH_PERIOD = 10_000L
private const val OBSERVING_PERIOD = 500L

class MainActivityViewModel(private val app: Application, private val screenHelper: ScreenHelper) : AndroidViewModel(app) {

    private val _goods = MutableLiveData<List<Good>>()
    private val _workers = MutableLiveData<List<Worker>>()
    private var isProducing = false
    private var isObserving = false
    private val _caughtGoods = MutableLiveData<List<Good>>()

    fun getGoods(): LiveData<List<Good>> = _goods
    fun getWorkers(): LiveData<List<Worker>> = _workers
    fun getCaughtGood(): LiveData<List<Good>> = _caughtGoods

    fun startProducingGoods() {
        if (!isProducing) {
            isProducing = true

            viewModelScope.launch {
                while (isProducing) {
                    val newGoods = mutableListOf<Good>()

                    repeat(GOODS_COUNT) {
                        newGoods.add(
                            Good(app.applicationContext,
                                screenHelper.calculateViewSize(),
                                screenHelper.getRandomX(),
                                screenHelper.getRandomY())
                        )
                    }

                    _goods.postValue(newGoods)
                    delay(REFRESH_PERIOD)
                }
            }
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

    fun startObservingIntersects() {
        if (!isObserving) {
            isObserving = true
            viewModelScope.launch {
                while (isObserving) {
                    val caughtGoods = mutableListOf<Good>()
                    _workers.value?.forEach { worker ->
                        _goods.value?.forEach { good ->
                            if (worker > good && screenHelper.checkIntersects(worker, good)) {
                                caughtGoods.add(good)
                            }
                        }
                    }
                    _caughtGoods.postValue(caughtGoods)
                    delay(OBSERVING_PERIOD)
                }
            }
        }
    }
}