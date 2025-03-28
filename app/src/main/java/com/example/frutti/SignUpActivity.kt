package com.example.frutti

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frutti.ui.theme.FruttiTheme

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

@Composable
fun SignUpScreen() {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordMatchError by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Check if all fields are filled
    val allFieldsFilled = username.isNotBlank() &&
            email.isNotBlank() &&
            password.isNotBlank() &&
            confirmPassword.isNotBlank()

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

            Spacer(modifier = Modifier.height(99.dp))

            CustomTextField(value = username, onValueChange = { username = it }, label = "Username", isUsername = true)

            Spacer(modifier = Modifier.height(8.dp))

            CustomTextField(value = email, onValueChange = { email = it }, label = "Email", isEmail = true)

            Spacer(modifier = Modifier.height(8.dp))

            CustomTextField(value = password, onValueChange = { password = it }, label = "Password", isPassword = true)

            Spacer(modifier = Modifier.height(8.dp))

            CustomTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Re-enter Password",
                isPassword = true
            )

            if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
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

            Button(
                onClick = {
                    if (!passwordMatchError) {
                        Toast.makeText(context, "Todo OK, pero no hay DB", Toast.LENGTH_SHORT).show()
                        val intent = Intent(context, AnalyzeFruitActivity::class.java)
                        context.startActivity(intent)
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
                enabled = !passwordMatchError && allFieldsFilled // Disabled if fields aren't complete or passwords don't match
            ) {
                Text(
                    text = "Sign Up",
                    fontSize = 18.sp,
                    color = Color.White
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    isEmail: Boolean = false,
    isUsername: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 2.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(33.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = { newValue ->
                    // Remove the email validation from here to allow free typing
                    onValueChange(newValue)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                singleLine = true,
                visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        innerTextField()
                    }
                }
            )

            if (isPassword) {
                val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = "Toggle Password")
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Gray)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    FruttiTheme {
        SignUpScreen()
    }
}