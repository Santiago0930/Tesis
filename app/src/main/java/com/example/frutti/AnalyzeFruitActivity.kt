package com.example.frutti

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.frutti.ui.theme.FruttiTheme
import kotlinx.coroutines.*
import org.tensorflow.lite.support.image.TensorImage

class AnalyzeFruitActivity : ComponentActivity() {
    private var fruitClassifier: FruitClassifier? = null
    private val TAG = "AnalyzeFruitActivity"
    private var modelInitializationComplete = false
    private var initializationError: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set initial content showing loading state
        setContent {
            FruttiTheme {
                LoadingScreen("Initializing fruit classifier...")
            }
        }

        // Initialize OpenCV and classifier
        initializeOpenCVAndClassifier()
    }

    private fun initializeOpenCVAndClassifier() {
        // Initialize OpenCV first, then create the classifier
        OpenCVInitializer.initOpenCV(this) {
            try {
                Log.d(TAG, "OpenCV initialized, now initializing FruitQualityModel")

                // Initialize the classifier on a background thread
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        fruitClassifier = FruitClassifier(this@AnalyzeFruitActivity)
                        Log.d(TAG, "Classifier initialized successfully")

                        // Update UI on main thread
                        withContext(Dispatchers.Main) {
                            modelInitializationComplete = true
                            // Now that everything is initialized, set the content
                            setContent {
                                FruttiTheme {
                                    MainNavigation(fruitClassifier)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error initializing model", e)
                        initializationError = e.message

                        // Update UI on main thread
                        withContext(Dispatchers.Main) {
                            modelInitializationComplete = true
                            Toast.makeText(
                                this@AnalyzeFruitActivity,
                                "Error loading model: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()

                            // Set content with error state
                            setContent {
                                FruttiTheme {
                                    MainNavigation(null)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in OpenCV callback", e)
                modelInitializationComplete = true
                initializationError = "OpenCV error: ${e.message}"

                // Set content with error state
                setContent {
                    FruttiTheme {
                        MainNavigation(null)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        try {
            fruitClassifier?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing model", e)
        }
        super.onDestroy()
    }
}

@Composable
fun LoadingScreen(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF53B175), Color(0xFF2E7D32))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzeFruitScreen(
    fruitClassifier: FruitClassifier? = null,
    navController: NavHostController? = null
) {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var modelAvailable by remember { mutableStateOf(fruitClassifier != null) }
    var resultText by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val screenTag = "AnalyzeFruitScreen"

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        if (isGranted) {
            Toast.makeText(context, "Camera permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        permissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            imageUri = tempImageUri
            resultText = null
            Toast.makeText(context, "Image captured successfully!", Toast.LENGTH_SHORT).show()

            if (modelAvailable) {
                imageUri?.let { uri ->
                    analyzeImage(uri, fruitClassifier, coroutineScope, context) { result ->
                        isAnalyzing = result.first
                        if (result.second != null) {
                            resultText = result.second
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Model not available for analysis", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imageUri = uri
            resultText = null
            Toast.makeText(context, "Image selected successfully!", Toast.LENGTH_SHORT).show()

            if (modelAvailable) {
                analyzeImage(uri, fruitClassifier, coroutineScope, context) { result ->
                    isAnalyzing = result.first
                    if (result.second != null) {
                        resultText = result.second
                    }
                }
            } else {
                Toast.makeText(context, "Model not available for analysis", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Analyze a Fruit",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF53B175)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF53B175), Color(0xFF2E7D32))
                    )
                )
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (!modelAvailable) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFCCCC)
                    )
                ) {
                    Text(
                        text = "TensorFlow model could not be loaded. Classification unavailable.",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(240.dp)
                    .background(Color.White, shape = CircleShape)
                    .border(4.dp, Color(0xFF53B175), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Selected Fruit Image",
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.lococolor),
                        contentDescription = "Default Image",
                        modifier = Modifier.size(200.dp)
                    )
                }
            }

            if (resultText != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Text(
                        text = "Result: $resultText",
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF53B175)
                    )
                }
            }

            if (isAnalyzing) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Analyzing fruit quality...",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CustomButton(
                    text = "Take Photo",
                    icon = Icons.Filled.CameraAlt,
                    onClick = {
                        if (permissionGranted) {
                            val newUri = createImageUri(context)
                            if (newUri != null) {
                                tempImageUri = newUri
                                takePictureLauncher.launch(newUri)
                            } else {
                                Toast.makeText(context, "Failed to create image URI", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    enabled = !isAnalyzing
                )

                CustomButton(
                    text = "Upload from Gallery",
                    icon = Icons.Filled.Upload,
                    onClick = {
                        pickImageLauncher.launch("image/*")
                    },
                    enabled = !isAnalyzing
                )

                OutlinedButton(
                    onClick = {
                        navController?.navigate(BottomNavItem.History.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    border = BorderStroke(2.dp, Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    enabled = !isAnalyzing
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = "History"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View History")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

fun createImageUri(context: Context): Uri? {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "frutti_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Frutti")
    }
    return context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    )
}

private fun analyzeImage(
    uri: Uri,
    fruitClassifier: FruitClassifier?,
    scope: CoroutineScope,
    context: Context,
    setAnalyzing: (Pair<Boolean, String?>) -> Unit
) {
    if (fruitClassifier == null) {
        Toast.makeText(context, "Classifier not initialized", Toast.LENGTH_SHORT).show()
        return
    }

    setAnalyzing(Pair(true, null))
    val analyzeTag = "AnalyzeImage"

    scope.launch {
        val result = withContext(Dispatchers.IO) {
            try {
                Log.d(analyzeTag, "Loading bitmap from URI: $uri")
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                Log.d(analyzeTag, "Bitmap loaded successfully, size: ${bitmap.width}x${bitmap.height}")

                // Get all predictions for detailed logging
                val allPredictions = fruitClassifier.getAllPredictions(bitmap)
                Log.d(analyzeTag, "All predictions:")
                allPredictions.forEachIndexed { index, prediction ->
                    Log.d(analyzeTag, "${index + 1}. ${prediction.first}: ${String.format("%.1f", prediction.second * 100)}%")
                }

                // Get the top prediction for display
                val topPrediction = allPredictions.firstOrNull()
                if (topPrediction != null) {
                    val (label, confidence) = topPrediction
                    "${label} (${String.format("%.1f", confidence * 100)}%)"
                } else {
                    "No prediction available"
                }
            } catch (e: Exception) {
                Log.e(analyzeTag, "Error analyzing image", e)
                "Error analyzing image: ${e.message}"
            }
        }

        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Result: $result", Toast.LENGTH_LONG).show()
            setAnalyzing(Pair(false, result))
        }
    }
}

@Composable
fun CustomButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF53B175),
            disabledContainerColor = Color.LightGray,
            disabledContentColor = Color.Gray
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
        enabled = enabled
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) Color(0xFF53B175) else Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyzeFruitPreview() {
    FruttiTheme {
        AnalyzeFruitScreen()
    }
}