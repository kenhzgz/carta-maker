package com.example.model

enum class BackgroundType {
    TEXTURE,
    SOLID_COLOR,
    CUSTOM_IMAGE
}

enum class TexturePreset(val displayName: String) {
    PIZARRA("Pizarra"),
    MADERA("Madera"),
    MARMOL("Mármol"),
    KRAFT("Kraft"),
    ESMERALDA("Esmeralda"),
    TERRACOTA("Terracota")
}

enum class ContrastMode(val displayName: String) {
    DARKEN("Oscurecer"),
    LIGHTEN("Aclarar")
}

enum class LogoShape(val displayName: String) {
    ORIGINAL("Original"),
    CIRCLE("Círculo"),
    ROUNDED("Redondeado")
}

enum class MenuAspectRatio(val displayName: String, val ratioWidth: Float, val ratioHeight: Float, val exportWidth: Int, val exportHeight: Int) {
    RATIO_9_16("9:16 (Stories)", 9f, 16f, 1080, 1920),
    RATIO_4_5("4:5 (Feed)", 4f, 5f, 1080, 1350),
    RATIO_A4("A4 (Imprimir)", 1f, 1.4142f, 1240, 1754),
    RATIO_1_1("1:1 (Cuadrado)", 1f, 1f, 1080, 1080)
}

enum class MenuFontFamily(val displayName: String) {
    SERIF("Serif Elegante"),
    SANS("Sans Moderna"),
    CURSIVE("Script Bistro"),
    MONO("Monospace Roastery")
}

enum class LeaderLineStyle(val displayName: String) {
    DOTS("Puntos (····)"),
    LINE("Línea continua (——)"),
    DASHES("Guiones (- - -)"),
    NONE("Ninguno")
}

enum class SectionDividerStyle(val displayName: String) {
    NONE("Ninguno"),
    SIMPLE_LINE("Línea fina"),
    CLASSIC_DIAMOND("Rombo clásico"),
    ELEGANT_FLOURISH("Floritura elegante")
}

enum class TextAlignment(val displayName: String) {
    LEFT("Izquierda"),
    CENTER("Centrado")
}

enum class CurrencyPosition(val displayName: String) {
    AFTER("Detrás (12,50 €)"),
    BEFORE("Delante ($12.50)")
}
