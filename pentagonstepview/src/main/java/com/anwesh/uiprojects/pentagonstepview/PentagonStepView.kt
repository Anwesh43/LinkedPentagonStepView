package com.anwesh.uiprojects.pentagonstepview

/**
 * Created by anweshmishra on 01/10/18.
 */

import android.view.MotionEvent
import android.view.View
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color

val nodes : Int = 5
val size : Int = 5

fun Canvas.drawPSNode(i : Int, scale : Float, paint : Paint) {
    paint.strokeCap = Paint.Cap.ROUND
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    paint.strokeWidth = Math.min(w, h) / 60
    paint.color = Color.parseColor("#01579B")
    val r : Float = gap / 3
    val deg : Float = 360f / size
    val fact : Float = 1f / size
    save()
    translate(w/2, i * gap + gap)
    for (j in 0..size - 1) {
        val sc : Float = Math.min(fact, Math.max(0f, scale - j * fact)) * size
        val x : Float = r * Math.sin((deg/2) * Math.PI/180).toFloat()
        val y : Float = r * Math.cos((deg/2) * Math.PI/180).toFloat()
        save()
        rotate(deg * j)
        drawLine(x, y, x - 2 * x * sc, y, paint)
        restore()
    }
    restore()
}

class PentagonStepView (ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

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

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += (0.1f/size) * dir
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

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }
    }

    data class PSNode(var i : Int, val state : State = State()) {
        private var next : PSNode? = null
        private var prev : PSNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = PSNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawPSNode(i, state.scale, paint)
            prev?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : PSNode {
            var curr : PSNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class PentagonStep(var i : Int) {
        private var curr : PSNode = PSNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i , scl ->
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

    data class Renderer(var view : PentagonStepView, var animated : Boolean = false) {

        private val ps : PentagonStep = PentagonStep(0)
        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            animator.animate {
                ps.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            ps.startUpdating {
                animator.start()
            }
        }
    }
 }