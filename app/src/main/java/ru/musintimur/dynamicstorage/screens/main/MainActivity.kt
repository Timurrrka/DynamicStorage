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
import ru.musintimur.dynamicstorage.R
import ru.musintimur.dynamicstorage.objects.Figure
import ru.musintimur.dynamicstorage.objects.Figure.Good
import ru.musintimur.dynamicstorage.objects.Figure.Worker
import ru.musintimur.dynamicstorage.objects.graphic.ScreenHelper
import kotlin.math.pow
import kotlin.math.sqrt

private const val BASE_DURATION = 50L

class MainActivity : AppCompatActivity() {

    private val screenHelper: ScreenHelper by lazy { ScreenHelper(this) }
    private val mainActivityViewModel: MainActivityViewModel by lazy {
        ViewModelProvider(
            this,
            MainActivityViewModelFactory(application, screenHelper)
        ).get(MainActivityViewModel::class.java)
    }
    private var goodsViews = mutableListOf<Good>()
    private var workersViews = mutableListOf<Worker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupObservers()
    }

    override fun onPause() {
        super.onPause()
        clearGoods()
        clearWorkers()
    }

    override fun onResume() {
        super.onResume()
        mainActivityViewModel.run {
            refreshScreenHelper(this@MainActivity)
            setupWorkers()
            startProducingGoods()
            startObservingIntersects()
        }
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

            getCaughtGood().observe(owner, Observer {
                removeGoods(it)
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

    private fun removeGoods(goods: List<Good>) {
        goods.forEach {
            activityMainLayout.removeView(it)
            goodsViews.remove(it)
        }
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
}
