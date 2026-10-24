package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.MenuData

@Composable
fun TemplatesDialog(
    templates: List<MenuData>,
    currentMenu: MenuData,
    onDismiss: () -> Unit,
    onSelectTemplate: (MenuData) -> Unit,
    onSaveCurrentAsTemplate: (name: String) -> Unit,
    onDeleteTemplate: (templateId: String) -> Unit,
    onResetDefaultTemplates: () -> Unit
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var newTemplateName by remember { mutableStateOf("${currentMenu.header.restaurantName} (Plantilla)") }
    var templateToDeleteId by remember { mutableStateOf<String?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Plantillas de Menú", style = MaterialTheme.typography.titleMedium)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Button to save current design as a new template
                Button(
                    onClick = {
                        newTemplateName = "${currentMenu.header.restaurantName} (Plantilla)"
                        showSaveDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_save_current_as_template"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Guardar diseño actual como nueva plantilla",
                        style = MaterialTheme.typography.labelLarge,
                        softWrap = true,
                        maxLines = 2
                    )
                }

                Text(
                    text = "Selecciona una plantilla para cargarla en el diseñador:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (templates.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay plantillas disponibles",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(templates, key = { it.id }) { template ->
                            TemplateCard(
                                template = template,
                                onClick = { onSelectTemplate(template) },
                                onDelete = { templateToDeleteId = template.id }
                            )
                        }
                    }
                }

                // Reset default templates button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = { showResetConfirmDialog = true },
                        modifier = Modifier.testTag("btn_reset_default_templates")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Restablecer plantillas predeterminadas",
                            style = MaterialTheme.typography.labelSmall,
                            softWrap = true
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )

    // Dialog to save current menu as custom template
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Guardar Plantilla") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Introduce un nombre personalizado para guardar la configuración de estilos, fuentes y platos:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = newTemplateName,
                        onValueChange = { newTemplateName = it },
                        label = { Text("Nombre de la plantilla") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTemplateName.isNotBlank()) {
                            onSaveCurrentAsTemplate(newTemplateName.trim())
                            showSaveDialog = false
                        }
                    },
                    enabled = newTemplateName.isNotBlank()
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Confirmation dialog to delete template
    if (templateToDeleteId != null) {
        val target = templates.find { it.id == templateToDeleteId }
        AlertDialog(
            onDismissRequest = { templateToDeleteId = null },
            title = { Text("¿Eliminar plantilla?") },
            text = {
                Text(
                    text = "¿Seguro que deseas eliminar la plantilla \"${target?.name ?: ""}\"? Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        templateToDeleteId?.let { onDeleteTemplate(it) }
                        templateToDeleteId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { templateToDeleteId = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Confirmation dialog to reset default templates
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("¿Restablecer plantillas?") },
            text = {
                Text(
                    text = "Esto restaurará todas las plantillas predeterminadas originales (Restaurante Cupey, Atelier Bistró, Trattoria Napoli, etc.).",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetDefaultTemplates()
                        showResetConfirmDialog = false
                    }
                ) {
                    Text("Restablecer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun TemplateCard(
    template: MenuData,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag("template_${template.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (template.background.solidColorHex.isNotBlank())
                            parseComposeColor(template.background.solidColorHex)
                        else MaterialTheme.colorScheme.primaryContainer
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RestaurantMenu,
                    contentDescription = null,
                    tint = if (template.background.solidColorHex.lowercase() in listOf("#f6efe5", "#ffffff", "#fafafa", "#f5ece1"))
                        Color(0xFF1D5C96)
                    else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (template.header.subtitle.isNotBlank()) {
                    Text(
                        text = template.header.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "${template.sections.size} secciones",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = template.style.aspectRatio.displayName.substringBefore(" "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Delete template button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("delete_template_${template.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Eliminar plantilla",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
