package com.martitech.barcodescanner.ext

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import com.martitech.barcodescanner.R

fun Int.dpToPx(): Float {
    return this * Resources.getSystem().displayMetrics.density
}

fun Float.dpToPx(): Float {
    return this * Resources.getSystem().displayMetrics.density
}

fun showAlert(context: Context, title: String? = null, msg: String, block: (() -> Unit)? = null) {
    val act = context as Activity
    if (!act.isFinishing && !act.isDestroyed) {
        val dialog = AlertDialog.Builder(context)
        title?.let {
            dialog.setTitle(title)
        }
        dialog.setMessage(msg)
        dialog.setCancelable(false)
        dialog.setPositiveButton(context.getString(R.string.btn_ok)) { d, _ ->
            block?.let {
                block()
            } ?: kotlin.run {
                d.cancel()
            }
        }
        when {
            msg.isNotEmpty() -> {
                dialog.create()
                dialog.show()
            }
        }
    }

}

infix fun View.visibleIf(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.inVisible() {
    visibility = View.INVISIBLE
}


fun Rect.convertToImageDimens(
    imageWidth: Float,
    imageHeight: Float,
    viewWidth: Float,
    viewHeight: Float
): RectF {
    val rectF = RectF()
    rectF.bottom = imageHeight * this.bottom / viewWidth
    rectF.top = imageHeight * this.top / viewWidth
    rectF.left = imageWidth * this.left / viewHeight
    rectF.right = imageWidth * this.right / viewHeight
    return rectF
}
