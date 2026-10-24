package com.example.data

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.R
import com.example.model.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object MenuExporter {

    fun generateHighResBitmap(context: Context, menu: MenuData): Bitmap {
        val width = menu.style.aspectRatio.exportWidth
        val height = menu.style.aspectRatio.exportHeight

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawMenuOnCanvas(context, canvas, width.toFloat(), height.toFloat(), menu, isExport = true)

        return bitmap
    }

    fun drawMenuOnCanvas(
        context: Context,
        canvas: Canvas,
        width: Float,
        height: Float,
        menu: MenuData,
        isExport: Boolean = false
    ) {
        val scale = width / 1080f // Base normalization scale against 1080px width

        // 1. Draw Background
        drawBackground(context, canvas, width, height, menu.background)

        // 2. Draw Contrast Layer
        if (menu.background.contrastOpacity > 0.01f) {
            val contrastPaint = Paint().apply {
                color = if (menu.background.contrastMode == ContrastMode.DARKEN) Color.BLACK else Color.WHITE
                alpha = (menu.background.contrastOpacity.coerceIn(0f, 1f) * 255).toInt()
            }
            canvas.drawRect(0f, 0f, width, height, contrastPaint)
        }

        // 3. Margin & Layout Calculations
        val horizPadding = menu.style.horizontalPadding * scale * 2.2f
        val contentWidth = width - (horizPadding * 2)

        // Text Colors
        val primaryColor = parseColor(menu.style.primaryTextColorHex, Color.WHITE)
        val secondaryColor = parseColor(menu.style.secondaryTextColorHex, Color.LTGRAY)
        val priceColor = parseColor(menu.style.priceColorHex, Color.parseColor("#F59E0B"))
        val accentColor = parseColor(menu.style.accentColorHex, Color.parseColor("#F59E0B"))

        // Fonts
        val titleTypeface = getTypeface(menu.style.titleFont, menu.style.customTitleFontPath, bold = true)
        val bodyTypeface = getTypeface(menu.style.bodyFont, menu.style.customBodyFontPath, bold = false)
        val boldBodyTypeface = getTypeface(menu.style.bodyFont, menu.style.customBodyFontPath, bold = true)

        val topPadding = menu.style.topPadding * scale * 2.2f
        val botMargin = menu.style.bottomMargin * scale * 2.2f
        var currentY = (56f * scale) + topPadding + (menu.header.headerOffsetY * scale)

        // 4. Draw Logo if available
        val logoUri = menu.header.logoUri
        if (!logoUri.isNullOrBlank()) {
            val logoBmp = loadBitmapFromUri(context, logoUri)
            if (logoBmp != null) {
                val baseLogoSize = 140f * scale * menu.header.logoSize
                val logoY = currentY + (menu.header.logoOffsetY * scale)
                val logoX = when (menu.header.logoAlignment) {
                    LogoAlignment.CENTER -> (width - baseLogoSize) / 2f
                    LogoAlignment.LEFT -> horizPadding
                    LogoAlignment.RIGHT -> width - horizPadding - baseLogoSize
                }
                val logoRect = RectF(
                    logoX,
                    logoY,
                    logoX + baseLogoSize,
                    logoY + baseLogoSize
                )
                drawShapedBitmap(canvas, logoBmp, logoRect, menu.header.logoShape)
                currentY = maxOf(currentY, logoY + baseLogoSize) + (20f * scale)
            }
        }

        // 5. Draw Restaurant Title (using restaurantNameStyle)
        val restStyle = menu.style.restaurantNameStyle
        val titleTf = getTypeface(
            font = restStyle.fontFamily,
            customPath = restStyle.customFontPath,
            bold = restStyle.isBold,
            italic = restStyle.isItalic
        )
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseColor(restStyle.colorHex, primaryColor)
            typeface = titleTf
            textSize = restStyle.fontSizeSp * menu.style.globalFontScale * scale * 2.1f
            textAlign = when (restStyle.alignment) {
                TextAlignment.CENTER -> Paint.Align.CENTER
                TextAlignment.RIGHT -> Paint.Align.RIGHT
                TextAlignment.LEFT -> Paint.Align.LEFT
            }
            isUnderlineText = restStyle.isUnderlined
            if (menu.style.textShadow) {
                setShadowLayer(8f * scale, 2f * scale, 2f * scale, Color.argb(180, 0, 0, 0))
            }
        }
        val titleX = when (restStyle.alignment) {
            TextAlignment.CENTER -> width / 2f
            TextAlignment.RIGHT -> width - horizPadding
            TextAlignment.LEFT -> horizPadding
        }
        currentY += restStyle.offsetY * scale
        canvas.drawText(menu.header.restaurantName, titleX, currentY + titlePaint.textSize * 0.8f, titlePaint)
        currentY += titlePaint.textSize + (12f * scale)

        // Subtitle (using subtitleStyle)
        if (menu.header.subtitle.isNotBlank()) {
            val subStyle = menu.style.subtitleStyle
            val subTf = getTypeface(
                font = subStyle.fontFamily,
                customPath = subStyle.customFontPath,
                bold = subStyle.isBold,
                italic = subStyle.isItalic
            )
            val subtitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = parseColor(subStyle.colorHex, secondaryColor)
                typeface = subTf
                textSize = subStyle.fontSizeSp * menu.style.globalFontScale * scale * 2.0f
                textAlign = when (subStyle.alignment) {
                    TextAlignment.CENTER -> Paint.Align.CENTER
                    TextAlignment.RIGHT -> Paint.Align.RIGHT
                    TextAlignment.LEFT -> Paint.Align.LEFT
                }
                isUnderlineText = subStyle.isUnderlined
                if (menu.style.textShadow) {
                    setShadowLayer(6f * scale, 1f * scale, 1f * scale, Color.argb(160, 0, 0, 0))
                }
            }
            val subX = when (subStyle.alignment) {
                TextAlignment.CENTER -> width / 2f
                TextAlignment.RIGHT -> width - horizPadding
                TextAlignment.LEFT -> horizPadding
            }
            currentY += subStyle.offsetY * scale
            canvas.drawText(menu.header.subtitle, subX, currentY + subtitlePaint.textSize * 0.8f, subtitlePaint)
            currentY += subtitlePaint.textSize + (24f * scale)
        } else {
            currentY += 12f * scale
        }

        // Main Header Divider (Optional)
        if (menu.style.showHeaderDivider) {
            val headerLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                strokeWidth = 2.5f * scale
                alpha = 180
            }
            canvas.drawLine(horizPadding, currentY, width - horizPadding, currentY, headerLinePaint)
            currentY += 28f * scale
        } else {
            currentY += 14f * scale
        }

        // 6. Draw Sections & Dishes
        val itemSpacing = menu.style.itemSpacing * scale * 1.8f
        val sectionSpacing = menu.style.sectionSpacing * scale * 1.8f

        menu.sections.forEach { section ->
            // Section Title (using sectionHeaderStyle)
            val secStyle = menu.style.sectionHeaderStyle
            val secTf = getTypeface(
                font = secStyle.fontFamily,
                customPath = secStyle.customFontPath,
                bold = secStyle.isBold,
                italic = secStyle.isItalic
            )
            val sectionTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = parseColor(secStyle.colorHex, accentColor)
                typeface = secTf
                textSize = secStyle.fontSizeSp * menu.style.globalFontScale * scale * 2.0f
                letterSpacing = 0.08f
                textAlign = when (secStyle.alignment) {
                    TextAlignment.CENTER -> Paint.Align.CENTER
                    TextAlignment.RIGHT -> Paint.Align.RIGHT
                    TextAlignment.LEFT -> Paint.Align.LEFT
                }
                isUnderlineText = secStyle.isUnderlined
                if (menu.style.textShadow) {
                    setShadowLayer(6f * scale, 1f * scale, 1f * scale, Color.argb(160, 0, 0, 0))
                }
            }
            val sX = when (secStyle.alignment) {
                TextAlignment.CENTER -> width / 2f
                TextAlignment.RIGHT -> width - horizPadding
                TextAlignment.LEFT -> horizPadding
            }
            currentY += secStyle.offsetY * scale
            canvas.drawText(section.title.uppercase(), sX, currentY + sectionTitlePaint.textSize * 0.8f, sectionTitlePaint)
            currentY += sectionTitlePaint.textSize + (8f * scale)

            // Section decorative divider
            drawSectionDivider(canvas, menu.style.sectionDivider, horizPadding, currentY, width - horizPadding, accentColor, scale)
            currentY += 18f * scale

            // Dishes in Section
            section.items.forEach { item ->
                currentY = drawDishItem(
                    canvas = canvas,
                    item = item,
                    startX = horizPadding,
                    startY = currentY,
                    contentWidth = contentWidth,
                    menu = menu,
                    scale = scale,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    priceColor = priceColor,
                    accentColor = accentColor,
                    boldBodyTypeface = boldBodyTypeface,
                    bodyTypeface = bodyTypeface
                )
                currentY += itemSpacing
            }

            currentY += sectionSpacing
        }

        // 7. Footer
        if (menu.header.footer.isNotBlank()) {
            val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = secondaryColor
                typeface = bodyTypeface
                textSize = menu.style.footerSizeSp * menu.style.globalFontScale * scale * 1.9f
                alpha = 200
            }
            val footerLayout = StaticLayout.Builder.obtain(
                menu.header.footer,
                0,
                menu.header.footer.length,
                footerPaint,
                contentWidth.toInt()
            ).setAlignment(Layout.Alignment.ALIGN_CENTER).build()

            val footerY = (height - (50f * scale) - botMargin - footerLayout.height).coerceAtLeast(currentY + 20f * scale)
            canvas.save()
            canvas.translate(horizPadding, footerY)
            footerLayout.draw(canvas)
            canvas.restore()
        }

        // 8. Draw Free Elements (Floating Text Stickers, Badges, Dish Photos)
        menu.freeElements.forEach { element ->
            drawFreeElement(context, canvas, width, height, element, scale, boldBodyTypeface)
        }
    }

    private fun drawDishItem(
        canvas: Canvas,
        item: MenuItem,
        startX: Float,
        startY: Float,
        contentWidth: Float,
        menu: MenuData,
        scale: Float,
        primaryColor: Int,
        secondaryColor: Int,
        priceColor: Int,
        accentColor: Int,
        boldBodyTypeface: Typeface,
        bodyTypeface: Typeface
    ): Float {
        var currentY = startY

        // Dish Box Background if opacity > 0
        val boxOpacity = menu.style.cardBgOpacity
        val cardRadius = menu.style.cardCornerRadius * scale
        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseColor(menu.style.cardBgColorHex, Color.BLACK)
            alpha = (boxOpacity.coerceIn(0f, 1f) * 255).toInt()
        }

        // Price formatting
        val formattedPrice = formatPrice(item.price, menu.style.currencySymbol, menu.style.currencyPosition)

        // Use individual TypographySectionStyle for Name, Price, and Description
        val dNameStyle = menu.style.dishNameStyle
        val dPriceStyle = menu.style.dishPriceStyle
        val dDescStyle = menu.style.dishDescStyle

        val dNameTf = getTypeface(
            font = dNameStyle.fontFamily,
            customPath = dNameStyle.customFontPath,
            bold = dNameStyle.isBold,
            italic = dNameStyle.isItalic
        )
        val dPriceTf = getTypeface(
            font = dPriceStyle.fontFamily,
            customPath = dPriceStyle.customFontPath,
            bold = dPriceStyle.isBold,
            italic = dPriceStyle.isItalic
        )
        val dDescTf = getTypeface(
            font = dDescStyle.fontFamily,
            customPath = dDescStyle.customFontPath,
            bold = dDescStyle.isBold,
            italic = dDescStyle.isItalic
        )

        val pricePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseColor(dPriceStyle.colorHex, priceColor)
            typeface = dPriceTf
            textSize = dPriceStyle.fontSizeSp * menu.style.globalFontScale * scale * 2.0f
            isUnderlineText = dPriceStyle.isUnderlined
            if (menu.style.textShadow) {
                setShadowLayer(5f * scale, 1f * scale, 1f * scale, Color.argb(160, 0, 0, 0))
            }
        }
        val priceWidth = if (formattedPrice.isNotBlank()) pricePaint.measureText(formattedPrice) else 0f

        val namePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseColor(dNameStyle.colorHex, primaryColor)
            typeface = dNameTf
            textSize = dNameStyle.fontSizeSp * menu.style.globalFontScale * scale * 2.0f
            isUnderlineText = dNameStyle.isUnderlined
            if (menu.style.textShadow) {
                setShadowLayer(5f * scale, 1f * scale, 1f * scale, Color.argb(160, 0, 0, 0))
            }
        }

        // Calculate card height dynamically
        val descPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseColor(dDescStyle.colorHex, secondaryColor)
            typeface = dDescTf
            textSize = dDescStyle.fontSizeSp * menu.style.globalFontScale * scale * 1.9f
            isUnderlineText = dDescStyle.isUnderlined
        }

        val paddingInside = if (boxOpacity > 0.05f) 14f * scale else 0f
        val usableWidth = contentWidth - (paddingInside * 2)

        val descAlignment = when (dDescStyle.alignment) {
            TextAlignment.CENTER -> Layout.Alignment.ALIGN_CENTER
            TextAlignment.RIGHT -> Layout.Alignment.ALIGN_OPPOSITE
            TextAlignment.LEFT -> Layout.Alignment.ALIGN_NORMAL
        }

        val descLayout = if (item.description.isNotBlank()) {
            StaticLayout.Builder.obtain(
                item.description,
                0,
                item.description.length,
                descPaint,
                usableWidth.toInt()
            ).setAlignment(descAlignment).build()
        } else null

        val estimatedItemHeight = namePaint.textSize + (descLayout?.height?.toFloat() ?: 0f) + (paddingInside * 2) + (8f * scale)

        // Draw card background
        if (boxOpacity > 0.05f) {
            val rect = RectF(
                startX,
                currentY - (4f * scale),
                startX + contentWidth,
                currentY + estimatedItemHeight
            )
            canvas.drawRoundRect(rect, cardRadius, cardRadius, boxPaint)
        }

        val itemContentX = startX + paddingInside
        var itemContentY = currentY + paddingInside + (dNameStyle.offsetY * scale)

        // Badge if present
        var badgeWidth = 0f
        val badge = item.badge
        if (!badge.isNullOrBlank()) {
            val badgePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                typeface = boldBodyTypeface
                textSize = (dNameStyle.fontSizeSp * 0.75f) * menu.style.globalFontScale * scale * 2.0f
            }
            val textW = badgePaint.measureText(badge.uppercase())
            val bHeight = badgePaint.textSize * 1.4f
            val bWidth = textW + (16f * scale)
            badgeWidth = bWidth + (8f * scale)

            val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                alpha = 40
            }
            val badgeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                style = Paint.Style.STROKE
                strokeWidth = 1.5f * scale
                alpha = 180
            }
            val bRect = RectF(
                itemContentX,
                itemContentY,
                itemContentX + bWidth,
                itemContentY + bHeight
            )
            canvas.drawRoundRect(bRect, 6f * scale, 6f * scale, badgeBgPaint)
            canvas.drawRoundRect(bRect, 6f * scale, 6f * scale, badgeBorderPaint)
            canvas.drawText(badge.uppercase(), itemContentX + (8f * scale), itemContentY + badgePaint.textSize * 1.05f, badgePaint)
        }

        val nameStartX = itemContentX + badgeWidth
        val nameText = item.name

        val isCentered = dNameStyle.alignment == TextAlignment.CENTER || menu.style.textAlignment == TextAlignment.CENTER

        if (isCentered) {
            // Centered layout
            val cX = startX + (contentWidth / 2f)
            canvas.drawText(nameText, cX, itemContentY + namePaint.textSize * 0.85f, namePaint.apply { textAlign = Paint.Align.CENTER })
            if (formattedPrice.isNotBlank()) {
                val pY = itemContentY + namePaint.textSize + (16f * scale) + (dPriceStyle.offsetY * scale)
                canvas.drawText(formattedPrice, cX, pY, pricePaint.apply { textAlign = Paint.Align.CENTER })
                itemContentY += pricePaint.textSize + (8f * scale)
            }
            itemContentY += namePaint.textSize + (6f * scale)
        } else {
            // Left or aligned layout with leader lines to right price
            namePaint.textAlign = Paint.Align.LEFT
            canvas.drawText(nameText, nameStartX, itemContentY + namePaint.textSize * 0.85f, namePaint)

            val measuredNameW = namePaint.measureText(nameText)
            val priceStartX = startX + contentWidth - paddingInside - priceWidth

            // Draw Leader Line (dots, line, dashes)
            if (formattedPrice.isNotBlank() && menu.style.leaderLineStyle != LeaderLineStyle.NONE) {
                val leaderStart = (nameStartX + measuredNameW + (12f * scale)).coerceAtMost(priceStartX - (16f * scale))
                val leaderEnd = priceStartX - (12f * scale)
                if (leaderEnd > leaderStart) {
                    val lineY = itemContentY + namePaint.textSize * 0.7f
                    drawLeaderLine(canvas, menu.style.leaderLineStyle, leaderStart, lineY, leaderEnd, secondaryColor, scale)
                }
            }

            // Draw Price on right
            if (formattedPrice.isNotBlank()) {
                pricePaint.textAlign = Paint.Align.LEFT
                canvas.drawText(formattedPrice, priceStartX, itemContentY + namePaint.textSize * 0.85f + (dPriceStyle.offsetY * scale), pricePaint)
            }
            itemContentY += namePaint.textSize + (6f * scale)
        }

        // Draw Description
        if (descLayout != null) {
            canvas.save()
            canvas.translate(itemContentX, itemContentY + (dDescStyle.offsetY * scale))
            descLayout.draw(canvas)
            canvas.restore()
            itemContentY += descLayout.height.toFloat() + (dDescStyle.offsetY * scale)
        }

        return itemContentY + paddingInside
    }

    private fun drawLeaderLine(
        canvas: Canvas,
        style: LeaderLineStyle,
        startX: Float,
        y: Float,
        endX: Float,
        color: Int,
        scale: Float
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            alpha = 140
            strokeWidth = 1.5f * scale
        }
        when (style) {
            LeaderLineStyle.DOTS -> {
                val step = 14f * scale
                var cur = startX
                while (cur <= endX) {
                    canvas.drawCircle(cur, y, 1.8f * scale, paint)
                    cur += step
                }
            }
            LeaderLineStyle.LINE -> {
                canvas.drawLine(startX, y, endX, y, paint)
            }
            LeaderLineStyle.DASHES -> {
                paint.pathEffect = DashPathEffect(floatArrayOf(12f * scale, 8f * scale), 0f)
                canvas.drawLine(startX, y, endX, y, paint)
            }
            LeaderLineStyle.NONE -> Unit
        }
    }

    private fun drawSectionDivider(
        canvas: Canvas,
        style: SectionDividerStyle,
        startX: Float,
        y: Float,
        endX: Float,
        color: Int,
        scale: Float
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            alpha = 150
            strokeWidth = 1.5f * scale
        }
        val midX = (startX + endX) / 2f

        when (style) {
            SectionDividerStyle.NONE -> Unit
            SectionDividerStyle.SIMPLE_LINE -> {
                canvas.drawLine(startX, y, endX, y, paint)
            }
            SectionDividerStyle.CLASSIC_DIAMOND -> {
                canvas.drawLine(startX, y, midX - (18f * scale), y, paint)
                val diamondPath = Path().apply {
                    moveTo(midX, y - (6f * scale))
                    lineTo(midX + (6f * scale), y)
                    lineTo(midX, y + (6f * scale))
                    lineTo(midX - (6f * scale), y)
                    close()
                }
                paint.style = Paint.Style.FILL
                canvas.drawPath(diamondPath, paint)
                paint.style = Paint.Style.STROKE
                canvas.drawLine(midX + (18f * scale), y, endX, y, paint)
            }
            SectionDividerStyle.ELEGANT_FLOURISH -> {
                canvas.drawLine(startX, y, midX - (24f * scale), y, paint)
                paint.style = Paint.Style.FILL
                canvas.drawCircle(midX - (10f * scale), y, 2.5f * scale, paint)
                canvas.drawCircle(midX, y, 4.5f * scale, paint)
                canvas.drawCircle(midX + (10f * scale), y, 2.5f * scale, paint)
                paint.style = Paint.Style.STROKE
                canvas.drawLine(midX + (24f * scale), y, endX, y, paint)
            }
        }
    }

    private fun drawBackground(
        context: Context,
        canvas: Canvas,
        width: Float,
        height: Float,
        bg: MenuBackground
    ) {
        when (bg.type) {
            BackgroundType.SOLID_COLOR -> {
                val color = parseColor(bg.solidColorHex, Color.parseColor("#121212"))
                canvas.drawColor(color)
            }
            BackgroundType.TEXTURE -> {
                drawTexturePreset(context, canvas, width, height, bg.texture)
            }
            BackgroundType.CUSTOM_IMAGE -> {
                val bmp = if (!bg.customImageUri.isNullOrBlank()) loadBitmapFromUri(context, bg.customImageUri) else null
                if (bmp != null) {
                    drawCoverBitmap(canvas, bmp, width, height)
                } else {
                    canvas.drawColor(Color.parseColor("#121212"))
                }
            }
        }
    }

    private fun drawTexturePreset(
        context: Context,
        canvas: Canvas,
        width: Float,
        height: Float,
        preset: TexturePreset
    ) {
        val resId = when (preset) {
            TexturePreset.PIZARRA -> R.drawable.img_tex_pizarra
            TexturePreset.MADERA -> R.drawable.img_tex_madera
            TexturePreset.MARMOL -> R.drawable.img_tex_marmol
            TexturePreset.KRAFT -> R.drawable.img_tex_kraft
            TexturePreset.ESMERALDA -> null
            TexturePreset.TERRACOTA -> null
        }

        if (resId != null) {
            try {
                val opts = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.RGB_565 }
                val bmp = BitmapFactory.decodeResource(context.resources, resId, opts)
                if (bmp != null) {
                    drawCoverBitmap(canvas, bmp, width, height)
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Procedural shader fallback for Esmeralda and Terracota or if resource decode fails
        val shaderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        when (preset) {
            TexturePreset.ESMERALDA -> {
                val gradient = LinearGradient(
                    0f, 0f, width, height,
                    intArrayOf(Color.parseColor("#06201B"), Color.parseColor("#0A382F"), Color.parseColor("#031713")),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
                )
                shaderPaint.shader = gradient
                canvas.drawRect(0f, 0f, width, height, shaderPaint)
            }
            TexturePreset.TERRACOTA -> {
                val gradient = LinearGradient(
                    0f, 0f, width, height,
                    intArrayOf(Color.parseColor("#381712"), Color.parseColor("#5A281E"), Color.parseColor("#260E0A")),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
                )
                shaderPaint.shader = gradient
                canvas.drawRect(0f, 0f, width, height, shaderPaint)
            }
            TexturePreset.PIZARRA -> canvas.drawColor(Color.parseColor("#1C2026"))
            TexturePreset.MADERA -> canvas.drawColor(Color.parseColor("#251914"))
            TexturePreset.MARMOL -> canvas.drawColor(Color.parseColor("#E8ECEF"))
            TexturePreset.KRAFT -> canvas.drawColor(Color.parseColor("#D4B996"))
        }
    }

    private fun drawCoverBitmap(canvas: Canvas, bmp: Bitmap, width: Float, height: Float) {
        val srcW = bmp.width.toFloat()
        val srcH = bmp.height.toFloat()
        val scale = maxOf(width / srcW, height / srcH)
        val scaledW = srcW * scale
        val scaledH = srcH * scale
        val left = (width - scaledW) / 2f
        val top = (height - scaledH) / 2f
        val destRect = RectF(left, top, left + scaledW, top + scaledH)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(bmp, null, destRect, paint)
    }

    private fun drawShapedBitmap(canvas: Canvas, bmp: Bitmap, destRect: RectF, shape: LogoShape) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        when (shape) {
            LogoShape.ORIGINAL -> {
                canvas.drawBitmap(bmp, null, destRect, paint)
            }
            LogoShape.CIRCLE -> {
                val path = Path().apply {
                    addOval(destRect, Path.Direction.CW)
                }
                canvas.save()
                canvas.clipPath(path)
                canvas.drawBitmap(bmp, null, destRect, paint)
                canvas.restore()
            }
            LogoShape.ROUNDED -> {
                val path = Path().apply {
                    addRoundRect(destRect, 20f, 20f, Path.Direction.CW)
                }
                canvas.save()
                canvas.clipPath(path)
                canvas.drawBitmap(bmp, null, destRect, paint)
                canvas.restore()
            }
        }
    }

    private fun loadBitmapFromUri(context: Context, uriStr: String): Bitmap? {
        return try {
            if (uriStr == "drawable/cupey_logo" || uriStr == "cupey_logo" || uriStr.contains("cupey_logo")) {
                val resId = context.resources.getIdentifier("cupey_logo", "drawable", context.packageName)
                if (resId != 0) {
                    return BitmapFactory.decodeResource(context.resources, resId)
                }
            }
            if (uriStr.startsWith("file://")) {
                val path = Uri.parse(uriStr).path
                if (path != null && java.io.File(path).exists()) {
                    return BitmapFactory.decodeFile(path)
                }
            } else if (uriStr.startsWith("/")) {
                if (java.io.File(uriStr).exists()) {
                    return BitmapFactory.decodeFile(uriStr)
                }
            }
            val uri = Uri.parse(uriStr)
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun formatPrice(price: String, symbol: String, pos: CurrencyPosition): String {
        val trimmed = price.trim()
        if (trimmed.isBlank()) return ""
        val cleanNumberStr = trimmed.replace(",", ".").replace("[^0-9.]".toRegex(), "")
        val numericVal = cleanNumberStr.toDoubleOrNull()
        if (numericVal != null && numericVal == 0.0) return ""

        return when (pos) {
            CurrencyPosition.AFTER -> if (symbol.isNotBlank()) "$trimmed $symbol" else trimmed
            CurrencyPosition.BEFORE -> if (symbol.isNotBlank()) "$symbol$trimmed" else trimmed
        }
    }

    private fun getTypeface(
        font: MenuFontFamily,
        customPath: String? = null,
        bold: Boolean = false,
        italic: Boolean = false
    ): Typeface {
        val baseTf = if (font == MenuFontFamily.CUSTOM && !customPath.isNullOrBlank()) {
            FontStorageManager.loadTypefaceFromFile(customPath) ?: Typeface.SANS_SERIF
        } else {
            when (font) {
                MenuFontFamily.SERIF -> Typeface.SERIF
                MenuFontFamily.SANS -> Typeface.SANS_SERIF
                MenuFontFamily.CURSIVE -> try { Typeface.create("cursive", Typeface.NORMAL) } catch (e: Exception) { Typeface.SERIF }
                MenuFontFamily.MONO -> Typeface.MONOSPACE
                MenuFontFamily.SERIF_BODONI -> Typeface.SERIF
                MenuFontFamily.SANS_GEOMETRIC -> Typeface.SANS_SERIF
                MenuFontFamily.CURSIVE_VINTAGE -> try { Typeface.create("cursive", Typeface.NORMAL) } catch (e: Exception) { Typeface.SERIF }
                MenuFontFamily.DISPLAY_GOURMET -> Typeface.SERIF
                MenuFontFamily.CUSTOM -> Typeface.SANS_SERIF
            }
        }
        val style = when {
            bold && italic -> Typeface.BOLD_ITALIC
            bold -> Typeface.BOLD
            italic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }
        return Typeface.create(baseTf, style)
    }

    private fun drawFreeElement(
        context: Context,
        canvas: Canvas,
        width: Float,
        height: Float,
        element: FreeElement,
        scale: Float,
        defaultTypeface: Typeface
    ) {
        val posX = element.xRatio * width
        val posY = element.yRatio * height

        canvas.save()
        canvas.translate(posX, posY)
        canvas.rotate(element.rotation)
        canvas.scale(element.scale, element.scale)

        when (element.type) {
            FreeElementType.TEXT -> {
                if (element.text.isNotBlank()) {
                    val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = parseColor(element.colorHex, Color.WHITE)
                        typeface = getTypeface(
                            font = element.fontFamily,
                            customPath = element.customFontPath,
                            bold = element.isBold,
                            italic = false
                        )
                        textSize = element.fontSizeSp * scale * 2.0f
                    }
                    val textW = paint.measureText(element.text)
                    val textH = paint.textSize

                    val padH = 12f * scale
                    val padV = 8f * scale
                    val rect = RectF(
                        -textW / 2f - padH,
                        -textH / 2f - padV,
                        textW / 2f + padH,
                        textH / 2f + padV
                    )

                    if (element.hasBackground) {
                        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = parseColor(element.bgColorHex, Color.BLACK)
                            alpha = (element.bgOpacity.coerceIn(0f, 1f) * 255).toInt()
                        }
                        val r = element.cornerRadiusDp * scale
                        canvas.drawRoundRect(rect, r, r, bgPaint)
                    }

                    canvas.drawText(element.text, -textW / 2f, (textH * 0.35f), paint)
                }
            }
            FreeElementType.PHOTO -> {
                if (!element.imageUri.isNullOrBlank()) {
                    val bmp = loadBitmapFromUri(context, element.imageUri)
                    if (bmp != null) {
                        val baseSize = 220f * scale
                        val aspect = bmp.width.toFloat() / bmp.height.toFloat().coerceAtLeast(1f)
                        val w = if (aspect >= 1f) baseSize else baseSize * aspect
                        val h = if (aspect >= 1f) baseSize / aspect else baseSize
                        val rect = RectF(-w / 2f, -h / 2f, w / 2f, h / 2f)

                        val cornerRadius = element.cornerRadiusDp * scale
                        if (cornerRadius > 1f) {
                            val path = Path().apply {
                                addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
                            }
                            canvas.save()
                            canvas.clipPath(path)
                            canvas.drawBitmap(bmp, null, rect, Paint(Paint.FILTER_BITMAP_FLAG))
                            canvas.restore()
                        } else {
                            canvas.drawBitmap(bmp, null, rect, Paint(Paint.FILTER_BITMAP_FLAG))
                        }
                    }
                }
            }
        }

        canvas.restore()
    }

    private fun parseColor(hex: String, fallback: Int): Int {
        return try {
            Color.parseColor(hex)
        } catch (e: Exception) {
            fallback
        }
    }

    // --- File Saving & Export Helpers ---

    fun saveToGallery(context: Context, bitmap: Bitmap, name: String, isPng: Boolean): Uri? {
        val extension = if (isPng) "png" else "jpg"
        val mimeType = if (isPng) "image/png" else "image/jpeg"
        val format = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        val quality = if (isPng) 100 else 95
        val filename = "${name.replace(" ", "_")}_${System.currentTimeMillis()}.$extension"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Carta")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null

        try {
            resolver.openOutputStream(uri)?.use { os ->
                bitmap.compress(format, quality, os)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            return uri
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun saveToCacheForSharing(context: Context, bitmap: Bitmap, name: String, isPng: Boolean): Uri? {
        val extension = if (isPng) "png" else "jpg"
        val format = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        val quality = if (isPng) 100 else 95
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "${name.replace(" ", "_")}_${System.currentTimeMillis()}.$extension")

        FileOutputStream(file).use { os ->
            bitmap.compress(format, quality, os)
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    // --- Multi-Page Folder (Carta Completa) Export ---

    fun generateFolderPdf(
        context: Context,
        folder: MenuFolder,
        orderedMenus: List<MenuData>
    ): File {
        val pdfDocument = android.graphics.pdf.PdfDocument()
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safeName = folder.name.replace(" ", "_").replace("/", "_")
        val pdfFile = File(exportDir, "Carta_${safeName}_${System.currentTimeMillis()}.pdf")

        var pageNumber = 1

        // 1. Portada (Cover) if present
        if (!folder.coverImageUri.isNullOrBlank()) {
            val coverBmp = loadBitmapFromUri(context, folder.coverImageUri)
            if (coverBmp != null) {
                val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(1080, 1920, pageNumber++).create()
                val page = pdfDocument.startPage(pageInfo)
                drawCoverBitmap(page.canvas, coverBmp, 1080f, 1920f)
                pdfDocument.finishPage(page)
            }
        }

        // 2. Ordered Menus / Draft Pages
        orderedMenus.forEach { menu ->
            val w = menu.style.aspectRatio.exportWidth
            val h = menu.style.aspectRatio.exportHeight
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(w, h, pageNumber++).create()
            val page = pdfDocument.startPage(pageInfo)
            drawMenuOnCanvas(context, page.canvas, w.toFloat(), h.toFloat(), menu, isExport = true)
            pdfDocument.finishPage(page)
        }

        // 3. Foto Final (Back Cover) if present
        if (!folder.backCoverImageUri.isNullOrBlank()) {
            val backBmp = loadBitmapFromUri(context, folder.backCoverImageUri)
            if (backBmp != null) {
                val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(1080, 1920, pageNumber++).create()
                val page = pdfDocument.startPage(pageInfo)
                drawCoverBitmap(page.canvas, backBmp, 1080f, 1920f)
                pdfDocument.finishPage(page)
            }
        }

        FileOutputStream(pdfFile).use { os ->
            pdfDocument.writeTo(os)
        }
        pdfDocument.close()

        return pdfFile
    }

    fun generateFolderLongImage(
        context: Context,
        folder: MenuFolder,
        orderedMenus: List<MenuData>
    ): Bitmap {
        val pageBitmaps = mutableListOf<Bitmap>()
        val targetWidth = 1080

        // 1. Portada (Cover)
        if (!folder.coverImageUri.isNullOrBlank()) {
            val coverBmp = loadBitmapFromUri(context, folder.coverImageUri)
            if (coverBmp != null) {
                val scaledHeight = (coverBmp.height.toFloat() / coverBmp.width.toFloat() * targetWidth).toInt().coerceIn(1080, 1920)
                val destBmp = Bitmap.createBitmap(targetWidth, scaledHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(destBmp)
                drawCoverBitmap(canvas, coverBmp, targetWidth.toFloat(), scaledHeight.toFloat())
                pageBitmaps.add(destBmp)
            }
        }

        // 2. Draft Pages
        orderedMenus.forEach { menu ->
            val bmp = generateHighResBitmap(context, menu)
            if (bmp.width != targetWidth) {
                val scaledHeight = (bmp.height.toFloat() / bmp.width.toFloat() * targetWidth).toInt()
                val scaled = Bitmap.createScaledBitmap(bmp, targetWidth, scaledHeight, true)
                pageBitmaps.add(scaled)
            } else {
                pageBitmaps.add(bmp)
            }
        }

        // 3. Foto Final (Back Cover)
        if (!folder.backCoverImageUri.isNullOrBlank()) {
            val backBmp = loadBitmapFromUri(context, folder.backCoverImageUri)
            if (backBmp != null) {
                val scaledHeight = (backBmp.height.toFloat() / backBmp.width.toFloat() * targetWidth).toInt().coerceIn(1080, 1920)
                val destBmp = Bitmap.createBitmap(targetWidth, scaledHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(destBmp)
                drawCoverBitmap(canvas, backBmp, targetWidth.toFloat(), scaledHeight.toFloat())
                pageBitmaps.add(destBmp)
            }
        }

        if (pageBitmaps.isEmpty()) {
            return Bitmap.createBitmap(targetWidth, 1920, Bitmap.Config.ARGB_8888)
        }

        val totalHeight = pageBitmaps.sumOf { it.height }
        val stitched = Bitmap.createBitmap(targetWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(stitched)

        var curY = 0f
        pageBitmaps.forEach { pageBmp ->
            canvas.drawBitmap(pageBmp, 0f, curY, null)
            curY += pageBmp.height.toFloat()
        }

        return stitched
    }

    fun exportFolderPagesToGallery(
        context: Context,
        folder: MenuFolder,
        orderedMenus: List<MenuData>,
        isPng: Boolean
    ): List<Uri> {
        val exported = mutableListOf<Uri>()
        var pageNum = 1

        // 1. Cover
        if (!folder.coverImageUri.isNullOrBlank()) {
            val coverBmp = loadBitmapFromUri(context, folder.coverImageUri)
            if (coverBmp != null) {
                val uri = saveToGallery(context, coverBmp, "${folder.name}_01_portada", isPng)
                if (uri != null) exported.add(uri)
                pageNum++
            }
        }

        // 2. Drafts in sequence
        orderedMenus.forEachIndexed { i, menu ->
            val bmp = generateHighResBitmap(context, menu)
            val numStr = String.format("%02d", pageNum++)
            val safeMenuName = menu.name.ifBlank { "pagina_${i + 1}" }.replace(" ", "_")
            val uri = saveToGallery(context, bmp, "${folder.name}_${numStr}_$safeMenuName", isPng)
            if (uri != null) exported.add(uri)
        }

        // 3. Back cover
        if (!folder.backCoverImageUri.isNullOrBlank()) {
            val backBmp = loadBitmapFromUri(context, folder.backCoverImageUri)
            if (backBmp != null) {
                val numStr = String.format("%02d", pageNum++)
                val uri = saveToGallery(context, backBmp, "${folder.name}_${numStr}_final", isPng)
                if (uri != null) exported.add(uri)
            }
        }

        return exported
    }
}
