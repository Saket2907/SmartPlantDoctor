package com.aman.smartplantdoctor

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanActivity : AppCompatActivity() {
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var database: AppDatabase
    private var lastResult: String? = null
    private var classifier: PlantClassifier? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { processGalleryImage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scan)

        database = AppDatabase.getDatabase(this)
        classifier = PlantClassifier(this)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        findViewById<MaterialButton>(R.id.captureButton).setOnClickListener {
            analyzeCurrentFrame()
        }

        findViewById<MaterialButton>(R.id.galleryButton).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        findViewById<MaterialButton>(R.id.addToGardenButton).setOnClickListener {
            lastResult?.let { saveToGarden(it) }
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun processGalleryImage(uri: Uri) {
        try {
            val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val source = android.graphics.ImageDecoder.createSource(contentResolver, uri)
                android.graphics.ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                android.provider.MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
            // Convert to ARGB_8888 — required by TFLite
            val rgbBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)
            analyzeBitmap(rgbBitmap)
        } catch (e: Exception) {
            showError("Failed to load image: ${e.message}")
        }
    }

    private fun analyzeCurrentFrame() {
        val viewFinder = findViewById<PreviewView>(R.id.viewFinder)
        val bitmap = viewFinder.bitmap
        if (bitmap != null) {
            val rgbBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)
            analyzeBitmap(rgbBitmap)
        } else {
            showError("Could not capture frame. Make sure camera is active.")
        }
    }

    private fun analyzeBitmap(bitmap: Bitmap) {
        val instructionText = findViewById<TextView>(R.id.instructionText)
        val resultCard = findViewById<com.google.android.material.card.MaterialCardView>(R.id.resultCard)
        val captureButton = findViewById<MaterialButton>(R.id.captureButton)
        val galleryButton = findViewById<MaterialButton>(R.id.galleryButton)

        // Show loading state
        instructionText.text = "🔍 AI Analyzing..."
        resultCard.visibility = android.view.View.GONE
        captureButton.isEnabled = false
        galleryButton.isEnabled = false

        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.Default) {
                    classifier?.classify(bitmap)
                }

                captureButton.isEnabled = true
                galleryButton.isEnabled = true

                when {
                    result == null -> showError("Model not loaded. Please restart the app.")
                    result.label == "Error" -> showError("Analysis failed. Try a clearer photo of a leaf.")
                    result.confidence < 0.3f -> showLowConfidenceResult(result.label, result.confidence)
                    else -> displayResult(result.label, result.confidence)
                }
            } catch (e: Exception) {
                captureButton.isEnabled = true
                galleryButton.isEnabled = true
                showError("Analysis error: ${e.message}")
            }
        }
    }

    private fun displayResult(label: String, confidence: Float) {
        val resultText = findViewById<TextView>(R.id.resultText)
        val resultCard = findViewById<com.google.android.material.card.MaterialCardView>(R.id.resultCard)
        val addToGardenBtn = findViewById<MaterialButton>(R.id.addToGardenButton)
        val instructionText = findViewById<TextView>(R.id.instructionText)

        lastResult = label
        instructionText.text = "✅ Analysis Complete"

        val confidencePct = (confidence * 100).toInt()
        val info = HealthAdvisor.getDetailedAdvice(label)

        resultText.text = buildString {
            appendLine("🌿 ${info.name}")
            appendLine("Confidence: $confidencePct%")
            appendLine()
            appendLine("📋 Symptoms:")
            appendLine(info.symptoms)
            appendLine()
            appendLine("💊 Treatment:")
            append(info.treatment)
        }

        resultCard.alpha = 0f
        resultCard.visibility = android.view.View.VISIBLE
        resultCard.animate().alpha(1f).setDuration(300).start()
        addToGardenBtn.visibility = android.view.View.VISIBLE

        saveToHistory(label)
    }

    private fun showLowConfidenceResult(label: String, confidence: Float) {
        val resultText = findViewById<TextView>(R.id.resultText)
        val resultCard = findViewById<com.google.android.material.card.MaterialCardView>(R.id.resultCard)
        val addToGardenBtn = findViewById<MaterialButton>(R.id.addToGardenButton)
        val instructionText = findViewById<TextView>(R.id.instructionText)

        lastResult = label
        instructionText.text = "⚠️ Low Confidence Result"

        val confidencePct = (confidence * 100).toInt()
        resultText.text = buildString {
            appendLine("Best guess: $label ($confidencePct%)")
            appendLine()
            append("Confidence is low. For a better result:\n• Make sure the leaf fills the frame\n• Use good lighting\n• Avoid blurry photos")
        }

        resultCard.alpha = 0f
        resultCard.visibility = android.view.View.VISIBLE
        resultCard.animate().alpha(1f).setDuration(300).start()
        addToGardenBtn.visibility = android.view.View.GONE
    }

    private fun showError(message: String) {
        val instructionText = findViewById<TextView>(R.id.instructionText)
        instructionText.text = "Point camera at a leaf"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun saveToGarden(result: String) {
        lifecycleScope.launch {
            val isHealthy = result.lowercase().contains("healthy")
            database.plantDao().insertPlant(
                Plant(
                    name = "Scanned Plant",
                    species = result,
                    healthScore = if (isHealthy) 100 else 55
                )
            )
            Toast.makeText(this@ScanActivity, "✅ Added to My Garden!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToHistory(result: String) {
        lifecycleScope.launch {
            database.scanDao().insertScan(ScanHistory(plantName = "Leaf Scan", result = result))
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(findViewById<PreviewView>(R.id.viewFinder).surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            } catch (e: Exception) {
                Toast.makeText(this, "Camera failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission is required to scan plants.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        classifier?.close()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
