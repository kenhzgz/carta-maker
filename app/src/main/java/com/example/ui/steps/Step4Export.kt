package com.example.ui.steps

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.model.MenuData
import com.example.model.MenuFolder
import com.example.ui.components.MenuPreviewCanvas
import com.example.ui.components.parseComposeColor

@Composable
fun Step4Export(
    menu: MenuData,
    folders: List<MenuFolder>,
    isExporting: Boolean,
    onMenuChange: (MenuData) -> Unit,
    onSaveDraft: () -> Unit,
    onSaveAsNewDraft: (name: String, folderId: String) -> Unit,
    onOpenDraftsManager: () -> Unit,
    onExportToGallery: (isPng: Boolean) -> Unit,
    onShareImage: (isPng: Boolean, (Uri) -> Unit) -> Unit,
    onExportFolderPdf: (folderId: String) -> Unit,
    onExportFolderLongImage: (folderId: String, saveToGallery: Boolean) -> Unit,
    onExportFolderPagesToGallery: ((folderId: String) -> Unit)? = null,
    onFullscreenPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showSaveAsDialog by remember { mutableStateOf(false) }
    var saveAsName by remember { mutableStateOf("${menu.name} (Nueva)") }
    var saveAsFolderId by remember { mutableStateOf(menu.folderId) }
    var exportFormatIsPng by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // High-res preview container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                MenuPreviewCanvas(
                    menu = menu,
                    modifier = Modifier.fillMaxSize(),
                    enableZoom = true,
                    onFullscreenClick = onFullscreenPreview
                )
            }
        }

        // --- 1. Guardar Borrador Local ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Borrador de la Carta",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = menu.name,
                    onValueChange = { onMenuChange(menu.copy(name = it)) },
                    label = { Text("Nombre del borrador") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("draft_name_input")
                )

                // Folder Selection Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Carpeta:", style = MaterialTheme.typography.bodySmall)

                    var folderMenuExpanded by remember { mutableStateOf(false) }
                    val currentFolder = folders.find { it.id == menu.folderId } ?: folders.firstOrNull()

                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .clickable { folderMenuExpanded = true }
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (currentFolder != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(parseComposeColor(currentFolder.colorHex))
                                    )
                                    Text(currentFolder.name, style = MaterialTheme.typography.labelMedium)
                                } else {
                                    Text("General", style = MaterialTheme.typography.labelMedium)
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }

                        DropdownMenu(
                            expanded = folderMenuExpanded,
                            onDismissRequest = { folderMenuExpanded = false }
                        ) {
                            folders.forEach { f ->
                                DropdownMenuItem(
                                    text = { Text(f.name) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(parseComposeColor(f.colorHex))
                                        )
                                    },
                                    onClick = {
                                        onMenuChange(menu.copy(folderId = f.id))
                                        folderMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onSaveDraft,
                        modifier = Modifier.weight(1f).testTag("save_draft_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Guardar")
                    }

                    OutlinedButton(
                        onClick = {
                            saveAsName = "${menu.name} (Copia)"
                            saveAsFolderId = menu.folderId
                            showSaveAsDialog = true
                        },
                        modifier = Modifier.weight(1f).testTag("save_as_new_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Guardar copia")
                    }
                }

                TextButton(
                    onClick = onOpenDraftsManager,
                    modifier = Modifier.fillMaxWidth().testTag("open_drafts_manager_button")
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Abrir Gestor de Borradores y Carpetas")
                }
            }
        }

        // --- 2. Exportar Imagen de Alta Resolución ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Exportar Carta",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Format selector (PNG vs JPG)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Formato de imagen:", style = MaterialTheme.typography.bodySmall)
                    SingleChoiceSegmentedButtonRow {
                        SegmentedButton(
                            selected = exportFormatIsPng,
                            onClick = { exportFormatIsPng = true },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text("PNG (Máx Calidad)", style = MaterialTheme.typography.labelSmall)
                        }
                        SegmentedButton(
                            selected = !exportFormatIsPng,
                            onClick = { exportFormatIsPng = false },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) {
                            Text("JPG (Ligero)", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Resolution preview tag
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Resolución de salida:", style = MaterialTheme.typography.labelSmall)
                        Text(
                            "${menu.style.aspectRatio.exportWidth} x ${menu.style.aspectRatio.exportHeight} px",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Action Buttons
                Button(
                    onClick = { onExportToGallery(exportFormatIsPng) },
                    enabled = !isExporting,
                    modifier = Modifier.fillMaxWidth().testTag("export_gallery_button")
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Generando alta resolución...")
                    } else {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Descargar imagen en Galería / Fotos")
                    }
                }

                FilledTonalButton(
                    onClick = {
                        onShareImage(exportFormatIsPng) { shareUri ->
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = if (exportFormatIsPng) "image/png" else "image/jpeg"
                                putExtra(Intent.EXTRA_STREAM, shareUri)
                                putExtra(Intent.EXTRA_SUBJECT, "Carta - ${menu.header.restaurantName}")
                                putExtra(Intent.EXTRA_TEXT, "Aquí tienes la carta de ${menu.header.restaurantName}")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartir Carta"))
                        }
                    },
                    enabled = !isExporting,
                    modifier = Modifier.fillMaxWidth().testTag("share_menu_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Compartir directamente (WhatsApp, Instagram...)")
                }
            }
        }

        // --- 3. Exportar Carta Completa Multi-Página ---
        val currentFolder = folders.find { it.id == menu.folderId } ?: folders.firstOrNull()
        if (currentFolder != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column {
                        Text(
                            text = "Carta Completa: ${currentFolder.name}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Exporta todas las páginas ordenadas de esta carta" +
                                    (if (currentFolder.coverImageUri != null) " (con Portada)" else "") +
                                    (if (currentFolder.backCoverImageUri != null) " (con Foto Final)" else ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { onExportFolderPdf(currentFolder.id) },
                            enabled = !isExporting,
                            modifier = Modifier.weight(1f).testTag("export_carta_completa_pdf"),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Exportar\nPDF",
                                style = MaterialTheme.typography.labelSmall,
                                softWrap = true,
                                maxLines = 2
                            )
                        }

                        if (onExportFolderPagesToGallery != null) {
                            FilledTonalButton(
                                onClick = { onExportFolderPagesToGallery(currentFolder.id) },
                                enabled = !isExporting,
                                modifier = Modifier.weight(1f).testTag("export_folder_pages_gallery"),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Páginas en\nGalería",
                                    style = MaterialTheme.typography.labelSmall,
                                    softWrap = true,
                                    maxLines = 2
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = { onExportFolderLongImage(currentFolder.id, true) },
                            enabled = !isExporting,
                            modifier = Modifier.weight(1f).testTag("export_carta_completa_long_image"),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.ViewAgenda, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Foto Larga\nContinua",
                                style = MaterialTheme.typography.labelSmall,
                                softWrap = true,
                                maxLines = 2
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onOpenDraftsManager,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Reordenar páginas, portada o foto final")
                    }
                }
            }
        }
    }

    // Save As New Draft Dialog
    if (showSaveAsDialog) {
        AlertDialog(
            onDismissRequest = { showSaveAsDialog = false },
            title = { Text("Guardar como nuevo borrador") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = saveAsName,
                        onValueChange = { saveAsName = it },
                        label = { Text("Nombre del borrador") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Carpeta de destino:", style = MaterialTheme.typography.bodySmall)
                    folders.forEach { f ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { saveAsFolderId = f.id }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(
                                selected = saveAsFolderId == f.id,
                                onClick = { saveAsFolderId = f.id }
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(parseComposeColor(f.colorHex))
                            )
                            Text(f.name, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveAsNewDraft(saveAsName, saveAsFolderId)
                        showSaveAsDialog = false
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveAsDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
