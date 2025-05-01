package com.example.frutti

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.filled.Info
import java.util.Calendar
import android.app.DatePickerDialog
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.ui.platform.LocalContext


class FruitDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get both fruit name and ripeness from intent
        val fruitName = intent.getStringExtra("FRUIT_NAME") ?: "Unknown Fruit"
        val initialRipeness = intent.getStringExtra("INITIAL_RIPENESS") ?: "Good"

        setContent {
            FruitDetailScreen(
                fruitName = fruitName,
                initialRipeness = initialRipeness
            )
        }
    }
}
fun navigateToFruitDetail(context: Context, fruitName: String, ripeness: String) {
    val intent = Intent(context, FruitDetailActivity::class.java).apply {
        putExtra("FRUIT_NAME", fruitName)
        putExtra("INITIAL_RIPENESS", ripeness)
    }
    context.startActivity(intent)
}
@Composable
fun FruitDetailScreen(
    fruitName: String,
    initialRipeness: String = "Good" // This would come from backend in real app
) {
    var store by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var ripeness by remember { mutableStateOf(initialRipeness) }
    val storeOptions = listOf("Carulla", "Exito", "D1", "Ara", "Other")
    val ripenessOptions = listOf("Fresh", "Good", "Overripe")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(127.dp)
                .background(Color(0xFFE3F2FD)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_fruit),
                contentDescription = fruitName,
                modifier = Modifier.size(75.dp),
                tint = Color(0xFF1E88E5)
            )
        }

        // User Input Card
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = fruitName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E88E5),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Ripeness Indicator Card (read from backend)
            AnimatedVisibility(initialRipeness.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when(initialRipeness) {
                            "Fresh" -> Color(0xFFE8F5E9)
                            "Good" -> Color(0xFFE3F2FD)
                            "Ripe" -> Color(0xFFFFF8E1)
                            "Overripe" -> Color(0xFFFFEBEE)
                            else -> Color(0xFFF5F5F5)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Ripeness Info",
                            tint = Color(0xFF1E88E5),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Detected Ripeness",
                                fontSize = 14.sp,
                                color = Color.Black.copy(alpha = 0.6f)
                            )
                            Text(
                                text = initialRipeness,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = when(initialRipeness) {
                                    "Fresh" -> Color(0xFF2E7D32)
                                    "Good" -> Color(0xFF1565C0)
                                    "Ripe" -> Color(0xFFF57F17)
                                    "Overripe" -> Color(0xFFC62828)
                                    else -> Color.Black
                                }
                            )
                        }
                    }
                }
            }

            // User Input Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, shape = RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StoreDropdownMenu(
                        selectedStore = store,
                        onStoreSelected = { store = it },
                        options = storeOptions
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Price (Left Column)
                        Box(modifier = Modifier.weight(1f)) {
                            CustomTextField(
                                label = "Purchase Price",
                                value = price,
                                isNumeric = true,
                                onValueChange = { price = it },
                                modifier = Modifier
                                    .height(77.dp)
                            )
                        }

                        // Weight (Right Column)
                        Box(modifier = Modifier.weight(1f)) {
                            CustomTextField(
                                label = "Weight (grams)",
                                value = weight,
                                isNumeric = true,
                                onValueChange = { weight = it },
                                modifier = Modifier
                                    .height(77.dp)
                            )
                        }
                    }


                    CustomTextField(
                        label = "Date (DD/MM/YYYY)",
                        value = date,
                        isDate = true,  // Use the date picker version
                        onValueChange = { newValue -> date = newValue }
                    )

                    // User can modify the ripeness
                    RipenessDropdownMenu(
                        selectedRipeness = ripeness,
                        onRipenessSelected = { ripeness = it },
                        options = ripenessOptions,
                        label = "Your Ripeness Assessment"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { /* Save action */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF53B175)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Save",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RipenessDropdownMenu(
    selectedRipeness: String,
    onRipenessSelected: (String) -> Unit,
    options: List<String>,
    label: String = "Ripeness",
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Gray.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp)
                .clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    if (selectedRipeness.isEmpty()) {
                        Text(
                            text = label,
                            color = Color.Black.copy(alpha = 0.9f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = label,
                            color = Color.Black.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            text = selectedRipeness,
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Dropdown Arrow",
                tint = Color.Black,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color.White)
                .width(IntrinsicSize.Max)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    onClick = {
                        onRipenessSelected(option)
                        expanded = false
                    },
                    modifier = Modifier.background(
                        if (option == selectedRipeness) Color.Gray.copy(alpha = 0.1f)
                        else Color.Transparent
                    )
                )
            }
        }
    }
}
@Composable
fun StoreDropdownMenu(
    selectedStore: String,
    onStoreSelected: (String) -> Unit,
    options: List<String>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Gray.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp)
                .clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Label and value container
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    // Label text with animation
                    Box {
                        if (selectedStore.isEmpty()) {
                            Text(
                                text = "Store",
                                color = Color.Black.copy(alpha = 0.9f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "Store",
                                color = Color.Black.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    // Value text with crossfade animation
                    if (selectedStore.isNotEmpty()) {
                        Crossfade(
                            targetState = selectedStore,
                            label = "text-crossfade"
                        ) { currentText ->
                            Text(
                                text = currentText,
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Dropdown arrow
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Dropdown Arrow",
                tint = Color.Black,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color.White)
                .width(IntrinsicSize.Max)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    onClick = {
                        onStoreSelected(option)
                        expanded = false
                    },
                    modifier = Modifier.background(
                        if (option == selectedStore) Color.Gray.copy(alpha = 0.1f)
                        else Color.Transparent
                    )
                )
            }
        }
    }
}




@Composable
fun CustomTextField(
    label: String,
    value: String,
    isNumeric: Boolean = false,
    isDate: Boolean = false, // New parameter to identify date fields
    onValueChange: (String) -> Unit,
    context: Context = LocalContext.current,
    modifier: Modifier = Modifier // <<< ADDED HERE
) {
    if (isDate) {
        // Special handling for date fields
        DatePickerField(
            label = label,
            value = value,
            onValueChange = onValueChange,
            context = context
        )
    } else {
        // Regular text field for other inputs
        Box(
            modifier = modifier // <<< USE the passed modifier here instead of hardcoded
                .fillMaxWidth()
                .background(Color.Gray.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label, color = Color.Black) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isNumeric) KeyboardType.Number else KeyboardType.Text
                ),
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.Black
                )
            )
        }
    }
}


@Composable
fun DatePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    context: Context
) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    // Remember the date picker dialog state
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format the date as DD/MM/YYYY
                val formattedDate = String.format(
                    "%02d/%02d/%04d",
                    selectedDay,
                    selectedMonth + 1,  // Month is 0-based
                    selectedYear
                )
                onValueChange(formattedDate)
            },
            year, month, day
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
            .padding(4.dp)
            .clickable { datePickerDialog.show() }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},  // No direct editing - only through date picker
            label = { Text(label, color = Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,  // Disable direct text input
            textStyle = LocalTextStyle.current.copy(color = Color.Black),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                disabledTextColor = Color.Black,
                disabledLabelColor = if (value.isEmpty()) Color.Black.copy(alpha = 0.9f)
                else Color.Black.copy(alpha = 0.6f)
            ),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,  // You'll need to add this icon
                    contentDescription = "Select Date",
                    tint = Color.Black.copy(alpha = 0.6f))
            }
        )
    }
}






@Composable
fun StarRating(rating: Int, onRatingChanged: (Int) -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            for (i in 1..5) {
                val scale by animateFloatAsState(
                    targetValue = if (i <= rating) 1.3f else 1f,
                    animationSpec = tween(durationMillis = 300)
                )

                IconButton(
                    onClick = { onRatingChanged(i) },
                    modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
                ) {
                    Icon(
                        painter = painterResource(id = if (i <= rating) R.drawable.star else R.drawable.star_outline),
                        contentDescription = "Star Rating",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewFruitDetailScreen() {
    FruitDetailScreen(fruitName = "Banana")
}
