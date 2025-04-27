package com.example.frutti

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer



class FruitQualityModelBinding(private val context: Context) {

    // Use the Interpreter type consistent with your dependencies/what works.
    private var interpreter: Interpreter? = null // Will be either LiteRT or TFLite Interpreter
    private val labels = mutableListOf<String>()
    private val TAG = "FruitQualityModel_Revised"

    // Model details (Adjust if different)
    private val modelName = "fruit_quality_model.tflite"
    private val labelsName = "labels.txt"
    private var inputWidth: Int = 0
    private var inputHeight: Int = 0
    private var modelInputDataType: DataType = DataType.FLOAT32 // Default, adjust if needed
    private var outputBufferSize: Int = 0
    private var modelOutputDataType: DataType = DataType.FLOAT32 // Default, adjust if needed

    // Processors (using org.tensorflow.lite.support)
    private var inputImageProcessor: ImageProcessor? = null

    // Configuration
    private val maxResults = 3 // How many top results to return

    init {
        try {
            val modelFile = copyModelToLocalStorage()
            loadLabels()
            setupInterpreter(modelFile)
            Log.d(TAG, "Interpreter, processors, and labels loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize model components", e)
            close() // Clean up partially initialized resources
        }
    }

    private fun copyModelToLocalStorage(): File {
        val modelFile = File(context.filesDir, modelName)
        if (!modelFile.exists()) {
            try {
                context.assets.open(modelName).use { input ->
                    FileOutputStream(modelFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d(TAG, "Model copied to ${modelFile.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Error copying model file", e)
                throw e
            }
        }
        return modelFile
    }

    private fun loadLabels() {
        // Consider using MetadataExtractor if available and model has metadata
        // import org.tensorflow.lite.support.metadata.MetadataExtractor
        try {
            // val mmb = context.assets.openFd(modelName).use { afd ->
            //     FileUtil.loadMappedFile(afd) // Requires MappedByteBuffer
            // }
            // val extractor = MetadataExtractor(mmb) // Hypothetical usage
            // if (extractor.hasMetadata() && extractor.getAssociatedFile("labels.txt") != null) { ... }

            // Fallback to manual loading
            context.assets.open(labelsName).bufferedReader().useLines { lines ->
                lines.forEach { labels.add(it) }
            }
            Log.d(TAG, "Loaded ${labels.size} labels from $labelsName")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading labels (or metadata)", e)
        }
    }

    private fun setupInterpreter(modelFile: File) {
        try {
            // Use Interpreter.Options from the *same package* as your Interpreter import
            val options = Interpreter.Options()
            // options.setNumThreads(4)
            // options.addDelegate(...) // Add delegates if needed

            // Initialize Interpreter (either LiteRT or TFLite version)
            interpreter = Interpreter(modelFile, options)

            val inputTensor = interpreter!!.getInputTensor(0)
            val outputTensor = interpreter!!.getOutputTensor(0)

            inputWidth = inputTensor.shape()[2]
            inputHeight = inputTensor.shape()[1]
            modelInputDataType = inputTensor.dataType()

            outputBufferSize = outputTensor.shape()[1]
            modelOutputDataType = outputTensor.dataType()

            // --- Setup Preprocessing (using org.tensorflow.lite.support) ---
            // TODO: Add correct normalization values if needed!
            // val MODEL_MEAN = 127.5f
            // val MODEL_STDDEV = 127.5f
            inputImageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(inputHeight, inputWidth, ResizeOp.ResizeMethod.BILINEAR))
                // .add(NormalizeOp(MODEL_MEAN, MODEL_STDDEV))
                .build()

            Log.d(TAG, "Interpreter initialized. Input: ${inputTensor.shape().contentToString()}, Output: ${outputTensor.shape().contentToString()}")

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing interpreter", e)
            interpreter = null
            throw e
        }
    }

    fun classifyImage(bitmap: Bitmap): String {
        if (interpreter == null || inputImageProcessor == null) {
            Log.e(TAG, "Classifier not initialized properly.")
            return "Classifier not initialized"
        }
        if (labels.isEmpty()) {
            Log.e(TAG, "Labels not loaded.")
            return "Labels not loaded"
        }

        try {
            // 1. Create TensorImage (org.tensorflow.lite.support)
            val tensorInputImage = TensorImage(modelInputDataType)
            tensorInputImage.load(bitmap)

            // 2. Preprocess the image (org.tensorflow.lite.support)
            val processedImageBuffer = inputImageProcessor!!.process(tensorInputImage).buffer

            // 3. Create TensorBuffer for output (org.tensorflow.lite.support)
            val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, outputBufferSize), modelOutputDataType)

            // 4. Run inference
            interpreter!!.run(processedImageBuffer, outputBuffer.buffer.rewind())

            // 5. Postprocess (org.tensorflow.lite.support)
            // TensorLabel assumes labels are in the correct order corresponding to model output indices
            val labelledResults = TensorLabel(labels, outputBuffer)
                .mapWithFloatValue // Use mapWithFloatValue for FLOAT32, check for quantized models

            // Sort all results by confidence (highest first)
            val sortedResults = labelledResults.entries.sortedByDescending { it.value }

            // Get only the highest confidence prediction
            if (sortedResults.isNotEmpty()) {
                val topPrediction = sortedResults[0]
                return "${topPrediction.key.replace('_', ' ')} (${String.format("%.1f", topPrediction.value * 100)}%)"
            }

            return "No results found"

        } catch (e: Exception) {
            Log.e(TAG, "Error classifying image", e)
            return "Classification error: ${e.message}"
        }
    }

    fun close() {
        try {
            interpreter?.close()
            interpreter = null
            Log.d(TAG, "Interpreter closed.")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing interpreter", e)
        }
    }

    // Add this method to your FruitQualityModelBinding class for debugging purposes
    fun getAllPredictions(bitmap: Bitmap): List<Pair<String, Float>> {
        if (interpreter == null || inputImageProcessor == null) {
            Log.e(TAG, "Classifier not initialized properly.")
            return emptyList()
        }
        if (labels.isEmpty()) {
            Log.e(TAG, "Labels not loaded.")
            return emptyList()
        }

        try {
            // 1. Create TensorImage (org.tensorflow.lite.support)
            val tensorInputImage = TensorImage(modelInputDataType)
            tensorInputImage.load(bitmap)

            // 2. Preprocess the image (org.tensorflow.lite.support)
            val processedImageBuffer = inputImageProcessor!!.process(tensorInputImage).buffer

            // 3. Create TensorBuffer for output (org.tensorflow.lite.support)
            val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, outputBufferSize), modelOutputDataType)

            // 4. Run inference
            interpreter!!.run(processedImageBuffer, outputBuffer.buffer.rewind())

            // 5. Postprocess (org.tensorflow.lite.support)
            val labelledResults = TensorLabel(labels, outputBuffer)
                .mapWithFloatValue

            // Sort all results by confidence (highest first) and return as list of pairs
            return labelledResults.entries
                .sortedByDescending { it.value }
                .map { Pair(it.key.replace('_', ' '), it.value) }

        } catch (e: Exception) {
            Log.e(TAG, "Error classifying image", e)
            return emptyList()
        }
    }
}