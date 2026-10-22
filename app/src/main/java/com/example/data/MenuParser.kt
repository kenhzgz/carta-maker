package com.example.data

import com.example.model.MenuItem
import com.example.model.MenuSection
import java.util.UUID

object MenuParser {

    /**
     * Parses raw user text into structured MenuSections and MenuItems.
     */
    fun parseRawText(rawText: String): List<MenuSection> {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        val sections = mutableListOf<MenuSection>()
        var currentSectionTitle = "Carta"
        var currentItems = mutableListOf<MenuItem>()

        // Regex for detecting prices like "9,50 €", "9.50", "9€", "$12.50", "12,00", "€15"
        val priceRegex = Regex("""(?:[$€£]\s*)?(\d{1,4}(?:[.,]\d{1,2})?)\s*(?:[$€£]|€|eur|usd)?""", RegexOption.IGNORE_CASE)
        val badgeRegex = Regex("""(?:\[|\()([^\]\)]+)(?:\]|\))""")

        fun commitCurrentSection() {
            if (currentItems.isNotEmpty()) {
                sections.add(
                    MenuSection(
                        id = UUID.randomUUID().toString(),
                        title = currentSectionTitle.trim(),
                        items = currentItems.toList()
                    )
                )
                currentItems = mutableListOf()
            }
        }

        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            // Check if this line is a Section Header
            if (isSectionHeader(line)) {
                commitCurrentSection()
                currentSectionTitle = cleanSectionTitle(line)
                i++
                continue
            }

            // Check if line contains a dish with price
            val (item, consumedExtraLine) = parseItemLine(line, if (i + 1 < lines.size) lines[i + 1] else null, priceRegex, badgeRegex)
            if (item != null) {
                currentItems.add(item)
                if (consumedExtraLine) {
                    i++
                }
            } else {
                // If it doesn't match standard item or header, if it looks like a section header, promote it
                if (line.length <= 40 && !line.any { it.isDigit() }) {
                    commitCurrentSection()
                    currentSectionTitle = cleanSectionTitle(line)
                } else {
                    // Fallback as simple dish item without price
                    currentItems.add(MenuItem(name = line, price = ""))
                }
            }
            i++
        }

        commitCurrentSection()
        return sections.filter { it.items.isNotEmpty() || it.title.isNotBlank() }
    }

    private fun isSectionHeader(line: String): Boolean {
        val trimmed = line.trim()
        if (trimmed.startsWith("#") || trimmed.startsWith("---") || trimmed.startsWith("===") || trimmed.startsWith("•") || trimmed.startsWith("★")) {
            return true
        }
        if (trimmed.endsWith(":") && trimmed.length <= 45 && !trimmed.any { it.isDigit() }) {
            return true
        }
        // ALL CAPS check (e.g. "ENTRANTES Y TAPAS")
        val lettersOnly = trimmed.filter { it.isLetter() }
        if (lettersOnly.length >= 3 && lettersOnly.all { it.isUpperCase() } && trimmed.length <= 45 && !trimmed.any { it.isDigit() }) {
            return true
        }
        // Known keywords in Spanish/English
        val commonKeywords = listOf(
            "entrantes", "tapas", "principales", "segundos", "primeros", "carnes", "pescados",
            "pastas", "pizzas", "postres", "bebidas", "vinos", "cocktails", "cocteles", "cafes",
            "brunch", "ensaladas", "para compartir", "raciones", "starters", "mains", "desserts",
            "drinks", "cocktails", "appetizers"
        )
        val normalized = trimmed.lowercase().removeSuffix(":")
        if (commonKeywords.any { normalized == it || normalized.startsWith("$it ") }) {
            return true
        }
        return false
    }

    private fun cleanSectionTitle(line: String): String {
        return line
            .removePrefix("#").removePrefix("#").removePrefix("#")
            .removePrefix("---").removeSuffix("---")
            .removePrefix("===").removeSuffix("===")
            .removePrefix("•").removePrefix("★")
            .removeSuffix(":")
            .trim()
            .split(" ")
            .joinToString(" ") { word ->
                if (word.all { it.isUpperCase() } && word.length > 1) {
                    word.lowercase().replaceFirstChar { it.uppercase() }
                } else word
            }
    }

    private fun parseItemLine(
        line: String,
        nextLine: String?,
        priceRegex: Regex,
        badgeRegex: Regex
    ): Pair<MenuItem?, Boolean> {
        var cleanLine = line

        // Extract badge if present (e.g. [Vegano] or (Especialidad))
        var badge: String? = null
        val badgeMatch = badgeRegex.find(cleanLine)
        if (badgeMatch != null) {
            val potentialBadge = badgeMatch.groupValues[1].trim()
            if (potentialBadge.length <= 25) {
                badge = potentialBadge
                cleanLine = cleanLine.replace(badgeMatch.value, "").trim()
            }
        }

        // Try extracting price from cleanLine
        val priceMatches = priceRegex.findAll(cleanLine).toList()
        if (priceMatches.isNotEmpty()) {
            val lastPriceMatch = priceMatches.last()
            val priceStr = lastPriceMatch.groupValues[1]
            val beforePrice = cleanLine.substring(0, lastPriceMatch.range.first).trim().removeSuffix("-").removeSuffix(":").removeSuffix("—").removeSuffix(".").trim()
            val afterPrice = cleanLine.substring(lastPriceMatch.range.last + 1).trim().removePrefix("-").removePrefix(":").removePrefix("—").trim()

            var name = beforePrice
            var description = afterPrice
            var consumedNextLine = false

            // If name has delimiter like "Croquetas de jamón - Bechamel suave"
            if (name.contains(" - ") || name.contains(" — ") || name.contains(" | ")) {
                val parts = name.split(Regex("""\s*[-—|]\s*"""))
                if (parts.size >= 2) {
                    name = parts[0].trim()
                    val embeddedDesc = parts.subList(1, parts.size).joinToString(" - ").trim()
                    description = if (description.isNotBlank()) "$embeddedDesc. $description" else embeddedDesc
                }
            }

            // If description was empty on the same line, check if nextLine is a candidate description
            if (description.isBlank() && nextLine != null && !isSectionHeader(nextLine)) {
                val nextHasPrice = priceRegex.containsMatchIn(nextLine)
                if (!nextHasPrice && nextLine.length in 5..180) {
                    description = nextLine.trim()
                    consumedNextLine = true
                }
            }

            if (name.isNotBlank()) {
                return Pair(
                    MenuItem(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        price = priceStr,
                        description = description,
                        badge = badge
                    ),
                    consumedNextLine
                )
            }
        }

        return Pair(null, false)
    }
}
