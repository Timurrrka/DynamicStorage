package ru.musintimur.dynamicstorage.objects.graphic

import android.app.Activity
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.View
import ru.musintimur.dynamicstorage.objects.characteristics.ScreenSide

private const val DEFAULT_SCREEN_SIZE = 100

class ScreenHelper(private var activity: Activity) {

    private val displayMetrics: DisplayMetrics = DisplayMetrics()

    init {
        refreshMetrics()
    }

    fun updateActivity(newActivity: Activity) {
        activity = newActivity
        refreshMetrics()
    }

    private fun refreshMetrics() {
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
    }

    fun getRandomX(min: Int = 0, max: Int = getMaxX()): Float = (min..max).random().toFloat()

    fun getRandomY(min: Int = 0, max: Int = getMaxY()): Float = (min..max).random().toFloat()

    private fun getMaxX(): Int = displayMetrics.widthPixels - calculateViewSize()

    private fun getMaxY(): Int = displayMetrics.heightPixels - getStatusBarHeight() - calculateViewSize()

    fun calculateViewSize(): Int =
        (setOf(displayMetrics.widthPixels, displayMetrics.heightPixels).min() ?: DEFAULT_SCREEN_SIZE) / 10

    private fun getStatusBarHeight(): Int {
        val resourceId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0)
            activity.resources.getDimensionPixelSize(resourceId)
        else
            Rect().apply { activity.window.decorView.getWindowVisibleDisplayFrame(this) }.top
    }

    fun getNewCoordinates(view: View): Pair<Float, Float> {
        val currentSide = getCurrentSide(view)
        val newSide = chooseNewScreenSide(currentSide)
        val marginX = getMarginX(currentSide)
        val marginY = getMarginY(currentSide)

        return when (newSide) {
            ScreenSide.TOP -> Pair(getRandomX(marginX.first, marginX.second), 0f)
            ScreenSide.BOTTOM -> Pair(getRandomX(marginX.first, marginX.second), getMaxY().toFloat())
            ScreenSide.LEFT -> Pair(0f, getRandomY(marginY.first, marginY.second))
            ScreenSide.RIGHT -> Pair(getMaxX().toFloat(), getRandomY(marginY.first, marginY.second))
            else -> Pair(getRandomX(marginX.first, marginX.second), getRandomY(marginY.first, marginY.second))
        }

    }

    private fun getCurrentSide(view: View): ScreenSide = when {
        view.x <= calculateViewSize() -> ScreenSide.LEFT
        view.x >= getMaxX() - calculateViewSize() -> ScreenSide.RIGHT
        view.y <= calculateViewSize() -> ScreenSide.TOP
        view.y >= getMaxY() - calculateViewSize() -> ScreenSide.BOTTOM
        else -> ScreenSide.NOT_A_SIDE
    }

    private fun chooseNewScreenSide(currentSide: ScreenSide): ScreenSide =
        enumValues<ScreenSide>().filter { it !in setOf(currentSide, ScreenSide.NOT_A_SIDE) }.random()

    private fun getMarginX(currentSide: ScreenSide): Pair<Int, Int> = when (currentSide) {
        ScreenSide.LEFT -> Pair(getMaxX() / 2, getMaxX())
        ScreenSide.RIGHT -> Pair(0, getMaxX() / 2)
        else -> Pair(0, getMaxX())
    }

    private fun getMarginY(currentSide: ScreenSide): Pair<Int, Int> = when (currentSide) {
        ScreenSide.TOP -> Pair(getMaxY() / 2, getMaxY())
        ScreenSide.BOTTOM -> Pair(0, getMaxY() / 2)
        else -> Pair(0, getMaxY())
    }

    fun checkIntersects(view1: View, view2: View): Boolean {
        val viewRect1 = getViewRect(view1)
        val viewRect2 = getViewRect(view2)
        return Rect.intersects(viewRect1, viewRect2)
    }

    private fun getViewRect(view: View): Rect =
        Rect(view.x.toInt(), view.y.toInt(), view.x.toInt() + view.width, view.y.toInt() + view.height)

}