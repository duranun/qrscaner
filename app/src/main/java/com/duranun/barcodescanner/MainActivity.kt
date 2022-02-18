package com.duranun.barcodescanner

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val button by lazy { findViewById<Button>(R.id.button) }
    private val resultText by lazy { findViewById<TextView>(R.id.textView) }
    private val barcodeScannerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val barcode: String? = it.data?.getStringExtra("code")
                resultText.text = barcode?.let { barcodeText ->
                    "Barcode : $barcodeText"
                } ?: "Cannot read barcode"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener {
                barcodeScannerLauncher.launch(Intent(this,QrScannerActivity::class.java))
        }
    }
}