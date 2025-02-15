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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frutti.ui.theme.FruttiTheme

class ResultxActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FruttiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    ResultxScreen()
                }
            }
        }
    }
}

@Composable
fun ResultxScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Contenedor para imágenes apiladas
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(322.dp) // Container size
        ) {
            Image(
                painter = painterResource(id = R.drawable.confetti), // Background confetti image
                contentDescription = "Confetti",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Image(
                painter = painterResource(id = R.drawable.ic_close), // Checkmark image
                contentDescription = "Check",
                modifier = Modifier
                    .size(150.dp)
                    .offset(y = 20.dp) // Moves the check image downward
            )
        }


        Spacer(modifier = Modifier.height(84.dp))

        // Texto principal
        Text(
            text = "The Quality of your fruit has been checked",
            fontSize = 27.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Texto de advertencia
        Text(
            text = "Please keep in mind that results are not 100% accurate",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(44.dp))

        // Botón
        Button(
            onClick = { /* Acción al hacer clic */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71E20)),
            shape = RoundedCornerShape(12.dp), // Bordes redondeados
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Results",
                fontSize = 20.sp,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResultxScreenPreview() {
    FruttiTheme {
        ResultxScreen()
    }
}
