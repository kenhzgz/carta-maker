package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FontStorageManager
import com.example.data.CustomFontItem
import com.example.data.ImageStorageManager
import com.example.data.MenuExporter
import com.example.data.MenuParser
import com.example.data.MenuRepository
import com.example.data.SavedImage
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MenuViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MenuRepository(application)

    private val _currentMenu = MutableStateFlow<MenuData>(MenuTemplates.atelierBistro)
    val currentMenu: StateFlow<MenuData> = _currentMenu.asStateFlow()

    private val _currentStep = MutableStateFlow(0) // 0: Fondo, 1: Platos, 2: Estilo, 3: Avanzado, 4: Exportar
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _folders = MutableStateFlow<List<MenuFolder>>(emptyList())
    val folders: StateFlow<List<MenuFolder>> = _folders.asStateFlow()

    private val _savedMenus = MutableStateFlow<List<MenuData>>(emptyList())
    val savedMenus: StateFlow<List<MenuData>> = _savedMenus.asStateFlow()

    // Saved images in app local storage
    private val _savedImages = MutableStateFlow<List<SavedImage>>(emptyList())
    val savedImages: StateFlow<List<SavedImage>> = _savedImages.asStateFlow()

    // Custom palette colors
    private val _customColors = MutableStateFlow<List<String>>(emptyList())
    val customColors: StateFlow<List<String>> = _customColors.asStateFlow()

    // Custom dish badges
    private val _customBadges = MutableStateFlow<List<String>>(emptyList())
    val customBadges: StateFlow<List<String>> = _customBadges.asStateFlow()

    // Custom imported fonts
    private val _customFonts = MutableStateFlow<List<CustomFontItem>>(emptyList())
    val customFonts: StateFlow<List<CustomFontItem>> = _customFonts.asStateFlow()

    // Dynamic Templates
    private val _templates = MutableStateFlow<List<MenuData>>(emptyList())
    val templates: StateFlow<List<MenuData>> = _templates.asStateFlow()

    private val _showImagePickerSheet = MutableStateFlow(false)
    val showImagePickerSheet: StateFlow<Boolean> = _showImagePickerSheet.asStateFlow()

    private val _imagePickerForLogo = MutableStateFlow(false)
    val imagePickerForLogo: StateFlow<Boolean> = _imagePickerForLogo.asStateFlow()

    // Holds Pair(folderId, isCover) if picking an image for a folder's cover / back cover
    private val _targetFolderForCover = MutableStateFlow<Pair<String, Boolean>?>(null)
    val targetFolderForCover: StateFlow<Pair<String, Boolean>?> = _targetFolderForCover.asStateFlow()

    // Target element ID if picking an image for a FreeElement
    private val _targetElementForPhoto = MutableStateFlow<String?>(null)
    val targetElementForPhoto: StateFlow<String?> = _targetElementForPhoto.asStateFlow()

    // Undo / Redo
    private val undoStack = mutableListOf<MenuData>()
    private val redoStack = mutableListOf<MenuData>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // Dialogs & UI states
    private val _showPreviewFullscreen = MutableStateFlow(false)
    val showPreviewFullscreen: StateFlow<Boolean> = _showPreviewFullscreen.asStateFlow()

    private val _showBulkImportDialog = MutableStateFlow(false)
    val showBulkImportDialog: StateFlow<Boolean> = _showBulkImportDialog.asStateFlow()

    private val _showTemplatesDialog = MutableStateFlow(false)
    val showTemplatesDialog: StateFlow<Boolean> = _showTemplatesDialog.asStateFlow()

    private val _showDraftsManagerSheet = MutableStateFlow(false)
    val showDraftsManagerSheet: StateFlow<Boolean> = _showDraftsManagerSheet.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch(Dispatchers.IO) {
            val loadedFolders = repository.getAllFolders()
            val loadedMenus = repository.getAllMenus()
            val loadedImages = ImageStorageManager.getSavedImages(getApplication())
            val loadedColors = repository.getCustomColors()
            val loadedBadges = repository.getCustomBadges()
            val loadedFonts = FontStorageManager.getCustomFonts(getApplication())
            val loadedTemplates = repository.getAllTemplates()

            _folders.value = loadedFolders
            _savedMenus.value = loadedMenus
            _savedImages.value = loadedImages
            _customColors.value = loadedColors
            _customBadges.value = loadedBadges
            _customFonts.value = loadedFonts
            _templates.value = loadedTemplates

            if (loadedMenus.isNotEmpty()) {
                _currentMenu.value = loadedMenus.first()
            }
        }
    }

    fun loadSavedImages() {
        viewModelScope.launch(Dispatchers.IO) {
            _savedImages.value = ImageStorageManager.getSavedImages(getApplication())
        }
    }

    fun openImagePicker(forLogo: Boolean) {
        _targetFolderForCover.value = null
        _targetElementForPhoto.value = null
        _imagePickerForLogo.value = forLogo
        _showImagePickerSheet.value = true
        loadSavedImages()
    }

    fun openImagePickerForFolder(folderId: String, isCover: Boolean) {
        _targetFolderForCover.value = Pair(folderId, isCover)
        _targetElementForPhoto.value = null
        _showImagePickerSheet.value = true
        loadSavedImages()
    }

    fun openImagePickerForElement(elementId: String) {
        _targetFolderForCover.value = null
        _targetElementForPhoto.value = elementId
        _showImagePickerSheet.value = true
        loadSavedImages()
    }

    fun closeImagePicker() {
        _showImagePickerSheet.value = false
        _targetFolderForCover.value = null
        _targetElementForPhoto.value = null
    }

    fun saveAndApplyImage(sourceUri: Uri, isForLogo: Boolean) {
        viewModelScope.launch {
            val saved = ImageStorageManager.saveImageFromUri(getApplication(), sourceUri)
            if (saved != null) {
                loadSavedImages()
                val targetElem = _targetElementForPhoto.value
                val folderTarget = _targetFolderForCover.value

                if (targetElem != null) {
                    applyImageToElement(saved.uriString, targetElem)
                } else if (folderTarget != null) {
                    if (folderTarget.second) {
                        setFolderCoverImage(folderTarget.first, saved.uriString)
                    } else {
                        setFolderBackCoverImage(folderTarget.first, saved.uriString)
                    }
                } else {
                    applySavedImage(saved.uriString, isForLogo)
                }
                closeImagePicker()
                showToast("Foto guardada en la app y aplicada")
            } else {
                showToast("Error al guardar la foto seleccionada")
            }
        }
    }

    fun applySavedImage(uriString: String, isForLogo: Boolean) {
        val targetElem = _targetElementForPhoto.value
        if (targetElem != null) {
            applyImageToElement(uriString, targetElem)
            _targetElementForPhoto.value = null
            return
        }

        val folderTarget = _targetFolderForCover.value
        if (folderTarget != null) {
            if (folderTarget.second) {
                setFolderCoverImage(folderTarget.first, uriString)
            } else {
                setFolderBackCoverImage(folderTarget.first, uriString)
            }
            return
        }

        val current = _currentMenu.value
        if (isForLogo) {
            updateMenu(current.copy(header = current.header.copy(logoUri = uriString)))
        } else {
            updateMenu(
                current.copy(
                    background = current.background.copy(
                        type = BackgroundType.CUSTOM_IMAGE,
                        customImageUri = uriString
                    )
                )
            )
        }
    }

    fun applyImageToElement(uriString: String, elementId: String) {
        val current = _currentMenu.value
        val updated = current.freeElements.map {
            if (it.id == elementId) it.copy(imageUri = uriString) else it
        }
        updateMenu(current.copy(freeElements = updated))
    }

    // --- Custom Colors Management ---
    fun saveCustomColor(hex: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.getCustomColors().toMutableList()
            if (!list.contains(hex)) {
                list.add(0, hex)
                repository.saveCustomColors(list)
                _customColors.value = list
                withContext(Dispatchers.Main) {
                    showToast("Color guardado en tu paleta personal")
                }
            }
        }
    }

    fun deleteCustomColor(hex: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.getCustomColors().toMutableList()
            list.remove(hex)
            repository.saveCustomColors(list)
            _customColors.value = list
        }
    }

    // --- Custom Badges Management ---
    fun saveCustomBadge(badge: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.getCustomBadges().toMutableList()
            if (!list.contains(badge)) {
                list.add(badge)
                repository.saveCustomBadges(list)
                _customBadges.value = list
                withContext(Dispatchers.Main) {
                    showToast("Etiqueta guardada para todos tus menús")
                }
            }
        }
    }

    fun deleteCustomBadge(badge: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.getCustomBadges().toMutableList()
            list.remove(badge)
            repository.saveCustomBadges(list)
            _customBadges.value = list
        }
    }

    // --- Custom Fonts Management ---
    fun importCustomFont(uri: Uri) {
        viewModelScope.launch {
            val savedFile = FontStorageManager.saveFontFromUri(getApplication(), uri)
            if (savedFile != null) {
                _customFonts.value = FontStorageManager.getCustomFonts(getApplication())
                showToast("Fuente instalada y guardada en la app")
            } else {
                showToast("Error al importar la fuente")
            }
        }
    }

    fun deleteCustomFont(fontItem: CustomFontItem) {
        viewModelScope.launch {
            val success = FontStorageManager.deleteFont(fontItem.file)
            if (success) {
                _customFonts.value = FontStorageManager.getCustomFonts(getApplication())
                val current = _currentMenu.value
                val path = fontItem.file.absolutePath
                var newStyle = current.style
                if (current.style.customTitleFontPath == path) {
                    newStyle = newStyle.copy(customTitleFontPath = null, titleFont = MenuFontFamily.SERIF)
                }
                if (current.style.customBodyFontPath == path) {
                    newStyle = newStyle.copy(customBodyFontPath = null, bodyFont = MenuFontFamily.SANS)
                }
                if (newStyle != current.style) {
                    updateMenu(current.copy(style = newStyle))
                }
                showToast("Fuente eliminada")
            }
        }
    }

    fun deleteSavedImage(image: SavedImage) {
        viewModelScope.launch {
            val success = ImageStorageManager.deleteImage(getApplication(), image.file.absolutePath)
            if (success) {
                loadSavedImages()
                // If the deleted image was currently in use, clear it
                val current = _currentMenu.value
                var changed = false
                var newHeader = current.header
                var newBackground = current.background

                if (current.header.logoUri == image.uriString || current.header.logoUri == image.file.absolutePath) {
                    newHeader = current.header.copy(logoUri = null)
                    changed = true
                }
                if (current.background.customImageUri == image.uriString || current.background.customImageUri == image.file.absolutePath) {
                    newBackground = current.background.copy(
                        customImageUri = null,
                        type = BackgroundType.TEXTURE
                    )
                    changed = true
                }
                if (changed) {
                    updateMenu(current.copy(header = newHeader, background = newBackground))
                }
                showToast("Foto eliminada correctamente")
            } else {
                showToast("No se pudo eliminar la foto")
            }
        }
    }

    fun setStep(step: Int) {
        _currentStep.value = step.coerceIn(0, 4)
    }

    fun togglePreviewFullscreen(show: Boolean? = null) {
        _currentPreviewFullscreen(show)
    }

    private fun _currentPreviewFullscreen(show: Boolean?) {
        _showPreviewFullscreen.value = show ?: !_showPreviewFullscreen.value
    }

    fun setShowBulkImport(show: Boolean) {
        _showBulkImportDialog.value = show
    }

    fun setShowTemplates(show: Boolean) {
        _showTemplatesDialog.value = show
    }

    fun setShowDraftsManager(show: Boolean) {
        _showDraftsManagerSheet.value = show
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    // --- Menu Editing & Undo/Redo ---

    fun updateMenu(newMenu: MenuData, recordUndo: Boolean = true) {
        if (recordUndo) {
            if (undoStack.size >= 40) undoStack.removeAt(0)
            undoStack.add(_currentMenu.value)
            redoStack.clear()
            _canUndo.value = true
            _canRedo.value = false
        }
        _currentMenu.value = newMenu
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeAt(undoStack.size - 1)
            redoStack.add(_currentMenu.value)
            _currentMenu.value = previous
            _canUndo.value = undoStack.isNotEmpty()
            _canRedo.value = true
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.size - 1)
            undoStack.add(_currentMenu.value)
            _currentMenu.value = next
            _canUndo.value = true
            _canRedo.value = redoStack.isNotEmpty()
        }
    }

    // --- Bulk Import ---

    fun applyBulkImport(rawText: String, replaceAll: Boolean) {
        val parsedSections = MenuParser.parseRawText(rawText)
        if (parsedSections.isEmpty()) {
            showToast("No se detectaron platos en el texto pegado")
            return
        }

        val current = _currentMenu.value
        val updatedSections = if (replaceAll) {
            parsedSections
        } else {
            current.sections + parsedSections
        }
        updateMenu(current.copy(sections = updatedSections))
        showToast("Se añadieron ${parsedSections.sumOf { it.items.size }} platos correctamente")
        _showBulkImportDialog.value = false
    }

    // --- Templates & Drafts ---

    fun loadTemplate(template: MenuData) {
        val newDraft = template.copy(
            id = java.util.UUID.randomUUID().toString(),
            name = template.name,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        updateMenu(newDraft)
        _showTemplatesDialog.value = false
        showToast("Plantilla \"${template.name}\" cargada")
    }

    fun saveCustomTemplate(name: String, menu: MenuData = _currentMenu.value) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveTemplate(name, menu)
            val updated = repository.getAllTemplates()
            _templates.value = updated
            withContext(Dispatchers.Main) {
                showToast("Plantilla \"$name\" guardada con éxito")
            }
        }
    }

    fun deleteTemplate(templateId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = repository.deleteTemplate(templateId)
            _templates.value = updated
            withContext(Dispatchers.Main) {
                showToast("Plantilla eliminada")
            }
        }
    }

    fun resetDefaultTemplates() {
        viewModelScope.launch(Dispatchers.IO) {
            val resetList = repository.resetDefaultTemplates()
            _templates.value = resetList
            withContext(Dispatchers.Main) {
                showToast("Plantillas restablecidas")
            }
        }
    }

    fun loadDraft(menuId: String) {
        val target = _savedMenus.value.find { it.id == menuId }
        if (target != null) {
            updateMenu(target)
            showToast("Borrador \"${target.name}\" abierto")
            _showDraftsManagerSheet.value = false
        }
    }

    fun saveCurrentDraft() {
        viewModelScope.launch(Dispatchers.IO) {
            val toSave = _currentMenu.value
            repository.saveMenu(toSave)
            val updated = repository.getAllMenus()
            _savedMenus.value = updated
            withContext(Dispatchers.Main) {
                showToast("Borrador \"${toSave.name}\" guardado")
            }
        }
    }

    fun saveAsNewDraft(name: String, folderId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newDraft = _currentMenu.value.copy(
                id = java.util.UUID.randomUUID().toString(),
                name = name.ifBlank { "Borrador de Carta" },
                folderId = folderId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.saveMenu(newDraft)
            _currentMenu.value = newDraft
            val updated = repository.getAllMenus()
            _savedMenus.value = updated
            withContext(Dispatchers.Main) {
                showToast("Guardado como nuevo borrador")
            }
        }
    }

    fun duplicateDraft(menuId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val copy = repository.duplicateMenu(menuId)
            val updated = repository.getAllMenus()
            _savedMenus.value = updated
            withContext(Dispatchers.Main) {
                if (copy != null) {
                    showToast("Borrador duplicado")
                }
            }
        }
    }

    fun deleteDraft(menuId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMenu(menuId)
            val updated = repository.getAllMenus()
            _savedMenus.value = updated
            withContext(Dispatchers.Main) {
                showToast("Borrador eliminado")
                if (_currentMenu.value.id == menuId && updated.isNotEmpty()) {
                    _currentMenu.value = updated.first()
                }
            }
        }
    }

    fun moveDraftToFolder(menuId: String, targetFolderId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.moveMenuToFolder(menuId, targetFolderId)
            _savedMenus.value = repository.getAllMenus()
            if (_currentMenu.value.id == menuId) {
                _currentMenu.value = _currentMenu.value.copy(folderId = targetFolderId)
            }
            withContext(Dispatchers.Main) {
                showToast("Borrador movido a la carpeta")
            }
        }
    }

    fun createFolder(name: String, colorHex: String) {
        if (name.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val newFolder = MenuFolder(name = name.trim(), colorHex = colorHex)
            repository.saveFolder(newFolder)
            _folders.value = repository.getAllFolders()
            withContext(Dispatchers.Main) {
                showToast("Carpeta \"$name\" creada")
            }
        }
    }

    fun deleteFolder(folderId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteFolder(folderId)
            _folders.value = repository.getAllFolders()
            _savedMenus.value = repository.getAllMenus()
            withContext(Dispatchers.Main) {
                showToast("Carpeta eliminada")
            }
        }
    }

    // --- Exporting & Sharing ---

    fun exportToGallery(context: Context, isPng: Boolean) {
        viewModelScope.launch {
            _isExporting.value = true
            val menu = _currentMenu.value
            val uri = withContext(Dispatchers.Default) {
                val bmp = MenuExporter.generateHighResBitmap(context, menu)
                MenuExporter.saveToGallery(context, bmp, menu.name, isPng)
            }
            _isExporting.value = false
            if (uri != null) {
                showToast("Carta exportada con éxito en Galería / Fotos")
            } else {
                showToast("Error al guardar la imagen en la galería")
            }
        }
    }

    fun shareMenu(context: Context, isPng: Boolean, onReadyToShare: (Uri) -> Unit) {
        viewModelScope.launch {
            _isExporting.value = true
            val menu = _currentMenu.value
            val uri = withContext(Dispatchers.Default) {
                val bmp = MenuExporter.generateHighResBitmap(context, menu)
                MenuExporter.saveToCacheForSharing(context, bmp, menu.name, isPng)
            }
            _isExporting.value = false
            if (uri != null) {
                onReadyToShare(uri)
            } else {
                showToast("Error al generar la imagen para compartir")
            }
        }
    }

    fun exportZipBackup(onReady: (File) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val zipFile = repository.createBackupZip()
            withContext(Dispatchers.Main) {
                showToast("Copia de seguridad ZIP creada")
                onReady(zipFile)
            }
        }
    }

    fun importZipBackup(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.restoreBackupZip(uri)
            if (success) {
                val loadedFolders = repository.getAllFolders()
                val loadedMenus = repository.getAllMenus()
                _folders.value = loadedFolders
                _savedMenus.value = loadedMenus
                if (loadedMenus.isNotEmpty()) {
                    _currentMenu.value = loadedMenus.first()
                }
                withContext(Dispatchers.Main) {
                    showToast("Copia restaurada correctamente")
                }
            } else {
                withContext(Dispatchers.Main) {
                    showToast("Error al leer el archivo ZIP de copia de seguridad")
                }
            }
        }
    }

    // --- Multi-page Folder (Carta Completa) Features ---

    fun getOrderedMenusForFolder(folderId: String): List<MenuData> {
        val folder = _folders.value.find { it.id == folderId }
        val folderMenus = _savedMenus.value.filter { it.folderId == folderId }
        if (folder == null || folder.menuOrderIds.isEmpty()) {
            return folderMenus
        }
        val orderMap = folder.menuOrderIds.withIndex().associate { it.value to it.index }
        return folderMenus.sortedBy { orderMap[it.id] ?: Int.MAX_VALUE }
    }

    fun reorderMenuInFolder(folderId: String, menuId: String, moveUp: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val folder = _folders.value.find { it.id == folderId } ?: return@launch
            val currentOrdered = getOrderedMenusForFolder(folderId).toMutableList()
            val index = currentOrdered.indexOfFirst { it.id == menuId }
            if (index == -1) return@launch

            if (moveUp && index > 0) {
                val temp = currentOrdered[index]
                currentOrdered[index] = currentOrdered[index - 1]
                currentOrdered[index - 1] = temp
            } else if (!moveUp && index < currentOrdered.size - 1) {
                val temp = currentOrdered[index]
                currentOrdered[index] = currentOrdered[index + 1]
                currentOrdered[index + 1] = temp
            }

            val newOrderIds = currentOrdered.map { it.id }
            repository.reorderMenusInFolder(folderId, newOrderIds)
            _folders.value = repository.getAllFolders()
        }
    }

    fun setFolderCoverImage(folderId: String, imageUri: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val folder = _folders.value.find { it.id == folderId } ?: return@launch
            val updated = folder.copy(coverImageUri = imageUri)
            repository.saveFolder(updated)
            _folders.value = repository.getAllFolders()
            withContext(Dispatchers.Main) {
                showToast(if (imageUri != null) "Portada asignada a ${folder.name}" else "Portada eliminada")
            }
        }
    }

    fun setFolderBackCoverImage(folderId: String, imageUri: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val folder = _folders.value.find { it.id == folderId } ?: return@launch
            val updated = folder.copy(backCoverImageUri = imageUri)
            repository.saveFolder(updated)
            _folders.value = repository.getAllFolders()
            withContext(Dispatchers.Main) {
                showToast(if (imageUri != null) "Foto final asignada a ${folder.name}" else "Foto final eliminada")
            }
        }
    }

    fun exportFolderAsPdf(context: Context, folderId: String, onReady: (File, Uri) -> Unit) {
        viewModelScope.launch {
            _isExporting.value = true
            val folder = _folders.value.find { it.id == folderId }
            if (folder == null) {
                _isExporting.value = false
                return@launch
            }
            val orderedMenus = getOrderedMenusForFolder(folderId)
            val pdfFile = withContext(Dispatchers.Default) {
                MenuExporter.generateFolderPdf(context, folder, orderedMenus)
            }
            _isExporting.value = false
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            showToast("Documento PDF de la Carta generado con éxito")
            onReady(pdfFile, uri)
        }
    }

    fun exportFolderAsLongImage(
        context: Context,
        folderId: String,
        isPng: Boolean,
        saveToGallery: Boolean,
        onReady: (Uri) -> Unit
    ) {
        viewModelScope.launch {
            _isExporting.value = true
            val folder = _folders.value.find { it.id == folderId }
            if (folder == null) {
                _isExporting.value = false
                return@launch
            }
            val orderedMenus = getOrderedMenusForFolder(folderId)
            val uri = withContext(Dispatchers.Default) {
                val stitched = MenuExporter.generateFolderLongImage(context, folder, orderedMenus)
                if (saveToGallery) {
                    MenuExporter.saveToGallery(context, stitched, "Carta_${folder.name}_Larga", isPng)
                } else {
                    MenuExporter.saveToCacheForSharing(context, stitched, "Carta_${folder.name}_Larga", isPng)
                }
            }
            _isExporting.value = false
            if (uri != null) {
                if (saveToGallery) {
                    showToast("Foto larga guardada en Galería / Fotos")
                }
                onReady(uri)
            } else {
                showToast("Error al exportar la foto larga de la carta")
            }
        }
    }

    fun exportFolderPagesToGallery(
        context: Context,
        folderId: String,
        isPng: Boolean = true
    ) {
        viewModelScope.launch {
            _isExporting.value = true
            val folder = _folders.value.find { it.id == folderId }
            if (folder == null) {
                _isExporting.value = false
                return@launch
            }
            val orderedMenus = getOrderedMenusForFolder(folderId)
            val uris = withContext(Dispatchers.Default) {
                MenuExporter.exportFolderPagesToGallery(context, folder, orderedMenus, isPng)
            }
            _isExporting.value = false
            if (uris.isNotEmpty()) {
                showToast("${uris.size} páginas de la carta guardadas en Galería")
            } else {
                showToast("Error al exportar páginas a la galería")
            }
        }
    }
}
