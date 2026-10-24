package com.example.ui.steps

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.CustomFontItem
import com.example.model.FreeElement
import com.example.model.FreeElementType
import com.example.model.MenuData
import com.example.model.MenuFontFamily
import com.example.ui.components.ColorPickerRow
import com.example.ui.components.MenuPreviewCanvas
import com.example.ui.components.parseComposeColor
import java.util.UUID
import kotlin.math.hypot

@Composable
fun Step3AdvancedElements(
    menu: MenuData,
    savedColors: List<String>,
    customFonts: List<CustomFontItem> = emptyList(),
    onMenuChange: (MenuData) -> Unit,
    onOpenImagePickerForElement: (elementId: String) -> Unit,
    onExternalPhotoPicked: (Uri, elementId: String) -> Unit,
    onFullscreenPreview: (() -> Unit)? = null,
    onNextStep: (() -> Unit)? = null,
    onImportFont: ((Uri) -> Unit)? = null,
    onDeleteFont: ((CustomFontItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedElementId by remember { mutableStateOf<String?>(menu.freeElements.firstOrNull()?.id) }

    // Custom Font File Picker Launcher
    val fontFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportFont?.invoke(uri)
        }
    }

    // External App File/Photo Picker Launcher
    var targetElementIdForExternal by remember { mutableStateOf<String?>(null) }
    val externalPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        val elemId = targetElementIdForExternal
        if (uri != null && elemId != null) {
            onExternalPhotoPicked(uri, elemId)
        }
    }

    val selectedElement = menu.freeElements.find { it.id == selectedElementId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Edición Avanzada: Elementos Libres",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Añade textos destacados, sellos, pegatinas y fotos sueltas de platos. Puedes arrastrarlos libremente, pellizcar para hacer zoom o girar con 2 dedos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Live Interactive Canvas with Multi-Touch Drag, Zoom & Rotation
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val density = LocalDensity.current
                val containerW = constraints.maxWidth.toFloat()
                val containerH = constraints.maxHeight.toFloat()
                val padPx = with(density) { 8.dp.toPx() } * 2f
                val availW = (containerW - padPx).coerceAtLeast(1f)
                val availH = (containerH - padPx).coerceAtLeast(1f)
                val aspectRatio = menu.style.aspectRatio.ratioWidth / menu.style.aspectRatio.ratioHeight

                val canvasW: Float
                val canvasH: Float
                if (availW / availH > aspectRatio) {
                    canvasH = availH
                    canvasW = availH * aspectRatio
                } else {
                    canvasW = availW
                    canvasH = availW / aspectRatio
                }

                val cardLeft = (containerW - canvasW) / 2f
                val cardTop = (containerH - canvasH) / 2f

                val currentMenuState = rememberUpdatedState(menu)
                val currentSelectedIdState = rememberUpdatedState(selectedElementId)
                val onMenuChangeState = rememberUpdatedState(onMenuChange)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures(panZoomLock = false) { centroid, pan, zoom, rotation ->
                                val curMenu = currentMenuState.value
                                if (curMenu.freeElements.isEmpty() || canvasW <= 0f || canvasH <= 0f) return@detectTransformGestures

                                val touchXRatio = (centroid.x - cardLeft) / canvasW
                                val touchYRatio = (centroid.y - cardTop) / canvasH

                                val activeId = currentSelectedIdState.value ?: curMenu.freeElements.firstOrNull()?.id
                                val closest = curMenu.freeElements.minByOrNull { elem ->
                                    hypot(elem.xRatio - touchXRatio, elem.yRatio - touchYRatio)
                                }
                                val targetId = if (closest != null && hypot(closest.xRatio - touchXRatio, closest.yRatio - touchYRatio) < 0.16f) {
                                    if (closest.id != selectedElementId) {
                                        selectedElementId = closest.id
                                    }
                                    closest.id
                                } else {
                                    activeId
                                }

                                if (targetId != null) {
                                    val dxRatio = pan.x / canvasW
                                    val dyRatio = pan.y / canvasH

                                    val updated = curMenu.freeElements.map { elem ->
                                        if (elem.id == targetId) {
                                            elem.copy(
                                                xRatio = (elem.xRatio + dxRatio).coerceIn(0.0f, 1.0f),
                                                yRatio = (elem.yRatio + dyRatio).coerceIn(0.0f, 1.0f),
                                                scale = (elem.scale * zoom).coerceIn(0.2f, 4.0f),
                                                rotation = (elem.rotation + rotation)
                                            )
                                        } else elem
                                    }
                                    onMenuChangeState.value(curMenu.copy(freeElements = updated))
                                }
                            }
                        }
                ) {
                    MenuPreviewCanvas(
                        menu = menu,
                        modifier = Modifier.fillMaxSize(),
                        onFullscreenClick = onFullscreenPreview
                    )

                    // Helper overlay badge explaining gestures
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.80f),
                        contentColor = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.OpenWith,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Arrastra para mover • Pellizca para zoom • Gira con 2 dedos",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }

        // Add Elements Buttons Row (Multi-line friendly)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val newElement = FreeElement(
                        id = UUID.randomUUID().toString(),
                        type = FreeElementType.TEXT,
                        text = "¡Sugerencia del Chef!",
                        xRatio = 0.5f,
                        yRatio = 0.35f,
                        colorHex = "#F59E0B",
                        hasBackground = true,
                        bgColorHex = "#000000",
                        bgOpacity = 0.75f,
                        fontSizeSp = 14f,
                        isBold = true,
                        cornerRadiusDp = 8f
                    )
                    onMenuChange(menu.copy(freeElements = menu.freeElements + newElement))
                    selectedElementId = newElement.id
                },
                modifier = Modifier.weight(1f).testTag("add_free_text_button")
            ) {
                Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "+ Añadir Texto\no Pegatina",
                    style = MaterialTheme.typography.labelSmall,
                    softWrap = true,
                    maxLines = 2
                )
            }

            FilledTonalButton(
                onClick = {
                    val newElement = FreeElement(
                        id = UUID.randomUUID().toString(),
                        type = FreeElementType.PHOTO,
                        text = "Foto de Plato",
                        xRatio = 0.5f,
                        yRatio = 0.5f,
                        scale = 1.0f,
                        cornerRadiusDp = 12f
                    )
                    onMenuChange(menu.copy(freeElements = menu.freeElements + newElement))
                    selectedElementId = newElement.id
                },
                modifier = Modifier.weight(1f).testTag("add_free_photo_button")
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "+ Añadir Foto\nde Plato",
                    style = MaterialTheme.typography.labelSmall,
                    softWrap = true,
                    maxLines = 2
                )
            }
        }

        // Elements List Chips
        if (menu.freeElements.isNotEmpty()) {
            Text(
                text = "Elementos en esta página (${menu.freeElements.size}):",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                menu.freeElements.forEachIndexed { idx, elem ->
                    val isSelected = elem.id == selectedElementId
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedElementId = elem.id },
                        label = {
                            Text(
                                if (elem.type == FreeElementType.TEXT)
                                    elem.text.ifBlank { "Texto ${idx + 1}" }
                                else "Foto ${idx + 1}"
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (elem.type == FreeElementType.TEXT) Icons.Default.TextFields else Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                }
            }
        }

        // Selected Element Customization Controls
        if (selectedElement != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedElement.type == FreeElementType.TEXT) "Ajustes de Texto / Pegatina" else "Ajustes de Foto",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        IconButton(
                            onClick = {
                                val updated = menu.freeElements.filter { it.id != selectedElement.id }
                                onMenuChange(menu.copy(freeElements = updated))
                                selectedElementId = updated.firstOrNull()?.id
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar elemento", tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    // Content editor based on type
                    if (selectedElement.type == FreeElementType.TEXT) {
                        OutlinedTextField(
                            value = selectedElement.text,
                            onValueChange = { newText ->
                                val updated = menu.freeElements.map {
                                    if (it.id == selectedElement.id) it.copy(text = newText) else it
                                }
                                onMenuChange(menu.copy(freeElements = updated))
                            },
                            label = { Text("Texto a mostrar") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Quick Stickers Suggestions
                        Text("Sugerencias rápidas:", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("¡Recomendado!", "⭐ Especialidad", "🌶️ Picante", "100% Casero", "🏆 Top Ventas").forEach { suggestion ->
                                SuggestionChip(
                                    onClick = {
                                        val updated = menu.freeElements.map {
                                            if (it.id == selectedElement.id) it.copy(text = suggestion) else it
                                        }
                                        onMenuChange(menu.copy(freeElements = updated))
                                    },
                                    label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }

                        // Font Family Selector for Free Text
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Fuente Tipográfica del Texto:", style = MaterialTheme.typography.bodySmall)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Preset fonts
                                MenuFontFamily.values().filter { it != MenuFontFamily.CUSTOM }.forEach { font ->
                                    FilterChip(
                                        selected = selectedElement.fontFamily == font && selectedElement.customFontPath == null,
                                        onClick = {
                                            val updated = menu.freeElements.map {
                                                if (it.id == selectedElement.id) it.copy(fontFamily = font, customFontPath = null) else it
                                            }
                                            onMenuChange(menu.copy(freeElements = updated))
                                        },
                                        label = { Text(font.displayName, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }

                                // Explicit "Fuente Personalizada" chip
                                FilterChip(
                                    selected = selectedElement.fontFamily == MenuFontFamily.CUSTOM || selectedElement.customFontPath != null,
                                    onClick = {
                                        if (customFonts.isNotEmpty()) {
                                            val targetFont = customFonts.find { it.file.absolutePath == selectedElement.customFontPath } ?: customFonts.first()
                                            val updated = menu.freeElements.map {
                                                if (it.id == selectedElement.id) it.copy(fontFamily = MenuFontFamily.CUSTOM, customFontPath = targetFont.file.absolutePath) else it
                                            }
                                            onMenuChange(menu.copy(freeElements = updated))
                                        } else {
                                            fontFilePickerLauncher.launch(
                                                arrayOf("font/ttf", "font/otf", "application/x-font-ttf", "application/x-font-otf", "application/octet-stream")
                                            )
                                        }
                                    },
                                    label = { Text("Fuente Personalizada", style = MaterialTheme.typography.labelSmall) },
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
                                    label = { Text("+ Subir .ttf / .otf", style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = {
                                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                )
                            }

                            // Dedicated Custom Fonts Panel in Free Element
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
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            customFonts.forEach { fontItem ->
                                                val isSelected = selectedElement.customFontPath == fontItem.file.absolutePath
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = {
                                                        val updated = menu.freeElements.map {
                                                            if (it.id == selectedElement.id) {
                                                                if (isSelected) it.copy(fontFamily = MenuFontFamily.SANS, customFontPath = null)
                                                                else it.copy(fontFamily = MenuFontFamily.CUSTOM, customFontPath = fontItem.file.absolutePath)
                                                            } else it
                                                        }
                                                        onMenuChange(menu.copy(freeElements = updated))
                                                    },
                                                    label = { Text(fontItem.name, style = MaterialTheme.typography.labelSmall) },
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
                                                    text = "Toca aquí para importar un archivo de fuente (.ttf o .otf) y aplicarlo",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Font Size Slider for Text
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tamaño de Fuente:", style = MaterialTheme.typography.bodySmall)
                                Text("${selectedElement.fontSizeSp.toInt()} sp", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = selectedElement.fontSizeSp,
                                onValueChange = { newSize ->
                                    val updated = menu.freeElements.map {
                                        if (it.id == selectedElement.id) it.copy(fontSizeSp = newSize) else it
                                    }
                                    onMenuChange(menu.copy(freeElements = updated))
                                },
                                valueRange = 8f..48f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Text Color
                        ColorPickerRow(
                            title = "Color del Texto",
                            selectedHex = selectedElement.colorHex,
                            onColorSelected = { newHex ->
                                val updated = menu.freeElements.map {
                                    if (it.id == selectedElement.id) it.copy(colorHex = newHex) else it
                                }
                                onMenuChange(menu.copy(freeElements = updated))
                            },
                            savedCustomColors = savedColors
                        )

                        // Bold & Background Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Texto en Negrita:", style = MaterialTheme.typography.bodySmall)
                            Switch(
                                checked = selectedElement.isBold,
                                onCheckedChange = { isBold ->
                                    val updated = menu.freeElements.map {
                                        if (it.id == selectedElement.id) it.copy(isBold = isBold) else it
                                    }
                                    onMenuChange(menu.copy(freeElements = updated))
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Fondo tipo distintivo/tarjeta:", style = MaterialTheme.typography.bodySmall)
                            Switch(
                                checked = selectedElement.hasBackground,
                                onCheckedChange = { hasBg ->
                                    val updated = menu.freeElements.map {
                                        if (it.id == selectedElement.id) it.copy(hasBackground = hasBg) else it
                                    }
                                    onMenuChange(menu.copy(freeElements = updated))
                                }
                            )
                        }

                        if (selectedElement.hasBackground) {
                            ColorPickerRow(
                                title = "Color de Fondo del Distintivo",
                                selectedHex = selectedElement.bgColorHex,
                                onColorSelected = { newHex ->
                                    val updated = menu.freeElements.map {
                                        if (it.id == selectedElement.id) it.copy(bgColorHex = newHex) else it
                                    }
                                    onMenuChange(menu.copy(freeElements = updated))
                                },
                                savedCustomColors = savedColors
                            )

                            // Background Opacity
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Opacidad de Fondo:", style = MaterialTheme.typography.bodySmall)
                                Text("${(selectedElement.bgOpacity * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                            }
                            Slider(
                                value = selectedElement.bgOpacity,
                                onValueChange = { newOp ->
                                    val updated = menu.freeElements.map {
                                        if (it.id == selectedElement.id) it.copy(bgOpacity = newOp) else it
                                    }
                                    onMenuChange(menu.copy(freeElements = updated))
                                },
                                valueRange = 0.1f..1.0f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        // PHOTO ELEMENT SETTINGS
                        if (!selectedElement.imageUri.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                AsyncImage(
                                    model = selectedElement.imageUri,
                                    contentDescription = "Foto plato",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Foto cargada", style = MaterialTheme.typography.titleSmall)
                                    Text("Ubicada en la carta", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }

                        // Photo Picker Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    targetElementIdForExternal = selectedElement.id
                                    val pickIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                        type = "image/*"
                                        addCategory(Intent.CATEGORY_OPENABLE)
                                    }
                                    val chooser = Intent.createChooser(pickIntent, "Seleccionar foto con app externa")
                                    externalPhotoLauncher.launch(chooser)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Elegir foto\n(App externa)",
                                    style = MaterialTheme.typography.labelSmall,
                                    softWrap = true,
                                    maxLines = 2
                                )
                            }

                            FilledTonalButton(
                                onClick = { onOpenImagePickerForElement(selectedElement.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Fotos\nguardadas",
                                    style = MaterialTheme.typography.labelSmall,
                                    softWrap = true,
                                    maxLines = 2
                                )
                            }
                        }

                        // Corner radius for photo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Bordes redondeados:", style = MaterialTheme.typography.bodySmall)
                            Text("${selectedElement.cornerRadiusDp.toInt()} dp", style = MaterialTheme.typography.labelSmall)
                        }
                        Slider(
                            value = selectedElement.cornerRadiusDp,
                            onValueChange = { newR ->
                                val updated = menu.freeElements.map {
                                    if (it.id == selectedElement.id) it.copy(cornerRadiusDp = newR) else it
                                }
                                onMenuChange(menu.copy(freeElements = updated))
                            },
                            valueRange = 0f..40f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Position X Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Posición Horizontal (X):", style = MaterialTheme.typography.bodySmall)
                        Text("${(selectedElement.xRatio * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = selectedElement.xRatio,
                        onValueChange = { newX ->
                            val updated = menu.freeElements.map {
                                if (it.id == selectedElement.id) it.copy(xRatio = newX) else it
                            }
                            onMenuChange(menu.copy(freeElements = updated))
                        },
                        valueRange = 0.0f..1.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Position Y Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Posición Vertical (Y):", style = MaterialTheme.typography.bodySmall)
                        Text("${(selectedElement.yRatio * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = selectedElement.yRatio,
                        onValueChange = { newY ->
                            val updated = menu.freeElements.map {
                                if (it.id == selectedElement.id) it.copy(yRatio = newY) else it
                            }
                            onMenuChange(menu.copy(freeElements = updated))
                        },
                        valueRange = 0.0f..1.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Scale Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Escala / Zoom:", style = MaterialTheme.typography.bodySmall)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${(selectedElement.scale * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            if (selectedElement.scale != 1.0f) {
                                TextButton(
                                    onClick = {
                                        val updated = menu.freeElements.map {
                                            if (it.id == selectedElement.id) it.copy(scale = 1.0f) else it
                                        }
                                        onMenuChange(menu.copy(freeElements = updated))
                                    },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("100%", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                    Slider(
                        value = selectedElement.scale,
                        onValueChange = { newScale ->
                            val updated = menu.freeElements.map {
                                if (it.id == selectedElement.id) it.copy(scale = newScale) else it
                            }
                            onMenuChange(menu.copy(freeElements = updated))
                        },
                        valueRange = 0.2f..3.5f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Rotation Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rotación / Inclinación:", style = MaterialTheme.typography.bodySmall)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${selectedElement.rotation.toInt()}°", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            if (selectedElement.rotation != 0f) {
                                TextButton(
                                    onClick = {
                                        val updated = menu.freeElements.map {
                                            if (it.id == selectedElement.id) it.copy(rotation = 0f) else it
                                        }
                                        onMenuChange(menu.copy(freeElements = updated))
                                    },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("0°", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                    Slider(
                        value = selectedElement.rotation,
                        onValueChange = { newRot ->
                            val updated = menu.freeElements.map {
                                if (it.id == selectedElement.id) it.copy(rotation = newRot) else it
                            }
                            onMenuChange(menu.copy(freeElements = updated))
                        },
                        valueRange = -180f..180f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Button to proceed to Export step
        Button(
            onClick = { onNextStep?.invoke() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 24.dp)
                .testTag("advanced_goto_export_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Continuar al paso de Exportar",
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                softWrap = true
            )
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}
