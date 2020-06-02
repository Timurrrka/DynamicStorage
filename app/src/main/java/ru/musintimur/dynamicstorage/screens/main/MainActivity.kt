package ru.musintimur.dynamicstorage.screens.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.LinearInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import ru.musintimur.dynamicstorage.R
import ru.musintimur.dynamicstorage.objects.Figure
import ru.musintimur.dynamicstorage.objects.Figure.Good
import ru.musintimur.dynamicstorage.objects.Figure.Worker
import ru.musintimur.dynamicstorage.objects.graphic.ScreenHelper
import kotlin.math.pow
import kotlin.math.sqrt

private const val BASE_DURATION = 50L
private const val OBSERVING_PERIOD = 500L

class MainActivity : AppCompatActivity() {

    private val screenHelper: ScreenHelper by lazy { ScreenHelper(this) }
    private val mainActivityViewModel: MainActivityViewModel by lazy {
        ViewModelProvider(
            this,
            MainActivityViewModelFactory(application, screenHelper)
        ).get(MainActivityViewModel::class.java)
    }
    private lateinit var observerJob: Job
    private var goodsViews = mutableListOf<Good>()
    private var workersViews = mutableListOf<Worker>()
    private var isObserving = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupObservers()
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
        mainActivityViewModel.run {
            refreshScreenHelper(this@MainActivity)
            startProducingGoods()
            setupWorkers()
        }
        startObservingIntersects()
    }

    private fun setupObservers() {
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

    fun moveFigure(figure: Figure) {
        val newCoordinates = screenHelper.getNewCoordinates(figure)
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

    private fun startObservingIntersects() {
        if (!isObserving) {
            isObserving = true
            observerJob = CoroutineScope(Dispatchers.Default).launch {
                while (isObserving) {
                    val caughtGoods = mutableSetOf<Good>()
                    workersViews.forEach { worker ->
                        goodsViews.forEach { good ->
                            if (worker > good && screenHelper.checkIntersects(worker, good)) {
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
}
