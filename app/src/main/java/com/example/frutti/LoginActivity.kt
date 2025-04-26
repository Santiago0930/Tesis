package com.example.frutti

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.addCallback
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frutti.model.LoginRequest
import com.example.frutti.model.Usuario
import com.example.frutti.retrofit.AuthApi
import com.example.frutti.retrofit.RetrofitService
import com.example.frutti.retrofit.UsuarioApi
import com.example.frutti.ui.theme.FruttiTheme
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prevent back navigation
        onBackPressedDispatcher.addCallback(this) {
            // Do nothing, effectively disabling the back button
        }

        setContent {
            FruttiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen()
                }
            }
        }
    }
}

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    var usuario by remember {
        mutableStateOf(LoginRequest())
    }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Create focus references
    val (emailFocus, passwordFocus) = remember { FocusRequester.createRefs() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto-focus email field when screen loads
    LaunchedEffect(Unit) {
        emailFocus.requestFocus()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 33.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.lococolor),
                contentDescription = "App Logo",
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Log In",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Enter your credentials",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Email Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                TextField(
                    value = usuario.email,
                    onValueChange = { usuario = usuario.copy(email = it) },
                    label = { Text("Email", color = Color.Gray) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { passwordFocus.requestFocus() }
                    ),
                    textStyle = TextStyle(color = Color.Black), // Set text color to black
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(emailFocus),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.Black,
                        focusedTextColor = Color.Black, // Ensure focused text is black
                        unfocusedTextColor = Color.Black // Ensure unfocused text is black
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Password Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                TextField(
                    value = usuario.password,
                    onValueChange = { usuario = usuario.copy(password = it) },
                    label = { Text("Password", color = Color.Gray) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (usuario.email.isNotEmpty() && usuario.password.isNotEmpty()) {
                                navigateToActivity(context, AnalyzeFruitActivity::class.java)
                            }
                        }
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    textStyle = TextStyle(color = Color.Black), // Set text color to black
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(passwordFocus),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.Black,
                        focusedTextColor = Color.Black, // Ensure focused text is black
                        unfocusedTextColor = Color.Black // Ensure unfocused text is black
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Forgot Password
            TextButton(
                onClick = { showToast(context, "Coming Soon") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Forgot Password?",
                    fontSize = 12.sp,
                    color = Color(0xFFA82C2C),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Log In Button
            Button(
                onClick = {
                    val retrofitService = RetrofitService()
                    val api = retrofitService.retrofit.create(AuthApi::class.java)
                    if (usuario.email.isEmpty() || usuario.password.isEmpty()) {
                        showToast(context, "Please fill in all fields")
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val response = api.login(usuario).execute()
                                if (response.isSuccessful) {
                                    val api2 = retrofitService.retrofit.create(UsuarioApi::class.java)
                                    val usuarioStorage = api2.obtenerUsuario(usuario.email).execute();
                                    Log.d("LoginScreen", "Datos del usuario: ${usuarioStorage.body()}")
                                    val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                                    with(sharedPref.edit()) {
                                        putString("usuario_guardado", Gson().toJson(usuarioStorage.body()))  // Guarda el usuario como JSON
                                        apply()
                                    }
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Welcome!", Toast.LENGTH_LONG).show()
                                        val intent = Intent(context, AnalyzeFruitActivity::class.java)
                                        context.startActivity(intent)
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Error: Invalid email or password. Please try again.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Excepción: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF53B175)),
                shape = RoundedCornerShape(17.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = "Log In", fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign Up Button
            TextButton(onClick = { navigateToActivity(context, SignUpActivity::class.java) }) {
                Text(
                    text = "Don't have an account? Sign Up",
                    fontSize = 14.sp,
                    color = Color(0xFF53B175)
                )
            }
        }
    }
}

// Function to show toast messages
fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

// Function to navigate to another activity
fun navigateToActivity(context: Context, destination: Class<*>) {
    val intent = Intent(context, destination)
    context.startActivity(intent)

    // Finish LoginActivity so the user cannot go back to it
    if (context is ComponentActivity) {
        context.finish()
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    FruttiTheme {
        LoginScreen()
    }
}