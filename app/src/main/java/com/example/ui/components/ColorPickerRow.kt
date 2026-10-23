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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class ColorOption(val name: String, val hex: String)

val DefaultPalette = listOf(
    ColorOption("Negro Carbón", "#121212"),
    ColorOption("Pizarra Oscura", "#1E2229"),
    ColorOption("Gris Grafito", "#2D3748"),
    ColorOption("Azul Marino", "#0F172A"),
    ColorOption("Azul Petróleo", "#164E63"),
    ColorOption("Verde Bosque", "#0D3325"),
    ColorOption("Verde Oliva", "#3F4F2E"),
    ColorOption("Verde Salvia", "#556B2F"),
    ColorOption("Borgoña", "#3B1219"),
    ColorOption("Rojo Teja / Terracota", "#8B3A2B"),
    ColorOption("Marrón Espresso", "#2B1810"),
    ColorOption("Ocre Cálido", "#78350F"),
    ColorOption("Ámbar Dorado", "#F59E0B"),
    ColorOption("Oro Viejo", "#D97706"),
    ColorOption("Mostaza Gourmet", "#EAB308"),
    ColorOption("Crema Suave", "#FDFBF7"),
    ColorOption("Blanco Marfil", "#F8F5EE"),
    ColorOption("Lino Claro", "#EAE6DF"),
    ColorOption("Gris Perla", "#ECEFF1"),
    ColorOption("Rosa Palo", "#9D5C63"),
    ColorOption("Cobre", "#B45309"),
    ColorOption("Bronce", "#92400E"),
    ColorOption("Índigo Profundo", "#1E1B4B"),
    ColorOption("Blanco Puro", "#FFFFFF")
)

@Composable
fun ColorPickerRow(
    title: String,
    selectedHex: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    palette: List<ColorOption> = DefaultPalette,
    savedCustomColors: List<String> = emptyList(),
    onSaveCustomColor: ((String) -> Unit)? = null,
    onDeleteCustomColor: ((String) -> Unit)? = null
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
                            .size(14.dp)
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
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.25f),
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
                    contentDescription = "Selector personalizado",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Saved Custom Colors Palette
        if (savedCustomColors.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Guardados:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    savedCustomColors.forEach { savedHex ->
                        val isSelected = selectedHex.equals(savedHex, ignoreCase = true)
                        val c = parseComposeColor(savedHex)

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onColorSelected(savedHex) }
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        width = if (isSelected) 2.dp else 0.5.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                            )
                            Text(savedHex.uppercase(), style = MaterialTheme.typography.labelSmall)

                            if (onDeleteCustomColor != null) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Eliminar color",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { onDeleteCustomColor(savedHex) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCustomHexDialog) {
        val parsedColor = parseComposeColor(customHexInput)
        AlertDialog(
            onDismissRequest = { showCustomHexDialog = false },
            title = { Text("Selector de Color Personalizado") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        "Introduce el código hexadecimal o selecciona un tono:",
                        style = MaterialTheme.typography.bodySmall
                    )

                    OutlinedTextField(
                        value = customHexInput,
                        onValueChange = { customHexInput = it },
                        label = { Text("Código Hexadecimal") },
                        placeholder = { Text("#RRGGBB") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Color Preview Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(parsedColor)
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = customHexInput.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isColorDark(parsedColor)) Color.White else Color.Black
                        )
                    }

                    // Quick Swatches inside Dialog
                    Text("Tonos rápidos:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("#000000", "#1C1917", "#1E3A8A", "#064E3B", "#831843", "#7C2D12", "#D97706", "#CA8A04", "#65A30D", "#0284C7", "#9333EA", "#FFFFFF").forEach { swatch ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(parseComposeColor(swatch))
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                    .clickable { customHexInput = swatch }
                            )
                        }
                    }

                    // Option to save to personal palette
                    if (onSaveCustomColor != null) {
                        OutlinedButton(
                            onClick = {
                                val valid = if (!customHexInput.startsWith("#")) "#$customHexInput" else customHexInput
                                onSaveCustomColor(valid)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Guardar color en mi paleta personal")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
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
