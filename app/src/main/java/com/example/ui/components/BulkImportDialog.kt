package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.MenuParser

@Composable
fun BulkImportDialog(
    onDismiss: () -> Unit,
    onImport: (text: String, replaceAll: Boolean) -> Unit
) {
    var textInput by remember {
        mutableStateOf(
            """# Entrantes
Croquetas de Jamón Ibérico - 9,50 € - Bechamel cremosa con jamón de bellota [Especialidad]
Carpaccio de Calabacín - 11,00 € - Con vinagreta de piñones y trufa [Vegano]
Pulpo a la brasa - 16,50 € - Con crema de patata y pimentón

# Platos Principales
Solomillo de Vaca Madurada - 24,00 € - A la brasa con patatas baby al romero
Lomo de Lubina Salvaje - 21,50 € - Con muselina de hinojo y cítricos
Risotto de Setas Silvestres - 15,50 € - Con boletus y parmesano [Vegetariano]

# Postres Caseros
Tarta de Queso Fluida - 7,00 € - Corazón cremoso al horno
Coulant de Chocolate 70% - 7,50 € - Con helado de vainilla [Sin Gluten]"""
        )
    }

    var replaceAll by remember { mutableStateOf(false) }

    // Live parsed count
    val parsedSections = remember(textInput) {
        MenuParser.parseRawText(textInput)
    }
    val totalDishes = parsedSections.sumOf { it.items.size }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Pegar lista de golpe")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Pega tu menú en texto plano. Se detectarán automáticamente los títulos (# Sección), platos, precios y etiquetas [Vegano, Nuevo...].",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    label = { Text("Texto del menú") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .testTag("bulk_import_textfield"),
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                // Parsed summary badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Detectado en tiempo real:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "${parsedSections.size} secciones · $totalDishes platos",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Append or Replace mode toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = replaceAll,
                        onCheckedChange = { replaceAll = it },
                        modifier = Modifier.testTag("bulk_replace_checkbox")
                    )
                    Text(
                        text = "Reemplazar los platos actuales (en vez de añadir)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onImport(textInput, replaceAll) },
                enabled = totalDishes > 0,
                modifier = Modifier.testTag("bulk_confirm_button")
            ) {
                Text("Insertar en la carta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
