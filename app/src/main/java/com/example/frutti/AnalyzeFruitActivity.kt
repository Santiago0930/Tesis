package com.example.frutti

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            .background(Color.White) // Fondo blanco
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(1.dp))

            // Título
            Text(
                text = "Analyze a Fruit",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            // Botón superior
            CustomButton(text = "Go to Results History", onClick = {})

            // Imagen en el centro
            Image(
                painter = painterResource(id = R.drawable.lococolor), // Cambia esto por el ID correcto de la imagen
                contentDescription = "Analyze Icon",
                modifier = Modifier.size(200.dp)
            )

            // Botón "Open Camera"
            CustomButton(text = "Open camera", onClick = {})

            // Botón "Upload Image"
            CustomButton(text = "Upload image", onClick = {})

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun CustomButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF53B175)), // Verde
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(50.dp)
    ) {
        Text(text = text, fontSize = 18.sp, color = Color.White)
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyzeFruitPreview() {
    FruttiTheme {
        AnalyzeFruitScreen()
    }
}
