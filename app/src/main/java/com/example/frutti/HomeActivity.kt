package com.example.frutti

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.example.frutti.model.Fruta
import com.example.frutti.model.Usuario
import com.example.frutti.retrofit.FrutaApi
import com.example.frutti.retrofit.RetrofitService
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HomeScreen(username = "John Doe")
            MainNavigation()
        }
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
    "Pair fruits with nuts for a balanced, energy-boosting snack.",
    "Stay hydrated by eating water-rich fruits like watermelon and cucumber.",
    "Citrus fruits like oranges and lemons boost your immune system.",
    "Bananas are a great source of potassium and help maintain heart health.",
    "Apples contain pectin, a fiber that promotes gut health.",
    "Berries are loaded with antioxidants that support brain function.",
    "Avocados are technically fruits and are packed with healthy fats.",
    "Pineapple contains bromelain, an enzyme that aids digestion.",
    "Grapes can support heart health thanks to their polyphenols.",
    "Mangoes are rich in vitamin A, which is vital for eye health.",
    "Cherries may help reduce inflammation and muscle soreness.",
    "Kiwi offers more vitamin C per serving than oranges!",
    "Papaya contains enzymes that help with protein digestion."
)

@Composable
fun HomeScreen(username: String = "Guest") {
    val randomTip = remember { dailyTips.random() }
    val scrollState = rememberScrollState()
    val bottomBarHeight = 56.dp
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    // State for exit confirmation dialog
    var showExitDialog by remember { mutableStateOf(false) }
    var backPressedTime by remember { mutableStateOf(0L) }

    val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val usuarioJson = sharedPref.getString("usuario_guardado", null)
    val usuario = Gson().fromJson(usuarioJson, Usuario::class.java)

    var frutas by remember{
        mutableStateOf<List<Fruta>>(emptyList())
    }

    // Estado para lista de frutas
    val retrofitService = RetrofitService()
    val frutaApi = retrofitService.retrofit.create(FrutaApi::class.java)

    // Cargar frutas desde el backend
    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = frutaApi.listarFrutas().execute()
                if (response.isSuccessful) {
                    val lista = response.body() ?: emptyList()
                    Log.d("HomeScreen", "Frutas obtenidas: ${lista.joinToString { it.nombre }}")

                    withContext(Dispatchers.Main) {
                        frutas = lista
                    }
                } else {
                    Log.e("HomeScreen", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("HomeScreen", "Excepción al listar frutas: ${e.localizedMessage}")
            }
        }
    }

    // Handle back press
    BackHandler {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            showExitDialog = true
        } else {
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            backPressedTime = System.currentTimeMillis()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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

        // Main Content (your existing content remains unchanged)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = bottomBarHeight)
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            // Clear preferences but keep last shown timestamp
                            val lastShown = sharedPref.getLong("photo_instructions_last_shown", 0L)
                            GlobalStates.showPhotoInstructions.value = true

                            sharedPref.edit().clear().apply()

                            sharedPref.edit()
                                .putLong("photo_instructions_last_shown", lastShown)
                                .putBoolean("photo_instructions_shown_this_session", false) // Reset dialog flag
                                .apply()

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
                    Text(text = "Log Out", fontSize = 12.sp, color = Color(0xFFE53935))
                }


                Text(
                    text = "Welcome, ${usuario?.nombre ?: "Guest"} 👋",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF1E88E5),
                    modifier = Modifier.weight(1f)
                )

                // Edit Profile Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            context.startActivity(Intent(context, EditUserInfoActivity::class.java))
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
                    Text(text = "Edit", fontSize = 12.sp, color = Color(0xFF1E88E5))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Fruits Analyzed Card
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
                        text = usuario.frutasAnalizadas.toString(),
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
                text = "Best Analyzed Fruits 🍓",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E88E5),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Lista de frutas analizadas
            frutas.forEach { fruta ->
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
                                text = fruta.nombre,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                            Text(
                                text = "State: ${fruta.estado}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "Location: ${fruta.lugarAnalisis}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        Text(
                            text = "$${fruta.precio}",
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

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Exit Confirmation Dialog
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Exit App") },
                text = { Text("Are you sure you want to exit?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            activity?.finishAffinity() // Close all activities
                        }
                    ) {
                        Text("EXIT", color = Color(0xFFE53935))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showExitDialog = false }
                    ) {
                        Text("CANCEL")
                    }
                },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(username = "John Doe")
}