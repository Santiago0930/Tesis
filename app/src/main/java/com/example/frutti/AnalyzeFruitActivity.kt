package com.example.frutti

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frutti.ui.theme.FruttiTheme

class AnalyzeFruitActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FruttiTheme {
                AnalyzeFruitScreen()
            }
        }
    }
}

@Composable
fun AnalyzeFruitScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF53B175), Color(0xFF2E7D32))
                )
            )
            .padding(11.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround // Ajuste para balance
        ) {
            Spacer(modifier = Modifier.height(11.dp)) // Más espacio arriba

            // Título con sombreado
            Text(
                text = "Analyze a Fruit",
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.shadow(124.dp)
            )

            CustomButton(
                text = "Go to Results History",
                icon = Icons.Filled.History,
                onClick = {}
            )

            Box(
                modifier = Modifier
                    .size(220.dp)
                    .background(Color.White, shape = CircleShape)
                    .border(4.dp, Color(0xFF53B175), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.lococolor),
                    contentDescription = "Analyze Icon",
                    modifier = Modifier.size(200.dp)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(6.dp, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .animateContentSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Capture or upload an image to analyze the freshness of your fruit!",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }

            CustomButton(
                text = "Open Camera",
                icon = Icons.Filled.CameraAlt,
                onClick = {}
            )

            CustomButton(
                text = "Upload Image",
                icon = Icons.Filled.Upload,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(50.dp)) // Más espacio vacío al final
        }
    }
}

@Composable
fun CustomButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF53B175)),
        shape = RoundedCornerShape(17.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(50.dp)
            .shadow(4.dp, shape = RoundedCornerShape(17.dp))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 18.sp, color = Color.White)
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
