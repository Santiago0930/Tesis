package com.example.frutti

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

class FruitDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FruitDetailScreen(fruitName = "Apple")
        }
    }
}

@Composable
fun FruitDetailScreen(fruitName: String) {
    var store by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(3) }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Fondo claro
    ) {
        // Imagen de la fruta
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFFE3F2FD)), // Color de fondo temporal
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_fruit),
                contentDescription = fruitName,
                modifier = Modifier.size(100.dp),
                tint = Color(0xFF1E88E5)
            )
        }

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
            Text(
                text = fruitName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E88E5),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Detalles de la fruta
            Card(
                modifier = Modifier.fillMaxWidth().shadow(8.dp, shape = RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CustomTextField(label = "Store", value = store) { store = it }
                    CustomTextField(label = "Purchase Price", value = price, isNumeric = true) { price = it }
                    CustomTextField(label = "Weight (grams)", value = weight, isNumeric = true) { weight = it }
                }
            }

            // Calificación de sabor
            Card(
                modifier = Modifier.fillMaxWidth().shadow(8.dp, shape = RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Flavor Rating",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E88E5)
                    )
                    StarRating(rating) { newRating -> rating = newRating }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Botón de guardar con efecto moderno
            Button(
                onClick = { /* Acción al hacer clic */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF53B175)),
                shape = RoundedCornerShape(12.dp), // Bordes redondeados
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 24.dp)
            ) {
                Text(text = "Save",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(label: String, value: String, isNumeric: Boolean = false, onValueChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)) // Fondo sutil
            .padding(4.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = Color.Black) },
            keyboardOptions = KeyboardOptions(keyboardType = if (isNumeric) KeyboardType.Number else KeyboardType.Text),
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = Color.Black),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color.Transparent, // Sin fondo
                focusedBorderColor = Color.Transparent, // Sin borde al enfocar
                unfocusedBorderColor = Color.Transparent, // Sin borde cuando no está enfocado
                cursorColor = Color.Black
            )
        )
    }
}





@Composable
fun StarRating(rating: Int, onRatingChanged: (Int) -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            for (i in 1..5) {
                val scale by animateFloatAsState(
                    targetValue = if (i <= rating) 1.3f else 1f,
                    animationSpec = tween(durationMillis = 300)
                )

                IconButton(
                    onClick = { onRatingChanged(i) },
                    modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
                ) {
                    Icon(
                        painter = painterResource(id = if (i <= rating) R.drawable.star else R.drawable.star_outline),
                        contentDescription = "Star Rating",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewFruitDetailScreen() {
    FruitDetailScreen(fruitName = "Banana")
}
