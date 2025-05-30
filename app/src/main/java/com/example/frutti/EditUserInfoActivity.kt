package com.example.frutti

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.frutti.model.Usuario
import com.example.frutti.model.UsuarioUpdate
import com.example.frutti.retrofit.RetrofitService
import com.example.frutti.retrofit.UsuarioApi
import com.example.frutti.ui.theme.FruttiTheme
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditUserInfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            FruttiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EditUserInfoScreen()
                }
            }
        }
    }
}

@Composable
fun EditUserInfoScreen() {
    var showPasswordScreen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    val navigateBackToHome = {
        activity?.finish() // Simplemente cierra la actividad actual
    }


    // Background Image
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = "Background",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.7f),
            contentScale = ContentScale.Crop
        )

        // Content laid out on top of the background
        if (showPasswordScreen) {
            ChangePasswordScreen(onBack = { showPasswordScreen = false })
        } else {
            EditProfileScreen(
                onPasswordChangeClick = { showPasswordScreen = true },
                onBackClick = navigateBackToHome as () -> Unit // Pass the navigation action
            )
        }
    }
}

@Composable
fun EditProfileScreen(
    onPasswordChangeClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val usuarioJson = sharedPref.getString("usuario_guardado", null)
    val usuario = Gson().fromJson(usuarioJson, Usuario::class.java)

    var username by remember { mutableStateOf(usuario?.nombre ?: "") }
    var email by remember { mutableStateOf(usuario?.email ?: "") }
    var age by remember { mutableStateOf(usuario?.edad?.toString() ?: "") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 33.dp, vertical = 44.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Add space above the image
            Spacer(modifier = Modifier.height(32.dp)) // Adjust this value as needed

            Image(
                painter = painterResource(id = R.drawable.lococolor),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopCenter)
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Edit Profile",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Username Field
        CustomField(
            value = username,
            onValueChange = { username = it },
            label = "Username"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email Field
        CustomField(
            value = email,
            onValueChange = { email = it },
            label = "Email"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Age Field
        CustomField(
            value = age,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*$"))) {
                    age = newValue
                }
            },
            label = "Age"
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Save Changes Button
        Button(
            onClick = {
                val retrofitService = RetrofitService()
                val api = retrofitService.retrofit.create(UsuarioApi::class.java)

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val usuarioUpdate = UsuarioUpdate(
                            nombre = username,
                            email = email,
                            // Only update age if the new value is a valid integer
                            edad = age.toIntOrNull() ?: usuario?.edad ?: 0 // Provide a default if usuario or edad is null
                        )

                        // Ensure usuario and usuario.id are not null before making the API call
                        if (usuario != null && usuario.id != null) {
                            val response = api.actualizarUsuario(usuario.id, usuarioUpdate).execute()
                            if (response.isSuccessful) {
                                // Update the local usuario object and SharedPreferences
                                usuario.nombre = username
                                usuario.email = email
                                usuario.edad = age.toIntOrNull() ?: usuario.edad

                                with(sharedPref.edit()) {
                                    putString("usuario_guardado", Gson().toJson(usuario))
                                    apply()
                                }

                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Update failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "User data not available", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF53B175),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Save,
                contentDescription = "Save Changes Icon",
                modifier = Modifier.size(24.dp).padding(end = 4.dp)
            )
            Text("Save Changes", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Change Password Button
        Button(
            onClick = onPasswordChangeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Change Password Icon",
                modifier = Modifier.size(24.dp).padding(end = 4.dp)
            )
            Text("Change Password", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Delete Account Button
        Button(
            onClick = { showDeleteDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF44336),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete Account Icon",
                modifier = Modifier.size(24.dp).padding(end = 4.dp)
            )
            Text("Delete Account", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cancel Button (Text Only, Red)
        TextButton(
            onClick = onBackClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Cancel,
                contentDescription = "Cancel Icon",
                tint = Color.Red, // Set icon tint to red
                modifier = Modifier.size(24.dp).padding(end = 4.dp)
            )
            Text("Cancel", fontSize = 18.sp, color = Color.Red) // Set text color to red
        }


        // Delete Account Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirm Deletion") },
                text = {
                    Column {
                        Text("This will permanently delete your account.")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = deletePassword,
                            onValueChange = { deletePassword = it },
                            label = { Text("Enter your password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Ensure usuario is not null before accessing its properties
                            if (usuario?.password == deletePassword) {
                                val retrofitService = RetrofitService()
                                val api = retrofitService.retrofit.create(UsuarioApi::class.java)

                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        // Ensure usuario.id is not null before making the API call
                                        if (usuario != null && usuario.id != null) {
                                            val response = api.eliminarUsuario(usuario.id).execute()
                                            if (response.isSuccessful) {
                                                with(sharedPref.edit()) {
                                                    clear()
                                                    apply()
                                                }
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                                                    context.startActivity(Intent(context, LoginActivity::class.java))
                                                    (context as? ComponentActivity)?.finish()
                                                }
                                            } else {
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(context, "Deletion failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } else {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "User data not available for deletion", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Delete", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val usuarioJson = sharedPref.getString("usuario_guardado", null)
    val usuario = Gson().fromJson(usuarioJson, Usuario::class.java)

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val passwordMismatchError = newPassword.isNotEmpty() &&
            confirmPassword.isNotEmpty() &&
            newPassword != confirmPassword

    val passwordLengthError = (newPassword.isNotEmpty() && newPassword.length < 6) ||
            (confirmPassword.isNotEmpty() && confirmPassword.length < 6) ||
            (currentPassword.isNotEmpty() && currentPassword.length < 6)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 33.dp, vertical = 44.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.lococolor),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopCenter)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Change Password",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Current Password
        PasswordField(
            value = currentPassword,
            onValueChange = { currentPassword = it },
            label = "Current Password",
            visible = currentPasswordVisible,
            onVisibilityToggle = { currentPasswordVisible = !currentPasswordVisible }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // New Password
        PasswordField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = "New Password",
            visible = newPasswordVisible,
            onVisibilityToggle = { newPasswordVisible = !newPasswordVisible }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password
        PasswordField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm Password",
            visible = confirmPasswordVisible,
            onVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible }
        )

        if (passwordMismatchError) {
            Text(
                text = "Passwords don't match",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.Start)
            )
        }

        if (passwordLengthError) {
            Text(
                text = "Passwords must be at least 6 characters",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Save Button
        Button(
            onClick = {
                Log.d("usuario1", "Datos del usuario: $usuario")
                if (passwordMismatchError || passwordLengthError) return@Button

                // Ensure usuario is not null before accessing its properties
                if (usuario?.password != currentPassword) {
                    Toast.makeText(context, "Current password is incorrect", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val retrofitService = RetrofitService()
                val api = retrofitService.retrofit.create(UsuarioApi::class.java)

                Log.d("SignUpScreen", "Datos del usuario antiguo: $usuario")
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (currentPassword != usuario.password) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Incorrect current password", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            if (newPassword.isNotBlank()) {
                                usuario.password = newPassword
                            }
                        }
                        Log.d("SignUpScreen", "Datos del usuario nuevo: $usuario")

                        val response = api.actualizarContraseña(usuario.id, newPassword).execute()
                        if (response.isSuccessful) {
                            with(sharedPref.edit()) {
                                putString("usuario_guardado", Gson().toJson(usuario))  // Guarda el usuario como JSON
                                apply()
                            }
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Contraseña actualizada correctamente", Toast.LENGTH_LONG).show()
                                onBack()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Excepción: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF53B175),
                contentColor = Color.White
            ),
            enabled = !passwordMismatchError &&
                    !passwordLengthError &&
                    currentPassword.length >= 6 &&
                    newPassword.length >= 6 &&
                    confirmPassword.length >= 6
        ) {
            Icon(
                imageVector = Icons.Filled.Save,
                contentDescription = "Save Password Icon",
                modifier = Modifier.size(24.dp).padding(end = 4.dp)
            )
            Text("Save New Password", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cancel Button (Text Only, Red)
        TextButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Cancel,
                tint = Color.Red,
                contentDescription = "Cancel Icon",
                modifier = Modifier.size(24.dp).padding(end = 4.dp)
            )
            Text("Cancel", fontSize = 18.sp, color = Color.Red)
        }
    }
}


@Composable
fun CustomField(value: String, onValueChange: (String) -> Unit, label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = Color.Black) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
    }
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onVisibilityToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = Color.Black) },
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onVisibilityToggle) {
                    Icon(
                        imageVector = if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditUserInfoScreenPreview() {
    FruttiTheme {
        EditUserInfoScreen()
    }
}