package com.example.frutti

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frutti.ui.theme.FruttiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FruttiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WelcomeScreen()
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen() {
    val context = LocalContext.current
    var backPressedOnce by remember { mutableStateOf(false) }
    val handler = remember { Handler(Looper.getMainLooper()) }

    // Handle Back Button Press
    BackHandler {
        if (backPressedOnce) {
            (context as? ComponentActivity)?.finish() // Exit App
        } else {
            backPressedOnce = true
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()

            // Reset after 2 seconds
            handler.postDelayed({ backPressedOnce = false }, 2000)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.fruit_image1),
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Content aligned at the bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
            ) {
                // App Icon
                Image(
                    painter = painterResource(id = R.drawable.fruit_icon),
                    contentDescription = "Fruit Icon",
                    modifier = Modifier.size(120.dp)
                )

                // Main Text
                Text(
                    text = "Welcome to the freshness",
                    fontSize = 38.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 44.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Subtitle
                Text(
                    text = "Check the quality of your fruits",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Login Button
                Button(
                    onClick = {
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF53B175)),
                    shape = RoundedCornerShape(17.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 50.dp)
                        .height(60.dp)
                ) {
                    Text(text = "Get Started", fontSize = 22.sp, color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    FruttiTheme {
        WelcomeScreen()
    }
}
