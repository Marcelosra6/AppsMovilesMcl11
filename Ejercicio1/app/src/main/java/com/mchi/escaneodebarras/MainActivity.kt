package com.mchi.escaneodebarras

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.mchi.escaneodebarras.databinding.ActivityMainBinding
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraController: LifecycleCameraController

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) iniciarCamara()
        else Toast.makeText(this, "Permiso de cámara denegado...", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnScan.setOnClickListener { pedirPermiso() }
    }

    private fun pedirPermiso() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            iniciarCamara()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun iniciarCamara() {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_EAN_13)
            .build()

        val barcodeScanner = BarcodeScanning.getClient(options)

        cameraController = LifecycleCameraController(this)
        cameraController.bindToLifecycle(this)
        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(this),
            MlKitAnalyzer(
                listOf(barcodeScanner),
                COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(this)
            ) { result ->
                val barcodes = result?.getValue(barcodeScanner)
                //la accion para abrir y usar la camara con los codigos de barras
                barcodes?.firstOrNull()?.rawValue?.let { value ->
                    runOnUiThread {
                        AlertDialog.Builder(this)
                            .setTitle("QR detectadowo")
                            .setMessage(value)
                            .setPositiveButton("Abrir") { _, _ ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(value))
                                startActivity(intent)
                            }
                            .setNegativeButton("Cancelar", null)
                            .show()
                    }
                }
            }
        )

        binding.previewView.controller = cameraController
    }
}