package io.github.chandu4221.composestudio

import io.github.chandu4221.composestudio.data.ComponentCatalog
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CatalogResourceTest {

    @Test
    fun catalog_deserialization_contains_all_135_components() {
        val stream = CatalogResourceTest::class.java.getResourceAsStream("/compose-catalog.json")
            ?: CatalogResourceTest::class.java.classLoader?.getResourceAsStream("compose-catalog.json")
        assertNotNull(stream, "compose-catalog.json should be present on classpath")

        val jsonText = stream.bufferedReader().use { it.readText() }
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
        val catalog = json.decodeFromString<ComponentCatalog>(jsonText)

        assertEquals("1.7.3", catalog.composeMultiplatformVersion)
        assertEquals("1.4.0", catalog.material3Version)
        assertEquals(135, catalog.totalCount)
        assertEquals(135, catalog.components.size)

        val scaffold = catalog.components.find { it.id == "Scaffold" }
        assertNotNull(scaffold, "Scaffold should be present in the catalog")
        assertEquals("LAYOUT", scaffold.category)
        assertTrue(scaffold.slots.any { it.name == "topBar" })
        assertTrue(scaffold.slots.any { it.name == "content" })
        assertTrue(scaffold.slots.any { it.name == "floatingActionButton" })
        assertTrue(scaffold.slots.any { it.name == "bottomBar" })
    }
}
