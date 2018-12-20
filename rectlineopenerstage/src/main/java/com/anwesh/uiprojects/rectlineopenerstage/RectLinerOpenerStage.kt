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

fun Float.divideScale(i : Int, n : Int) = Math.min(n.getInverse(), this - i * n.getInverse()) * n

fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()

fun Float.mirrorValue(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.getInverse() + scaleFactor()  * b.getInverse()

fun Float.updateScale(dir : Float, a : Int, b : Int) : Float = scGap * dir * mirrorValue(lines, 1)