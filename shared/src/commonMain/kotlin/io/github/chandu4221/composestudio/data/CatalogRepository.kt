package io.github.chandu4221.composestudio.data

import composestudio.shared.generated.resources.Res
import kotlinx.serialization.json.Json

/**
 * Repository responsible for loading and querying component & modifier catalogs.
 * Uses Compose Multiplatform Resources to load packaged JSON assets on Desktop and Web.
 */
object CatalogRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private var cachedComponentCatalog: ComponentCatalog? = null
    private var cachedModifierCatalog: ModifierCatalog? = null

    /**
     * Loads the component catalog containing all 43 Compose Multiplatform & Material 3 components.
     */
    suspend fun getComponentCatalog(): ComponentCatalog {
        cachedComponentCatalog?.let { return it }

        return try {
            val bytes = Res.readBytes("files/compose-catalog.json")
            val catalog = json.decodeFromString<ComponentCatalog>(bytes.decodeToString())
            cachedComponentCatalog = catalog
            catalog
        } catch (e: Exception) {
            println("Error loading compose-catalog.json: ${e.message}")
            ComponentCatalog()
        }
    }

    /**
     * Loads the modifier catalog containing all 55 Compose Multiplatform modifiers.
     */
    suspend fun getModifierCatalog(): ModifierCatalog {
        cachedModifierCatalog?.let { return it }

        return try {
            val bytes = Res.readBytes("files/compose-modifiers.json")
            val catalog = json.decodeFromString<ModifierCatalog>(bytes.decodeToString())
            cachedModifierCatalog = catalog
            catalog
        } catch (e: Exception) {
            println("Error loading compose-modifiers.json: ${e.message}")
            ModifierCatalog()
        }
    }

    /**
     * Filters components by search query across display names and categories.
     */
    fun filterComponents(
        components: List<ComponentDefinition>,
        query: String
    ): List<ComponentDefinition> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return components
        return components.filter {
            it.displayName.contains(trimmed, ignoreCase = true) ||
            it.id.contains(trimmed, ignoreCase = true) ||
            it.category.contains(trimmed, ignoreCase = true)
        }
    }

    /**
     * Returns modifiers applicable for the given parent scope.
     * Includes all 'ANY' modifiers plus modifiers matching [parentScope] (e.g. 'COLUMN', 'ROW', 'BOX').
     */
    suspend fun getApplicableModifiers(parentScope: String?): List<ModifierDefinition> {
        val catalog = getModifierCatalog()
        return catalog.modifiers.filter { modifier ->
            "ANY" in modifier.applicableScopes ||
            (parentScope != null && parentScope in modifier.applicableScopes)
        }
    }
}
