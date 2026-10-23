package com.example.ui.steps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.*
import java.util.UUID

@Composable
fun Step2Dishes(
    menu: MenuData,
    customBadges: List<String> = emptyList(),
    onMenuChange: (MenuData) -> Unit,
    onOpenBulkImport: () -> Unit,
    onOpenTemplates: () -> Unit,
    onAddCustomBadge: ((String) -> Unit)? = null,
    onDeleteCustomBadge: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Action Bar (Multi-line friendly buttons)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = onOpenBulkImport,
                modifier = Modifier.weight(1f).testTag("action_bulk_import"),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Pegar lista\nde golpe",
                    style = MaterialTheme.typography.labelSmall,
                    softWrap = true,
                    maxLines = 2
                )
            }

            FilledTonalButton(
                onClick = onOpenTemplates,
                modifier = Modifier.weight(1f).testTag("action_templates"),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Plantillas\nprofesionales",
                    style = MaterialTheme.typography.labelSmall,
                    softWrap = true,
                    maxLines = 2
                )
            }
        }

        // --- 1. Información General (Cabecera y Pie) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Cabecera y Textos",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = menu.header.restaurantName,
                    onValueChange = {
                        onMenuChange(menu.copy(header = menu.header.copy(restaurantName = it)))
                    },
                    label = { Text("Nombre del Restaurante") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_restaurant_name")
                )

                OutlinedTextField(
                    value = menu.header.subtitle,
                    onValueChange = {
                        onMenuChange(menu.copy(header = menu.header.copy(subtitle = it)))
                    },
                    label = { Text("Subtítulo / Especialidad") },
                    placeholder = { Text("ej: Cocina Mediterránea & Brasa") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_subtitle")
                )

                OutlinedTextField(
                    value = menu.header.footer,
                    onValueChange = {
                        onMenuChange(menu.copy(header = menu.header.copy(footer = it)))
                    },
                    label = { Text("Pie de página (Alérgenos, IVA...)") },
                    placeholder = { Text("Precios con IVA incluido. Carta de alérgenos disponible.") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("input_footer")
                )

                // Header Vertical Offset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Desplazamiento vertical de textos:", style = MaterialTheme.typography.bodySmall)
                    Text("${menu.header.headerOffsetY.toInt()} dp", style = MaterialTheme.typography.labelSmall)
                }
                Slider(
                    value = menu.header.headerOffsetY,
                    onValueChange = {
                        onMenuChange(menu.copy(header = menu.header.copy(headerOffsetY = it)))
                    },
                    valueRange = -40f..80f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // --- 2. Secciones y Platos ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Secciones y Platos (${menu.sections.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = {
                    val newSec = MenuSection(
                        title = "Nueva Sección",
                        items = listOf(MenuItem(name = "Primer plato", price = "10,00"))
                    )
                    onMenuChange(menu.copy(sections = menu.sections + newSec))
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_section_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Añadir\nSección",
                    style = MaterialTheme.typography.labelSmall,
                    softWrap = true,
                    maxLines = 2
                )
            }
        }

        // List of Sections
        menu.sections.forEachIndexed { sIndex, section ->
            SectionEditorCard(
                section = section,
                sIndex = sIndex,
                totalSections = menu.sections.size,
                customBadges = customBadges,
                onAddCustomBadge = onAddCustomBadge,
                onDeleteCustomBadge = onDeleteCustomBadge,
                onUpdateSection = { updatedSection ->
                    val newSections = menu.sections.toMutableList()
                    newSections[sIndex] = updatedSection
                    onMenuChange(menu.copy(sections = newSections))
                },
                onDeleteSection = {
                    val newSections = menu.sections.toMutableList().apply { removeAt(sIndex) }
                    onMenuChange(menu.copy(sections = newSections))
                },
                onMoveSectionUp = {
                    if (sIndex > 0) {
                        val newSections = menu.sections.toMutableList()
                        val temp = newSections[sIndex]
                        newSections[sIndex] = newSections[sIndex - 1]
                        newSections[sIndex - 1] = temp
                        onMenuChange(menu.copy(sections = newSections))
                    }
                },
                onMoveSectionDown = {
                    if (sIndex < menu.sections.size - 1) {
                        val newSections = menu.sections.toMutableList()
                        val temp = newSections[sIndex]
                        newSections[sIndex] = newSections[sIndex + 1]
                        newSections[sIndex + 1] = temp
                        onMenuChange(menu.copy(sections = newSections))
                    }
                }
            )
        }
    }
}

@Composable
fun SectionEditorCard(
    section: MenuSection,
    sIndex: Int,
    totalSections: Int,
    customBadges: List<String>,
    onAddCustomBadge: ((String) -> Unit)?,
    onDeleteCustomBadge: ((String) -> Unit)?,
    onUpdateSection: (MenuSection) -> Unit,
    onDeleteSection: () -> Unit,
    onMoveSectionUp: () -> Unit,
    onMoveSectionDown: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Section Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Desplegar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedTextField(
                    value = section.title,
                    onValueChange = { onUpdateSection(section.copy(title = it)) },
                    placeholder = { Text("Título de la sección (ej: Entrantes)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("section_title_$sIndex")
                )

                // Reorder section Up/Down
                IconButton(
                    onClick = onMoveSectionUp,
                    enabled = sIndex > 0,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Subir sección", modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = onMoveSectionDown,
                    enabled = sIndex < totalSections - 1,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Bajar sección", modifier = Modifier.size(16.dp))
                }

                // Delete Section
                IconButton(
                    onClick = onDeleteSection,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar sección", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Dishes list
                    section.items.forEachIndexed { dIndex, item ->
                        DishItemEditor(
                            item = item,
                            dIndex = dIndex,
                            totalItems = section.items.size,
                            customBadges = customBadges,
                            onAddCustomBadge = onAddCustomBadge,
                            onDeleteCustomBadge = onDeleteCustomBadge,
                            onUpdateItem = { updatedItem ->
                                val newItems = section.items.toMutableList()
                                newItems[dIndex] = updatedItem
                                onUpdateSection(section.copy(items = newItems))
                            },
                            onDeleteItem = {
                                val newItems = section.items.toMutableList().apply { removeAt(dIndex) }
                                onUpdateSection(section.copy(items = newItems))
                            },
                            onMoveItemUp = {
                                if (dIndex > 0) {
                                    val newItems = section.items.toMutableList()
                                    val temp = newItems[dIndex]
                                    newItems[dIndex] = newItems[dIndex - 1]
                                    newItems[dIndex - 1] = temp
                                    onUpdateSection(section.copy(items = newItems))
                                }
                            },
                            onMoveItemDown = {
                                if (dIndex < section.items.size - 1) {
                                    val newItems = section.items.toMutableList()
                                    val temp = newItems[dIndex]
                                    newItems[dIndex] = newItems[dIndex + 1]
                                    newItems[dIndex + 1] = temp
                                    onUpdateSection(section.copy(items = newItems))
                                }
                            }
                        )
                    }

                    // Add Dish Button
                    OutlinedButton(
                        onClick = {
                            val newItem = MenuItem(name = "Plato nuevo", price = "12,00")
                            onUpdateSection(section.copy(items = section.items + newItem))
                        },
                        modifier = Modifier.fillMaxWidth().testTag("add_dish_to_section_$sIndex")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Añadir plato a ${section.title}",
                            softWrap = true,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DishItemEditor(
    item: MenuItem,
    dIndex: Int,
    totalItems: Int,
    customBadges: List<String>,
    onAddCustomBadge: ((String) -> Unit)?,
    onDeleteCustomBadge: ((String) -> Unit)?,
    onUpdateItem: (MenuItem) -> Unit,
    onDeleteItem: () -> Unit,
    onMoveItemUp: () -> Unit,
    onMoveItemDown: () -> Unit
) {
    val defaultBadges = listOf("Vegano", "Vegetariano", "Nuevo", "Especialidad", "Sin Gluten", "Recomendado", "Top")
    var showNewBadgeDialog by remember { mutableStateOf(false) }
    var newBadgeInput by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Row 1: Dish Name + Price + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = item.name,
                    onValueChange = { onUpdateItem(item.copy(name = it)) },
                    placeholder = { Text("Nombre del plato") },
                    singleLine = true,
                    modifier = Modifier.weight(2f).testTag("dish_name_$dIndex")
                )

                OutlinedTextField(
                    value = item.price,
                    onValueChange = { onUpdateItem(item.copy(price = it)) },
                    placeholder = { Text("Precio (0 = ocultar)") },
                    singleLine = true,
                    modifier = Modifier.weight(1.3f).testTag("dish_price_$dIndex")
                )

                IconButton(
                    onClick = onMoveItemUp,
                    enabled = dIndex > 0,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Subir plato", modifier = Modifier.size(14.dp))
                }
                IconButton(
                    onClick = onMoveItemDown,
                    enabled = dIndex < totalItems - 1,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Bajar plato", modifier = Modifier.size(14.dp))
                }
                IconButton(
                    onClick = onDeleteItem,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar plato", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                }
            }

            // Helper notice when price is blank or 0
            val cleanPrice = item.price.trim().replace(",", ".").replace("[^0-9.]".toRegex(), "")
            val isZeroOrBlank = item.price.isBlank() || cleanPrice.toDoubleOrNull() == 0.0
            if (isZeroOrBlank) {
                Text(
                    text = "ℹ️ Sin precio: se mostrará solo el plato sin precio ni líneas de puntos.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Row 2: Description
            OutlinedTextField(
                value = item.description,
                onValueChange = { onUpdateItem(item.copy(description = it)) },
                placeholder = { Text("Descripción (ingredientes, alérgenos, etc.)") },
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            // Row 3: Badge chips with Custom Badges Support
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Badge: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Default badges
                    defaultBadges.forEach { badge ->
                        val isSelected = item.badge.equals(badge, ignoreCase = true)
                        SuggestionChip(
                            onClick = {
                                val next = if (isSelected) null else badge
                                onUpdateItem(item.copy(badge = next))
                            },
                            label = { Text(badge, style = MaterialTheme.typography.labelSmall) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        )
                    }

                    // User Custom Badges
                    customBadges.forEach { customBadge ->
                        val isSelected = item.badge.equals(customBadge, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    val next = if (isSelected) null else customBadge
                                    onUpdateItem(item.copy(badge = next))
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = customBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (onDeleteCustomBadge != null) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Eliminar badge",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clickable { onDeleteCustomBadge(customBadge) }
                                )
                            }
                        }
                    }

                    // Add Custom Badge Button
                    FilledTonalButton(
                        onClick = {
                            newBadgeInput = ""
                            showNewBadgeDialog = true
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("+ Badge", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }

    // New Custom Badge Dialog
    if (showNewBadgeDialog) {
        AlertDialog(
            onDismissRequest = { showNewBadgeDialog = false },
            title = { Text("Nueva Etiqueta / Badge") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Crea una etiqueta personalizada que podrás usar en cualquier plato:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = newBadgeInput,
                        onValueChange = { newBadgeInput = it },
                        placeholder = { Text("ej: Picante, Sin Lactosa, Del Chef...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newBadgeInput.trim()
                        if (trimmed.isNotBlank()) {
                            onAddCustomBadge?.invoke(trimmed)
                            onUpdateItem(item.copy(badge = trimmed))
                            showNewBadgeDialog = false
                        }
                    }
                ) {
                    Text("Añadir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewBadgeDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
