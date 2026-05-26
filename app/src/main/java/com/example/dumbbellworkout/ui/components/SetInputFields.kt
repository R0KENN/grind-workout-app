package com.example.dumbbellworkout.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dumbbellworkout.Purple

@Composable
fun WeightRepsInput(
    weightInput: String,
    repsInput: String,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onSave: () -> Unit,
    onSkip: () -> Unit
) {
    OutlinedTextField(
        value = weightInput,
        onValueChange = onWeightChange,
        label = { Text("Вес (кг)", fontSize = 13.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Поле ввода веса в килограммах" },
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Purple,
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
        )
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = repsInput,
        onValueChange = onRepsChange,
        label = { Text("Повторения", fontSize = 13.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Поле ввода количества повторений" },
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Purple,
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
        )
    )

    Spacer(modifier = Modifier.height(8.dp))

    val enabled = weightInput.isNotBlank() && repsInput.isNotBlank()
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if (enabled) Brush.horizontalGradient(listOf(Purple, Purple.copy(alpha = 0.7f)))
                else Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.05f)))
            )
            .border(1.dp, if (enabled) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f), shape)
            .clickable(enabled = enabled) { onSave() }
            .padding(vertical = 13.dp)
            .semantics { contentDescription = "Записать подход" },
        contentAlignment = Alignment.Center
    ) {
        Text("Записать подход", fontWeight = FontWeight.Bold, fontSize = 14.sp,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.3f))
    }

    Spacer(modifier = Modifier.height(4.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onSkip() }
            .padding(vertical = 10.dp)
            .semantics { contentDescription = "Пропустить упражнение" },
        contentAlignment = Alignment.Center
    ) {
        Text("Пропустить упражнение", fontSize = 13.sp, color = Color.White.copy(alpha = 0.3f))
    }
}
