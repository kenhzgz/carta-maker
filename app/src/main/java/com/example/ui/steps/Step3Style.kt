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

        // --- 2. Tipografías y Fuentes Personalizadas ---
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "2. Tipografías",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Upload Custom Font Button (Multi-line friendly)
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

            // Title Font Selector
            Text("Fuente para Títulos:", style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MenuFontFamily.values().forEach { font ->
                    FilterChip(
                        selected = menu.style.titleFont == font && menu.style.customTitleFontPath == null,
                        onClick = {
                            onMenuChange(menu.copy(style = menu.style.copy(titleFont = font, customTitleFontPath = null)))
                        },
                        label = { Text(font.displayName) }
                    )
                }
            }

            // Body Font Selector
            Text("Fuente para Cuerpo de Texto:", style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MenuFontFamily.values().forEach { font ->
                    FilterChip(
                        selected = menu.style.bodyFont == font && menu.style.customBodyFontPath == null,
                        onClick = {
                            onMenuChange(menu.copy(style = menu.style.copy(bodyFont = font, customBodyFontPath = null)))
                        },
                        label = { Text(font.displayName) }
                    )
                }
            }

            // Uploaded Custom Fonts List
            if (customFonts.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Mis Fuentes Subidas (Guardadas en el teléfono):",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        customFonts.forEach { fontItem ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(fontItem.name, style = MaterialTheme.typography.bodySmall)

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    val isTitle = menu.style.customTitleFontPath == fontItem.file.absolutePath
                                    val isBody = menu.style.customBodyFontPath == fontItem.file.absolutePath

                                    FilterChip(
                                        selected = isTitle,
                                        onClick = {
                                            val newPath = if (isTitle) null else fontItem.file.absolutePath
                                            val newFont = if (isTitle) MenuFontFamily.SERIF else MenuFontFamily.CUSTOM
                                            onMenuChange(menu.copy(style = menu.style.copy(titleFont = newFont, customTitleFontPath = newPath)))
                                        },
                                        label = { Text("Títulos", style = MaterialTheme.typography.labelSmall) }
                                    )

                                    FilterChip(
                                        selected = isBody,
                                        onClick = {
                                            val newPath = if (isBody) null else fontItem.file.absolutePath
                                            val newFont = if (isBody) MenuFontFamily.SANS else MenuFontFamily.CUSTOM
                                            onMenuChange(menu.copy(style = menu.style.copy(bodyFont = newFont, customBodyFontPath = newPath)))
                                        },
                                        label = { Text("Texto", style = MaterialTheme.typography.labelSmall) }
                                    )

                                    if (onDeleteFont != null) {
                                        IconButton(
                                            onClick = { onDeleteFont(fontItem) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar fuente", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // --- 3. Colores Personalizables y Paleta Guardada ---
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "3. Paleta de Colores de la Carta",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            ColorPickerRow(
                title = "Color de Texto Principal",
                selectedHex = menu.style.primaryTextColorHex,
                onColorSelected = { onMenuChange(menu.copy(style = menu.style.copy(primaryTextColorHex = it))) },
                savedCustomColors = savedColors,
                onSaveCustomColor = onSaveCustomColor,
                onDeleteCustomColor = onDeleteCustomColor
            )

            ColorPickerRow(
                title = "Color de Texto Secundario (descripciones)",
                selectedHex = menu.style.secondaryTextColorHex,
                onColorSelected = { onMenuChange(menu.copy(style = menu.style.copy(secondaryTextColorHex = it))) },
                savedCustomColors = savedColors,
                onSaveCustomColor = onSaveCustomColor,
                onDeleteCustomColor = onDeleteCustomColor
            )

            ColorPickerRow(
                title = "Color de Precios",
                selectedHex = menu.style.priceColorHex,
                onColorSelected = { onMenuChange(menu.copy(style = menu.style.copy(priceColorHex = it))) },
                savedCustomColors = savedColors,
                onSaveCustomColor = onSaveCustomColor,
                onDeleteCustomColor = onDeleteCustomColor
            )

            ColorPickerRow(
                title = "Color de Acento (líneas y títulos)",
                selectedHex = menu.style.accentColorHex,
                onColorSelected = { onMenuChange(menu.copy(style = menu.style.copy(accentColorHex = it))) },
                savedCustomColors = savedColors,
                onSaveCustomColor = onSaveCustomColor,
                onDeleteCustomColor = onDeleteCustomColor
            )

            ColorPickerRow(
                title = "Fondo de las Cajas de Platos",
                selectedHex = menu.style.cardBgColorHex,
                onColorSelected = { onMenuChange(menu.copy(style = menu.style.copy(cardBgColorHex = it))) },
                savedCustomColors = savedColors,
                onSaveCustomColor = onSaveCustomColor,
                onDeleteCustomColor = onDeleteCustomColor
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // --- 4. Caja de Platos y Líneas de Líderes ---
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "4. Cajas y Líneas de Precios",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
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

        // --- 5. Alineación, Moneda y Divisores ---
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "5. Diseño y Disposición",
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
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // --- 6. Espaciados, Márgenes y Escala Global ---
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "6. Márgenes y Espaciados",
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

        // --- 7. Controles de Precisión Avanzada ---
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
