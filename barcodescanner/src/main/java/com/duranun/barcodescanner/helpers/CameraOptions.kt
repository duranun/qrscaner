package com.duranun.barcodescanner.helpers

import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize
class CameraOptions(
    @DrawableRes
    var customLightView: Int? = null,

    @DrawableRes
    var customCodeView: Int? = null,

    @ColorRes
    var laserColor: Int? = null,

    @ColorRes
    var codeButtonColor: Int? = null,

    var lineBorderWidth: Int? = null,

    var lineWidth: Int? = null,

    @ColorRes
    var lineStrokeColor: Int? = null,

    var frameHeight: Int? = null,

    var framerWidth: Int? = null,

    var description: String? = null,

    var codeHint: String? = null,

    var title: String? = null,

    val codeVerificationEnabled: Boolean = true,

    @DrawableRes
    var descriptionDrawable: Int? = null
) : Parcelable {

    companion object {
        const val CAMERA_OPTIONS = "cameraOptions"
    }

}

