package com.martitech.barcodescanner

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.TorchState
import androidx.core.content.ContextCompat
import com.martitech.barcodescanner.databinding.ActivityQrScannerBinding
import com.martitech.barcodescanner.ext.showAlert
import com.martitech.barcodescanner.ext.visibleIf
import com.martitech.barcodescanner.helpers.CameraOptions


class QrScannerActivity : AppCompatActivity() {
    private var isCodeState = false
    private var isLightOn: Boolean = false
    private val binding: ActivityQrScannerBinding by lazy {
        ActivityQrScannerBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (hasCameraPermission()) {
            initCamera()
        } else {
            getCameraPermission()
        }
        initListeners()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            initCamera()
        } else {
            showAlert(
                this,
                getString(R.string.permission_denied_title),
                getString(R.string.camera_permission_denied)
            ) {
                finish()
            }
        }
    }

    private fun initListeners() {
        binding.toggleQrCode.setOnClickListener {
            binding.camView.setOverlayViewVisibility(isCodeState)
            binding.codeLayout visibleIf !isCodeState
            isCodeState = !isCodeState
        }

        binding.btnQrScannerOk.setOnClickListener {
            binding.code.text?.let { editable ->
                if (editable.length == 4 || editable.length == 6) {
                    setCode(editable.toString())
                } else {
                    binding.code.error = getString(R.string.code_length_error)
                }
            }
        }
        binding.close.setOnClickListener { finish() }
    }

    private fun getCameraPermission() {
        requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun initCamera() {
        intent?.extras?.getParcelable<CameraOptions>(CameraOptions.CAMERA_OPTIONS)?.let {
            setCameraOptions(it)
        }
        binding.camView.setLifecycleOwner(this)
        binding.camView.addListener { code ->
            if (code.isNotBlank()) {
                setCode(code)
            }
        }

        binding.light.setOnClickListener {
            isLightOn = binding.camView.camera?.cameraInfo?.torchState?.value == TorchState.ON
            binding.camView.camera?.cameraControl?.enableTorch(isLightOn.not())
        }
    }

    private fun setCode(code: String) {
        Intent().apply {
            putExtra("code", code)
        }.also {
            setResult(Activity.RESULT_OK, it)
        }
        finish()
    }

    override fun onDestroy() {
        binding.camView.destroyView()
        super.onDestroy()
    }

    private fun setCameraOptions(cameraOptions: CameraOptions) {
        val camera = binding.camView
        cameraOptions.laserColor?.let {
            camera.setLaserColor(it)
        }
        cameraOptions.frameHeight?.let {
            camera.setFrameHeight(it)
        }
        cameraOptions.framerWidth?.let {
            camera.setFrameWidth(it)
        }
        cameraOptions.lineBorderWidth?.let {
            camera.setLineBorderWidth(it)
        }
        cameraOptions.lineStrokeColor?.let {
            camera.setLineStrokeColor(it)
        }
        cameraOptions.lineWidth?.let {
            camera.setLineWidth(it)
        }
        cameraOptions.customLightView?.let {
            binding.light.setImageResource(it)
        }
        cameraOptions.codeHint?.let {
            binding.code.hint = it
        }
        cameraOptions.customCodeView?.let {
            binding.toggleQrCode.setImageResource(it)
        }
        cameraOptions.title?.let {
            binding.title.text = it
        }
        binding.descriptionText visibleIf (cameraOptions.description != null)
        cameraOptions.description?.let {
            binding.descriptionText.text = it
        }
        cameraOptions.descriptionDrawable?.let {
            binding.descriptionText.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(applicationContext, it),
                null,
                null,
                null
            )
        }
    }


}