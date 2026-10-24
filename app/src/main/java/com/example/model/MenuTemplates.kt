package com.example.model

object MenuTemplates {

    val restauranteCupey: MenuData
        get() = MenuData(
            id = "template_restaurante_cupey",
            name = "Restaurante Cupey",
            folderId = "default",
            header = MenuHeader(
                restaurantName = "Restaurante Cupey",
                subtitle = "Menú del día",
                footer = "",
                logoUri = "drawable/cupey_logo",
                logoSize = 1.0f,
                logoShape = LogoShape.CIRCLE,
                logoAlignment = LogoAlignment.CENTER,
                logoOffsetY = 0f,
                headerOffsetY = 0f
            ),
            background = MenuBackground(
                type = BackgroundType.SOLID_COLOR,
                texture = TexturePreset.KRAFT,
                solidColorHex = "#F6EFE5",
                contrastMode = ContrastMode.LIGHTEN,
                contrastOpacity = 0.0f
            ),
            sections = listOf(
                MenuSection(
                    title = "POSTRES",
                    items = listOf(
                        MenuItem(
                            name = "Flan de leche",
                            price = "350",
                            description = "",
                            badge = null
                        )
                    )
                ),
                MenuSection(
                    title = "BEBIDAS",
                    items = listOf(
                        MenuItem(
                            name = "Cerveza Mayabe",
                            price = "",
                            description = "Lata de 355 ml",
                            badge = null
                        ),
                        MenuItem(
                            name = "Refresco de piña",
                            price = "",
                            description = "Lata de 355 ml",
                            badge = null
                        ),
                        MenuItem(
                            name = "Néctar de pera",
                            price = "",
                            description = "Lata de 355 ml",
                            badge = null
                        ),
                        MenuItem(
                            name = "Malta Bucanero",
                            price = "",
                            description = "Lata de 355 ml",
                            badge = null
                        )
                    )
                )
            ),
            style = MenuStyle(
                aspectRatio = MenuAspectRatio.RATIO_4_5,
                titleFont = MenuFontFamily.SANS_GEOMETRIC,
                bodyFont = MenuFontFamily.SERIF,
                primaryTextColorHex = "#111111",
                secondaryTextColorHex = "#1A1A1A",
                priceColorHex = "#1D5C96",
                accentColorHex = "#1D5C96",
                cardBgColorHex = "#000000",
                cardBgOpacity = 0f,
                cardCornerRadius = 0f,
                leaderLineStyle = LeaderLineStyle.DOTS,
                globalFontScale = 1.05f,
                itemSpacing = 16f,
                sectionSpacing = 28f,
                horizontalPadding = 28f,
                topPadding = 28f,
                bottomMargin = 28f,
                textAlignment = TextAlignment.LEFT,
                currencySymbol = "$",
                currencyPosition = CurrencyPosition.BEFORE,
                textShadow = false,
                sectionDivider = SectionDividerStyle.NONE,
                showHeaderDivider = false,
                titleSizeSp = 28f,
                subtitleSizeSp = 18f,
                sectionTitleSizeSp = 20f,
                itemNameSizeSp = 17f,
                itemPriceSizeSp = 19f,
                itemDescSizeSp = 14f,
                footerSizeSp = 11f,
                restaurantNameStyle = TypographySectionStyle(
                    fontSizeSp = 28f,
                    offsetY = 0f,
                    alignment = TextAlignment.CENTER,
                    fontFamily = MenuFontFamily.SANS_GEOMETRIC,
                    colorHex = "#111111",
                    isBold = true,
                    isItalic = false,
                    isUnderlined = false
                ),
                subtitleStyle = TypographySectionStyle(
                    fontSizeSp = 18f,
                    offsetY = 0f,
                    alignment = TextAlignment.CENTER,
                    fontFamily = MenuFontFamily.SERIF,
                    colorHex = "#111111",
                    isBold = false,
                    isItalic = true,
                    isUnderlined = false
                ),
                sectionHeaderStyle = TypographySectionStyle(
                    fontSizeSp = 20f,
                    offsetY = 0f,
                    alignment = TextAlignment.LEFT,
                    fontFamily = MenuFontFamily.SERIF_BODONI,
                    colorHex = "#1D5C96",
                    isBold = true,
                    isItalic = false,
                    isUnderlined = false
                ),
                dishNameStyle = TypographySectionStyle(
                    fontSizeSp = 17f,
                    offsetY = 0f,
                    alignment = TextAlignment.LEFT,
                    fontFamily = MenuFontFamily.SERIF,
                    colorHex = "#111111",
                    isBold = false,
                    isItalic = false,
                    isUnderlined = false
                ),
                dishPriceStyle = TypographySectionStyle(
                    fontSizeSp = 19f,
                    offsetY = 0f,
                    alignment = TextAlignment.RIGHT,
                    fontFamily = MenuFontFamily.SERIF,
                    colorHex = "#1D5C96",
                    isBold = true,
                    isItalic = false,
                    isUnderlined = false
                ),
                dishDescStyle = TypographySectionStyle(
                    fontSizeSp = 14f,
                    offsetY = 0f,
                    alignment = TextAlignment.LEFT,
                    fontFamily = MenuFontFamily.SERIF,
                    colorHex = "#1A1A1A",
                    isBold = false,
                    isItalic = true,
                    isUnderlined = false
                )
            )
        )

    val atelierBistro: MenuData
        get() = MenuData(
            id = "template_atelier_bistro",
            name = "Atelier Bistró (Ejemplo)",
            folderId = "default",
            header = MenuHeader(
                restaurantName = "Atelier Bistró",
                subtitle = "Cocina de Temporada & Carbón de Encina",
                footer = "Precios con IVA incluido (10%). Disponemos de carta de alérgenos bajo petición. Pan artesanal y aperitivo de la casa: 2,50 €",
                logoSize = 1.0f,
                logoShape = LogoShape.ROUNDED
            ),
            background = MenuBackground(
                type = BackgroundType.TEXTURE,
                texture = TexturePreset.PIZARRA,
                solidColorHex = "#121212",
                contrastMode = ContrastMode.DARKEN,
                contrastOpacity = 0.25f
            ),
            sections = listOf(
                MenuSection(
                    title = "Entrantes & Tapas",
                    items = listOf(
                        MenuItem(
                            name = "Croquetas Melosas de Jamón Ibérico",
                            price = "9,50",
                            description = "Bechamel cremosa infusionada con jamón de bellota y rebozado panko crujiente (4 uds)",
                            badge = "Especialidad"
                        ),
                        MenuItem(
                            name = "Carpaccio de Calabacín y Trufa",
                            price = "11,00",
                            description = "Láminas finas con vinagreta de piñones tostados, lascas de parmesano de 24 meses y aceite de trufa",
                            badge = "Vegano"
                        ),
                        MenuItem(
                            name = "Burrata Pugliese con Higos Caramelizados",
                            price = "13,50",
                            description = "Burrata fresca con higos confitados, rúcula selvática, nueces de pecán y reducción de Módena",
                            badge = "Nuevo"
                        ),
                        MenuItem(
                            name = "Pulpo a la Brasa sobre Crema de Patata",
                            price = "16,50",
                            description = "Pata de pulpo braseada al carbón con aceite de pimentón de la Vera y sal Maldon ahumada",
                            badge = "Recomendado"
                        )
                    )
                ),
                MenuSection(
                    title = "Platos Principales",
                    items = listOf(
                        MenuItem(
                            name = "Solomillo de Vaca Madurada (30 días)",
                            price = "24,00",
                            description = "250g de carne seleccionada a la brasa con guarnición de chalotas glaseadas y patatas baby al romero",
                            badge = "Especialidad"
                        ),
                        MenuItem(
                            name = "Lomo de Lubina Salvaje en Costra de Hierbas",
                            price = "21,50",
                            description = "Pescado salvaje con muselina de hinojo asado, tirabeques salteados y vinagreta de cítricos",
                            badge = null
                        ),
                        MenuItem(
                            name = "Risotto de Setas Silvestres y Parmesano",
                            price = "15,50",
                            description = "Arroz Acquerello con boletus edulis, trompetas de la muerte y emulsión cremosa de mantequilla francesa",
                            badge = "Vegetariano"
                        ),
                        MenuItem(
                            name = "Magret de Pato con Salsa de Frutos Rojos",
                            price = "19,00",
                            description = "Corte rosado crujiente con puré aterciopelado de castañas y reducción de oporto y moras",
                            badge = null
                        )
                    )
                ),
                MenuSection(
                    title = "Postres Caseros",
                    items = listOf(
                        MenuItem(
                            name = "Tarta de Queso Fluida al Horno",
                            price = "7,00",
                            description = "Elaborada con quesos artesanos, corazón cremoso fundente y base crujiente de galleta sablée",
                            badge = "Especialidad"
                        ),
                        MenuItem(
                            name = "Coulant de Chocolate Guanaja 70%",
                            price = "7,50",
                            description = "Bizcocho caliente con corazón de chocolate negro derretido y quenelle de helado de vainilla Bourbon",
                            badge = "Sin Gluten"
                        ),
                        MenuItem(
                            name = "Tiramisú Tradicional de Mascarpone",
                            price = "6,50",
                            description = "Bizcochos savoiardi empapados en café espresso italiano, licor amaretto y cacao puro Valrhona",
                            badge = null
                        )
                    )
                )
            ),
            style = MenuStyle(
                aspectRatio = MenuAspectRatio.RATIO_9_16,
                titleFont = MenuFontFamily.SERIF,
                bodyFont = MenuFontFamily.SANS,
                primaryTextColorHex = "#F8FAFC",
                secondaryTextColorHex = "#94A3B8",
                priceColorHex = "#F59E0B",
                accentColorHex = "#F59E0B",
                cardBgColorHex = "#000000",
                cardBgOpacity = 0.25f,
                cardCornerRadius = 12f,
                leaderLineStyle = LeaderLineStyle.DOTS,
                globalFontScale = 1.0f,
                itemSpacing = 13f,
                sectionSpacing = 20f,
                horizontalPadding = 24f,
                textAlignment = TextAlignment.LEFT,
                currencySymbol = "€",
                currencyPosition = CurrencyPosition.AFTER,
                sectionDivider = SectionDividerStyle.SIMPLE_LINE
            )
        )

    val pizzeriaNapoli: MenuData
        get() = MenuData(
            id = "template_pizzeria_napoli",
            name = "Trattoria & Pizzeria Napoli",
            folderId = "default",
            header = MenuHeader(
                restaurantName = "Trattoria da Luigi",
                subtitle = "Autentica Pizza Napoletana & Pasta Fresca",
                footer = "Masa fermentada 48 horas en horno de leña a 450°C. Ingredientes con D.O.P. Campania."
            ),
            background = MenuBackground(
                type = BackgroundType.TEXTURE,
                texture = TexturePreset.TERRACOTA,
                solidColorHex = "#3B1219",
                contrastMode = ContrastMode.DARKEN,
                contrastOpacity = 0.20f
            ),
            sections = listOf(
                MenuSection(
                    title = "Antipasti",
                    items = listOf(
                        MenuItem(name = "Focaccia al Rosmarino", price = "5,50", description = "Aceite de oliva virgen extra y romero fresco", badge = "Vegano"),
                        MenuItem(name = "Tagliere di Salumi & Formaggi", price = "14,00", description = "Prosciutto di Parma 24 meses, Salame Napoli, Mortadella y Pecorino", badge = "Para compartir")
                    )
                ),
                MenuSection(
                    title = "Pizze Tradizionali",
                    items = listOf(
                        MenuItem(name = "Margherita D.O.P.", price = "10,50", description = "Pomodoro San Marzano, Mozzarella di Bufala, albahaca fresca y AOVE", badge = "Clásico"),
                        MenuItem(name = "Diavola Piccante", price = "12,50", description = "Tomate San Marzano, fior di latte, salame spianata piccante y orégano", badge = null),
                        MenuItem(name = "Quattro Formaggi", price = "13,00", description = "Fior di latte, gorgonzola dolce, provola ahumada y parmigiano reggiano", badge = null)
                    )
                ),
                MenuSection(
                    title = "Dolci",
                    items = listOf(
                        MenuItem(name = "Cannoli Siciliani", price = "5,50", description = "Rellenos al momento con ricotta de oveja y pistacho de Bronte", badge = "Nuevo"),
                        MenuItem(name = "Panna Cotta ai Frutti di Bosco", price = "5,00", description = "Con coulis artesanal de frambuesas y moras silvestres", badge = null)
                    )
                )
            ),
            style = MenuStyle(
                aspectRatio = MenuAspectRatio.RATIO_9_16,
                titleFont = MenuFontFamily.SERIF,
                bodyFont = MenuFontFamily.SANS,
                primaryTextColorHex = "#FFFBEB",
                secondaryTextColorHex = "#FDE68A",
                priceColorHex = "#F59E0B",
                accentColorHex = "#F59E0B",
                cardBgColorHex = "#000000",
                cardBgOpacity = 0.35f,
                cardCornerRadius = 14f,
                leaderLineStyle = LeaderLineStyle.LINE,
                sectionDivider = SectionDividerStyle.CLASSIC_DIAMOND
            )
        )

    val specialtyCoffee: MenuData
        get() = MenuData(
            id = "template_specialty_coffee",
            name = "Café de Especialidad & Brunch",
            folderId = "default",
            header = MenuHeader(
                restaurantName = "Origen Coffee Bar",
                subtitle = "Micro-tostadores de Café de Especialidad & Brunch",
                footer = "Leches vegetales disponibles: avena y almendra sin coste adicional. Café de origen Etiopía y Colombia."
            ),
            background = MenuBackground(
                type = BackgroundType.TEXTURE,
                texture = TexturePreset.KRAFT,
                solidColorHex = "#FDFBF7",
                contrastMode = ContrastMode.LIGHTEN,
                contrastOpacity = 0.05f
            ),
            sections = listOf(
                MenuSection(
                    title = "Café de Especialidad",
                    items = listOf(
                        MenuItem(name = "Espresso Doble (Origen Único)", price = "2,40", description = "Etiopía Yirgacheffe, notas florales de jazmín y bergamota", badge = "Especialidad"),
                        MenuItem(name = "Flat White", price = "3,20", description = "Doble shot de espresso con micro-espuma de leche sedosa", badge = "Top"),
                        MenuItem(name = "Batch Brew Filter (V60 / Moccamaster)", price = "3,50", description = "Extracción lenta en filtro, taza limpia y afrutada", badge = null),
                        MenuItem(name = "Iced Matcha Latte Ceremonial", price = "4,20", description = "Matcha Uji de grado ceremonial con leche de avena", badge = "Vegano")
                    )
                ),
                MenuSection(
                    title = "Brunch & Toasts",
                    items = listOf(
                        MenuItem(name = "Avocado Toast con Huevo Poché", price = "8,50", description = "Pan de masa madre con aguacate hass, semillas de chía, lima y rúcula", badge = "Top"),
                        MenuItem(name = "Acai Energy Bowl Orgánico", price = "9,00", description = "Açaí batido con plátano, granola casera, frutos rojos y crema de cacahuete", badge = "Vegano"),
                        MenuItem(name = "Croissant Relleno de Salmón & Ricotta", price = "7,80", description = "Mantequilla francesa, salmón ahumado salvaje, eneldo y brotes frescos", badge = null)
                    )
                )
            ),
            style = MenuStyle(
                aspectRatio = MenuAspectRatio.RATIO_9_16,
                titleFont = MenuFontFamily.SANS,
                bodyFont = MenuFontFamily.SANS,
                primaryTextColorHex = "#261C14",
                secondaryTextColorHex = "#6E5B4B",
                priceColorHex = "#854D0E",
                accentColorHex = "#B45309",
                cardBgColorHex = "#FFFFFF",
                cardBgOpacity = 0.40f,
                cardCornerRadius = 8f,
                leaderLineStyle = LeaderLineStyle.DOTS,
                currencySymbol = "€",
                sectionDivider = SectionDividerStyle.SIMPLE_LINE
            )
        )

    val cocktailBar: MenuData
        get() = MenuData(
            id = "template_cocktail_bar",
            name = "Coctelería de Autor",
            folderId = "default",
            header = MenuHeader(
                restaurantName = "The Velvet Lounge",
                subtitle = "Signature Cocktails, Tapas & Speakeasy Vibes",
                footer = "Todos nuestros jarabes y bitters son de elaboración propia en nuestro laboratorio botánico."
            ),
            background = MenuBackground(
                type = BackgroundType.TEXTURE,
                texture = TexturePreset.ESMERALDA,
                solidColorHex = "#0F172A",
                contrastMode = ContrastMode.DARKEN,
                contrastOpacity = 0.30f
            ),
            sections = listOf(
                MenuSection(
                    title = "Cócteles de Autor",
                    items = listOf(
                        MenuItem(name = "Smoked Old Fashioned", price = "12,00", description = "Bourbon macerado en madera de roble, bitter de naranja y humo de canela", badge = "Especialidad"),
                        MenuItem(name = "Paloma Botánica de Mezcal", price = "11,50", description = "Mezcal artesanal Oaxaqueño, zumo de pomelo rosa, lima fresca y sal de gusano", badge = "Nuevo"),
                        MenuItem(name = "Yuzu & Basil Gin Fizz", price = "11,00", description = "Ginebra seca japonesa, cordial de yuzu fresco, albahaca morada y soda", badge = null)
                    )
                ),
                MenuSection(
                    title = "Bocados Selectos",
                    items = listOf(
                        MenuItem(name = "Brioche de Steak Tartar Trufado", price = "9,00", description = "Pan brioche tostado con mantequilla, vaca madurada y yema curada", badge = "Recomendado"),
                        MenuItem(name = "Gildas Gourmet de Anchoa del Cantábrico", price = "4,50", description = "Piparra de Ibarra, aceituna manzanilla rellena y anchoa 00", badge = null)
                    )
                )
            ),
            style = MenuStyle(
                aspectRatio = MenuAspectRatio.RATIO_9_16,
                titleFont = MenuFontFamily.SERIF,
                bodyFont = MenuFontFamily.SANS,
                primaryTextColorHex = "#F1F5F9",
                secondaryTextColorHex = "#94A3B8",
                priceColorHex = "#FBBF24",
                accentColorHex = "#FBBF24",
                cardBgColorHex = "#041B15",
                cardBgOpacity = 0.50f,
                cardCornerRadius = 16f,
                leaderLineStyle = LeaderLineStyle.DASHES,
                sectionDivider = SectionDividerStyle.ELEGANT_FLOURISH
            )
        )

    val allTemplates = listOf(
        restauranteCupey,
        atelierBistro,
        pizzeriaNapoli,
        specialtyCoffee,
        cocktailBar
    )
}
