package com.duranun.barcodescanner.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.camera.core.*
import androidx.camera.core.CameraUnavailableException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import com.duranun.barcodescanner.R
import com.duranun.barcodescanner.ext.dpToPx
import com.duranun.barcodescanner.ext.showAlert
import com.duranun.barcodescanner.ext.visibleIf
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


typealias BarcodeListener = (barcode: String) -> Unit

class CameraView(context: Context, attributeSet: AttributeSet) :
    FrameLayout(context, attributeSet) {
    var camera: Camera? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var preview: Preview? = null
    private var lensFacing: CameraSelector? = null
    private var displayId: Int = 0
    private val overlayView: OverlayView by lazy { OverlayView(context) }
    private val previewView: PreviewView by lazy { PreviewView(context) }
    private var cameraProvider: ProcessCameraProvider? = null
    private var cornerRadius: Int = 0
    private var frameWidth: Int = 250.dpToPx().toInt()
    private var frameHeight: Int = 250.dpToPx().toInt()
    private var lineBorderWidth: Int = 8
    private var lineWidth: Int = 0
    private var lineColor: Int = 0
    private var defaultMargin = 32
    private var barcodeListener: BarcodeListener? = null

    private var cameraExecutor: ExecutorService? = null
    private val barcodeAnalyser by lazy {
        BarcodeAnalyzer(this, overlayView.getFramingRect()) { barcode ->
            barcodeListener?.invoke(barcode)
            cameraProvider?.unbindAll()
        }
    }

    init {
        val a = context.theme.obtainStyledAttributes(attributeSet, R.styleable.CameraView, 0, 0)
        try {
            lineBorderWidth = a.getDimensionPixelSize(R.styleable.CameraView_qrLineBorderWidth, 8)
            lineWidth = a.getDimensionPixelSize(R.styleable.CameraView_qrLineWidth, 60)
            lineColor = a.getColor(R.styleable.CameraView_qrLineColor, Color.rgb(0, 136, 253))
            frameHeight = try {
                a.getDimensionPixelSize(R.styleable.CameraView_qrFrameHeight, 200)

            } catch (ex: Exception) {
                a.getInt(R.styleable.CameraView_qrFrameHeight, ViewGroup.LayoutParams.MATCH_PARENT)
            }
            frameWidth = try {
                a.getDimensionPixelSize(R.styleable.CameraView_qrFrameWidth, 200)
            } catch (ex: Exception) {
                a.getInt(R.styleable.CameraView_qrFrameWidth, ViewGroup.LayoutParams.MATCH_PARENT)
            }
            cornerRadius = a.getDimensionPixelSize(R.styleable.CameraView_qrCornerRadius, 0)
        } finally {
            a.recycle()
        }
        addView(previewView)
        initCamera()
        initAttributes()
    }

    private fun initCamera() {
        try {
            cameraExecutor = Executors.newSingleThreadExecutor()
            previewView.post {
                previewView.display?.displayId?.let {
                    displayId = previewView.display.displayId
                    setUpCamera()
                }
            }
        } catch (ex: SecurityException) {

        }
    }

    @SuppressLint("RestrictedApi")
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                lensFacing =
                    CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()
                bindCameraUseCase()
                invalidate()
            } catch (exception: ExecutionException) {
                val cause = exception.cause
                if (cause is InitializationException) {
                    if (cause.cause is CameraUnavailableException) {
                        val cue = cause.cause as CameraUnavailableException
                        if (cue.reason == CameraUnavailableException.CAMERA_UNAVAILABLE_DO_NOT_DISTURB) {
                            showAlert(
                                this.context,
                                msg = context.getString(R.string.do_not_disturb_mode_enabled)
                            )
                        }
                    }
                }
            }
        }, ContextCompat.getMainExecutor(context))

    }


    fun addListener(listener: BarcodeListener) {
        this.barcodeListener = listener
    }

    private fun bindCameraUseCase() {
        try {
            val metrics = DisplayMetrics().also { previewView.display.getRealMetrics(it) }
            val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
            val rotation = previewView.display.rotation
            val cameraProvider = cameraProvider
                ?: throw IllegalStateException("Camera initialization failed.")

            preview = Preview.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()

            imageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()
                .also {
                    it.setAnalyzer(
                        cameraExecutor!!,
                        barcodeAnalyser
                    )
                }

            cameraProvider.unbindAll()
            lensFacing?.let { lensFacing ->
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner!!,
                    lensFacing,
                    preview, imageAnalyzer
                )
                preview?.setSurfaceProvider(previewView.surfaceProvider)
            }
        } catch (exc: Exception) {
            Log.e(this.javaClass.simpleName, "Use case binding failed", exc)
        }
    }


    private fun initAttributes() {
        if (lineBorderWidth > 0)
            overlayView.setLineBorderWidth(lineBorderWidth)
        if (lineWidth > 0)
            overlayView.setLineWidth(lineWidth)

        overlayView.setLineStrokeColor(lineColor)
        overlayView.setFrameHeight(
            if (frameHeight == -1) (resources.displayMetrics.heightPixels - defaultMargin.dpToPx()
                .toInt()) else frameHeight
        )
        overlayView.setFrameWidth(
            if (frameWidth == -1) (resources.displayMetrics.widthPixels - defaultMargin.dpToPx()
                .toInt()) else frameWidth
        )
        addView(overlayView)
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    @Deprecated("deprecated")
    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    @Deprecated("deprecated")
    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }


    fun destroyView() {
        cameraExecutor?.isShutdown
    }

    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        if (camera == null) initCamera()
        this.lifecycleOwner = lifecycleOwner
    }

    fun setLaserColor(@ColorRes laserColor: Int): CameraView {
        overlayView.setLaserColor(
            ResourcesCompat.getColor(
                context.applicationContext.resources,
                laserColor,
                null
            )
        )
        return this
    }

    fun setLineBorderWidth(lineBorderWidth: Int): CameraView {
        overlayView.setLineBorderWidth(lineBorderWidth)
        return this
    }

    fun setLineWidth(lineWidth: Int): CameraView {
        overlayView.setLineWidth(lineWidth)
        return this
    }

    fun setLineStrokeColor(@ColorRes lineColor: Int): CameraView {
        overlayView.setLineStrokeColor(
            ResourcesCompat.getColor(
                context.applicationContext.resources,
                lineColor,
                null
            )
        )
        return this
    }

    fun setFrameHeight(frameHeight: Int): CameraView {
        overlayView.setFrameHeight(frameHeight)
        return this
    }

    fun setFrameWidth(frameWidth: Int): CameraView {
        overlayView.setFrameWidth(frameWidth)
        return this
    }

    fun setOverlayViewVisibility(isVisible: Boolean) {
        cameraExecutor?.let { executor ->
            imageAnalyzer?.setAnalyzer(
                executor,
                if (isVisible) barcodeAnalyser else BarcodeAnalyzer(this, Rect(), barcodeListener)
            )
        }
        overlayView visibleIf isVisible
    }


    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

}