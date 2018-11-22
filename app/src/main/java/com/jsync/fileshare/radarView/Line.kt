package com.radarview.jsync

import android.util.Log

/**
 * Created by jaseem on 5/11/18.
 */
class Line(x1: Float, y1 : Float,len: Float, angle: Double){
    var x1: Float = 0f
    var y1: Float = 0f
    var x2: Float = 0f
    var y2: Float = 0f
    var len: Float = 0f
    var angle: Double = 0.0
    var speed: Double = 2.5
    init {
        this.x1 = x1
        this.y1 = y1
        this.angle = angle
        this.len = len
        update()
    }

    fun update(){
        angle = (angle + speed) % 360
        x2 = (Math.cos(Math.toRadians(angle)) * len).toFloat() + x1
        y2 = (Math.sin(Math.toRadians(angle)) * len).toFloat() + y1
    }
}