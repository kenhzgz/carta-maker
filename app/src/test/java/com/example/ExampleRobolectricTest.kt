package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.MenuParser
import com.example.data.MenuRepository
import com.example.model.MenuTemplates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Carta", appName)
    }

    @Test
    fun `test bulk menu parsing`() {
        val raw = """
            # Entrantes
            Croquetas de Jamón - 9,50 € - Con bechamel suave [Especialidad]
            Carpaccio de Calabacín - 11,00 € [Vegano]
            
            # Principales
            Solomillo de Vaca - 24,00 € - A la brasa
        """.trimIndent()

        val sections = MenuParser.parseRawText(raw)
        assertEquals(2, sections.size)
        assertEquals("Entrantes", sections[0].title)
        assertEquals(2, sections[0].items.size)
        assertEquals("Croquetas de Jamón", sections[0].items[0].name)
        assertEquals("9,50", sections[0].items[0].price)
        assertEquals("Especialidad", sections[0].items[0].badge)
        assertEquals("Principales", sections[1].title)
        assertEquals(1, sections[1].items.size)
    }

    @Test
    fun `test repository initial menus and folders`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = MenuRepository(context)
        val menus = repo.getAllMenus()
        val folders = repo.getAllFolders()
        assertTrue(menus.isNotEmpty())
        assertTrue(folders.isNotEmpty())
        assertEquals("Atelier Bistró (Ejemplo)", menus.first().name)
    }
}
