package com.example.frutti

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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

data class FruitItem(val name: String, val status: String, val image: Int, val isApproved: Boolean)

class ResultsHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
        }
    }
}

@Composable
fun ResultsHistoryScreen() {

    val context = LocalContext.current
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
                val response = frutaApi.obtenerHistorialFrutas(usuario.id).execute()
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
                items(frutas) { fruta ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = fruta.nombre,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1565C0)
                                )
                                Text(text = "Estado: ${fruta.estado}", fontSize = 14.sp, color = Color.DarkGray)
                                Text(text = "Peso: ${fruta.peso} gm", fontSize = 14.sp, color = Color.DarkGray)
                                Text(text = "Precio: $${fruta.precio}", fontSize = 14.sp, color = Color.DarkGray)
                                Text(text = "Lugar: ${fruta.lugarAnalisis}", fontSize = 14.sp, color = Color.DarkGray)
                                Text(text = "Fecha: ${fruta.fechaAnalisis}", fontSize = 12.sp, color = Color.Gray)
                            }

                            IconButton(
                                onClick = { CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val eliminarResponse = frutaApi.eliminarFruta(fruta.id, usuario.id).execute()
                                        if (eliminarResponse.isSuccessful) {

                                            // 1. Actualizar lista de frutas en UI
                                            withContext(Dispatchers.Main) {
                                                frutas = frutas.filterNot { it.id == fruta.id }
                                                Toast.makeText(context, "Fruta eliminada", Toast.LENGTH_SHORT).show()
                                            }

                                            // 2. Obtener el usuario actualizado
                                            val usuarioResponse = retrofitService.retrofit
                                                .create(UsuarioApi::class.java).
                                                obtenerUsuario(usuario.email).execute()
                                            if (usuarioResponse.isSuccessful) {
                                                val usuarioActualizado = usuarioResponse.body()
                                                if (usuarioActualizado != null) {
                                                    withContext(Dispatchers.Main) {
                                                        // Guardar en SharedPreferences
                                                        sharedPref.edit()
                                                            .putString("usuario_guardado", Gson().toJson(usuarioActualizado))
                                                            .apply()
                                                    }
                                                }
                                            }

                                        } else {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "Error al eliminar fruta", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Excepción: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(start = 8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_close), // Asegúrate de tener este ícono
                                    contentDescription = "Eliminar fruta",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val eliminarResponse = frutaApi.eliminarHistorial(usuario.id).execute()
                            if (eliminarResponse.isSuccessful) {
                                // Luego de borrar, pedimos el usuario actualizado
                                val usuarioResponse = retrofitService.retrofit
                                    .create(UsuarioApi::class.java)
                                    .obtenerUsuario(usuario.email)
                                    .execute()

                                if (usuarioResponse.isSuccessful) {
                                    val usuarioActualizado = usuarioResponse.body()

                                    withContext(Dispatchers.Main) {
                                        frutas = emptyList()

                                        // Guardamos el usuario actualizado
                                        if (usuarioActualizado != null) {
                                            sharedPref.edit()
                                                .putString("usuario_guardado", Gson().toJson(usuarioActualizado))
                                                .apply()
                                        }

                                        Toast.makeText(context, "Historial eliminado", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Historial eliminado, pero no se pudo actualizar usuario", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Error al eliminar historial", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Excepción: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                ,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71E20)),
                shape = RoundedCornerShape(17.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(50.dp)
            ) {
                Text(text = "Clean History", color = Color.White, fontSize = 18.sp)
            }



            Spacer(modifier = Modifier.height(32.dp)) // Extra space at the bottom
        }
    }
}


@Composable
fun FruitListItem(fruit: FruitItem, onItemClick: (FruitItem) -> Unit) {
    val context = LocalContext.current

    Column( // Cambiado de Row a Column para permitir más espacio
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp) // Aumentar el padding vertical para más separación
            .clickable { navigateToFruitDetail(
                context = context,
                fruitName = fruit.name,
                ripeness = fruit.status
            )
            }
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
fun PreviewFruitListItem() {
    val sampleFruit = FruitItem("Strawberry", "Fresh", R.drawable.ic_fruit, true)
    FruitListItem(fruit = sampleFruit, onItemClick = {})
}
