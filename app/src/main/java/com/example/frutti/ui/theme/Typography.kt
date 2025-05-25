package com.example.frutti.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.frutti.R

val MPlusFontFamily = FontFamily(
    Font(R.font.mplus1p_thin, FontWeight.Thin),
    Font(R.font.mplus1p_light, FontWeight.Light),
    Font(R.font.mplus1p_regular, FontWeight.Normal),
    Font(R.font.mplus1p_medium, FontWeight.Medium),
    Font(R.font.mplus1p_bold, FontWeight.Bold),
    Font(R.font.mplus1p_extrabold, FontWeight.ExtraBold),
    Font(R.font.mplus1p_black, FontWeight.Black),
)

val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = MPlusFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    titleLarge = TextStyle(
        fontFamily = MPlusFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    // Add other styles as needed
)
