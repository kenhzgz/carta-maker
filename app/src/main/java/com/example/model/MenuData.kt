package com.example.model

import java.util.UUID

data class MenuItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val price: String = "",
    val description: String = "",
    val badge: String? = null
)

data class MenuSection(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val items: List<MenuItem> = emptyList()
)

data class MenuHeader(
    val restaurantName: String = "Atelier Bistró",
    val subtitle: String = "Cocina de Temporada & Brasa",
    val footer: String = "Precios con IVA incluido. Informe al personal si padece alguna alergia alimentaria.",
    val logoUri: String? = null,
    val logoSize: Float = 1.0f,
    val logoShape: LogoShape = LogoShape.ROUNDED
)

data class MenuBackground(
    val type: BackgroundType = BackgroundType.TEXTURE,
    val texture: TexturePreset = TexturePreset.PIZARRA,
    val solidColorHex: String = "#121212",
    val customImageUri: String? = null,
    val contrastMode: ContrastMode = ContrastMode.DARKEN,
    val contrastOpacity: Float = 0.25f
)

data class MenuStyle(
    val aspectRatio: MenuAspectRatio = MenuAspectRatio.RATIO_9_16,
    val titleFont: MenuFontFamily = MenuFontFamily.SERIF,
    val bodyFont: MenuFontFamily = MenuFontFamily.SANS,
    val primaryTextColorHex: String = "#F8FAFC",
    val secondaryTextColorHex: String = "#94A3B8",
    val priceColorHex: String = "#F59E0B",
    val accentColorHex: String = "#F59E0B",
    val cardBgColorHex: String = "#000000",
    val cardBgOpacity: Float = 0.25f,
    val cardCornerRadius: Float = 12f,
    val leaderLineStyle: LeaderLineStyle = LeaderLineStyle.DOTS,
    val globalFontScale: Float = 1.0f,
    val itemSpacing: Float = 14f,
    val sectionSpacing: Float = 22f,
    val horizontalPadding: Float = 24f,
    val textAlignment: TextAlignment = TextAlignment.LEFT,
    val currencySymbol: String = "€",
    val currencyPosition: CurrencyPosition = CurrencyPosition.AFTER,
    val textShadow: Boolean = false,
    val sectionDivider: SectionDividerStyle = SectionDividerStyle.SIMPLE_LINE,
    // Granular precision sizes (in sp base, scaled with global scale)
    val titleSizeSp: Float = 26f,
    val subtitleSizeSp: Float = 13f,
    val sectionTitleSizeSp: Float = 17f,
    val itemNameSizeSp: Float = 14f,
    val itemPriceSizeSp: Float = 14f,
    val itemDescSizeSp: Float = 11f,
    val footerSizeSp: Float = 10f
)

data class MenuData(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Carta Principal",
    val folderId: String = "default",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val header: MenuHeader = MenuHeader(),
    val background: MenuBackground = MenuBackground(),
    val sections: List<MenuSection> = emptyList(),
    val style: MenuStyle = MenuStyle()
)

data class MenuFolder(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Carta Principal",
    val description: String = "",
    val colorHex: String = "#F59E0B",
    val coverImageUri: String? = null,
    val backCoverImageUri: String? = null,
    val menuOrderIds: List<String> = emptyList()
)
