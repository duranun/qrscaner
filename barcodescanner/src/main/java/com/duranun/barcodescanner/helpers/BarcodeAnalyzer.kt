package com.duranun.barcodescanner.helpers

import android.graphics.Rect
import android.net.Uri
import android.view.ViewGroup
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.core.graphics.toRectF
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.duranun.barcodescanner.ext.convertToImageDimens

class BarcodeAnalyzer(
    private val view: ViewGroup,
    private val clipRect: Rect,
    private val barcodeListener: BarcodeListener?
) :
    ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
    )

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.let { _image ->
            val image = InputImage.fromMediaImage(_image, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    val bitmapRectangle =
                        clipRect.convertToImageDimens(
                            image.width.toFloat(),
                            image.height.toFloat(),
                            view.width.toFloat(),
                            view.height.toFloat(),
                        )
                    for (barcode in barcodes) {
                        barcode?.boundingBox?.toRectF()?.let { rectF ->
                            if (bitmapRectangle.intersect(rectF)) {
                                barcodeListener?.invoke(barcode.rawValue?.let { _barcode ->
                                    when {
                                        _barcode.contains("https") -> {
                                            val url = Uri.parse(_barcode)
                                            url.getQueryParameter("c")
                                        }
                                        else -> _barcode
                                    }
                                } ?: "")
                            }
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}
