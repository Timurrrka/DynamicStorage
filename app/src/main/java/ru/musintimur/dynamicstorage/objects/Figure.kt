package ru.musintimur.dynamicstorage.objects

import android.annotation.SuppressLint
import android.content.Context
import android.widget.FrameLayout
import android.widget.ImageView
import ru.musintimur.dynamicstorage.R
import ru.musintimur.dynamicstorage.objects.characteristics.GoodWeight
import ru.musintimur.dynamicstorage.objects.characteristics.WorkerPower
import ru.musintimur.dynamicstorage.utils.getMyDrawable

sealed class Figure(context: Context) : ImageView(context) {

    abstract val speed: Int

    @SuppressLint("ViewConstructor")
    class Good private constructor(context: Context, val weight: GoodWeight) : Figure(context) {

        companion object {

            private enum class GoodSkin(val drawableId: Int, val weight: GoodWeight) {
                APPLE(R.drawable.apple, GoodWeight.LIGHT),
                BANANA(R.drawable.banana, GoodWeight.LIGHT),
                BREAD(R.drawable.bread, GoodWeight.LIGHT),
                PINEAPPLE(R.drawable.pineapple, GoodWeight.LIGHT),
                BOX(R.drawable.box, GoodWeight.STANDARD),
                PC(R.drawable.pc, GoodWeight.STANDARD),
                PRINTER(R.drawable.printer, GoodWeight.STANDARD),
                FRIDGE(R.drawable.fridge, GoodWeight.HEAVY),
                PIANO(R.drawable.piano, GoodWeight.HEAVY),
                TV(R.drawable.tv, GoodWeight.HEAVY)
            }

            operator fun invoke(context: Context, figureSize: Int, startX: Float, startY: Float): Good {
                val skin = getRandomSkin()
                val good = Good(context, skin.weight)
                good.apply {
                    layoutParams = FrameLayout.LayoutParams(figureSize, figureSize)
                    setImageDrawable(context.getMyDrawable(skin.drawableId))
                    x = startX
                    y = startY
                }
                return good
            }

            private fun getRandomSkin(): GoodSkin =
                enumValues<GoodSkin>().random()
        }

        override val speed = when (weight) {
            GoodWeight.LIGHT -> 6
            GoodWeight.STANDARD -> 3
            GoodWeight.HEAVY -> 2
        }

    }

    @SuppressLint("ViewConstructor")
    class Worker private constructor(context: Context, private val power: WorkerPower) : Figure(context),
        Comparable<Figure> {

        companion object {

            enum class WorkerSkin(val drawableId: Int, val power: WorkerPower) {
                GALYA(R.drawable.galya, WorkerPower.WEAK),
                ZINA(R.drawable.zina, WorkerPower.WEAK),
                MAHMUD(R.drawable.mahmud, WorkerPower.STRONG),
                SERYOGA(R.drawable.seryoga, WorkerPower.STRONG)
            }

            operator fun invoke(
                context: Context,
                workerSkin: WorkerSkin,
                figureSize: Int,
                startX: Float,
                startY: Float
            ): Worker {
                val worker = Worker(context, workerSkin.power)
                worker.apply {
                    layoutParams = FrameLayout.LayoutParams(figureSize, figureSize)
                    setImageDrawable(context.getMyDrawable(workerSkin.drawableId))
                    x = startX
                    y = startY
                }
                return worker
            }
        }

        override fun compareTo(other: Figure): Int {
            return when (other) {
                is Worker -> 0
                is Good -> when {
                    this.power == WorkerPower.WEAK && other.weight in setOf(GoodWeight.STANDARD, GoodWeight.HEAVY) -> -1
                    else -> 1
                }
            }
        }

        override val speed = 3
    }
}