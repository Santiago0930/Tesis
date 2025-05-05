package com.example.frutti

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
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
import com.example.frutti.model.Usuario
import com.example.frutti.retrofit.RetrofitService
import com.example.frutti.retrofit.UsuarioApi
import com.example.frutti.ui.theme.FruttiTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log
import com.google.gson.Gson
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
    var usuario by remember { mutableStateOf(Usuario()) }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordMatchError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var ageError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Create focus references
    val (usernameFocus, emailFocus, passwordFocus, confirmPasswordFocus, ageFocus) = remember {
        FocusRequester.createRefs()
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Validation functions
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPasswordStrong(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidAge(age: Int): Boolean {
        return age in 10..120
    }

    // Check if all fields are filled and valid
    val allFieldsValid = usuario.nombre.isNotBlank() &&
            usuario.email.isNotBlank() && isValidEmail(usuario.email) &&
            usuario.password.isNotBlank() && isPasswordStrong(usuario.password) &&
            confirmPassword.isNotBlank() &&
            isValidAge(usuario.edad) &&
            usuario.genero.isNotBlank() &&
            !passwordMatchError

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

            // Email Field (case insensitive)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                TextField(
                    value = usuario.email,
                    onValueChange = {
                        usuario = usuario.copy(email = it.lowercase())
                        emailError = !isValidEmail(it)
                    },
                    label = { Text("Email", color = Color.Black) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            if (isValidEmail(usuario.email)) {
                                passwordFocus.requestFocus()
                            } else {
                                emailError = true
                            }
                        }
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
                    ),
                )
            }

            if (emailError) {
                Text(
                    text = "Please enter a valid email address",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
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
                                        usuario = usuario.copy(genero = selectionOption)
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
                        value = if (usuario.edad == 0) "" else usuario.edad.toString(),
                        onValueChange = {
                            if (it.isEmpty()) {
                                usuario = usuario.copy(edad = 0)
                                ageError = false
                            } else if (it.matches(Regex("^\\d+$"))) {
                                val ageValue = it.toInt()
                                usuario = usuario.copy(edad = ageValue)
                                ageError = !isValidAge(ageValue)
                            }
                        },
                        label = { Text("Age", color = Color.Black) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                if (isValidAge(usuario.edad)) {
                                    passwordFocus.requestFocus()
                                } else {
                                    ageError = true
                                }
                            }
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
                        ),
                    )
                }
            }

            if (ageError) {
                Text(
                    text = "Please enter a valid age (10-120)",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
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
                var passwordVisible by remember { mutableStateOf(false) }

                TextField(
                    value = usuario.password,
                    onValueChange = {
                        usuario = usuario.copy(password = it)
                        passwordError = !isPasswordStrong(it)
                    },
                    label = { Text("Password (min 6 chars)", color = Color.Black) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            if (isPasswordStrong(usuario.password)) {
                                confirmPasswordFocus.requestFocus()
                            } else {
                                passwordError = true
                            }
                        }
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
                    ),
                )
            }

            if (passwordError) {
                Text(
                    text = "Password must be at least 6 characters",
                    color = Color.Red,
                    fontSize = 12.sp,
                    //modifier = Modifier.padding(start = 4.dp, top = 4.dp)
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
                    onValueChange = {
                        confirmPassword = it
                        passwordMatchError = it != usuario.password
                    },
                    label = { Text("Confirm Password", color = Color.Black) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (allFieldsValid) {
                                handleSignUp(context, usuario)
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

            if (passwordMatchError) {
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
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Terms and Privacy Policy
            val annotatedText = buildAnnotatedString {
                append("By continuing you agree to our ")

                pushStringAnnotation(tag = "TERMS", annotation = "https://docs.google.com/document/d/1BSwbORkGapA3NyvCHFcvWqP2IfhmnipBpVx_mbEWfJA/edit?usp=sharing")
                withStyle(style = SpanStyle(color = Color(0xFF53B175), fontWeight = FontWeight.Bold)) {
                    append("Terms of Service")
                }
                pop()

                append(" and ")

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

            // Sign Up Button
            Button(
                onClick = {
                    if (allFieldsValid) {
                        handleSignUp(context, usuario)
                    } else {
                        Toast.makeText(context, "Please correct all errors", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF53B175),
                    disabledContainerColor = Color(0xFFA0A0A0)
                ),
                shape = RoundedCornerShape(17.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = allFieldsValid
            ) {
                Text(
                    text = "Sign Up",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Link
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

private fun handleSignUp(context: Context, usuario: Usuario) {
    val retrofitService = RetrofitService()
    val api = retrofitService.retrofit.create(UsuarioApi::class.java)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            // First check if email already exists
            //val checkResponse = api.checkEmailExists(usuario.email).execute()

            //if (checkResponse.isSuccessful && checkResponse.body() == true) {
                //withContext(Dispatchers.Main) {
                    //Toast.makeText(context, "Email already registered", Toast.LENGTH_LONG).show()
                //}
                //return@launch
            //}

            // If email doesn't exist, proceed with registration
            val response = api.registrarUsuario(usuario).execute()

            if (response.isSuccessful) {
                val id = api.obtenerIdUsuario(usuario.email).execute()
                val registeredUser = usuario.copy(id = id.body())

                Log.d("SignUpScreen", "User data: $registeredUser")
                val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("usuario_guardado", Gson().toJson(registeredUser))
                    apply()
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Registration successful!", Toast.LENGTH_LONG).show()
                    val intent = Intent(context, AnalyzeFruitActivity::class.java)
                    context.startActivity(intent)
                }
            } else {
                withContext(Dispatchers.Main) {
                    when (response.code()) {
                        400 -> Toast.makeText(context, "Invalid request data", Toast.LENGTH_LONG).show()
                        409 -> Toast.makeText(context, "Email already registered", Toast.LENGTH_LONG).show()
                        else -> Toast.makeText(context, "Registration failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                Log.e("SignUpScreen", "Registration error", e)
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