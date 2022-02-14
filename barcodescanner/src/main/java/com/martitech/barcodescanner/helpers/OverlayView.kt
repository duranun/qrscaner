package com.martitech.barcodescanner.helpers

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toRect
import com.martitech.barcodescanner.R
import com.martitech.barcodescanner.ext.dpToPx


class OverlayView(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    private val scannerAlphaArray = arrayOf(0, 64, 128, 192, 255, 192, 128, 64, 0)
    private var scannerAlpha = 0
    private val pointSize = 10
    private val animationDelay = 20L

    interface RectangleChanged {
        fun onChange(rectangle: RectF?)
    }

    private var mLaserPaint: Paint? = null

    private var mPaddingPaint: Paint? = null
    private var mBorderPaint: Paint? = null
    private var mPaddingColor: Int = ContextCompat.getColor(context, R.color.default_padding_color)
    private var mDefaultLaserColor: Int =
        ContextCompat.getColor(context, R.color.default_laser_color)
    private var mDefaultBorderColor: Int =
        ContextCompat.getColor(context, R.color.default_border_color)
    private var mFrameWidth: Int = 250.dpToPx().toInt()
    private var mFrameHeight: Int = 220.dpToPx().toInt()
    private var mPaddingWidth: Int = 0
    private var mPaddingHeight: Int = 0

    private var mDefaultBorderStrokeWidth: Int = 4.dpToPx().toInt()
    private var mDefaultBorderLineLength: Int = 60.dpToPx().toInt()
    private val defaultFrameMargin = 32.dpToPx()
    private val path = Path()

    private var mRectangleChanged: RectangleChanged? = null
    private var scanningRectangle: RectF? = null

    init {
        initPaints()
    }

    private fun initPaints() {
        //set up laser paint
        mLaserPaint = Paint()
        mLaserPaint?.color = mDefaultLaserColor
        mLaserPaint?.style = Paint.Style.FILL


        mPaddingPaint = Paint(0)
        mPaddingPaint?.color = mPaddingColor
        mPaddingPaint?.style = Paint.Style.FILL

        mBorderPaint = Paint()
        mBorderPaint?.color = mDefaultBorderColor
        mBorderPaint?.style = Paint.Style.STROKE
        mBorderPaint?.strokeWidth = mDefaultBorderStrokeWidth.toFloat()
        mBorderPaint?.isAntiAlias = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (mFrameWidth > w) mFrameWidth = w - defaultFrameMargin.dpToPx().toInt()
        if (mFrameHeight > h) mFrameHeight = h - defaultFrameMargin.dpToPx().toInt()

        mPaddingWidth = (w - mFrameWidth) / 2
        mPaddingHeight = (h - mFrameHeight) / 2

        scanningRectangle = RectF(
            mPaddingWidth.toFloat(),
            mPaddingHeight.toFloat(),
            (mPaddingWidth + mFrameWidth).toFloat(),
            (mPaddingHeight + mFrameHeight).toFloat()
        )
        mRectangleChanged?.onChange(scanningRectangle!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Top Padding
        val width = width
        val height = height
        val framingRect = scanningRectangle!!

        canvas.drawRect(0f, 0f, width.toFloat(), framingRect.top, mPaddingPaint!!)
        canvas.drawRect(
            0f, framingRect.top, framingRect.left,
            framingRect.bottom, mPaddingPaint!!
        )
        canvas.drawRect(
            framingRect.right, framingRect.top, width.toFloat(),
            framingRect.bottom, mPaddingPaint!!
        )
        canvas.drawRect(
            0f,
            framingRect.bottom, width.toFloat(), height.toFloat(), mPaddingPaint!!
        )

        val borderLength = mDefaultBorderLineLength

        path.moveTo(framingRect.left, framingRect.top + borderLength)
        path.lineTo(framingRect.left, framingRect.top)
        path.lineTo(framingRect.left + borderLength, framingRect.top)
        canvas.drawPath(path, mBorderPaint!!)

        // Top-right corner
        path.moveTo(framingRect.right, framingRect.top + borderLength)
        path.lineTo(framingRect.right, framingRect.top)
        path.lineTo(framingRect.right - borderLength, framingRect.top)
        canvas.drawPath(path, mBorderPaint!!)

        // Bottom-right corner
        path.moveTo(framingRect.right, framingRect.bottom - borderLength)
        path.lineTo(framingRect.right, framingRect.bottom)
        path.lineTo(framingRect.right - borderLength, framingRect.bottom)
        canvas.drawPath(path, mBorderPaint!!)

        // Bottom-left corner
        path.moveTo(framingRect.left, framingRect.bottom - borderLength)
        path.lineTo(framingRect.left, framingRect.bottom)
        path.lineTo(framingRect.left + borderLength, framingRect.bottom)
        canvas.drawPath(path, mBorderPaint!!)


        drawLaser(canvas)

    }

    fun getFramingRect() = scanningRectangle?.toRect()!!

    private fun drawLaser(canvas: Canvas) {
        val mFramingRect: RectF = scanningRectangle!!

        mLaserPaint!!.alpha =
            scannerAlphaArray[scannerAlpha]
        scannerAlpha =
            (scannerAlpha + 1) % scannerAlphaArray.size

        val middle = mFramingRect.height() / 2 + mFramingRect.top
        canvas.drawRect(
            mFramingRect.left + 2,
            middle - 4,
            mFramingRect.right - 1,
            middle + 2,
            mLaserPaint!!
        )

        postInvalidateDelayed(
            animationDelay,
            (mFramingRect.left - pointSize).toInt(),
            (mFramingRect.top - pointSize).toInt(),
            (mFramingRect.right + pointSize).toInt(),
            (mFramingRect.bottom + pointSize).toInt()
        )
    }

    fun setLineBorderWidth(lineBorderWidth: Int) {
        this.mDefaultBorderStrokeWidth = lineBorderWidth
        initPaints()
    }

    fun setLineWidth(lineWidth: Int) {
        this.mDefaultBorderLineLength = lineWidth
        initPaints()
    }

    fun setLineStrokeColor(lineColor: Int) {
        this.mDefaultBorderColor = lineColor
        initPaints()
    }

    fun setFrameHeight(frameHeight: Int) {
        this.mFrameHeight = frameHeight
        initPaints()
    }

    fun setFrameWidth(frameWidth: Int) {
        this.mFrameWidth = frameWidth
        initPaints()
    }

    fun setLaserColor(laserColor: Int) {
        this.mDefaultLaserColor =laserColor
        initPaints()
    }

}