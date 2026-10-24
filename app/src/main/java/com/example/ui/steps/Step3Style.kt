package com.example.ui.steps

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.data.CustomFontItem
import com.example.model.*
import com.example.ui.components.ColorPickerRow

@Composable
fun Step3Style(
    menu: MenuData,
    savedColors: List<String> = emptyList(),
    customFonts: List<CustomFontItem> = emptyList(),
    onMenuChange: (MenuData) -> Unit,
    onSaveCustomColor: ((String) -> Unit)? = null,
    onDeleteCustomColor: ((String) -> Unit)? = null,
    onImportFont: ((Uri) -> Unit)? = null,
    onDeleteFont: ((CustomFontItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showAdvancedPrecision by remember { mutableStateOf(false) }
    var activeSectionIndex by remember { mutableIntStateOf(0) }

    // Font file picker
    val fontFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportFont?.invoke(uri)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- 1. Formato de la Carta ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "1. Formato y Proporción",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MenuAspectRatio.values().forEach { ratio ->
                    val isSelected = menu.style.aspectRatio == ratio
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onMenuChange(menu.copy(style = menu.style.copy(aspectRatio = ratio)))
                            }
                            .testTag("ratio_${ratio.name.lowercase()}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = ratio.displayName.substringBefore(" "),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = ratio.displayName.substringAfter(" ").removeSurrounding("(", ")"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // --- 2. Secciones y Tipografías Individuales ---
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "2. Secciones y Tipografías",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Edita tamaño, posición, alineación, formato y color de cada elemento",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Upload Custom Font Button
                OutlinedButton(
                    onClick = {
                        fontFilePickerLauncher.launch(
                            arrayOf("font/ttf", "font/otf", "application/x-font-ttf", "application/x-font-otf", "application/octet-stream")
                        )
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Subir fuente\n(.ttf / .otf)",
                        style = MaterialTheme.typography.labelSmall,
                        softWrap = true,
                        maxLines = 2
                    )
                }
            }

            // 6-Section Selector Chips
            val sectionNames = listOf(
                "Restaurante",
                "Subtítulo",
                "Encabezados",
                "Platos",
                "Precios",
                "Descripción"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sectionNames.forEachIndexed { index, name ->
                    FilterChip(
                        selected = activeSectionIndex == index,
                        onClick = { activeSectionIndex = index },
                        label = { Text(name, style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = if (activeSectionIndex == index) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            // Current Section Data & Updater
            val currentStyle = when (activeSectionIndex) {
                0 -> menu.style.restaurantNameStyle
                1 -> menu.style.subtitleStyle
                2 -> menu.style.sectionHeaderStyle
                3 -> menu.style.dishNameStyle
                4 -> menu.style.dishPriceStyle
                else -> menu.style.dishDescStyle
            }

            val currentTitle = when (activeSectionIndex) {
                0 -> "Nombre del Restaurante"
                1 -> "Subtítulo / Lema"
                2 -> "Encabezados (Postres, Bebidas...)"
                3 -> "Nombre de los Platos"
                4 -> "Precios de los Platos"
                else -> "Descripción de los Platos"
            }

            val updateSection: (TypographySectionStyle) -> Unit = { newStyle ->
                val updatedMenuStyle = when (activeSectionIndex) {
                    0 -> menu.style.copy(restaurantNameStyle = newStyle)
                    1 -> menu.style.copy(subtitleStyle = newStyle)
                    2 -> menu.style.copy(sectionHeaderStyle = newStyle)
                    3 -> menu.style.copy(dishNameStyle = newStyle)
                    4 -> menu.style.copy(dishPriceStyle = newStyle)
                    else -> menu.style.copy(dishDescStyle = newStyle)
                }
                onMenuChange(menu.copy(style = updatedMenuStyle))
            }

            // Editing Card for Selected Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header with Section Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Editando: $currentTitle",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = try {
                                Color(android.graphics.Color.parseColor(currentStyle.colorHex))
                            } catch (_: Exception) {
                                MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier
                                .size(24.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                        ) {}
                    }

                    // A) Font Size (sp)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tamaño de Fuente:", style = MaterialTheme.typography.bodySmall)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        if (currentStyle.fontSizeSp > 8f) {
                                            updateSection(currentStyle.copy(fontSizeSp = currentStyle.fontSizeSp - 1f))
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Reducir tamaño", modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = "${currentStyle.fontSizeSp.toInt()} sp",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(
                                    onClick = {
                                        if (currentStyle.fontSizeSp < 56f) {
                                            updateSection(currentStyle.copy(fontSizeSp = currentStyle.fontSizeSp + 1f))
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Aumentar tamaño", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        Slider(
                            value = currentStyle.fontSizeSp,
                            onValueChange = { updateSection(currentStyle.copy(fontSizeSp = it)) },
                            valueRange = 8f..56f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // B) Vertical Position (Offset Y)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Posición Vertical (Offset Y):", style = MaterialTheme.typography.bodySmall)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "${if (currentStyle.offsetY > 0) "+" else ""}${currentStyle.offsetY.toInt()} px",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (currentStyle.offsetY != 0f) {
                                    TextButton(
                                        onClick = { updateSection(currentStyle.copy(offsetY = 0f)) },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("0 px", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                        Slider(
                            value = currentStyle.offsetY,
                            onValueChange = { updateSection(currentStyle.copy(offsetY = it)) },
                            valueRange = -50f..50f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // C) Alignment
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Alineación:", style = MaterialTheme.typography.bodySmall)
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = currentStyle.alignment == TextAlignment.LEFT,
                                onClick = { updateSection(currentStyle.copy(alignment = TextAlignment.LEFT)) },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                                icon = { Icon(Icons.Default.FormatAlignLeft, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            ) {
                                Text("Izquierda", style = MaterialTheme.typography.labelSmall)
                            }
                            SegmentedButton(
                                selected = currentStyle.alignment == TextAlignment.CENTER,
                                onClick = { updateSection(currentStyle.copy(alignment = TextAlignment.CENTER)) },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                                icon = { Icon(Icons.Default.FormatAlignCenter, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            ) {
                                Text("Centro", style = MaterialTheme.typography.labelSmall)
                            }
                            SegmentedButton(
                                selected = currentStyle.alignment == TextAlignment.RIGHT,
                                onClick = { updateSection(currentStyle.copy(alignment = TextAlignment.RIGHT)) },
                                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                                icon = { Icon(Icons.Default.FormatAlignRight, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            ) {
                                Text("Derecha", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    // D) Bold, Italic, Underlined
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Estilo y Formato:", style = MaterialTheme.typography.bodySmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = currentStyle.isBold,
                                onClick = { updateSection(currentStyle.copy(isBold = !currentStyle.isBold)) },
                                label = { Text("Negrita (Bold)") },
                                leadingIcon = {
                                    Icon(Icons.Default.FormatBold, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = currentStyle.isItalic,
                                onClick = { updateSection(currentStyle.copy(isItalic = !currentStyle.isItalic)) },
                                label = { Text("Cursiva (Italic)") },
                                leadingIcon = {
                                    Icon(Icons.Default.FormatItalic, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = currentStyle.isUnderlined,
                                onClick = { updateSection(currentStyle.copy(isUnderlined = !currentStyle.isUnderlined)) },
                                label = { Text("Subrayado (Underlined)") },
                                leadingIcon = {
                                    Icon(Icons.Default.FormatUnderlined, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // E) Font Family Selector
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Fuente Tipográfica:", style = MaterialTheme.typography.bodySmall)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Preset fonts
                            MenuFontFamily.values().filter { it != MenuFontFamily.CUSTOM }.forEach { font ->
                                FilterChip(
                                    selected = currentStyle.fontFamily == font && currentStyle.customFontPath == null,
                                    onClick = {
                                        updateSection(currentStyle.copy(fontFamily = font, customFontPath = null))
                                    },
                                    label = { Text(font.displayName) }
                                )
                            }

                            // Explicit "Fuente Personalizada" chip in main list
                            FilterChip(
                                selected = currentStyle.fontFamily == MenuFontFamily.CUSTOM || currentStyle.customFontPath != null,
                                onClick = {
                                    if (customFonts.isNotEmpty()) {
                                        val targetFont = customFonts.find { it.file.absolutePath == currentStyle.customFontPath } ?: customFonts.first()
                                        updateSection(currentStyle.copy(fontFamily = MenuFontFamily.CUSTOM, customFontPath = targetFont.file.absolutePath))
                                    } else {
                                        fontFilePickerLauncher.launch(
                                            arrayOf("font/ttf", "font/otf", "application/x-font-ttf", "application/x-font-otf", "application/octet-stream")
                                        )
                                    }
                                },
                                label = { Text("Fuente Personalizada") },
                                leadingIcon = {
                                    Icon(Icons.Default.Extension, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            )

                            // Direct upload action chip
                            AssistChip(
                                onClick = {
                                    fontFilePickerLauncher.launch(
                                        arrayOf("font/ttf", "font/otf", "application/x-font-ttf", "application/x-font-otf", "application/octet-stream")
                                    )
                                },
                                label = { Text("+ Subir .ttf / .otf") },
                                leadingIcon = {
                                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            )
                        }

                        // Dedicated Custom Fonts Panel (Always visible so users can always add and select their fonts)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.FontDownload, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                        Text(
                                            text = "Fuentes Personalizadas (${customFonts.size}):",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    TextButton(
                                        onClick = {
                                            fontFilePickerLauncher.launch(
                                                arrayOf("font/ttf", "font/otf", "application/x-font-ttf", "application/x-font-otf", "application/octet-stream")
                                            )
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Añadir fuente", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                if (customFonts.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        customFonts.forEach { fontItem ->
                                            val isSelected = currentStyle.customFontPath == fontItem.file.absolutePath
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    if (isSelected) {
                                                        updateSection(currentStyle.copy(fontFamily = MenuFontFamily.SERIF, customFontPath = null))
                                                    } else {
                                                        updateSection(currentStyle.copy(fontFamily = MenuFontFamily.CUSTOM, customFontPath = fontItem.file.absolutePath))
                                                    }
                                                },
                                                label = { Text(fontItem.name) },
                                                leadingIcon = if (isSelected) {
                                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                                } else null,
                                                trailingIcon = if (onDeleteFont != null) {
                                                    {
                                                        IconButton(
                                                            onClick = { onDeleteFont(fontItem) },
                                                            modifier = Modifier.size(18.dp)
                                                        ) {
                                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                                        }
                                                    }
                                                } else null
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                fontFilePickerLauncher.launch(
                                                    arrayOf("font/ttf", "font/otf", "application/x-font-ttf", "application/x-font-otf", "application/octet-stream")
                                                )
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.UploadFile, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "No has añadido fuentes externas todavía",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Toca aquí para importar un archivo de fuente (.ttf o .otf) y usarlo en este texto",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // F) Color for this Section
                    ColorPickerRow(
                        title = "Color de $currentTitle",
                        selectedHex = currentStyle.colorHex,
                        onColorSelected = { updateSection(currentStyle.copy(colorHex = it)) },
                        savedCustomColors = savedColors,
                        onSaveCustomColor = onSaveCustomColor,
                        onDeleteCustomColor = onDeleteCustomColor
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // --- 3. Caja de Platos y Líneas de Líderes ---
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "3. Cajas y Líneas de Precios",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Card Background Color
            ColorPickerRow(
                title = "Color de Fondo de las Cajas de Platos",
                selectedHex = menu.style.cardBgColorHex,
                onColorSelected = { onMenuChange(menu.copy(style = menu.style.copy(cardBgColorHex = it))) },
                savedCustomColors = savedColors,
                onSaveCustomColor = onSaveCustomColor,
                onDeleteCustomColor = onDeleteCustomColor
            )

            // Card Opacity Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Opacidad de fondo de plato:", style = MaterialTheme.typography.bodySmall)
                Text("${(menu.style.cardBgOpacity * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
            }
            Slider(
                value = menu.style.cardBgOpacity,
                onValueChange = { onMenuChange(menu.copy(style = menu.style.copy(cardBgOpacity = it))) },
                valueRange = 0.0f..0.85f,
                modifier = Modifier.fillMaxWidth().testTag("card_opacity_slider")
            )

            // Card Corner Radius Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Radio de esquinas de caja:", style = MaterialTheme.typography.bodySmall)
                Text("${menu.style.cardCornerRadius.toInt()} dp", style = MaterialTheme.typography.labelSmall)
            }
            Slider(
                value = menu.style.cardCornerRadius,
                onValueChange = { onMenuChange(menu.copy(style = menu.style.copy(cardCornerRadius = it))) },
                valueRange = 0.0f..32.0f,
                modifier = Modifier.fillMaxWidth()
            )

            // Leader Line Style
            Text("Estilo de líneas de unión a precios:", style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LeaderLineStyle.values().forEach { lStyle ->
                    FilterChip(
                        selected = menu.style.leaderLineStyle == lStyle,
                        onClick = { onMenuChange(menu.copy(style = menu.style.copy(leaderLineStyle = lStyle))) },
                        label = { Text(lStyle.displayName.substringBefore(" ")) }
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // --- 4. Alineación, Moneda y Divisores ---
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "4. Diseño y Disposición",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Alignment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Alineación del texto:", style = MaterialTheme.typography.bodySmall)
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = menu.style.textAlignment == TextAlignment.LEFT,
                        onClick = { onMenuChange(menu.copy(style = menu.style.copy(textAlignment = TextAlignment.LEFT))) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("Izquierda", style = MaterialTheme.typography.labelSmall)
                    }
                    SegmentedButton(
                        selected = menu.style.textAlignment == TextAlignment.CENTER,
                        onClick = { onMenuChange(menu.copy(style = menu.style.copy(textAlignment = TextAlignment.CENTER))) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("Centrado", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Currency Position & Symbol
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = menu.style.currencySymbol,
                    onValueChange = { onMenuChange(menu.copy(style = menu.style.copy(currencySymbol = it))) },
                    label = { Text("Moneda") },
                    singleLine = true,
                    modifier = Modifier.width(90.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text("Posición:", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = menu.style.currencyPosition == CurrencyPosition.AFTER,
                            onClick = { onMenuChange(menu.copy(style = menu.style.copy(currencyPosition = CurrencyPosition.AFTER))) },
                            label = { Text("12,50 €") }
                        )
                        FilterChip(
                            selected = menu.style.currencyPosition == CurrencyPosition.BEFORE,
                            onClick = { onMenuChange(menu.copy(style = menu.style.copy(currencyPosition = CurrencyPosition.BEFORE))) },
                            label = { Text("$12.50") }
                        )
                    }
                }
            }

            // Section Dividers
            Text("Separadores de sección decorativos:", style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SectionDividerStyle.values().forEach { dStyle ->
                    FilterChip(
                        selected = menu.style.sectionDivider == dStyle,
                        onClick = { onMenuChange(menu.copy(style = menu.style.copy(sectionDivider = dStyle))) },
                        label = { Text(dStyle.displayName.substringBefore(" ")) }
                    )
                }
            }

            // Text shadow toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sombra de texto (mejora legibilidad sobre fotos):", style = MaterialTheme.typography.bodySmall)
                Switch(
                    checked = menu.style.textShadow,
                    onCheckedChange = { onMenuChange(menu.copy(style = menu.style.copy(textShadow = it))) }
                )
            }

            // Header divider line toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Línea separadora del encabezado:", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "Separa el nombre y subtítulo del restaurante de las secciones",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = menu.style.showHeaderDivider,
                    onCheckedChange = { onMenuChange(menu.copy(style = menu.style.copy(showHeaderDivider = it))) }
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // --- 5. Espaciados, Márgenes y Escala Global ---
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "5. Márgenes y Espaciados",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Top Padding Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Margen Superior (Top):", style = MaterialTheme.typography.bodySmall)
                Text("${menu.style.topPadding.toInt()} dp", style = MaterialTheme.typography.labelSmall)
            }
            Slider(
                value = menu.style.topPadding,
                onValueChange = { onMenuChange(menu.copy(style = menu.style.copy(topPadding = it))) },
                valueRange = 0f..80f,
                modifier = Modifier.fillMaxWidth()
            )

            // Bottom Margin Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Margen Inferior (Bottom):", style = MaterialTheme.typography.bodySmall)
                Text("${menu.style.bottomMargin.toInt()} dp", style = MaterialTheme.typography.labelSmall)
            }
            Slider(
                value = menu.style.bottomMargin,
                onValueChange = { onMenuChange(menu.copy(style = menu.style.copy(bottomMargin = it))) },
                valueRange = 0f..80f,
                modifier = Modifier.fillMaxWidth()
            )

            // Horizontal Margin Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Márgenes Laterales:", style = MaterialTheme.typography.bodySmall)
                Text("${menu.style.horizontalPadding.toInt()} dp", style = MaterialTheme.typography.labelSmall)
            }
            Slider(
                value = menu.style.horizontalPadding,
                onValueChange = { onMenuChange(menu.copy(style = menu.style.copy(horizontalPadding = it))) },
                valueRange = 12f..56f,
                modifier = Modifier.fillMaxWidth()
            )

            // Global Font Scale Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Escala de tipografía global:", style = MaterialTheme.typography.bodySmall)
                Text("${(menu.style.globalFontScale * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
            }
            Slider(
                value = menu.style.globalFontScale,
                onValueChange = { onMenuChange(menu.copy(style = menu.style.copy(globalFontScale = it))) },
                valueRange = 0.8f..1.3f,
                modifier = Modifier.fillMaxWidth().testTag("font_scale_slider")
            )

            // Item Spacing Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Espaciado entre platos:", style = MaterialTheme.typography.bodySmall)
                Text("${menu.style.itemSpacing.toInt()} dp", style = MaterialTheme.typography.labelSmall)
            }
            Slider(
                value = menu.style.itemSpacing,
                onValueChange = { onMenuChange(menu.copy(style = menu.style.copy(itemSpacing = it))) },
                valueRange = 6f..30f,
                modifier = Modifier.fillMaxWidth()
            )

            // Section Spacing Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Espaciado entre secciones:", style = MaterialTheme.typography.bodySmall)
                Text("${menu.style.sectionSpacing.toInt()} dp", style = MaterialTheme.typography.labelSmall)
            }
            Slider(
                value = menu.style.sectionSpacing,
                onValueChange = { onMenuChange(menu.copy(style = menu.style.copy(sectionSpacing = it))) },
                valueRange = 10f..40f,
                modifier = Modifier.fillMaxWidth()
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // --- 6. Controles de Precisión Avanzada ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAdvancedPrecision = !showAdvancedPrecision }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Ajustes de Precisión de Tamaños",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        imageVector = if (showAdvancedPrecision) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }

                AnimatedVisibility(visible = showAdvancedPrecision) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PrecisionSlider(
                            label = "Título del restaurante",
                            value = menu.style.titleSizeSp,
                            range = 18f..38f,
                            onValueChange = { onMenuChange(menu.copy(style = menu.style.copy(titleSizeSp = it))) }
                        )

                        PrecisionSlider(
                            label = "Subtítulo",
                            value = menu.style.subtitleSizeSp,
                            range = 10f..20f,
                            onValueChange = { onMenuChange(menu.copy(style = menu.style.copy(subtitleSizeSp = it))) }
                        )

                        PrecisionSlider(
                            label = "Títulos de sección",
                            value = menu.style.sectionTitleSizeSp,
                            range = 12f..26f,
                            onValueChange = { onMenuChange(menu.copy(style = menu.style.copy(sectionTitleSizeSp = it))) }
                        )

                        PrecisionSlider(
                            label = "Nombre de platos",
                            value = menu.style.itemNameSizeSp,
                            range = 10f..22f,
                            onValueChange = { onMenuChange(menu.copy(style = menu.style.copy(itemNameSizeSp = it))) }
                        )

                        PrecisionSlider(
                            label = "Precios",
                            value = menu.style.itemPriceSizeSp,
                            range = 10f..22f,
                            onValueChange = { onMenuChange(menu.copy(style = menu.style.copy(itemPriceSizeSp = it))) }
                        )

                        PrecisionSlider(
                            label = "Descripción de ingredientes",
                            value = menu.style.itemDescSizeSp,
                            range = 8f..16f,
                            onValueChange = { onMenuChange(menu.copy(style = menu.style.copy(itemDescSizeSp = it))) }
                        )

                        PrecisionSlider(
                            label = "Pie de página",
                            value = menu.style.footerSizeSp,
                            range = 8f..15f,
                            onValueChange = { onMenuChange(menu.copy(style = menu.style.copy(footerSizeSp = it))) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrecisionSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${value.toInt()} sp", style = MaterialTheme.typography.labelSmall)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
