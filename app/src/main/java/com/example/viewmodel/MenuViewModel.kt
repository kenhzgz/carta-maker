package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MenuExporter
import com.example.data.MenuParser
import com.example.data.MenuRepository
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

    private val _currentStep = MutableStateFlow(0) // 0: Fondo, 1: Platos, 2: Estilo, 3: Guardar
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _folders = MutableStateFlow<List<MenuFolder>>(emptyList())
    val folders: StateFlow<List<MenuFolder>> = _folders.asStateFlow()

    private val _savedMenus = MutableStateFlow<List<MenuData>>(emptyList())
    val savedMenus: StateFlow<List<MenuData>> = _savedMenus.asStateFlow()

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
            _folders.value = loadedFolders
            _savedMenus.value = loadedMenus

            if (loadedMenus.isNotEmpty()) {
                _currentMenu.value = loadedMenus.first()
            }
        }
    }

    fun setStep(step: Int) {
        _currentStep.value = step.coerceIn(0, 3)
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
}
