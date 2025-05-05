package com.example.frutti

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
// Keep original TensorFlow imports
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.HashMap
// OpenCV imports
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc


class FruitClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val labels = mutableListOf<String>()
    // --- Modified tag to follow naming conventions ---
    private val tag = "FruitQualityModel_DBG" // Added DBG suffix, lowercase to fix warning

    // Model details
    private val modelName = "fruit_quality_multiclass_model.tflite" // Updated model name
    private val labelsName = "labels.txt"
    private var inputWidth: Int = 224  // Updated according to migration instructions
    private var inputHeight: Int = 224 // Updated according to migration instructions
    private var modelInputDataType: DataType = DataType.FLOAT32
    private var outputBufferSize: Int = 12  // 12 classes as mentioned in migration guide
    private var modelOutputDataType: DataType = DataType.FLOAT32

    // Feature extractor settings (renamed to follow Kotlin conventions)
    private val numFeatures = 117 // Based on Python code: 96 (HSV histograms) + 16 (texture) + 1 (laplacian var) + 4 (shape)

    // Processors
    private var inputImageProcessor: ImageProcessor? = null

    // Track OpenCV initialization status
    private var openCVInitialized = false

    init {
        // Initialize OpenCV first, then proceed with model initialization
        initializeOpenCV {
            try {
                // Force delete existing model file to ensure using latest version
                val existingModelFile = File(context.filesDir, modelName)
                if (existingModelFile.exists()) {
                    existingModelFile.delete()
                    Log.d(tag, "Deleted existing model file to ensure using latest version")
                }

                val modelFile = copyModelToLocalStorage()
                loadLabels()
                setupInterpreter(modelFile)
                Log.i(tag, "--- Model Initialized Successfully ---") // Use Info level for milestones
            } catch (e: Exception) {
                Log.e(tag, "Failed to initialize model components", e)
                close() // Ensure cleanup on initialization failure
            }
        }
    }

    private fun initializeOpenCV(onInitialized: () -> Unit) {
        OpenCVInitializer.initOpenCV(context) {
            openCVInitialized = true
            Log.i(tag, "OpenCV initialized successfully")
            onInitialized()
        }
    }

    private fun copyModelToLocalStorage(): File {
        val modelFile = File(context.filesDir, modelName)
        if (modelFile.exists()){
            Log.d(tag, "Model file already exists, skipping copy: ${modelFile.absolutePath}")
            return modelFile
        }
        try {
            context.assets.open(modelName).use { input ->
                FileOutputStream(modelFile).use { output ->
                    input.copyTo(output)
                }
            }
            Log.d(tag, "Model copied to ${modelFile.absolutePath}, size: ${modelFile.length()} bytes")
        } catch (e: Exception) {
            Log.e(tag, "Error copying model file $modelName from assets", e)
            throw e // Re-throw to indicate initialization failure
        }
        return modelFile
    }

    private fun loadLabels() {
        labels.clear()
        try {
            context.assets.open(labelsName).bufferedReader().useLines { lines ->
                lines.forEach {
                    val trimmedLabel = it.trim()
                    if (trimmedLabel.isNotEmpty()) {
                        labels.add(trimmedLabel)
                    }
                }
            }
            Log.d(tag, "Loaded ${labels.size} labels from $labelsName: ${labels.joinToString(", ", limit = 5)}")
        } catch (e: Exception) {
            Log.e(tag, "Error loading labels file $labelsName from assets", e)
            // Depending on requirements, you might want to throw here too
        }
    }

    private fun setupInterpreter(modelFile: File) {
        try {
            interpreter?.close() // Close previous interpreter if any

            val options = Interpreter.Options()
            options.setNumThreads(2)
            // Add delegates if needed (GPU, NNAPI) - Note: This can sometimes affect results
            // options.addDelegate(NnApiDelegate())
            // options.addDelegate(GpuDelegate())

            // Log the model file size to verify it's the correct one
            Log.d(tag, "Loading model file: ${modelFile.absolutePath}, size: ${modelFile.length()} bytes")

            interpreter = Interpreter(modelFile, options)
            Log.i(tag, "Interpreter created for ${modelFile.name}")

            // --- DETAILED LOGGING for Input/Output Tensors ---
            // Log complete details of all input and output tensors
            Log.d(tag, "Number of input tensors: ${interpreter!!.inputTensorCount}")
            Log.d(tag, "Number of output tensors: ${interpreter!!.outputTensorCount}")

            for (i in 0 until interpreter!!.inputTensorCount) {
                val tensor = interpreter!!.getInputTensor(i)
                Log.d(tag, "Input tensor #$i name: ${tensor.name()}, shape: ${tensor.shape().contentToString()}, dataType: ${tensor.dataType()}")
            }

            for (i in 0 until interpreter!!.outputTensorCount) {
                val tensor = interpreter!!.getOutputTensor(i)
                Log.d(tag, "Output tensor #$i name: ${tensor.name()}, shape: ${tensor.shape().contentToString()}, dataType: ${tensor.dataType()}")
            }

            // Get input tensors (just for logging, no need to store references)
            val imageShape = interpreter!!.getInputTensor(0).shape()
            val featuresShape = interpreter!!.getInputTensor(1).shape()
            val outputShape = interpreter!!.getOutputTensor(0).shape()

            // Determine input dimensions safely
            if (imageShape.size >= 3) {
                inputHeight = imageShape[1]
                inputWidth = imageShape[2]
            } else {
                Log.w(tag, "Unexpected input tensor shape: ${imageShape.contentToString()}, using defaults H=$inputHeight, W=$inputWidth")
            }

            modelInputDataType = interpreter!!.getInputTensor(0).dataType()
            outputBufferSize = if (outputShape.isNotEmpty()) outputShape[outputShape.size - 1] else 0 // Get last dimension size
            modelOutputDataType = interpreter!!.getOutputTensor(0).dataType()

            Log.d(tag, "--- Tensor Details ---")
            Log.d(tag, "Image Input Tensor Shape: ${imageShape.contentToString()}")
            Log.d(tag, "Features Input Tensor Shape: ${featuresShape.contentToString()}")
            Log.d(tag, "Input Tensor DataType: $modelInputDataType")
            Log.d(tag, "Output Tensor Shape: ${outputShape.contentToString()}")
            Log.d(tag, "Output Tensor DataType: $modelOutputDataType")
            Log.d(tag, "Output Buffer Size (Num Classes): $outputBufferSize")
            Log.d(tag, "Using Input Dimensions: H=${inputHeight}, W=${inputWidth}")
            Log.d(tag, "----------------------")

            // --- Verify Image Processor ---
            // Normalization is crucial: [0, 255] -> [0.0, 1.0] matches Python's Rescaling(1.0/255)
            // In FruitClassifier.kt -> setupInterpreter() or where inputImageProcessor is defined:
            inputImageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(inputHeight, inputWidth, ResizeOp.ResizeMethod.BILINEAR))
                // Replace NormalizeOp(0.0f, 255.0f) with:
                .add(NormalizeOp(127.5f, 127.5f)) // Scales to [-1, 1] range
                .build()

            Log.d(tag, "InputImageProcessor created: Resize(${inputHeight}x${inputWidth}, BILINEAR), Normalize(127.5, 127.5)") // Update log message

        } catch (e: Exception) {
            Log.e(tag, "Error initializing interpreter or ImageProcessor", e)
            interpreter = null // Ensure interpreter is null on error
            throw e // Propagate error
        }
    }

    // Comprehensive feature extraction function that closely matches the Python implementation
    private fun extractColorFeatures(bitmap: Bitmap): FloatArray {
        // Check OpenCV initialization before proceeding
        if (!openCVInitialized) {
            Log.e(tag, "OpenCV not initialized - cannot extract features")
            return FloatArray(numFeatures) // Return empty features
        }

        // Create a placeholder array filled with zeros (will be populated with real values)
        val features = FloatArray(numFeatures)

        try {
            // Convert Bitmap to OpenCV Mat format
            val rgbMat = Mat()
            Utils.bitmapToMat(bitmap, rgbMat)

            // FIXED: Explicitly convert RGB to BGR for consistency with Python code
            // Android bitmaps are stored in ARGB or RGB format, not RGBA
            val bgrMat = Mat()
            if (rgbMat.channels() == 4) {
                // For ARGB format
                Imgproc.cvtColor(rgbMat, bgrMat, Imgproc.COLOR_RGBA2BGR)
            } else if (rgbMat.channels() == 3) {
                // For RGB format
                Imgproc.cvtColor(rgbMat, bgrMat, Imgproc.COLOR_RGB2BGR)
            } else {
                Log.e(tag, "Unexpected number of channels in bitmap: ${rgbMat.channels()}")
                bgrMat.release()
                rgbMat.release()
                return features
            }

            // Log the color conversion
            Log.d(tag, "Color conversion: rgbMat channels: ${rgbMat.channels()}, bgrMat channels: ${bgrMat.channels()}")

            // -------------- HSV Histograms (96 features) --------------
            // Convert to HSV color space
            val hsvMat = Mat()
            Imgproc.cvtColor(bgrMat, hsvMat, Imgproc.COLOR_BGR2HSV)

            // Split the HSV channels
            val hChannels = ArrayList<Mat>()
            Core.split(hsvMat, hChannels)

            // Check if channels were split correctly
            if (hChannels.size != 3) {
                Log.e(tag, "Error: Could not split HSV channels properly. Expected 3 channels but got ${hChannels.size}")
                return features
            }

            // 1. Hue Histogram (32 bins)
            val hHistMat = Mat()
            val hHistSize = MatOfInt(32)
            val hRanges = MatOfFloat(0f, 180f) // Hue range in OpenCV is [0, 180]
            val hChannelsMat = MatOfInt(0)

            // FIXED: Create a list with the single Mat
            val hueMatList = listOf(hChannels[0])

            // Correct way to call calcHist
            Imgproc.calcHist(
                hueMatList,     // List of Mat objects
                hChannelsMat,   // Channels to use from each Mat (0 for single channel)
                Mat(),          // No mask
                hHistMat,       // Output histogram
                hHistSize,      // Histogram size
                hRanges         // Ranges
            )
            Core.normalize(hHistMat, hHistMat, 0.0, 1.0, Core.NORM_MINMAX)

            // 2. Saturation Histogram (32 bins)
            val sHistMat = Mat()
            val sHistSize = MatOfInt(32)
            val sRanges = MatOfFloat(0f, 256f)
            val sChannelsMat = MatOfInt(0) // We'll use channel 0 of the saturation Mat

            // FIXED: Create list with just the saturation channel
            val satMatList = listOf(hChannels[1])

            Imgproc.calcHist(
                satMatList,
                sChannelsMat,
                Mat(), // No mask
                sHistMat,
                sHistSize,
                sRanges
            )
            Core.normalize(sHistMat, sHistMat, 0.0, 1.0, Core.NORM_MINMAX)

            // 3. Value Histogram (32 bins)
            val vHistMat = Mat()
            val vHistSize = MatOfInt(32)
            val vRanges = MatOfFloat(0f, 256f)
            val vChannelsMat = MatOfInt(0) // We'll use channel 0 of the value Mat

            // FIXED: Create list with just the value channel
            val valMatList = listOf(hChannels[2])

            Imgproc.calcHist(
                valMatList,
                vChannelsMat,
                Mat(), // No mask
                vHistMat,
                vHistSize,
                vRanges
            )
            Core.normalize(vHistMat, vHistMat, 0.0, 1.0, Core.NORM_MINMAX)

            // Copy histogram values to our features array
            // H histogram (first 32 values)
            for (i in 0 until 32) {
                features[i] = hHistMat.get(i, 0)[0].toFloat()
            }

            // S histogram (next 32 values)
            for (i in 0 until 32) {
                features[i + 32] = sHistMat.get(i, 0)[0].toFloat()
            }

            // V histogram (next 32 values)
            for (i in 0 until 32) {
                features[i + 64] = vHistMat.get(i, 0)[0].toFloat()
            }

            // Log first few values of each histogram for debugging
            Log.d(tag, "H_hist values (first 5): ${features.slice(0..4)}")
            Log.d(tag, "S_hist values (first 5): ${features.slice(32..36)}")
            Log.d(tag, "V_hist values (first 5): ${features.slice(64..68)}")

            // -------------- Texture Features (16 features) --------------
            // Convert to grayscale
            val grayMat = Mat()
            Imgproc.cvtColor(bgrMat, grayMat, Imgproc.COLOR_BGR2GRAY)

            // Resize to 64x64 for texture analysis (as in Python)
            val grayResized = Mat()
            Imgproc.resize(grayMat, grayResized, Size(64.0, 64.0))

            // Calculate Sobel gradients (as in Python)
            val sobelX = Mat()
            val sobelY = Mat()
            Imgproc.Sobel(grayResized, sobelX, CvType.CV_64F, 1, 0, 3)
            Imgproc.Sobel(grayResized, sobelY, CvType.CV_64F, 0, 1, 3)

            // Calculate magnitude
            val magnitude = Mat()
            Core.magnitude(sobelX, sobelY, magnitude)

            // Convert to 8-bit for histogram calculation
            val magU8 = Mat()
            magnitude.convertTo(magU8, CvType.CV_8U)

            // Calculate texture histogram (16 bins)
            val textureHistMat = Mat()
            val textureHistSize = MatOfInt(16)
            val textureRanges = MatOfFloat(0f, 256f)
            val textureChannelsMat = MatOfInt(0)

            // FIXED: Create list with magnitudes
            val magMatList = listOf(magU8)

            Imgproc.calcHist(
                magMatList,
                textureChannelsMat,
                Mat(), // No mask
                textureHistMat,
                textureHistSize,
                textureRanges
            )
            Core.normalize(textureHistMat, textureHistMat, 0.0, 1.0, Core.NORM_MINMAX)

            // Copy texture histogram to our features array (next 16 values after HSV histograms)
            for (i in 0 until 16) {
                features[i + 96] = textureHistMat.get(i, 0)[0].toFloat()
            }

            val laplacian = Mat()
            Imgproc.Laplacian(grayResized, laplacian, CvType.CV_64F)

            // Fix meanStdDev call - create output matrices for mean and stddev
            val mean = MatOfDouble()
            val stddev = MatOfDouble()
            Core.meanStdDev(laplacian, mean, stddev)

            // Divide by 1000 as in Python (normalize value)
            features[96 + 16] = (stddev.get(0, 0)[0] * stddev.get(0, 0)[0] / 1000.0).toFloat()

            // -------------- Shape Features (4 features) --------------
            var aspectRatio = 0f
            var extent = 0f
            var solidity = 0f
            var circularity = 0f

            try {
                // Apply Gaussian blur to reduce noise
                val blurred = Mat()
                Imgproc.GaussianBlur(grayMat, blurred, Size(5.0, 5.0), 0.0)

                // Threshold image
                val thresholded = Mat()
                Imgproc.threshold(blurred, thresholded, 60.0, 255.0, Imgproc.THRESH_BINARY)

                // Find contours
                val contours = ArrayList<MatOfPoint>()
                val hierarchy = Mat()
                Imgproc.findContours(
                    thresholded.clone(), // Use clone to avoid modifying original
                    contours,
                    hierarchy,
                    Imgproc.RETR_EXTERNAL,
                    Imgproc.CHAIN_APPROX_SIMPLE
                )

                // Find the largest contour
                if (contours.isNotEmpty()) {
                    var maxContourIdx = 0
                    var maxContourArea = 0.0

                    for (i in contours.indices) {
                        val area = Imgproc.contourArea(contours[i])
                        if (area > maxContourArea) {
                            maxContourArea = area
                            maxContourIdx = i
                        }
                    }

                    // Only proceed if the area is meaningful
                    if (maxContourArea > 100) {
                        val contour = contours[maxContourIdx]

                        // Get bounding rect
                        val rect = Imgproc.boundingRect(contour)
                        aspectRatio = rect.width.toFloat() / rect.height.toFloat()

                        // Calculate extent (area/bounding rect area)
                        val rectArea = rect.width * rect.height
                        extent = maxContourArea.toFloat() / rectArea

                        // Calculate solidity (area/convex hull area)
                        val hull = MatOfInt()  // Fixed: Changed from MatOfPoint to MatOfInt
                        Imgproc.convexHull(contour, hull)  // Fixed: Hull computation

                        // Create hull points
                        val hullPoints = ArrayList<Point>()
                        val hullIndexes = hull.toArray()
                        val contourPoints = contour.toArray()

                        for (index in hullIndexes) {
                            if (index >= 0 && index < contourPoints.size) {
                                hullPoints.add(contourPoints[index])
                            }
                        }

                        if (hullPoints.isNotEmpty()) {
                            val hullMat = MatOfPoint()
                            hullMat.fromList(hullPoints)

                            val hullArea = Imgproc.contourArea(hullMat)
                            solidity = if (hullArea > 0) maxContourArea.toFloat() / hullArea.toFloat() else 0f

                            // Calculate circularity (4*π*area/perimeter^2)
                            val contour2f = MatOfPoint2f()
                            contour.convertTo(contour2f, CvType.CV_32F)
                            val perimeter = Imgproc.arcLength(contour2f, true)

                            circularity = if (perimeter > 0)
                                (4 * Math.PI * maxContourArea / (perimeter * perimeter)).toFloat()
                            else
                                0f
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Error calculating shape features: ${e.message}")
                // We'll use default values (0) for shape features
            }

            // Store shape features in our array (last 4 values)
            features[96 + 16 + 1 + 0] = aspectRatio
            features[96 + 16 + 1 + 1] = extent
            features[96 + 16 + 1 + 2] = solidity
            features[96 + 16 + 1 + 3] = circularity

            // Clean up OpenCV resources
            rgbMat.release()
            bgrMat.release()
            hsvMat.release()
            grayMat.release()
            grayResized.release()
            hHistMat.release()
            sHistMat.release()
            vHistMat.release()
            textureHistMat.release()

            // Log a brief summary of feature extraction
            Log.d(tag, "Feature extraction complete. Feature vector length: ${features.size}")
            Log.d(tag, String.format(
                java.util.Locale.US,
                "Feature highlights: H_hist[0]=%.3f, S_hist[0]=%.3f, V_hist[0]=%.3f, Texture_hist[0]=%.3f, Laplacian_var=%.3f, Shape=[%.3f, %.3f, %.3f, %.3f]",
                features[0], features[32], features[64], features[96], features[96+16],
                features[96+16+1], features[96+16+2], features[96+16+3], features[96+16+4]
            ))

        } catch (e: Exception) {
            Log.e(tag, "Error extracting features: ${e.message}")
            e.printStackTrace()
        }

        return features
    }

    // --- Function with Enhanced Logging ---
    fun classifyImage(bitmap: Bitmap): String {
        // Check OpenCV initialization first
        if (!openCVInitialized) {
            Log.e(tag, "Cannot classify image - OpenCV not initialized")
            return "OpenCV not initialized"
        }

        val startTime = System.currentTimeMillis() // Start timing

        if (interpreter == null || inputImageProcessor == null) {
            Log.e(tag, "Classifier not initialized properly.")
            return "Classifier not initialized"
        }
        if (labels.isEmpty()) {
            Log.e(tag, "Labels not loaded.")
            return "Labels not loaded"
        }
        if (outputBufferSize == 0 || outputBufferSize != labels.size) {
            Log.e(tag, "Output buffer size ($outputBufferSize) mismatch with labels count (${labels.size}).")
            return "Label/Output mismatch"
        }

        try {
            Log.d(tag, "Starting classification for bitmap: ${bitmap.width}x${bitmap.height}")

            // 1. Preprocess Image using TensorImage and ImageProcessor
            val tensorImage = TensorImage(modelInputDataType)
            tensorImage.load(bitmap) // Load bitmap into TensorImage

            val processedImage = inputImageProcessor!!.process(tensorImage)
            Log.d(tag, "Image processed to: ${processedImage.width}x${processedImage.height}, Buffer size: ${processedImage.buffer.capacity()}")

            // --- LOG First few bytes/floats of the processed input buffer ---
            try {
                val inputBuffer = processedImage.buffer.order(ByteOrder.nativeOrder()) // Ensure correct byte order
                inputBuffer.rewind() // Ensure we read from the start
                val numBytesToLog = 24 // Log first N bytes (e.g., 6 floats if FLOAT32)
                val bytes = ByteArray(minOf(numBytesToLog, inputBuffer.remaining()))
                inputBuffer.get(bytes)
                val hexString = bytes.joinToString("") { String.format("%02X", it) }
                Log.d(tag, "First $numBytesToLog bytes of input buffer (Hex): $hexString")

                // If FLOAT32, try logging first few float values
                if (modelInputDataType == DataType.FLOAT32 && inputBuffer.capacity() >= 12) {
                    inputBuffer.rewind()
                    val firstFloats = FloatArray(3)
                    inputBuffer.asFloatBuffer().get(firstFloats) // Read first 3 floats
                    Log.d(tag, "First 3 floats of input buffer: ${firstFloats.contentToString()}")
                }
                inputBuffer.rewind() // IMPORTANT: Rewind buffer again before inference!
            } catch (e: Exception) {
                Log.w(tag, "Could not log input buffer values: ${e.message}")
            }
            // --- End Input Buffer Logging ---

            // 2. Extract Color Features (117 float values) using comprehensive implementation
            val colorFeatures = extractColorFeatures(bitmap)

            // Create a ByteBuffer for the features
            val featuresBuffer = ByteBuffer.allocateDirect(numFeatures * 4) // 4 bytes per float
            featuresBuffer.order(ByteOrder.nativeOrder())
            val floatBuffer = featuresBuffer.asFloatBuffer()
            floatBuffer.put(colorFeatures)
            featuresBuffer.rewind()

            Log.d(tag, "Color features buffer created with size: ${featuresBuffer.capacity()} bytes")

            // 3. Prepare Output Buffer
            val outputShape = intArrayOf(1, outputBufferSize) // Standard shape [batch_size, num_classes]
            val outputBuffer = TensorBuffer.createFixedSize(outputShape, modelOutputDataType)

            // 4. Run Inference with multiple inputs
            Log.d(tag, "Running inference with multiple inputs...")
            val inferenceStartTime = System.currentTimeMillis()

            // Log input buffer sizes
            Log.d(tag, "Image buffer size: ${processedImage.buffer.capacity()} bytes")
            Log.d(tag, "Features buffer size: ${featuresBuffer.capacity()} bytes")

            // IMPORTANT: Use the SAME order as in the Python model:
            // inputs=[image_input, features_input]
            val inputs = arrayOf<Any>(processedImage.buffer, featuresBuffer)
            val outputs = HashMap<Int, Any>()
            outputs[0] = outputBuffer.buffer

            try {
                // Try with image first, then features (matching Python model)
                interpreter!!.runForMultipleInputsOutputs(inputs, outputs)
                Log.d(tag, "Inference succeeded with image first, features second (matching Python model)")
            } catch (e: Exception) {
                Log.d(tag, "First attempt failed: ${e.message}, trying alternative orders")

                try {
                    // Try features first, then image as fallback
                    val alternativeInputs = arrayOf<Any>(featuresBuffer, processedImage.buffer)
                    interpreter!!.runForMultipleInputsOutputs(alternativeInputs, outputs)
                    Log.d(tag, "Inference succeeded with features first, image second")
                } catch (e2: Exception) {
                    Log.e(tag, "Both direct inference attempts failed: ${e2.message}")

                    // Try a different approach
                    Log.d(tag, "Trying fallback approach...")

                    // Direct invoke with both inputs
                    val imageInputIndex = 0
                    val featureInputIndex = 1

                    try {
                        // Run model directly
                        interpreter!!.run(
                            arrayOf(processedImage.buffer.duplicate(), featuresBuffer.duplicate()),
                            arrayOf(outputBuffer.buffer.duplicate())
                        )
                        Log.d(tag, "Fallback inference approach succeeded")
                    } catch (e3: Exception) {
                        throw Exception("All inference approaches failed: ${e3.message}")
                    }
                }
            }

            val inferenceTime = System.currentTimeMillis() - inferenceStartTime
            Log.d(tag, "Inference complete in ${inferenceTime}ms")

            // 5. Process Results
            val outputFloats = outputBuffer.floatArray // Get probabilities as float array

            // --- LOG Raw Output Probabilities ---
            val outputLog = StringBuilder("Raw Output Probabilities (${outputFloats.size}): [")
            outputFloats.forEachIndexed { index, value ->
                if (index < 5 || index >= outputFloats.size - 5) { // Log first 5 and last 5
                    outputLog.append(String.format(java.util.Locale.US, "%.4f", value))
                    if (index < outputFloats.size - 1) outputLog.append(", ")
                } else if (index == 5) {
                    outputLog.append("..., ")
                }
            }
            outputLog.append("]")
            Log.d(tag, outputLog.toString())

            // Log all class probabilities
            Log.d(tag, "All class probabilities:")
            outputFloats.forEachIndexed { index, value ->
                Log.d(tag, "${labels[index]}: ${String.format(java.util.Locale.US, "%.2f%%", value * 100)}")
            }

            // Map to labels and find best result
            var maxConfidence = -1f
            var bestLabelIndex = -1

            for (i in labels.indices) {
                if (i < outputFloats.size) {
                    if (outputFloats[i] > maxConfidence) {
                        maxConfidence = outputFloats[i]
                        bestLabelIndex = i
                    }
                } else {
                    Log.w(tag, "Output array size (${outputFloats.size}) smaller than labels list size (${labels.size}) at index $i")
                    break // Avoid index out of bounds
                }
            }

            val totalTime = System.currentTimeMillis() - startTime
            Log.i(tag, "Classification finished in ${totalTime}ms") // Log total time

            if (bestLabelIndex != -1) {
                val bestLabel = labels[bestLabelIndex].replace('_', ' ') // Format label
                val confidencePercent = String.format(java.util.Locale.US, "%.1f", maxConfidence * 100)
                Log.i(tag, "Prediction: '$bestLabel' ($confidencePercent%)") // Log final prediction clearly
                return "$bestLabel ($confidencePercent%)"
            } else {
                Log.w(tag, "No confident prediction found.")
                return "No confident prediction"
            }

        } catch (e: Exception) {
            Log.e(tag, "Error during classification process", e)
            return "Classification error: ${e.message}"
        }
    }

    fun close() {
        try {
            interpreter?.close()
            interpreter = null
            Log.i(tag, "Interpreter closed.")
        } catch (e: Exception) {
            Log.e(tag, "Error closing interpreter", e)
        }
    }

    // --- getAllPredictions with Enhanced Logging (similar to classifyImage) ---
    fun getAllPredictions(bitmap: Bitmap): List<Pair<String, Float>> {
        // Check OpenCV initialization first
        if (!openCVInitialized) {
            Log.e(tag, "Cannot get predictions - OpenCV not initialized")
            return emptyList()
        }

        val startTime = System.currentTimeMillis()

        if (interpreter == null || inputImageProcessor == null) {
            Log.e(tag, "Classifier not initialized properly for getAllPredictions.")
            return emptyList()
        }
        if (labels.isEmpty()) {
            Log.e(tag, "Labels not loaded for getAllPredictions.")
            return emptyList()
        }
        if (outputBufferSize == 0 || outputBufferSize != labels.size) {
            Log.e(tag, "Output buffer size ($outputBufferSize) mismatch with labels count (${labels.size}) for getAllPredictions.")
            return emptyList()
        }

        try {
            Log.d(tag, "[getAllPredictions] Starting for bitmap: ${bitmap.width}x${bitmap.height}")

            // 1. Preprocess Image
            val tensorImage = TensorImage(modelInputDataType)
            tensorImage.load(bitmap)
            val processedImage = inputImageProcessor!!.process(tensorImage)
            Log.d(tag, "[getAllPredictions] Image processed to: ${processedImage.width}x${processedImage.height}")
            processedImage.buffer.rewind() // Ensure buffer is ready

            // 2. Extract Color Features (117 float values) using comprehensive implementation
            val colorFeatures = extractColorFeatures(bitmap)

            // Create a ByteBuffer for the features
            val featuresBuffer = ByteBuffer.allocateDirect(numFeatures * 4) // 4 bytes per float
            featuresBuffer.order(ByteOrder.nativeOrder())
            val floatBuffer = featuresBuffer.asFloatBuffer()
            floatBuffer.put(colorFeatures)
            featuresBuffer.rewind()

            Log.d(tag, "[getAllPredictions] Color features buffer created with size: ${featuresBuffer.capacity()} bytes")

            // 3. Prepare Output Buffer
            val outputShape = intArrayOf(1, outputBufferSize)
            val outputBuffer = TensorBuffer.createFixedSize(outputShape, modelOutputDataType)

            // 4. Run Inference with multiple inputs
            Log.d(tag, "[getAllPredictions] Running inference with multiple inputs...")
            val inferenceStartTime = System.currentTimeMillis()

            // Log input buffer sizes
            Log.d(tag, "[getAllPredictions] Image buffer size: ${processedImage.buffer.capacity()} bytes")
            Log.d(tag, "[getAllPredictions] Features buffer size: ${featuresBuffer.capacity()} bytes")

            // IMPORTANT: Use the SAME order as in the Python model:
            // inputs=[image_input, features_input]
            val inputs = arrayOf<Any>(processedImage.buffer, featuresBuffer)
            val outputs = HashMap<Int, Any>()
            outputs[0] = outputBuffer.buffer

            try {
                // Try with image first, then features (matching Python model)
                interpreter!!.runForMultipleInputsOutputs(inputs, outputs)
                Log.d(tag, "[getAllPredictions] Inference succeeded with image first, features second (matching Python model)")
            } catch (e: Exception) {
                Log.d(tag, "[getAllPredictions] First attempt failed: ${e.message}, trying alternative orders")

                try {
                    // Try features first, then image as fallback
                    val alternativeInputs = arrayOf<Any>(featuresBuffer, processedImage.buffer)
                    interpreter!!.runForMultipleInputsOutputs(alternativeInputs, outputs)
                    Log.d(tag, "[getAllPredictions] Inference succeeded with features first, image second")
                } catch (e2: Exception) {
                    Log.e(tag, "[getAllPredictions] Both direct inference attempts failed: ${e2.message}")

                    // Try a different approach
                    Log.d(tag, "[getAllPredictions] Trying fallback approach...")

                    try {
                        // Run model directly
                        interpreter!!.run(
                            arrayOf(processedImage.buffer.duplicate(), featuresBuffer.duplicate()),
                            arrayOf(outputBuffer.buffer.duplicate())
                        )
                        Log.d(tag, "[getAllPredictions] Fallback inference approach succeeded")
                    } catch (e3: Exception) {
                        throw Exception("All inference approaches failed: ${e3.message}")
                    }
                }
            }

            val inferenceTime = System.currentTimeMillis() - inferenceStartTime
            Log.d(tag, "[getAllPredictions] Inference complete in ${inferenceTime}ms")

            // 5. Process Results
            val outputFloats = outputBuffer.floatArray

            // Log raw outputs
            val outputLog = StringBuilder("[getAllPredictions] Raw Outputs (${outputFloats.size}): [")
            outputFloats.take(10).forEachIndexed { index, value -> // Log first 10
                outputLog.append(String.format(java.util.Locale.US, "%.4f", value))
                if (index < 9 && index < outputFloats.size -1) outputLog.append(", ")
            }
            if (outputFloats.size > 10) outputLog.append("...")
            outputLog.append("]")
            Log.d(tag, outputLog.toString())

            // Log all class probabilities
            Log.d(tag, "[getAllPredictions] All class probabilities:")
            outputFloats.forEachIndexed { index, value ->
                Log.d(tag, "${labels[index]}: ${String.format(java.util.Locale.US, "%.2f%%", value * 100)}")
            }

            // Map to labels
            val resultList = mutableListOf<Pair<String, Float>>()
            for (i in labels.indices) {
                if (i < outputFloats.size) {
                    resultList.add(Pair(labels[i].replace('_', ' '), outputFloats[i]))
                } else {
                    Log.w(tag, "[getAllPredictions] Output array size (${outputFloats.size}) smaller than labels list size (${labels.size}) at index $i")
                    break
                }
            }

            val totalTime = System.currentTimeMillis() - startTime
            Log.i(tag, "[getAllPredictions] Finished in ${totalTime}ms")

            // Return sorted list
            return resultList.sortedByDescending { it.second }

        } catch (e: Exception) {
            Log.e(tag, "Error getting all predictions", e)
            return emptyList()
        }
    }

    // Helper function to save debug images for visual inspection
    private fun saveBitmapForDebug(bitmap: Bitmap, filename: String) {
        try {
            val file = File(context.getExternalFilesDir(null), filename)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            Log.d(tag, "Saved debug image to: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(tag, "Failed to save debug image: ${e.message}")
        }
    }
}