package com.jsync.fileshare

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.radarview.jsync.Line

/**
 * Created by jaseem on 6/11/18.
 */
class RadarScanView : RelativeLayout{
    private var mLeft : Int = 0
    private var mRight : Int = 0
    private var mTop : Int = 0
    private var mBottom : Int = 0
    private var mWidth : Int = 0
    private var mHeight : Int = 0

    private var radius: Float = 0f
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var startX: Float = 0f
    private var startY: Float = 0f
    private var endX: Float = 0f
    private var endY: Float = 0f
    private var dSize: Int = 0
    private lateinit var dlayoutParams: RelativeLayout.LayoutParams
    private lateinit var contextT: Context

    var isFound : Boolean = false
    var radarBackground : Int = 0
    var radarColor : Int = 0
    private lateinit var paintRadarBackground: Paint
    private lateinit var paintRadar: Paint
    private var lines = arrayListOf<Line>()

    private var isLayoutReady: Boolean = false
    private lateinit var foundItem: View

    private lateinit var foundClickListener: View.OnClickListener
    var foundItemText: String = ""

    constructor(context: Context): super(context){
        init(context, null, -1 , -1)
    }
    constructor(context: Context, attrSet: AttributeSet): super(context, attrSet){
        init(context, attrSet, -1, -1)
    }
    constructor(context: Context, attrSet: AttributeSet, defStyleAttr: Int): super(context, attrSet, defStyleAttr){
        init(context, attrSet, defStyleAttr, -1)
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrSet: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attrSet, defStyleAttr, defStyleRes){
        init(context, attrSet, defStyleAttr, defStyleRes)
    }

    private fun init(context: Context, attrSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int){
        setWillNotDraw(false)
        contextT = context
        paintRadar = Paint()
        paintRadar.style = Paint.Style.STROKE
        paintRadar.isAntiAlias = true
        paintRadar.isDither = true
        paintRadar.strokeWidth = 3f
        paintRadar.strokeJoin = Paint.Join.ROUND
        paintRadar.strokeCap = Paint.Cap.ROUND

        paintRadarBackground = Paint()
        paintRadarBackground.style = Paint.Style.FILL
        paintRadarBackground.isAntiAlias = true



        var typedArray: TypedArray = context.obtainStyledAttributes(attrSet, R.styleable.RadarScanView,0, 0)
        try {
            paintRadarBackground.color = typedArray.getColor(R.styleable.RadarScanView_radarBackground, Color.parseColor("#3ce17b"))
            paintRadar.color = typedArray.getColor(R.styleable.RadarScanView_radarColor, Color.WHITE)
        }catch (e: Exception){
            e.printStackTrace()
            typedArray.recycle()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mLeft = left
        mTop = top
        mRight = right
        mBottom = bottom
        mWidth = mRight - mLeft
        mHeight = mBottom - mTop
        isLayoutReady = true

        var angle = 135.0

        if( mWidth > mHeight){
            var temp = mHeight
            mHeight = mWidth
            mWidth = temp
        }

        for (i in 0..9) {
            lines.add(Line((mWidth / 2).toFloat(), (mHeight / 2).toFloat(), (mWidth * 0.9 /2).toFloat(), angle))
            angle -= 1.5
        }
        dSize = (mWidth * 0.3 / 2).toInt()
        dlayoutParams = LayoutParams(dSize, dSize)
        dlayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        radius = (mWidth * 0.9 / 2).toFloat()
        centerX = (mWidth / 2).toFloat()
        centerY = (mHeight / 2).toFloat()

        startY = centerY - radius
        endY = startY + (radius * 2)

        startX = centerX - radius
        endX = startX + ( radius * 2)
    }

    @SuppressLint("ResourceType")
    override fun onDraw(canvas: Canvas?) {
        if (isLayoutReady) {
            canvas?.drawCircle(centerX, centerY, radius, paintRadarBackground)
            for (i in 9 downTo 1 step 3) {
                var temp: Float = i.toFloat() / 10
                canvas?.drawCircle(centerX, centerY, (mWidth * temp) / 2, paintRadar)
            }
            canvas?.drawLine(centerX, startY, centerX, endY, paintRadar)
            canvas?.drawLine(startX, centerY, endX, centerY, paintRadar)

            if (!isFound) {
                for (i in 0..9) {
                    canvas?.drawLine(lines[i].x1, lines[i].y1, lines[i].x2, lines[i].y2, paintRadar)
                    lines[i].update()
                }
            }else{
                foundItem = View(contextT)
                foundItem.id = 111
                addView(foundItem, dlayoutParams)

                foundItem.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ic_computer))
                //foundItem.background = ContextCompat.getDrawable(context, R.drawable.ic_android)
                foundItem.setOnClickListener(foundClickListener)

                val tv1 = TextView(context)
                tv1.id = 1
                tv1.text = foundItemText

                var tP = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                tP.addRule(RelativeLayout.BELOW, foundItem.id)
                tP.addRule(RelativeLayout.CENTER_HORIZONTAL)
                addView(tv1, tP)
            }
        }

        invalidate()
    }

    fun addFoundClickListener(foundClickListener: OnClickListener){
        this.foundClickListener = foundClickListener
    }
}