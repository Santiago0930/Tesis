package com.example.frutti

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.example.frutti.EditUserInfoActivity
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.frutti.model.Usuario
import com.google.gson.Gson


class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HomeScreen(username = "John Doe")
            MainNavigation()
        }
    }

    private var backPressedOnce = false

    override fun onBackPressed() {
        if (backPressedOnce) {
            super.onBackPressed() // Allows exit only on the second press
            return
        }

        this.backPressedOnce = true
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()

        // Reset after 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            backPressedOnce = false
        }, 2000)
    }
}

val dailyTips = listOf(
    "Eat seasonal fruits for maximum freshness and nutrition!",
    "Wash fruits thoroughly before eating to remove pesticides.",
    "Frozen fruits retain most nutrients and are great for smoothies!",
    "The brighter the fruit color, the more antioxidants it usually contains.",
    "Eating fruits with skins provides more fiber and nutrients.",
    "Try to eat at least 2-3 different colored fruits each day.",
    "Fruits make excellent natural snacks between meals.",
    "Pair fruits with nuts for a balanced, energy-boosting snack."
)
@Composable
fun HomeScreen(username: String = "Guest") {
    val randomTip = remember { dailyTips.random() }
    val scrollState = rememberScrollState()
    val bottomBarHeight = 56.dp // Standard bottom navigation height
    val context = LocalContext.current

    val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val usuarioJson = sharedPref.getString("usuario_guardado", null)
    val usuario = Gson().fromJson(usuarioJson, Usuario::class.java)


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background Image
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.bg),
                contentDescription = "Background",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = 0.3f),
                contentScale = ContentScale.Crop
            )
        }

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = bottomBarHeight) // Reserve space for bottom bar
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Welcome title with buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Log Out Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = {
                            // Clear user session and navigate to login
                            sharedPref.edit().clear().apply()
                            val intent = Intent(context, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color(0xFFE53935).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ExitToApp,
                            contentDescription = "Log Out",
                            tint = Color(0xFFE53935)
                        )
                    }
                    Text(
                        text = "Log Out",
                        fontSize = 12.sp,
                        color = Color(0xFFE53935)
                    )
                }

                Text(
                    text = "Welcome, " + usuario.nombre +" 👋",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF1E88E5),
                    modifier = Modifier.weight(1f)
                )

                // Edit Profile Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = {
                            val intent = Intent(context, EditUserInfoActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color(0xFF1E88E5).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Edit Profile",
                            tint = Color(0xFF1E88E5)
                        )
                    }
                    Text(
                        text = "Edit",
                        fontSize = 12.sp,
                        color = Color(0xFF1E88E5)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Fruits Analyzed 🍎🍌🍊",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "35",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E88E5),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Keep analyzing to improve your health!",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Markets section
            Text(
                text = "Best Markets for Fruits 🛒",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E88E5),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Markets list
            val markets = listOf(
                "XYZ Market" to Pair("Best Quality", "\$2.50"),
                "ABC Store" to Pair("Affordable", "\$1.80"),
                "FreshMart" to Pair("Organic", "\$3.00")
            )

            markets.forEach { (market, details) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = market,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                            Text(
                                text = details.first,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        Text(
                            text = details.second,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E88E5)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Daily tip card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💡 Tip of the Day:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF57C00),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = randomTip,
                        fontSize = 16.sp,
                        color = Color(0xFFF57C00)
                    )
                }
            }

            // Extra spacer to ensure content doesn't get cut off
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Vista previa
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(username = "John Doe")
    MainNavigation()
}