package com.example.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.MenuData
import com.example.ui.components.*
import com.example.ui.steps.*
import com.example.ui.theme.Amber500
import com.example.ui.theme.Zinc950
import com.example.viewmodel.MenuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartaApp(
    viewModel: MenuViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentMenu by viewModel.currentMenu.collectAsStateWithLifecycle()
    val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val savedMenus by viewModel.savedMenus.collectAsStateWithLifecycle()
    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()
    val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val showPreviewFullscreen by viewModel.showPreviewFullscreen.collectAsStateWithLifecycle()
    val showBulkImportDialog by viewModel.showBulkImportDialog.collectAsStateWithLifecycle()
    val showTemplatesDialog by viewModel.showTemplatesDialog.collectAsStateWithLifecycle()
    val showDraftsManagerSheet by viewModel.showDraftsManagerSheet.collectAsStateWithLifecycle()

    val savedImages by viewModel.savedImages.collectAsStateWithLifecycle()
    val customColors by viewModel.customColors.collectAsStateWithLifecycle()
    val customBadges by viewModel.customBadges.collectAsStateWithLifecycle()
    val customFonts by viewModel.customFonts.collectAsStateWithLifecycle()
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    val showImagePickerSheet by viewModel.showImagePickerSheet.collectAsStateWithLifecycle()
    val imagePickerForLogo by viewModel.imagePickerForLogo.collectAsStateWithLifecycle()

    var showLivePreviewFloating by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    // Back handling
    BackHandler(enabled = showPreviewFullscreen || currentStep > 0) {
        if (showPreviewFullscreen) {
            viewModel.togglePreviewFullscreen(false)
        } else if (currentStep > 0) {
            viewModel.setStep(currentStep - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Logo: letter "C" in amber rounded square
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Amber500),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "C",
                                color = Zinc950,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                        }

                        Column {
                            Text(
                                text = "Carta",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Diseñador de menús",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Undo
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = canUndo,
                        modifier = Modifier.testTag("action_undo")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Deshacer",
                            tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
                        )
                    }

                    // Redo
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = canRedo,
                        modifier = Modifier.testTag("action_redo")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Redo,
                            contentDescription = "Rehacer",
                            tint = if (canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
                        )
                    }

                    // Floating preview toggle (steps 0, 1, 2, 3)
                    if (currentStep < 4) {
                        IconButton(
                            onClick = { showLivePreviewFloating = !showLivePreviewFloating },
                            modifier = Modifier.testTag("toggle_live_preview")
                        ) {
                            Icon(
                                imageVector = if (showLivePreviewFloating) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Vista previa flotante",
                                tint = if (showLivePreviewFloating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Templates
                    IconButton(
                        onClick = { viewModel.setShowTemplates(true) },
                        modifier = Modifier.testTag("action_templates_top")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Plantillas",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Drafts
                    IconButton(
                        onClick = { viewModel.setShowDraftsManager(true) },
                        modifier = Modifier.testTag("action_drafts_top")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Borradores y carpetas",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Column {
                // Next / Previous step wizard footer
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentStep > 0) {
                            TextButton(
                                onClick = { viewModel.setStep(currentStep - 1) },
                                modifier = Modifier.testTag("wizard_prev_button")
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Paso anterior")
                            }
                        } else {
                            Spacer(Modifier.width(1.dp))
                        }

                        if (currentStep < 4) {
                            Button(
                                onClick = { viewModel.setStep(currentStep + 1) },
                                modifier = Modifier.testTag("wizard_next_button")
                            ) {
                                Text(
                                    when (currentStep) {
                                        0 -> "Siguiente: Platos"
                                        1 -> "Siguiente: Estilo"
                                        2 -> "Siguiente: Avanzado"
                                        else -> "Siguiente: Exportar"
                                    }
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        } else {
                            Button(
                                onClick = { viewModel.saveCurrentDraft() },
                                modifier = Modifier.testTag("wizard_save_button")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Guardar carta")
                            }
                        }
                    }
                }

                // 5-Step Navigation Bar
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val stepItems = listOf(
                        Triple(0, "Fondo", Icons.Default.Wallpaper),
                        Triple(1, "Platos", Icons.Default.RestaurantMenu),
                        Triple(2, "Estilo", Icons.Default.Palette),
                        Triple(3, "Avanzado", Icons.Default.AutoAwesome),
                        Triple(4, "Exportar", Icons.Default.Share)
                    )

                    stepItems.forEach { (index, label, icon) ->
                        NavigationBarItem(
                            selected = currentStep == index,
                            onClick = { viewModel.setStep(index) },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Zinc950,
                                indicatorColor = Amber500
                            ),
                            modifier = Modifier.testTag("nav_step_$index")
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Step Content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                label = "step_transition"
            ) { step ->
                when (step) {
                    0 -> Step1Background(
                        menu = currentMenu,
                        savedColors = customColors,
                        onMenuChange = { viewModel.updateMenu(it) },
                        onOpenImagePicker = { forLogo -> viewModel.openImagePicker(forLogo) },
                        onExternalImagePicked = { uri, forLogo -> viewModel.saveAndApplyImage(uri, forLogo) },
                        onSaveCustomColor = { viewModel.saveCustomColor(it) },
                        onDeleteCustomColor = { viewModel.deleteCustomColor(it) }
                    )
                    1 -> Step2Dishes(
                        menu = currentMenu,
                        customBadges = customBadges,
                        onMenuChange = { viewModel.updateMenu(it) },
                        onOpenBulkImport = { viewModel.setShowBulkImport(true) },
                        onOpenTemplates = { viewModel.setShowTemplates(true) },
                        onAddCustomBadge = { viewModel.saveCustomBadge(it) },
                        onDeleteCustomBadge = { viewModel.deleteCustomBadge(it) }
                    )
                    2 -> Step3Style(
                        menu = currentMenu,
                        savedColors = customColors,
                        customFonts = customFonts,
                        onMenuChange = { viewModel.updateMenu(it) },
                        onSaveCustomColor = { viewModel.saveCustomColor(it) },
                        onDeleteCustomColor = { viewModel.deleteCustomColor(it) },
                        onImportFont = { viewModel.importCustomFont(it) },
                        onDeleteFont = { viewModel.deleteCustomFont(it) }
                    )
                    3 -> Step3AdvancedElements(
                        menu = currentMenu,
                        savedColors = customColors,
                        customFonts = customFonts,
                        onMenuChange = { viewModel.updateMenu(it) },
                        onOpenImagePickerForElement = { viewModel.openImagePickerForElement(it) },
                        onExternalPhotoPicked = { uri, elemId ->
                            viewModel.saveAndApplyImage(uri, false)
                        },
                        onFullscreenPreview = { viewModel.togglePreviewFullscreen(true) },
                        onNextStep = { viewModel.setStep(4) },
                        onImportFont = { viewModel.importCustomFont(it) },
                        onDeleteFont = { viewModel.deleteCustomFont(it) }
                    )
                    4 -> Step4Export(
                        menu = currentMenu,
                        folders = folders,
                        isExporting = isExporting,
                        onMenuChange = { viewModel.updateMenu(it) },
                        onSaveDraft = { viewModel.saveCurrentDraft() },
                        onSaveAsNewDraft = { name, folderId -> viewModel.saveAsNewDraft(name, folderId) },
                        onOpenDraftsManager = { viewModel.setShowDraftsManager(true) },
                        onExportToGallery = { isPng -> viewModel.exportToGallery(context, isPng) },
                        onShareImage = { isPng, onReady -> viewModel.shareMenu(context, isPng, onReady) },
                        onExportFolderPdf = { folderId ->
                            viewModel.exportFolderAsPdf(context, folderId) { _, uri ->
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_SUBJECT, "Carta en PDF")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Compartir Carta en PDF"))
                            }
                        },
                        onExportFolderLongImage = { folderId, saveToGallery ->
                            viewModel.exportFolderAsLongImage(context, folderId, isPng = true, saveToGallery = saveToGallery) { uri ->
                                if (!saveToGallery) {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/png"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(Intent.EXTRA_SUBJECT, "Carta Completa")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Compartir Foto Larga"))
                                }
                            }
                        },
                        onExportFolderPagesToGallery = { folderId ->
                            viewModel.exportFolderPagesToGallery(context, folderId, isPng = true)
                        },
                        onFullscreenPreview = { viewModel.togglePreviewFullscreen(true) }
                    )
                }
            }

            // Floating live preview overlay (Steps 0, 1, 2, 3)
            if (showLivePreviewFloating && currentStep < 4) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .width(180.dp)
                        .height(260.dp)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        MenuPreviewCanvas(
                            menu = currentMenu,
                            modifier = Modifier.fillMaxSize(),
                            onFullscreenClick = { viewModel.togglePreviewFullscreen(true) }
                        )

                        // Close button for floating preview
                        IconButton(
                            onClick = { showLivePreviewFloating = false },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(28.dp)
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar flotante",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Fullscreen Interactive Preview Dialog with Pinch-to-zoom
    if (showPreviewFullscreen) {
        Dialog(
            onDismissRequest = { viewModel.togglePreviewFullscreen(false) },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    MenuPreviewCanvas(
                        menu = currentMenu,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 48.dp, bottom = 48.dp, start = 16.dp, end = 16.dp),
                        enableZoom = true
                    )

                    // Top Bar in fullscreen preview
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.togglePreviewFullscreen(false) },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Black.copy(alpha = 0.6f),
                            contentColor = Color.White
                        ) {
                            Text(
                                text = "Pellizca para zoom · ${currentMenu.style.aspectRatio.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Bottom Bar in fullscreen preview with direct Export button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                viewModel.togglePreviewFullscreen(false)
                                viewModel.setStep(4)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = Zinc950),
                            modifier = Modifier.testTag("preview_goto_export_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Ir a Exportar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Bulk Import Dialog ("Pegar lista de golpe")
    if (showBulkImportDialog) {
        BulkImportDialog(
            onDismiss = { viewModel.setShowBulkImport(false) },
            onImport = { text, replaceAll ->
                viewModel.applyBulkImport(text, replaceAll)
            }
        )
    }

    // Templates Dialog
    if (showTemplatesDialog) {
        TemplatesDialog(
            templates = templates,
            currentMenu = currentMenu,
            onDismiss = { viewModel.setShowTemplates(false) },
            onSelectTemplate = { template ->
                viewModel.loadTemplate(template)
            },
            onSaveCurrentAsTemplate = { name ->
                viewModel.saveCustomTemplate(name)
            },
            onDeleteTemplate = { templateId ->
                viewModel.deleteTemplate(templateId)
            },
            onResetDefaultTemplates = {
                viewModel.resetDefaultTemplates()
            }
        )
    }

    // Drafts and Folders Manager Sheet
    if (showDraftsManagerSheet) {
        DraftsManagerSheet(
            currentMenuId = currentMenu.id,
            folders = folders,
            savedMenus = savedMenus,
            onDismiss = { viewModel.setShowDraftsManager(false) },
            onSelectDraft = { viewModel.loadDraft(it) },
            onDuplicateDraft = { viewModel.duplicateDraft(it) },
            onDeleteDraft = { viewModel.deleteDraft(it) },
            onMoveDraft = { menuId, folderId -> viewModel.moveDraftToFolder(menuId, folderId) },
            onCreateFolder = { name, color -> viewModel.createFolder(name, color) },
            onDeleteFolder = { viewModel.deleteFolder(it) },
            onReorderDraft = { folderId, menuId, moveUp ->
                viewModel.reorderMenuInFolder(folderId, menuId, moveUp)
            },
            onSelectCoverImage = { folderId, isCover ->
                viewModel.openImagePickerForFolder(folderId, isCover)
            },
            onRemoveCoverImage = { folderId, isCover ->
                if (isCover) viewModel.setFolderCoverImage(folderId, null)
                else viewModel.setFolderBackCoverImage(folderId, null)
            },
            onExportFolderPdf = { folderId ->
                viewModel.exportFolderAsPdf(context, folderId) { _, uri ->
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "Carta en PDF")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Compartir Carta en PDF"))
                }
            },
            onExportFolderLongImage = { folderId, saveToGallery ->
                viewModel.exportFolderAsLongImage(context, folderId, isPng = true, saveToGallery = saveToGallery) { uri ->
                    if (!saveToGallery) {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_SUBJECT, "Carta Completa")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Compartir Foto Larga"))
                    }
                }
            },
            onExportFolderPagesToGallery = { folderId ->
                viewModel.exportFolderPagesToGallery(context, folderId, isPng = true)
            },
            onExportZip = {
                viewModel.exportZipBackup { zipFile ->
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        zipFile
                    )
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/zip"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "Copia de Seguridad - Carta")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Compartir Copia ZIP"))
                }
            },
            onImportZip = { uri ->
                viewModel.importZipBackup(uri)
            }
        )
    }

    // Saved Images & External App Photo Picker Sheet
    if (showImagePickerSheet) {
        val currentUri = if (imagePickerForLogo) currentMenu.header.logoUri else currentMenu.background.customImageUri
        SavedImagesSheet(
            isForLogo = imagePickerForLogo,
            currentSelectedUri = currentUri,
            savedImages = savedImages,
            onDismiss = { viewModel.closeImagePicker() },
            onImageSelected = { image ->
                viewModel.applySavedImage(image.uriString, imagePickerForLogo)
                viewModel.closeImagePicker()
            },
            onExternalImagePicked = { uri ->
                viewModel.saveAndApplyImage(uri, imagePickerForLogo)
            },
            onDeleteImage = { image ->
                viewModel.deleteSavedImage(image)
            }
        )
    }
}
