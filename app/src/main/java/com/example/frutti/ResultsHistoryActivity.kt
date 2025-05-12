package com.example.frutti

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frutti.model.Fruta
import com.example.frutti.model.Usuario
import com.example.frutti.retrofit.FrutaApi
import com.example.frutti.retrofit.RetrofitService
import com.example.frutti.retrofit.UsuarioApi
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResultsHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ResultsHistoryScreen()
        }
    }
}

@Composable
fun ResultsHistoryScreen() {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val usuarioJson = sharedPref.getString("usuario_guardado", null)
    val usuario = Gson().fromJson(usuarioJson, Usuario::class.java)

    var frutas by remember { mutableStateOf<List<Fruta>>(emptyList()) }
    val retrofitService = RetrofitService()
    val frutaApi = retrofitService.retrofit.create(FrutaApi::class.java)
    val usuarioApi = retrofitService.retrofit.create(UsuarioApi::class.java)

    // Load frutas
    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = frutaApi.obtenerHistorialFrutas(usuario.id).execute()
                if (response.isSuccessful) {
                    response.body()?.let { lista ->
                        withContext(Dispatchers.Main) {
                            frutas = lista
                        }
                    }
                } else {
                    Log.e("ResultsScreen", "Error ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ResultsScreen", "Error: ${e.localizedMessage}")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.65f))
                .padding(16.dp)
        ) {
            Text(
                text = "Results History",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(10.dp))

            //Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(frutas) { fruta ->
                    FruitCard(fruta, usuario, frutaApi, usuarioApi, sharedPref, context) {
                        frutas = frutas.filterNot { it.id == fruta.id }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val eliminar = frutaApi.eliminarHistorial(usuario.id).execute()
                            if (eliminar.isSuccessful) {
                                val userUpdated = usuarioApi.obtenerUsuario(usuario.email).execute()
                                withContext(Dispatchers.Main) {
                                    frutas = emptyList()
                                    userUpdated.body()?.let {
                                        sharedPref.edit()
                                            .putString("usuario_guardado", Gson().toJson(it))
                                            .apply()
                                    }
                                    Toast.makeText(context, "Historial eliminado", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Clean History", fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun FruitCard(
    fruta: Fruta,
    usuario: Usuario,
    frutaApi: FrutaApi,
    usuarioApi: UsuarioApi,
    sharedPref: SharedPreferences,
    context: Context,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(24.dp))
            .animateContentSize()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

// 🖼️ Imagen o ícono de fruta
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_fruit), // Replace with fruta.imagenUrl if available
                    contentDescription = "Fruta",
                    tint = Color(0xFF66BB6A),
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            // 🧾 Detalles de la fruta
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        fruta.nombre,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // 🏷️ Badge para estado
                    Text(
                        text = fruta.estado,
                        fontSize = 12.sp,
                        color = Color.White,
                        modifier = Modifier
                            .background(
                                when (fruta.estado) {
                                    "Good" -> Color(0xFF43A047)
                                    "Bad" -> Color(0xFFF44336)
                                    else -> Color.Gray
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "💰 $${fruta.precio}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE64A19)
                )

                Spacer(modifier = Modifier.height(8.dp))

                InfoLine("⚖️ Peso", "${fruta.peso}g")
                InfoLine("📍 Lugar", fruta.lugarAnalisis)
                InfoLine("📅 Fecha", fruta.fechaAnalisis.toString(), fontSize = 12.sp)
            }

            // 🗑️ Botón de eliminar con animación y ripple
            IconButton(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val eliminar = frutaApi.eliminarFruta(fruta.id, usuario.id).execute()
                            if (eliminar.isSuccessful) {
                                val userUpdated = usuarioApi.obtenerUsuario(usuario.email).execute()
                                withContext(Dispatchers.Main) {
                                    onDelete()
                                    userUpdated.body()?.let {
                                        sharedPref.edit()
                                            .putString("usuario_guardado", Gson().toJson(it))
                                            .apply()
                                    }
                                    Toast.makeText(context, "🍓 Fruta eliminada", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "⚠️ No se pudo eliminar", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "💥 Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEBEE))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar fruta",
                    tint = Color.Red
                )
            }
        }
    }
}

@Composable
fun InfoLine(label: String, value: String, fontSize: TextUnit = 14.sp, color: Color = Color.DarkGray) {
    Text(
        text = "$label: $value",
        fontSize = fontSize,
        color = color,
        modifier = Modifier.padding(vertical = 1.dp)
    )
}
