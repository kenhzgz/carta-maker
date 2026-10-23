package com.example.data

import android.content.Context
import android.net.Uri
import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class MenuRepository(private val context: Context) {

    private val menusFile: File get() = File(context.filesDir, "carta_menus.json")
    private val foldersFile: File get() = File(context.filesDir, "carta_folders.json")

    init {
        ensureInitialData()
    }

    private fun ensureInitialData() {
        if (!foldersFile.exists()) {
            val defaultFolders = listOf(
                MenuFolder(id = "default", name = "Carta Principal", colorHex = "#F59E0B"),
                MenuFolder(id = "vinos", name = "Bebidas & Vinos", colorHex = "#8B5CF6"),
                MenuFolder(id = "degustacion", name = "Menú Degustación", colorHex = "#EC4899"),
                MenuFolder(id = "temporada", name = "Especiales de Temporada", colorHex = "#10B981")
            )
            saveFoldersList(defaultFolders)
        }
        if (!menusFile.exists()) {
            val initialMenu = MenuTemplates.atelierBistro
            saveMenusList(listOf(initialMenu))
        }
    }

    @Synchronized
    fun getAllFolders(): List<MenuFolder> {
        if (!foldersFile.exists()) return emptyList()
        return try {
            val jsonStr = foldersFile.readText()
            val array = JSONArray(jsonStr)
            val list = mutableListOf<MenuFolder>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val orderArray = obj.optJSONArray("menuOrderIds")
                val orderList = mutableListOf<String>()
                if (orderArray != null) {
                    for (j in 0 until orderArray.length()) {
                        orderList.add(orderArray.getString(j))
                    }
                }
                list.add(
                    MenuFolder(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        name = obj.optString("name", "Carta Principal"),
                        description = obj.optString("description", ""),
                        colorHex = obj.optString("colorHex", "#F59E0B"),
                        coverImageUri = if (obj.isNull("coverImageUri")) null else obj.optString("coverImageUri"),
                        backCoverImageUri = if (obj.isNull("backCoverImageUri")) null else obj.optString("backCoverImageUri"),
                        menuOrderIds = orderList
                    )
                )
            }
            if (list.isEmpty()) {
                listOf(MenuFolder(id = "default", name = "Carta Principal", colorHex = "#F59E0B"))
            } else list
        } catch (e: Exception) {
            e.printStackTrace()
            listOf(MenuFolder(id = "default", name = "Carta Principal", colorHex = "#F59E0B"))
        }
    }

    @Synchronized
    fun saveFolder(folder: MenuFolder) {
        val current = getAllFolders().toMutableList()
        val index = current.indexOfFirst { it.id == folder.id }
        if (index >= 0) {
            current[index] = folder
        } else {
            current.add(folder)
        }
        saveFoldersList(current)
    }

    @Synchronized
    fun deleteFolder(folderId: String) {
        if (folderId == "default") return // Do not delete default
        val current = getAllFolders().filter { it.id != folderId }
        saveFoldersList(current)

        // Move any menus in that folder to default
        val menus = getAllMenus().map {
            if (it.folderId == folderId) it.copy(folderId = "default") else it
        }
        saveMenusList(menus)
    }

    @Synchronized
    fun reorderMenusInFolder(folderId: String, newOrderIds: List<String>) {
        val current = getAllFolders().toMutableList()
        val index = current.indexOfFirst { it.id == folderId }
        if (index >= 0) {
            current[index] = current[index].copy(menuOrderIds = newOrderIds)
            saveFoldersList(current)
        }
    }

    private fun saveFoldersList(list: List<MenuFolder>) {
        val array = JSONArray()
        list.forEach { folder ->
            val obj = JSONObject()
            obj.put("id", folder.id)
            obj.put("name", folder.name)
            obj.put("description", folder.description)
            obj.put("colorHex", folder.colorHex)
            obj.put("coverImageUri", folder.coverImageUri ?: JSONObject.NULL)
            obj.put("backCoverImageUri", folder.backCoverImageUri ?: JSONObject.NULL)
            val orderArray = JSONArray()
            folder.menuOrderIds.forEach { orderArray.put(it) }
            obj.put("menuOrderIds", orderArray)
            array.put(obj)
        }
        foldersFile.writeText(array.toString(2))
    }

    @Synchronized
    fun getAllMenus(): List<MenuData> {
        if (!menusFile.exists()) return emptyList()
        return try {
            val jsonStr = menusFile.readText()
            val array = JSONArray(jsonStr)
            val list = mutableListOf<MenuData>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(deserializeMenu(obj))
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            listOf(MenuTemplates.atelierBistro)
        }
    }

    @Synchronized
    fun saveMenu(menu: MenuData) {
        val current = getAllMenus().toMutableList()
        val index = current.indexOfFirst { it.id == menu.id }
        val updated = menu.copy(updatedAt = System.currentTimeMillis())
        if (index >= 0) {
            current[index] = updated
        } else {
            current.add(0, updated)
        }
        saveMenusList(current)
    }

    @Synchronized
    fun duplicateMenu(menuId: String): MenuData? {
        val current = getAllMenus()
        val target = current.find { it.id == menuId } ?: return null
        val copy = target.copy(
            id = UUID.randomUUID().toString(),
            name = "${target.name} (Copia)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        saveMenu(copy)
        return copy
    }

    @Synchronized
    fun deleteMenu(menuId: String) {
        val current = getAllMenus().filter { it.id != menuId }
        saveMenusList(current)
    }

    @Synchronized
    fun moveMenuToFolder(menuId: String, targetFolderId: String) {
        val current = getAllMenus().map {
            if (it.id == menuId) it.copy(folderId = targetFolderId, updatedAt = System.currentTimeMillis()) else it
        }
        saveMenusList(current)
    }

    private fun saveMenusList(list: List<MenuData>) {
        val array = JSONArray()
        list.forEach { menu ->
            array.put(serializeMenu(menu))
        }
        menusFile.writeText(array.toString(2))
    }

    // --- Serialization Helpers ---

    fun serializeMenu(menu: MenuData): JSONObject {
        val root = JSONObject()
        root.put("id", menu.id)
        root.put("name", menu.name)
        root.put("folderId", menu.folderId)
        root.put("createdAt", menu.createdAt)
        root.put("updatedAt", menu.updatedAt)

        // Header
        val h = JSONObject()
        h.put("restaurantName", menu.header.restaurantName)
        h.put("subtitle", menu.header.subtitle)
        h.put("footer", menu.header.footer)
        h.put("logoUri", menu.header.logoUri ?: JSONObject.NULL)
        h.put("logoSize", menu.header.logoSize.toDouble())
        h.put("logoShape", menu.header.logoShape.name)
        root.put("header", h)

        // Background
        val b = JSONObject()
        b.put("type", menu.background.type.name)
        b.put("texture", menu.background.texture.name)
        b.put("solidColorHex", menu.background.solidColorHex)
        b.put("customImageUri", menu.background.customImageUri ?: JSONObject.NULL)
        b.put("contrastMode", menu.background.contrastMode.name)
        b.put("contrastOpacity", menu.background.contrastOpacity.toDouble())
        root.put("background", b)

        // Sections
        val secArray = JSONArray()
        menu.sections.forEach { sec ->
            val sObj = JSONObject()
            sObj.put("id", sec.id)
            sObj.put("title", sec.title)
            val itemsArray = JSONArray()
            sec.items.forEach { item ->
                val itObj = JSONObject()
                itObj.put("id", item.id)
                itObj.put("name", item.name)
                itObj.put("price", item.price)
                itObj.put("description", item.description)
                itObj.put("badge", item.badge ?: JSONObject.NULL)
                itemsArray.put(itObj)
            }
            sObj.put("items", itemsArray)
            secArray.put(sObj)
        }
        root.put("sections", secArray)

        // Style
        val st = JSONObject()
        st.put("aspectRatio", menu.style.aspectRatio.name)
        st.put("titleFont", menu.style.titleFont.name)
        st.put("bodyFont", menu.style.bodyFont.name)
        st.put("primaryTextColorHex", menu.style.primaryTextColorHex)
        st.put("secondaryTextColorHex", menu.style.secondaryTextColorHex)
        st.put("priceColorHex", menu.style.priceColorHex)
        st.put("accentColorHex", menu.style.accentColorHex)
        st.put("cardBgColorHex", menu.style.cardBgColorHex)
        st.put("cardBgOpacity", menu.style.cardBgOpacity.toDouble())
        st.put("cardCornerRadius", menu.style.cardCornerRadius.toDouble())
        st.put("leaderLineStyle", menu.style.leaderLineStyle.name)
        st.put("globalFontScale", menu.style.globalFontScale.toDouble())
        st.put("itemSpacing", menu.style.itemSpacing.toDouble())
        st.put("sectionSpacing", menu.style.sectionSpacing.toDouble())
        st.put("horizontalPadding", menu.style.horizontalPadding.toDouble())
        st.put("textAlignment", menu.style.textAlignment.name)
        st.put("currencySymbol", menu.style.currencySymbol)
        st.put("currencyPosition", menu.style.currencyPosition.name)
        st.put("textShadow", menu.style.textShadow)
        st.put("sectionDivider", menu.style.sectionDivider.name)
        st.put("titleSizeSp", menu.style.titleSizeSp.toDouble())
        st.put("subtitleSizeSp", menu.style.subtitleSizeSp.toDouble())
        st.put("sectionTitleSizeSp", menu.style.sectionTitleSizeSp.toDouble())
        st.put("itemNameSizeSp", menu.style.itemNameSizeSp.toDouble())
        st.put("itemPriceSizeSp", menu.style.itemPriceSizeSp.toDouble())
        st.put("itemDescSizeSp", menu.style.itemDescSizeSp.toDouble())
        st.put("footerSizeSp", menu.style.footerSizeSp.toDouble())
        root.put("style", st)

        return root
    }

    fun deserializeMenu(obj: JSONObject): MenuData {
        val id = obj.optString("id", UUID.randomUUID().toString())
        val name = obj.optString("name", "Mi Carta")
        val folderId = obj.optString("folderId", "default")
        val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
        val updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())

        val hObj = obj.optJSONObject("header") ?: JSONObject()
        val header = MenuHeader(
            restaurantName = hObj.optString("restaurantName", "Atelier Bistró"),
            subtitle = hObj.optString("subtitle", ""),
            footer = hObj.optString("footer", ""),
            logoUri = if (hObj.isNull("logoUri")) null else hObj.optString("logoUri"),
            logoSize = hObj.optDouble("logoSize", 1.0).toFloat(),
            logoShape = try {
                LogoShape.valueOf(hObj.optString("logoShape", "ROUNDED"))
            } catch (e: Exception) {
                LogoShape.ROUNDED
            }
        )

        val bObj = obj.optJSONObject("background") ?: JSONObject()
        val background = MenuBackground(
            type = try {
                BackgroundType.valueOf(bObj.optString("type", "TEXTURE"))
            } catch (e: Exception) {
                BackgroundType.TEXTURE
            },
            texture = try {
                TexturePreset.valueOf(bObj.optString("texture", "PIZARRA"))
            } catch (e: Exception) {
                TexturePreset.PIZARRA
            },
            solidColorHex = bObj.optString("solidColorHex", "#121212"),
            customImageUri = if (bObj.isNull("customImageUri")) null else bObj.optString("customImageUri"),
            contrastMode = try {
                ContrastMode.valueOf(bObj.optString("contrastMode", "DARKEN"))
            } catch (e: Exception) {
                ContrastMode.DARKEN
            },
            contrastOpacity = bObj.optDouble("contrastOpacity", 0.25).toFloat()
        )

        val secList = mutableListOf<MenuSection>()
        val secArray = obj.optJSONArray("sections")
        if (secArray != null) {
            for (i in 0 until secArray.length()) {
                val sObj = secArray.getJSONObject(i)
                val itemsList = mutableListOf<MenuItem>()
                val itArray = sObj.optJSONArray("items")
                if (itArray != null) {
                    for (j in 0 until itArray.length()) {
                        val itObj = itArray.getJSONObject(j)
                        itemsList.add(
                            MenuItem(
                                id = itObj.optString("id", UUID.randomUUID().toString()),
                                name = itObj.optString("name", ""),
                                price = itObj.optString("price", ""),
                                description = itObj.optString("description", ""),
                                badge = if (itObj.isNull("badge")) null else itObj.optString("badge")
                            )
                        )
                    }
                }
                secList.add(
                    MenuSection(
                        id = sObj.optString("id", UUID.randomUUID().toString()),
                        title = sObj.optString("title", ""),
                        items = itemsList
                    )
                )
            }
        }

        val stObj = obj.optJSONObject("style") ?: JSONObject()
        val style = MenuStyle(
            aspectRatio = try {
                MenuAspectRatio.valueOf(stObj.optString("aspectRatio", "RATIO_9_16"))
            } catch (e: Exception) {
                MenuAspectRatio.RATIO_9_16
            },
            titleFont = try {
                MenuFontFamily.valueOf(stObj.optString("titleFont", "SERIF"))
            } catch (e: Exception) {
                MenuFontFamily.SERIF
            },
            bodyFont = try {
                MenuFontFamily.valueOf(stObj.optString("bodyFont", "SANS"))
            } catch (e: Exception) {
                MenuFontFamily.SANS
            },
            primaryTextColorHex = stObj.optString("primaryTextColorHex", "#F8FAFC"),
            secondaryTextColorHex = stObj.optString("secondaryTextColorHex", "#94A3B8"),
            priceColorHex = stObj.optString("priceColorHex", "#F59E0B"),
            accentColorHex = stObj.optString("accentColorHex", "#F59E0B"),
            cardBgColorHex = stObj.optString("cardBgColorHex", "#000000"),
            cardBgOpacity = stObj.optDouble("cardBgOpacity", 0.25).toFloat(),
            cardCornerRadius = stObj.optDouble("cardCornerRadius", 12.0).toFloat(),
            leaderLineStyle = try {
                LeaderLineStyle.valueOf(stObj.optString("leaderLineStyle", "DOTS"))
            } catch (e: Exception) {
                LeaderLineStyle.DOTS
            },
            globalFontScale = stObj.optDouble("globalFontScale", 1.0).toFloat(),
            itemSpacing = stObj.optDouble("itemSpacing", 14.0).toFloat(),
            sectionSpacing = stObj.optDouble("sectionSpacing", 22.0).toFloat(),
            horizontalPadding = stObj.optDouble("horizontalPadding", 24.0).toFloat(),
            textAlignment = try {
                TextAlignment.valueOf(stObj.optString("textAlignment", "LEFT"))
            } catch (e: Exception) {
                TextAlignment.LEFT
            },
            currencySymbol = stObj.optString("currencySymbol", "€"),
            currencyPosition = try {
                CurrencyPosition.valueOf(stObj.optString("currencyPosition", "AFTER"))
            } catch (e: Exception) {
                CurrencyPosition.AFTER
            },
            textShadow = stObj.optBoolean("textShadow", false),
            sectionDivider = try {
                SectionDividerStyle.valueOf(stObj.optString("sectionDivider", "SIMPLE_LINE"))
            } catch (e: Exception) {
                SectionDividerStyle.SIMPLE_LINE
            },
            titleSizeSp = stObj.optDouble("titleSizeSp", 26.0).toFloat(),
            subtitleSizeSp = stObj.optDouble("subtitleSizeSp", 13.0).toFloat(),
            sectionTitleSizeSp = stObj.optDouble("sectionTitleSizeSp", 17.0).toFloat(),
            itemNameSizeSp = stObj.optDouble("itemNameSizeSp", 14.0).toFloat(),
            itemPriceSizeSp = stObj.optDouble("itemPriceSizeSp", 14.0).toFloat(),
            itemDescSizeSp = stObj.optDouble("itemDescSizeSp", 11.0).toFloat(),
            footerSizeSp = stObj.optDouble("footerSizeSp", 10.0).toFloat()
        )

        return MenuData(
            id = id,
            name = name,
            folderId = folderId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            header = header,
            background = background,
            sections = secList,
            style = style
        )
    }

    // --- ZIP Backup & Export ---

    fun createBackupZip(): File {
        val backupDir = File(context.filesDir, "backups").apply { mkdirs() }
        val zipFile = File(backupDir, "carta_backup_${System.currentTimeMillis()}.zip")

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            // Menus
            if (menusFile.exists()) {
                zos.putNextEntry(ZipEntry("carta_menus.json"))
                FileInputStream(menusFile).use { it.copyTo(zos) }
                zos.closeEntry()
            }
            // Folders
            if (foldersFile.exists()) {
                zos.putNextEntry(ZipEntry("carta_folders.json"))
                FileInputStream(foldersFile).use { it.copyTo(zos) }
                zos.closeEntry()
            }
        }
        return zipFile
    }

    fun restoreBackupZip(zipUri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(zipUri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zis ->
                    var entry: ZipEntry? = zis.nextEntry
                    while (entry != null) {
                        when (entry.name) {
                            "carta_menus.json" -> {
                                val target = menusFile
                                FileOutputStream(target).use { zis.copyTo(it) }
                            }
                            "carta_folders.json" -> {
                                val target = foldersFile
                                FileOutputStream(target).use { zis.copyTo(it) }
                            }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
