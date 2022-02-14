package com.martitech.barcodescanner.helpers

import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize
class CameraOptions(
    @DrawableRes
    var customLightView: Int? = null,

    @DrawableRes
    var customCodeView: Int? = null,

    @ColorInt
    var laserColor: Int? = null,

    @ColorInt
    var codeButtonColor: Int? = null,

    var lineBorderWidth: Int? = null,

    var lineWidth: Int? = null,

    @ColorInt
    var lineStrokeColor: Int? = null,

    var frameHeight: Int? = null,

    var framerWidth: Int? = null,

    var description: String? = null,

    var codeHint: String? = null,

    var title: String? = null,

    @DrawableRes
    var descriptionDrawable: Int? = null
) : Parcelable {
    companion object {
        const val CAMERA_OPTIONS = "cameraOptions"
    }

}

