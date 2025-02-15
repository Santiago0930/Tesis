package com.example.frutti

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.fruit_image1), // Verifica que esta imagen exista en res/drawable
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Contenido alineado en la parte inferior
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter // Alinea todo el contenido en la parte inferior
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp), // Espaciado entre elementos
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp) // Ajusta el padding inferior para darle más margen
            ) {
                // Icono de la aplicación
                Image(
                    painter = painterResource(id = R.drawable.fruit_icon), // Verifica que esta imagen exista en res/drawable
                    contentDescription = "Fruit Icon",
                    modifier = Modifier.size(100.dp)
                )

                // Texto principal
                Text(
                    text = "Welcome to the freshness",
                    fontSize = 28.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Subtítulo
                Text(
                    text = "Check the quality of your fruits",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Botón estilizado
                Button(
                    onClick = {
                        // Acción al hacer clic en el botón
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF53B175)),
                    shape = RoundedCornerShape(17.dp), // Bordes redondeados
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 50.dp)
                        .height(50.dp)
                ) {
                    Text(
                        text = "Get Started",
                        fontSize = 18.sp,
                        color = Color.White
                    )
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
