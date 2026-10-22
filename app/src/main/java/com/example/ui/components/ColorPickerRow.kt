package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

data class ColorOption(val name: String, val hex: String)

val DefaultPalette = listOf(
    ColorOption("Negro Carbón", "#121212"),
    ColorOption("Pizarra", "#22272E"),
    ColorOption("Azul Marino", "#0F172A"),
    ColorOption("Verde Pino", "#142E24"),
    ColorOption("Borgoña", "#3B1219"),
    ColorOption("Crema", "#FDFBF7"),
    ColorOption("Gris Perla", "#ECEFF1"),
    ColorOption("Ocre", "#3D2E14"),
    ColorOption("Ámbar Dorado", "#F59E0B"),
    ColorOption("Blanco Puro", "#FFFFFF")
)

@Composable
fun ColorPickerRow(
    title: String,
    selectedHex: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    palette: List<ColorOption> = DefaultPalette
) {
    var showCustomHexDialog by remember { mutableStateOf(false) }
    var customHexInput by remember { mutableStateOf(selectedHex) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Current hex badge and edit button
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable {
                    customHexInput = selectedHex
                    showCustomHexDialog = true
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(parseComposeColor(selectedHex))
                            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                    )
                    Text(
                        text = selectedHex.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Horizontal color circles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            palette.forEach { option ->
                val isSelected = selectedHex.equals(option.hex, ignoreCase = true)
                val c = parseComposeColor(option.hex)

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(c)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(option.hex) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        val iconTint = if (isColorDark(c)) Color.White else Color.Black
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Seleccionado",
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Custom hex button
            IconButton(
                onClick = {
                    customHexInput = selectedHex
                    showCustomHexDialog = true
                },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Colorize,
                    contentDescription = "Personalizar color hex",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    if (showCustomHexDialog) {
        AlertDialog(
            onDismissRequest = { showCustomHexDialog = false },
            title = { Text("Color personalizado (Hex)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Introduce el código hexadecimal (ej: #F59E0B):",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = customHexInput,
                        onValueChange = { customHexInput = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(parseComposeColor(customHexInput))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val valid = if (!customHexInput.startsWith("#")) "#$customHexInput" else customHexInput
                        onColorSelected(valid)
                        showCustomHexDialog = false
                    }
                ) {
                    Text("Aplicar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomHexDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

fun parseComposeColor(hex: String): Color {
    return try {
        val colorInt = android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex")
        Color(colorInt)
    } catch (e: Exception) {
        Color.Gray
    }
}

fun isColorDark(color: Color): Boolean {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return luminance < 0.5
}
