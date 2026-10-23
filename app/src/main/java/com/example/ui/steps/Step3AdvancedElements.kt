package com.example.ui.steps

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.model.FreeElement
import com.example.model.FreeElementType
import com.example.model.MenuData
import com.example.ui.components.ColorPickerRow
import com.example.ui.components.MenuPreviewCanvas
import com.example.ui.components.parseComposeColor
import java.util.UUID

@Composable
fun Step3AdvancedElements(
    menu: MenuData,
    savedColors: List<String>,
    onMenuChange: (MenuData) -> Unit,
    onOpenImagePickerForElement: (elementId: String) -> Unit,
    onExternalPhotoPicked: (Uri, elementId: String) -> Unit,
    onFullscreenPreview: (() -> Unit)? = null,
    onNextStep: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedElementId by remember { mutableStateOf<String?>(menu.freeElements.firstOrNull()?.id) }
    var previewSize by remember { mutableStateOf(IntSize.Zero) }

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
                text = "Añade textos destacados, sellos, pegatinas y fotos sueltas de platos. Puedes arrastrarlos o usar los controles de posición.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Live Interactive Canvas with Drag Support
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .onSizeChanged { previewSize = it },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(selectedElementId, menu.freeElements) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val curId = selectedElementId ?: menu.freeElements.firstOrNull()?.id
                            if (curId != null && previewSize.width > 0 && previewSize.height > 0) {
                                val dxRatio = dragAmount.x / previewSize.width.toFloat()
                                val dyRatio = dragAmount.y / previewSize.height.toFloat()

                                val updatedElements = menu.freeElements.map { elem ->
                                    if (elem.id == curId) {
                                        elem.copy(
                                            xRatio = (elem.xRatio + dxRatio).coerceIn(0.05f, 0.95f),
                                            yRatio = (elem.yRatio + dyRatio).coerceIn(0.05f, 0.95f)
                                        )
                                    } else elem
                                }
                                onMenuChange(menu.copy(freeElements = updatedElements))
                            }
                        }
                    }
            ) {
                MenuPreviewCanvas(
                    menu = menu,
                    modifier = Modifier.fillMaxSize(),
                    onFullscreenClick = onFullscreenPreview
                )

                // Drag gesture helper overlay indicator
                if (selectedElement != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.75f),
                        contentColor = Color.White
                    ) {
                        Text(
                            text = "Arrastra en pantalla para mover el elemento seleccionado",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
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
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Posición Horizontal (X):", style = MaterialTheme.typography.bodySmall)
                        Text("${(selectedElement.xRatio * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                    }
                    Slider(
                        value = selectedElement.xRatio,
                        onValueChange = { newX ->
                            val updated = menu.freeElements.map {
                                if (it.id == selectedElement.id) it.copy(xRatio = newX) else it
                            }
                            onMenuChange(menu.copy(freeElements = updated))
                        },
                        valueRange = 0.05f..0.95f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Position Y Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Posición Vertical (Y):", style = MaterialTheme.typography.bodySmall)
                        Text("${(selectedElement.yRatio * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                    }
                    Slider(
                        value = selectedElement.yRatio,
                        onValueChange = { newY ->
                            val updated = menu.freeElements.map {
                                if (it.id == selectedElement.id) it.copy(yRatio = newY) else it
                            }
                            onMenuChange(menu.copy(freeElements = updated))
                        },
                        valueRange = 0.05f..0.95f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Scale Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Escala / Tamaño:", style = MaterialTheme.typography.bodySmall)
                        Text("${(selectedElement.scale * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                    }
                    Slider(
                        value = selectedElement.scale,
                        onValueChange = { newScale ->
                            val updated = menu.freeElements.map {
                                if (it.id == selectedElement.id) it.copy(scale = newScale) else it
                            }
                            onMenuChange(menu.copy(freeElements = updated))
                        },
                        valueRange = 0.4f..2.5f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Rotation Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Inclinación / Rotación:", style = MaterialTheme.typography.bodySmall)
                        Text("${selectedElement.rotation.toInt()}°", style = MaterialTheme.typography.labelSmall)
                    }
                    Slider(
                        value = selectedElement.rotation,
                        onValueChange = { newRot ->
                            val updated = menu.freeElements.map {
                                if (it.id == selectedElement.id) it.copy(rotation = newRot) else it
                            }
                            onMenuChange(menu.copy(freeElements = updated))
                        },
                        valueRange = -45f..45f,
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
