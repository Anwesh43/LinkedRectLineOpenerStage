package com.anwesh.uiprojects.rectlineopenerstage

/**
 * Created by anweshmishra on 20/12/18.
 */
import android.view.View
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.RectF
import android.content.Context
import android.app.Activity

val nodes : Int = 5
val lines : Int = 4
val scDiv : Double = 0.51
val scGap : Float = 0.05f
val color : Int = Color.parseColor("#4527A0")
val strokeWidth : Int = 90
val sizeFactor : Float = 2.7f

fun Int.getInverse() : Float = 1f / this

fun Float.divideScale(i : Int, n : Int) = Math.min(n.getInverse(), Math.max(0f, this - i * n.getInverse())) * n

fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()

fun Float.mirrorValue(a : Int, b : Int) : Float = ((1 - scaleFactor()) * a.getInverse()) + (scaleFactor()  * b.getInverse())

fun Float.updateScale(dir : Float, a : Int, b : Int) : Float = scGap * dir * mirrorValue(a, b)

fun Int.scaleX() : Float =  1f - 2 * (this % 2)

fun Int.scaleY() : Float = 1f - 2 * (this / 2)

val DELAY : Long = 25

fun Canvas.drawProgressLineNode(y : Float, size : Float, scale : Float, paint : Paint) {
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    for (j in 0..1) {
        save()
        translate(0f, y * (1 - 2 * j))
        rotate(90f * sc2)
        for (k in 0..1) {
            val sc : Float = sc1.divideScale(k, 2)
            save()
            drawLine(0f, 0f, 0f, size * sc * (1 - 2 * k), paint)
            restore()
        }
        restore()
    }
}

fun Canvas.drawRLONode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val size : Float = gap / sizeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    paint.strokeWidth = Math.min(w, h) / strokeWidth
    paint.color = color
    paint.strokeCap = Paint.Cap.ROUND
    save()
    translate(gap * (i + 1), h/2)
    drawProgressLineNode(0.3f * h, gap/2, scale, paint)
    drawRect(RectF(-size, -size/2, size, size/2), paint)
    for (j in 0..(lines - 1)) {
        val sc1j : Float = sc1.divideScale(j, lines)
        val sc2j : Float = sc2.divideScale(j, lines)
        save()
        scale(j.scaleX(), j.scaleY())
        translate(size - paint.strokeWidth/2, size/2 - paint.strokeWidth/2)
        rotate(90f * sc2j)
        drawLine(0f, 0f, 0f, size/2 * sc1j, paint)
        restore()
    }
    restore()
}

class RectLinerOpenerStage(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    var onAnimationCompleteListener : OnAnimationCompleteListener? = null

    fun addOnAnimationCompleteListener(onComplete : (Int) -> Unit, onReset : (Int) -> Unit) {
        onAnimationCompleteListener = OnAnimationCompleteListener(onComplete, onReset)
    }

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateScale(dir, lines, lines)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(DELAY)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class RLONode(var i : Int, val state : State = State()) {
        private var next : RLONode? = null
        private var prev : RLONode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = RLONode(i + 1)
                next?.prev = this
            }
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : RLONode {
            var curr : RLONode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawRLONode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }
    }

    data class RectLinerOpener(var i : Int) {

        private val root : RLONode = RLONode(0)
        private var curr : RLONode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : RectLinerOpenerStage) {
        private val rlo : RectLinerOpener = RectLinerOpener(0)
        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            rlo.draw(canvas, paint)
            animator.animate {
                rlo.update{i, scl ->
                    animator.stop()
                    when (scl) {
                        1f -> view.onAnimationCompleteListener?.onComplete?.invoke(i)
                        0f -> view.onAnimationCompleteListener?.onReset?.invoke(i)
                    }
                }
            }
        }

        fun handleTap() {
            rlo.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : RectLinerOpenerStage {
            val view : RectLinerOpenerStage = RectLinerOpenerStage(activity)
            activity.setContentView(view)
            return view
        }
    }

    data class OnAnimationCompleteListener(var onComplete : (Int) -> Unit, var onReset : (Int) -> Unit)
}