package com.example.ui.steps

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.model.*
import com.example.ui.components.ColorPickerRow

@Composable
fun Step1Background(
    menu: MenuData,
    onMenuChange: (MenuData) -> Unit,
    onOpenImagePicker: (forLogo: Boolean) -> Unit,
    onExternalImagePicked: (Uri, forLogo: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // External App File/Photo Picker Launchers for direct access
    val externalBgPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        if (uri != null) {
            onExternalImagePicked(uri, false)
        }
    }

    val externalLogoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        if (uri != null) {
            onExternalImagePicked(uri, true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- 1. Background Type Selector ---
        Text(
            text = "1. Tipo de Fondo",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = menu.background.type == BackgroundType.TEXTURE,
                onClick = {
                    onMenuChange(menu.copy(background = menu.background.copy(type = BackgroundType.TEXTURE)))
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                modifier = Modifier.testTag("bg_type_texture")
            ) {
                Text("Texturas", style = MaterialTheme.typography.labelMedium)
            }
            SegmentedButton(
                selected = menu.background.type == BackgroundType.SOLID_COLOR,
                onClick = {
                    onMenuChange(menu.copy(background = menu.background.copy(type = BackgroundType.SOLID_COLOR)))
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                modifier = Modifier.testTag("bg_type_solid")
            ) {
                Text("Color Liso", style = MaterialTheme.typography.labelMedium)
            }
            SegmentedButton(
                selected = menu.background.type == BackgroundType.CUSTOM_IMAGE,
                onClick = {
                    onMenuChange(menu.copy(background = menu.background.copy(type = BackgroundType.CUSTOM_IMAGE)))
                },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                modifier = Modifier.testTag("bg_type_custom")
            ) {
                Text("Foto Propia", style = MaterialTheme.typography.labelMedium)
            }
        }

        // Texture Grid or Solid Color or Custom Photo
        when (menu.background.type) {
            BackgroundType.TEXTURE -> {
                Text(
                    text = "Texturas predeterminadas:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val textures = TexturePreset.values()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (row in textures.toList().chunked(3)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (t in row) {
                                val isSelected = menu.background.texture == t
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(72.dp)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            val newPrimaryText = if (t == TexturePreset.MARMOL || t == TexturePreset.KRAFT) "#1A1A1A" else "#F8FAFC"
                                            val newSecText = if (t == TexturePreset.MARMOL || t == TexturePreset.KRAFT) "#555555" else "#94A3B8"
                                            onMenuChange(
                                                menu.copy(
                                                    background = menu.background.copy(texture = t),
                                                    style = menu.style.copy(
                                                        primaryTextColorHex = newPrimaryText,
                                                        secondaryTextColorHex = newSecText
                                                    )
                                                )
                                            )
                                        }
                                        .testTag("texture_${t.name.lowercase()}"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = t.displayName,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            BackgroundType.SOLID_COLOR -> {
                ColorPickerRow(
                    title = "Color de Fondo del Restaurante",
                    selectedHex = menu.background.solidColorHex,
                    onColorSelected = { hex ->
                        onMenuChange(menu.copy(background = menu.background.copy(solidColorHex = hex)))
                    }
                )
            }

            BackgroundType.CUSTOM_IMAGE -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (menu.background.customImageUri != null) {
                            AsyncImage(
                                model = menu.background.customImageUri,
                                contentDescription = "Fondo personalizado",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                "Selecciona una foto para el fondo de tu carta",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val pickIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                        type = "image/*"
                                        addCategory(Intent.CATEGORY_OPENABLE)
                                    }
                                    val chooser = Intent.createChooser(pickIntent, "Seleccionar fondo con app externa")
                                    externalBgPickerLauncher.launch(chooser)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("upload_bg_external_button")
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("App externa", style = MaterialTheme.typography.labelSmall)
                            }

                            FilledTonalButton(
                                onClick = { onOpenImagePicker(false) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("view_saved_bg_photos_button")
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Fotos guardadas", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        if (menu.background.customImageUri != null) {
                            TextButton(
                                onClick = {
                                    onMenuChange(
                                        menu.copy(
                                            background = menu.background.copy(
                                                customImageUri = null,
                                                type = BackgroundType.TEXTURE
                                            )
                                        )
                                    )
                                }
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.width(4.dp))
                                Text("Quitar foto de fondo", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // --- 2. Capa de Contraste ---
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "2. Capa de Contraste",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${(menu.background.contrastOpacity * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Mode: Oscurecer vs Aclarar
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = menu.background.contrastMode == ContrastMode.DARKEN,
                    onClick = {
                        onMenuChange(menu.copy(background = menu.background.copy(contrastMode = ContrastMode.DARKEN)))
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Oscurecer", style = MaterialTheme.typography.labelSmall)
                }
                SegmentedButton(
                    selected = menu.background.contrastMode == ContrastMode.LIGHTEN,
                    onClick = {
                        onMenuChange(menu.copy(background = menu.background.copy(contrastMode = ContrastMode.LIGHTEN)))
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Aclarar", style = MaterialTheme.typography.labelSmall)
                }
            }

            Slider(
                value = menu.background.contrastOpacity,
                onValueChange = {
                    onMenuChange(menu.copy(background = menu.background.copy(contrastOpacity = it)))
                },
                valueRange = 0.0f..0.85f,
                modifier = Modifier.fillMaxWidth().testTag("contrast_slider")
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // --- 3. Logo del Restaurante ---
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "3. Logo del Restaurante",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (menu.header.logoUri != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val shapeModifier = when (menu.header.logoShape) {
                                LogoShape.ORIGINAL -> Modifier
                                LogoShape.CIRCLE -> Modifier.clip(CircleShape)
                                LogoShape.ROUNDED -> Modifier.clip(RoundedCornerShape(8.dp))
                            }
                            AsyncImage(
                                model = menu.header.logoUri,
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(56.dp)
                                    .then(shapeModifier),
                                contentScale = ContentScale.Crop
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Logo cargado", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Guardado localmente en la app",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = {
                                    onMenuChange(menu.copy(header = menu.header.copy(logoUri = null)))
                                }
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Quitar logo", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        // Logo Shape
                        Text("Forma del logo:", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LogoShape.values().forEach { shape ->
                                FilterChip(
                                    selected = menu.header.logoShape == shape,
                                    onClick = {
                                        onMenuChange(menu.copy(header = menu.header.copy(logoShape = shape)))
                                    },
                                    label = { Text(shape.displayName) }
                                )
                            }
                        }

                        // Logo Size Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tamaño del logo:", style = MaterialTheme.typography.bodySmall)
                            Text("${(menu.header.logoSize * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                        }
                        Slider(
                            value = menu.header.logoSize,
                            onValueChange = {
                                onMenuChange(menu.copy(header = menu.header.copy(logoSize = it)))
                            },
                            valueRange = 0.5f..2.0f,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Change Logo Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val pickIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                        type = "image/*"
                                        addCategory(Intent.CATEGORY_OPENABLE)
                                    }
                                    val chooser = Intent.createChooser(pickIntent, "Seleccionar logo con app externa")
                                    externalLogoPickerLauncher.launch(chooser)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Cambiar con app externa", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = { onOpenImagePicker(true) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Mis fotos", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val pickIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                type = "image/*"
                                addCategory(Intent.CATEGORY_OPENABLE)
                            }
                            val chooser = Intent.createChooser(pickIntent, "Seleccionar logo con app externa")
                            externalLogoPickerLauncher.launch(chooser)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("upload_logo_external_button")
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Subir logo (App externa)", style = MaterialTheme.typography.labelSmall)
                    }

                    FilledTonalButton(
                        onClick = { onOpenImagePicker(true) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("view_saved_logo_photos_button")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Fotos guardadas", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
