package com.aman.smartplantdoctor

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.label.TensorLabel
import java.nio.MappedByteBuffer

/**
 * Professional AI Classifier for Plant Diseases.
 * This class handles the loading of a TFLite model and image preprocessing.
 */
class PlantClassifier(private val context: Context) {
    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()
    
    private val modelPath = "plant_disease_model.tflite"
    private val labelPath = "labels.txt"

    init {
        try {
            val model = loadModelFile()
            val options = Interpreter.Options().apply {
                setNumThreads(4)
                setUseNNAPI(true)
            }
            interpreter = Interpreter(model, options)
            labels = FileUtil.loadLabels(context, labelPath)
            android.util.Log.d("PlantClassifier", "Model and labels loaded successfully")
        } catch (e: Exception) {
            android.util.Log.e("PlantClassifier", "Error loading model or labels: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        return FileUtil.loadMappedFile(context, modelPath)
    }

    fun classify(bitmap: Bitmap): RecognitionResult {
        if (interpreter == null) return RecognitionResult("Error", 0f, "Model not loaded")

        // 1. Preprocess the image
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f)) // Scale pixels to [0, 1]
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // 2. Run Inference
        val probabilityBuffer = org.tensorflow.lite.support.tensorbuffer.TensorBuffer.createFixedSize(
            intArrayOf(1, labels.size), DataType.FLOAT32
        )
        interpreter?.run(tensorImage.buffer, probabilityBuffer.buffer)

        // 3. Post-process results
        val labelsMap = TensorLabel(labels, probabilityBuffer).mapWithFloatValue
        val topResult = labelsMap.entries.maxByOrNull { it.value }

        return RecognitionResult(
            label = topResult?.key ?: "Unknown",
            confidence = topResult?.value ?: 0f,
            advice = HealthAdvisor.getAdvice(topResult?.key ?: "Unknown")
        )
    }

    data class RecognitionResult(
        val label: String,
        val confidence: Float,
        val advice: String
    )
    
    fun close() {
        interpreter?.close()
    }
}
