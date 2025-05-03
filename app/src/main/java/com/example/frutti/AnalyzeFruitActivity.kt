package com.example.frutti

import android.Manifest
import android.R.attr.text
import android.content.ContentValues
import android.content.ContentValues.TAG
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    private lateinit var fruitClassifier: FruitQualityModelBinding
    private val TAG = "AnalyzeFruitActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d(TAG, "Initializing FruitQualityModelBinding")
            fruitClassifier = FruitQualityModelBinding(this)
            Log.d(TAG, "Classifier initialized successfully")

            setContent {
                FruttiTheme {
                    MainNavigation(fruitClassifier)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing model", e)
            Toast.makeText(
                this,
                "Error loading model: ${e.message}",
                Toast.LENGTH_LONG
            ).show()

            setContent {
                FruttiTheme {
                    MainNavigation(null)
                }
            }
        }
    }

    override fun onDestroy() {
        try {
            if (::fruitClassifier.isInitialized) {
                fruitClassifier.close()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error closing model", e)
        }
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzeFruitScreen(
    fruitClassifier: FruitQualityModelBinding? = null,
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
                    analyzeImage(uri, fruitClassifier, coroutineScope, context, navController) { result ->
                        isAnalyzing = result.first
                        if (result.second != null) {
                            resultText = result.second
                        }
                    }
                }
            }
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
                analyzeImage(uri, fruitClassifier, coroutineScope, context, navController) { result ->
                    isAnalyzing = result.first
                    if (result.second != null) {
                        resultText = result.second
                    }
                }
            }
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
                Button(
                    onClick = {
                        if (permissionGranted) {
                            val newUri = createImageUri(context)
                            if (newUri != null) {
                                tempImageUri = newUri
                                takePictureLauncher.launch(newUri)
                            }
                        } else {
                            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF53B175)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "Take Photo",
                            tint = Color(0xFF53B175)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Take Photo",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Button(
                    onClick = {
                        pickImageLauncher.launch("image/*")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF53B175)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Upload,
                            contentDescription = "Upload from Gallery",
                            tint = Color(0xFF53B175)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Upload from Gallery",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        navController?.navigate("history")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    border = BorderStroke(2.dp, Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    )
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

@Composable
fun GoodQualityScreen(navController: NavHostController?, fruitName: String = "This fruit") {
    QualityResultScreen(
        title = "Excellent Quality!",
        message = "$fruitName is fresh and in perfect condition. Enjoy your healthy snack!",
        icon = Icons.Default.CheckCircle,
        iconColor = Color(0xFF4CAF50),
        navController = navController
    )
}

@Composable
fun BadQualityScreen(navController: NavHostController?, fruitName: String = "This fruit") {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Stacked images container with warning icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(322.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.confetti),
                contentDescription = "Confetti Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.3f // Make background more subtle
            )
            Image(
                painter = painterResource(id = R.drawable.ic_close), // Checkmark image
                contentDescription = "Check",
                modifier = Modifier
                    .size(150.dp)
                    .offset(y = 20.dp) // Moves the check image downward
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Main title with fruit name
        Text(
            text = "Poor Quality Detected",
            fontSize = 27.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Specific warning message
        Text(
            text = "$fruitName appears overripe or spoiled.",
            fontSize = 18.sp,
            color = Color(0xFFF44336),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Additional health warning
        Text(
            text = "For your health, we recommend avoiding this fruit.",
            fontSize = 16.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Disclaimer text
        Text(
            text = "Please note: Results are not 100% accurate",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(44.dp))

        // Action button
        Button(
            onClick = { navController?.popBackStack() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF44336) // Red color matching warning
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Analyze Another Fruit",
                fontSize = 20.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun MixedQualityScreen(navController: NavHostController?, fruitName: String = "This fruit") {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(322.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.confetti),
                contentDescription = "Confetti Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.3f
            )
            Image(
                painter = painterResource(id = R.drawable.sim), // Checkmark image
                contentDescription = "Check",
                modifier = Modifier
                    .size(275.dp)
                    .offset(y = 20.dp) // Moves the check image downward
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Moderate Quality",
            fontSize = 27.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "$fruitName is edible but not at its best.",
            fontSize = 18.sp,
            color = Color(0xFFFFC107),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Consider consuming it soon or using it in cooking.",
            fontSize = 16.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Please note: Results are not 100% accurate",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(44.dp))

        Button(
            onClick = { navController?.popBackStack() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFC107) // Amber color
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Analyze Another Fruit",
                fontSize = 20.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun QualityResultScreen(
    title: String,
    message: String,
    icon: ImageVector,
    iconColor: Color,
    navController: NavHostController?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF53B175), Color(0xFF2E7D32))
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            color = Color.White,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController?.popBackStack() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF53B175)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Analyze Another Fruit")
        }
    }
}

private fun analyzeImage(
    uri: Uri,
    fruitClassifier: FruitQualityModelBinding?,
    scope: CoroutineScope,
    context: Context,
    navController: NavHostController?,
    setAnalyzing: (Pair<Boolean, String?>) -> Unit
) {
    val TAG = "AnalyzeFruitScreen"

    if (fruitClassifier == null) {
        Log.e(TAG, "Classifier not initialized when analyzeImage called.")
        Toast.makeText(context, "Classifier not initialized", Toast.LENGTH_SHORT).show()
        setAnalyzing(Pair(false, "Error: Classifier not available"))
        return
    }

    Log.d(TAG, "Starting image analysis for URI: $uri")
    setAnalyzing(Pair(true, null))

    scope.launch {
        val result: Pair<String, String> = withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Loading bitmap from URI...")
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                Log.d(TAG, "Bitmap loaded. Classifying image...")
                val classificationResult = fruitClassifier.classifyImage(bitmap)
                Log.d(TAG, "Classification result: $classificationResult")

                // Parse the classification result (format: "Fruit Quality (percentage%)")
                val pattern = """([a-zA-Z]+)\s+([a-zA-Z]+)\s*\((\d+\.?\d*)%\)""".toRegex()
                val matchResult = pattern.find(classificationResult)

                if (matchResult != null) {
                    val (fruitName, quality, confidence) = matchResult.destructured

                    val formattedFruitName = fruitName
                        .lowercase()
                        .replaceFirstChar { it.uppercase() }

                    val formattedQuality = quality.lowercase()

                    // Navigate to appropriate screen based on quality
                    when (formattedQuality) {
                        "good" -> {
                            withContext(Dispatchers.Main) {
                                navController?.navigate("good_quality/$formattedFruitName")
                            }
                            Pair(formattedFruitName, "Good Quality ($confidence%)")
                        }
                        "bad" -> {
                            withContext(Dispatchers.Main) {
                                navController?.navigate("bad_quality/$formattedFruitName")
                            }
                            Pair(formattedFruitName, "Bad Quality ($confidence%)")
                        }
                        "mixed" -> {
                            withContext(Dispatchers.Main) {
                                navController?.navigate("mixed_quality/$formattedFruitName")
                            }
                            Pair(formattedFruitName, "Mixed Quality ($confidence%)")
                        }
                        else -> {
                            Pair(formattedFruitName, "Unknown Quality: $classificationResult")
                        }
                    }
                } else {
                    // Try alternative parsing if regex fails
                    val parts = classificationResult.split(" ")
                    if (parts.size >= 2) {
                        val fruitName = parts[0]
                            .lowercase()
                            .replaceFirstChar { it.uppercase() }

                        val quality = parts[1].lowercase()

                        when (quality) {
                            "good" -> {
                                withContext(Dispatchers.Main) {
                                    navController?.navigate("good_quality/$fruitName")
                                }
                                Pair(fruitName, "Good Quality")
                            }
                            "bad" -> {
                                withContext(Dispatchers.Main) {
                                    navController?.navigate("bad_quality/$fruitName")
                                }
                                Pair(fruitName, "Bad Quality")
                            }
                            "mixed" -> {
                                withContext(Dispatchers.Main) {
                                    navController?.navigate("mixed_quality/$fruitName")
                                }
                                Pair(fruitName, "Mixed Quality")
                            }
                            else -> {
                                Pair(fruitName, "Unknown Quality: $classificationResult")
                            }
                        }
                    } else {
                        Pair("Unknown", "Unknown format: $classificationResult")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error analyzing image", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Analysis failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
                Pair("Unknown", "Error analyzing image: ${e.message}")
            }
        }

        withContext(Dispatchers.Main) {
            Log.d(TAG, "Analysis finished. Setting UI result: ${result.second}")
            setAnalyzing(Pair(false, "${result.first} - ${result.second}"))
        }
    }
}

// Helper function to extract percentage from quality string
private fun extractPercentage(qualityStr: String): String {
    val regex = """(\d+\.?\d*)%""".toRegex()
    val matchResult = regex.find(qualityStr)
    return matchResult?.groupValues?.getOrNull(1)?.let { "$it%" } ?: "N/A"
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