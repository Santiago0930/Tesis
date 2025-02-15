package com.example.frutti

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FruitItem(val name: String, val status: String, val image: Int, val isApproved: Boolean)

class ResultsHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val sampleFruits = remember {
                mutableStateListOf(
                    FruitItem("Apple", "Fresh", R.drawable.ic_fruit, true),
                    FruitItem("Banana", "Overripe", R.drawable.ic_fruit, false),
                    FruitItem("Mango", "Good", R.drawable.ic_fruit, true)
                )
            }

            ResultsHistoryScreen(
                fruitList = sampleFruits,
                onItemClick = { /* Acción al hacer clic en un ítem */ },
                onClearHistory = { sampleFruits.clear() }
            )
        }
    }
}

@Composable
fun ResultsHistoryScreen(fruitList: List<FruitItem>, onItemClick: (FruitItem) -> Unit, onClearHistory: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.bg), // Ensure you have 'bg' in your drawable resources
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x99FFFFFF)) // Semi-transparent white overlay for readability
                .padding(16.dp)
        ) {
            Text(
                text = "Results History",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )

            // Decorative Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Check the freshness status of your detected fruits.",
                        fontSize = 14.sp,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(fruitList) { fruit ->
                    FruitListItem(fruit, onItemClick)
                    Divider(color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClearHistory,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71E20)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text(text = "Clean History", color = Color.White, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(32.dp)) // Extra space at the bottom
        }
    }
}



@Composable
fun FruitListItem(fruit: FruitItem, onItemClick: (FruitItem) -> Unit) {
    Column( // Cambiado de Row a Column para permitir más espacio
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp) // Aumentar el padding vertical para más separación
            .clickable { onItemClick(fruit) }
    ) {
        Spacer(modifier = Modifier.height(8.dp)) // Más espacio entre elementos

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        )
        {
            Image(
                painter = painterResource(id = fruit.image),
                contentDescription = fruit.name,
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = fruit.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = fruit.status, color = if (fruit.isApproved) Color.Green else Color.Red)
            }
            Image(
                painter = painterResource(id = if (fruit.isApproved) R.drawable.ic_check else R.drawable.ic_close),
                contentDescription = if (fruit.isApproved) "Approved" else "Not Approved",
                modifier = Modifier
                    .size(42.dp)
                    .padding(end = 8.dp)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = "More Info",
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp)) // Más espacio entre elementos
    }
}


@Preview(showBackground = true)
@Composable

fun PreviewResultsHistoryScreen() {
    ResultsHistoryScreen(
        fruitList = listOf(
            FruitItem("Apple", "Fresh", R.drawable.ic_fruit, true),
            FruitItem("Banana", "Overripe", R.drawable.ic_fruit, false),
            FruitItem("Mango", "Good", R.drawable.ic_fruit, true),
            FruitItem("Grapes", "Ripe", R.drawable.ic_fruit, true),
            FruitItem("Apple", "Fresh", R.drawable.ic_fruit, true),
            FruitItem("Banana", "Overripe", R.drawable.ic_fruit, false),
            FruitItem("Mango", "Good", R.drawable.ic_fruit, true),
            FruitItem("Grapes", "Ripe", R.drawable.ic_fruit, true),
            FruitItem("Orange", "Overripe", R.drawable.ic_fruit, false),
            FruitItem("Strawberry", "Fresh", R.drawable.ic_fruit, true),
            FruitItem("Pineapple", "Good", R.drawable.ic_fruit, true),
            FruitItem("Orange", "Overripe", R.drawable.ic_fruit, false),
            FruitItem("Strawberry", "Fresh", R.drawable.ic_fruit, true),
            FruitItem("Pineapple", "Good", R.drawable.ic_fruit, true),
            FruitItem("Pear", "Overripe", R.drawable.ic_fruit, false)
        ),
        onItemClick = {},
        onClearHistory = {}
    )
}


@Preview(showBackground = true)
@Composable
fun PreviewFruitListItem() {
    val sampleFruit = FruitItem("Strawberry", "Fresh", R.drawable.ic_fruit, true)
    FruitListItem(fruit = sampleFruit, onItemClick = {})
}
