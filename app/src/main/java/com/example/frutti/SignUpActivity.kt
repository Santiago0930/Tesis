package com.example.frutti

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frutti.ui.theme.FruttiTheme
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.frutti.model.Usuario
import com.example.frutti.retrofit.RetrofitService
import com.example.frutti.retrofit.UsuarioApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.withContext

class SignUpActivity : ComponentActivity() {
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
                    SignUpScreen()
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen() {
    var usuario by remember {
        mutableStateOf(Usuario())
    }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordMatchError by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Create focus references
    val (usernameFocus, emailFocus, passwordFocus, confirmPasswordFocus, ageFocus) = remember { FocusRequester.createRefs() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Check if all fields are filled
    val allFieldsFilled = usuario.nombre.isNotBlank() &&
            usuario.email.isNotBlank() &&
            usuario.password.isNotBlank() &&
            confirmPassword.isNotBlank() &&
            usuario.edad > 0 &&
            usuario.genero.isNotBlank()

    // Auto-focus username field when screen loads
    LaunchedEffect(Unit) {
        usernameFocus.requestFocus()
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
                .padding(horizontal = 33.dp, vertical = 1.dp),
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
                text = "Sign Up",
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

            // Username Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                TextField(
                    value = usuario.nombre,
                    onValueChange = { usuario = usuario.copy(nombre = it) },
                    label = { Text("Username", color = Color.Black) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { emailFocus.requestFocus() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(usernameFocus),
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

            Spacer(modifier = Modifier.height(8.dp))

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
                    label = { Text("Email", color = Color.Black) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { passwordFocus.requestFocus() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(emailFocus),
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

            Spacer(modifier = Modifier.height(8.dp))

            // Gender and Age Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Gender Field (Dropdown)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.Gray.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    val genders = listOf("Male", "Female")

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = usuario.genero,
                            onValueChange = {},
                            label = { Text("Gender", color = Color.Black) },
                            singleLine = true,
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Color.Black,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            modifier = Modifier.menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            genders.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        usuario.genero = selectionOption
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Age Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.Gray.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    TextField(
                        value = usuario.edad.toString(),
                        onValueChange = {
                            if (it.isEmpty()) {
                                usuario = usuario.copy(edad = 0)
                            } else if (it.matches(Regex("^\\d+$"))) {
                                usuario= usuario.copy(edad = it.toInt())
                            }
                        }
                        ,
                        label = { Text("Age", color = Color.Black) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { passwordFocus.requestFocus() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(ageFocus),
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

            Spacer(modifier = Modifier.height(8.dp))

            // Password Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                var passwordVisible by remember { mutableStateOf(false) }

                TextField(
                    value = usuario.password,
                    onValueChange = { usuario = usuario.copy(password = it) },
                    label = { Text("Password", color = Color.Black) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { confirmPasswordFocus.requestFocus() }
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(passwordFocus),
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

            Spacer(modifier = Modifier.height(8.dp))

            // Confirm Password Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                var confirmPasswordVisible by remember { mutableStateOf(false) }

                TextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password", color = Color.Black) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (allFieldsFilled && !passwordMatchError) {
                                Toast.makeText(context, "Todo OK, pero no hay DB", Toast.LENGTH_SHORT).show()
                                val intent = Intent(context, AnalyzeFruitActivity::class.java)
                                context.startActivity(intent)
                            }
                        }
                    ),
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(confirmPasswordFocus),
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

            if (usuario.password.isNotEmpty() && confirmPassword.isNotEmpty() && usuario.password != confirmPassword) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Error",
                        tint = Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Passwords do not match",
                        fontSize = 12.sp,
                        color = Color.Red
                    )
                }
                passwordMatchError = true
            } else {
                passwordMatchError = false
            }

            Spacer(modifier = Modifier.height(8.dp))

            val annotatedText = buildAnnotatedString {
                append("By continuing you agree to our ")

                // Terms of Service link
                pushStringAnnotation(tag = "TERMS", annotation = "https://docs.google.com/document/d/1BSwbORkGapA3NyvCHFcvWqP2IfhmnipBpVx_mbEWfJA/edit?usp=sharing")
                withStyle(style = SpanStyle(color = Color(0xFF53B175), fontWeight = FontWeight.Bold)) {
                    append("Terms of Service")
                }
                pop()

                append(" and ")

                // Privacy Policy link
                pushStringAnnotation(tag = "PRIVACY", annotation = "https://docs.google.com/document/d/1RE9ZRnGmMwDtsGQq2xhhIKfyt9YRPo_tKjeOSdDHXho/edit?usp=sharing")
                withStyle(style = SpanStyle(color = Color(0xFF53B175), fontWeight = FontWeight.Bold)) {
                    append("Privacy Policy")
                }
                pop()

                append(".")
            }

            ClickableText(
                text = annotatedText,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 3.dp),
                onClick = { offset ->
                    annotatedText.getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                            context.startActivity(intent)
                        }

                    annotatedText.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                            context.startActivity(intent)
                        }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clickable(enabled = !(!allFieldsFilled || passwordMatchError)) {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    }
            ) {
                Button(
                    onClick = {
                        val retrofitService = RetrofitService()
                        val api = retrofitService.retrofit.create(UsuarioApi::class.java)

                        if (!passwordMatchError && allFieldsFilled) {
                            Log.d("SignUpScreen", "Datos del usuario: $usuario") // 👈 Muestra en Logcat los datos del usuario

                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val response = api.registrarUsuario(usuario).execute()
                                    if (response.isSuccessful) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Usuario registrado correctamente", Toast.LENGTH_LONG).show()
                                            val intent = Intent(context, AnalyzeFruitActivity::class.java)
                                            context.startActivity(intent)
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
                        } else {
                            Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF53B175),
                        disabledContainerColor = Color(0xFFA0A0A0)
                    ),
                    shape = RoundedCornerShape(17.dp),
                    modifier = Modifier.fillMaxSize(),
                    enabled = !passwordMatchError && allFieldsFilled
                ) {
                    Text(
                        text = "Sign Up",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = {
                val intent = Intent(context, LoginActivity::class.java)
                context.startActivity(intent)
            }) {
                Text(
                    text = "Already have an account? Login",
                    fontSize = 14.sp,
                    color = Color(0xFF53B175)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    FruttiTheme {
        SignUpScreen()
    }
}