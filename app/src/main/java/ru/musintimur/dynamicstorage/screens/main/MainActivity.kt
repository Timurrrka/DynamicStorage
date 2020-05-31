package ru.musintimur.dynamicstorage.screens.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.animation.LinearInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import ru.musintimur.dynamicstorage.R
import ru.musintimur.dynamicstorage.objects.Figure
import ru.musintimur.dynamicstorage.objects.Figure.Good
import ru.musintimur.dynamicstorage.objects.Figure.Worker
import ru.musintimur.dynamicstorage.objects.characteristics.ScreenSide
import kotlin.math.pow
import kotlin.math.sqrt

private const val BASE_DURATION = 50L
private const val OBSERVING_PERIOD = 500L

class MainActivity : AppCompatActivity() {

    private lateinit var mainActivityViewModel: MainActivityViewModel
    private lateinit var displayMetrics: DisplayMetrics
    private lateinit var observerJob: Job
    private var goodsViews = mutableListOf<Good>()
    private var workersViews = mutableListOf<Worker>()
    private var isObserving = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        refreshMetrics()

        mainActivityViewModel =
            ViewModelProvider(
                this,
                MainActivityViewModelFactory(application, displayMetrics)
            ).get(MainActivityViewModel::class.java)

        val owner = this

        mainActivityViewModel.run {
            getGoods().observe(owner, Observer {
                updateGoods(it)
            })

            getWorkers().observe(owner, Observer {
                updateWorkers(it)
            })
        }
    }

    override fun onPause() {
        super.onPause()
        mainActivityViewModel.stopProduceGoods()
        stopObservingIntersects()
        clearGoods()
        clearWorkers()
    }

    override fun onResume() {
        super.onResume()
        refreshMetrics()
        mainActivityViewModel.startProducingGoods()
        mainActivityViewModel.setupWorkers()
        startObservingIntersects()
    }

    private fun clearGoods() {
        goodsViews.forEach { activityMainLayout.removeView(it) }
        goodsViews.clear()
    }

    private fun addGoods(newGoods: List<Good>) {
        goodsViews.addAll(newGoods)
        goodsViews.forEach {
            activityMainLayout.addView(it)
            moveFigure(it)
        }
    }

    private fun updateGoods(newGoods: List<Good>) {
        clearGoods()
        addGoods(newGoods)
    }

    private fun clearWorkers() {
        workersViews.forEach { activityMainLayout.removeView(it) }
        workersViews.clear()
    }

    private fun addWorkers(newWorkers: List<Worker>) {
        workersViews.addAll(newWorkers)
        workersViews.forEach {
            activityMainLayout.addView(it)
            moveFigure(it)
        }
    }

    private fun updateWorkers(newWorkers: List<Worker>) {
        clearWorkers()
        addWorkers(newWorkers)
    }

    private fun refreshMetrics() {
        val metrics = DisplayMetrics()
        this.windowManager.defaultDisplay.getMetrics(metrics)
        displayMetrics = metrics
        if (::mainActivityViewModel.isInitialized) {
            mainActivityViewModel.updateMetrics(metrics)
        }
    }

    fun moveFigure(figure: Figure) {
        val newCoordinates = getNewCoordinates(figure)
        val distance = sqrt((newCoordinates.first - figure.x).pow(2) + (newCoordinates.second - figure.y).pow(2))
        val duration = BASE_DURATION * (distance / figure.speed).toLong()

        val xAnimator = objectAnimatorOfFloat(figure, "x", duration, figure.x, newCoordinates.first)
        val yAnimator = objectAnimatorOfFloat(figure, "y", duration, figure.y, newCoordinates.second)

        AnimatorSet().apply {
            play(xAnimator).with(yAnimator)

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    moveFigure(figure)
                }
            })

            start()
        }
    }

    private fun objectAnimatorOfFloat(
        figure: Figure,
        propertyName: String,
        animationDuration: Long,
        startValue: Float,
        endValue: Float
    ): ObjectAnimator = ObjectAnimator.ofFloat(figure, propertyName, startValue, endValue).apply {
        interpolator = LinearInterpolator()
        duration = animationDuration
    }

    private fun getNewCoordinates(figure: Figure): Pair<Float, Float> {
        val currentSide = getCurrentSide(figure)
        val newSide = chooseNewScreenSide(currentSide)
        val marginX = getMarginX(currentSide)
        val marginY = getMarginY(currentSide)

        mainActivityViewModel.run {
            return when (newSide) {
                ScreenSide.TOP -> Pair(getRandomX(marginX.first, marginX.second), 0f)
                ScreenSide.BOTTOM -> Pair(getRandomX(marginX.first, marginX.second), getMaxY().toFloat())
                ScreenSide.LEFT -> Pair(0f, getRandomY(marginY.first, marginY.second))
                ScreenSide.RIGHT -> Pair(getMaxX().toFloat(), getRandomY(marginY.first, marginY.second))
                else -> Pair(getRandomX(marginX.first, marginX.second), getRandomY(marginY.first, marginY.second))
            }
        }
    }

    private fun getCurrentSide(figure: Figure): ScreenSide = when {
        figure.x <= mainActivityViewModel.calculateViewSize() -> ScreenSide.LEFT
        figure.x >= mainActivityViewModel.getMaxX() - mainActivityViewModel.calculateViewSize() -> ScreenSide.RIGHT
        figure.y <= mainActivityViewModel.calculateViewSize() -> ScreenSide.TOP
        figure.y >= mainActivityViewModel.getMaxY() - mainActivityViewModel.calculateViewSize() -> ScreenSide.BOTTOM
        else -> ScreenSide.NOT_A_SIDE
    }

    private fun chooseNewScreenSide(currentSide: ScreenSide): ScreenSide =
        enumValues<ScreenSide>().filter { it !in setOf(currentSide, ScreenSide.NOT_A_SIDE) }.random()

    private fun getMarginX(currentSide: ScreenSide): Pair<Int, Int> {
        mainActivityViewModel.run {
            return when (currentSide) {
                ScreenSide.LEFT -> Pair(getMaxX() / 2, getMaxX())
                ScreenSide.RIGHT -> Pair(0, getMaxX() / 2)
                else -> Pair(0, getMaxX())
            }
        }
    }

    private fun getMarginY(currentSide: ScreenSide): Pair<Int, Int> {
        mainActivityViewModel.run {
            return when (currentSide) {
                ScreenSide.TOP -> Pair(getMaxY() / 2, getMaxY())
                ScreenSide.BOTTOM -> Pair(0, getMaxY() / 2)
                else -> Pair(0, getMaxY())
            }
        }
    }

    private fun startObservingIntersects() {
        if (!isObserving) {
            isObserving = true
            observerJob = CoroutineScope(Dispatchers.Default).launch {
                while (isObserving) {
                    val caughtGoods = mutableSetOf<Good>()
                    workersViews.forEach { worker ->
                        goodsViews.forEach { good ->
                            if (worker > good && checkIntersects(worker, good)) {
                                caughtGoods.add(good)
                            }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        caughtGoods.forEach {
                            activityMainLayout.removeView(it)
                            goodsViews.remove(it)
                        }
                    }
                    delay(OBSERVING_PERIOD)
                }
            }
        }
    }

    private fun stopObservingIntersects() {
        isObserving = false
        if (::observerJob.isInitialized) {
            observerJob.cancel()
        }
    }

    private fun checkIntersects(worker: Worker, good: Good): Boolean {
        val workerRect = getFigureRect(worker)
        val goodRect = getFigureRect(good)
        return Rect.intersects(workerRect, goodRect)
    }

    private fun getFigureRect(figure: Figure): Rect =
        Rect(figure.x.toInt(), figure.y.toInt(), figure.x.toInt() + figure.width, figure.y.toInt() + figure.height)

}
