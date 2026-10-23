package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.model.MenuData
import com.example.model.MenuFolder
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftsManagerSheet(
    currentMenuId: String,
    folders: List<MenuFolder>,
    savedMenus: List<MenuData>,
    onDismiss: () -> Unit,
    onSelectDraft: (String) -> Unit,
    onDuplicateDraft: (String) -> Unit,
    onDeleteDraft: (String) -> Unit,
    onMoveDraft: (String, String) -> Unit,
    onCreateFolder: (String, String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onReorderDraft: (folderId: String, menuId: String, moveUp: Boolean) -> Unit,
    onSelectCoverImage: (folderId: String, isCover: Boolean) -> Unit,
    onRemoveCoverImage: (folderId: String, isCover: Boolean) -> Unit,
    onExportFolderPdf: (folderId: String) -> Unit,
    onExportFolderLongImage: (folderId: String, saveToGallery: Boolean) -> Unit,
    onExportZip: () -> Unit,
    onImportZip: (Uri) -> Unit
) {
    var selectedFolderId by remember {
        mutableStateOf(folders.firstOrNull()?.id ?: "default")
    }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var movingMenuId by remember { mutableStateOf<String?>(null) }

    // File picker for importing ZIP
    val importZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportZip(uri)
        }
    }

    val activeFolder = folders.find { it.id == selectedFolderId } ?: folders.firstOrNull()

    // Ordered menus for this folder
    val folderMenus = remember(selectedFolderId, savedMenus, activeFolder) {
        if (activeFolder == null) emptyList()
        else {
            val allInFolder = savedMenus.filter { it.folderId == activeFolder.id }
            if (activeFolder.menuOrderIds.isEmpty()) allInFolder
            else {
                val orderMap = activeFolder.menuOrderIds.withIndex().associate { it.value to it.index }
                allInFolder.sortedBy { orderMap[it.id] ?: Int.MAX_VALUE }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CollectionsBookmark,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "Cartas Completas & Páginas",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Agrupa borradores como páginas, añade portada y foto final",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            // Backup bar
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Copia de Seguridad:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(
                            onClick = onExportZip,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Exportar ZIP", style = MaterialTheme.typography.labelSmall)
                        }
                        TextButton(
                            onClick = { importZipLauncher.launch(arrayOf("application/zip", "application/octet-stream")) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Unarchive, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Restaurar ZIP", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Folder selector chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Seleccionar Carta:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(
                    onClick = { showNewFolderDialog = true },
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Nueva Carta", style = MaterialTheme.typography.labelSmall)
                }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(folders) { folder ->
                    val count = savedMenus.count { it.folderId == folder.id }
                    val isSelected = selectedFolderId == folder.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFolderId = folder.id },
                        label = { Text("${folder.name} ($count pág)") },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(parseComposeColor(folder.colorHex))
                            )
                        }
                    )
                }
            }

            // Main Content Area of the Active Folder
            if (activeFolder != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // --- 1. Export Full Multi-Page Menu Banner ---
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Exportar \"${activeFolder.name}\"",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "${folderMenus.size} páginas de menú" +
                                                    (if (activeFolder.coverImageUri != null) " + Portada" else "") +
                                                    (if (activeFolder.backCoverImageUri != null) " + Foto Final" else ""),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onExportFolderPdf(activeFolder.id) },
                                        enabled = folderMenus.isNotEmpty(),
                                        modifier = Modifier.weight(1f).testTag("export_folder_pdf_button")
                                    ) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Exportar PDF", style = MaterialTheme.typography.labelSmall)
                                    }

                                    FilledTonalButton(
                                        onClick = { onExportFolderLongImage(activeFolder.id, true) },
                                        enabled = folderMenus.isNotEmpty(),
                                        modifier = Modifier.weight(1f).testTag("export_folder_long_image_button")
                                    ) {
                                        Icon(Icons.Default.ViewAgenda, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Foto Larga Continua", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }

                    // --- 2. Portada & Foto Final Slots ---
                    item {
                        Text(
                            text = "Portada y Cierre de la Carta (Opcionales):",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Cover Slot (At the very beginning)
                            CoverSlotCard(
                                title = "1. Portada (Inicio)",
                                imageUri = activeFolder.coverImageUri,
                                onSelect = { onSelectCoverImage(activeFolder.id, true) },
                                onRemove = { onRemoveCoverImage(activeFolder.id, true) },
                                modifier = Modifier.weight(1f)
                            )

                            // Back Cover Slot (At the very end)
                            CoverSlotCard(
                                title = "Cierre (Foto Final)",
                                imageUri = activeFolder.backCoverImageUri,
                                onSelect = { onSelectCoverImage(activeFolder.id, false) },
                                onRemove = { onRemoveCoverImage(activeFolder.id, false) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // --- 3. Pages List with Reordering ---
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Páginas del Menú (${folderMenus.size}):",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Usa ⬆️ ⬇️ para ordenar",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    if (folderMenus.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Esta carta aún no tiene páginas guardadas.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Guarda borradores en esta carta o mueve páginas existentes aquí.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    } else {
                        itemsIndexed(folderMenus, key = { _, menu -> menu.id }) { index, menu ->
                            val isCurrent = menu.id == currentMenuId
                            val isFirst = index == 0
                            val isLast = index == folderMenus.size - 1

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isCurrent) 1.5.dp else 1.dp,
                                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Page Number Badge
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${index + 1}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onSelectDraft(menu.id) }
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = menu.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isCurrent) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.primary
                                                ) {
                                                    Text(
                                                        text = "Abierta",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "${menu.sections.size} secciones · ${menu.sections.sumOf { it.items.size }} platos",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Up/Down reorder buttons
                                    IconButton(
                                        onClick = { onReorderDraft(activeFolder.id, menu.id, true) },
                                        enabled = !isFirst,
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = "Subir página",
                                            modifier = Modifier.size(16.dp),
                                            tint = if (!isFirst) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                        )
                                    }

                                    IconButton(
                                        onClick = { onReorderDraft(activeFolder.id, menu.id, false) },
                                        enabled = !isLast,
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = "Bajar página",
                                            modifier = Modifier.size(16.dp),
                                            tint = if (!isLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                        )
                                    }

                                    // Move to another folder
                                    IconButton(
                                        onClick = { movingMenuId = menu.id },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.DriveFileMove,
                                            contentDescription = "Mover",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Duplicate
                                    IconButton(
                                        onClick = { onDuplicateDraft(menu.id) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Duplicar",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Delete
                                    if (savedMenus.size > 1) {
                                        IconButton(
                                            onClick = { onDeleteDraft(menu.id) },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Eliminar",
                                                tint = MaterialTheme.colorScheme.error,
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
    }

    // New Folder Dialog
    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text("Nueva Carta Completa") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Nombre de la Carta") },
                    placeholder = { Text("ej: Carta de Vinos, Menú de Verano...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            onCreateFolder(newFolderName, "#F59E0B")
                            newFolderName = ""
                            showNewFolderDialog = false
                        }
                    }
                ) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Move menu to folder dialog
    if (movingMenuId != null) {
        val targetMenu = savedMenus.find { it.id == movingMenuId }
        AlertDialog(
            onDismissRequest = { movingMenuId = null },
            title = { Text("Mover a otra Carta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Selecciona la Carta de destino para \"${targetMenu?.name}\":")
                    folders.forEach { f ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onMoveDraft(movingMenuId!!, f.id)
                                    movingMenuId = null
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(parseComposeColor(f.colorHex))
                            )
                            Text(f.name, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { movingMenuId = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CoverSlotCard(
    title: String,
    imageUri: String?,
    onSelect: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Overlay tag
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Action buttons: Change & Remove
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onSelect,
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Cambiar", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Quitar",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = onSelect)
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "+ Añadir foto",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
