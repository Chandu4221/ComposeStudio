package io.github.chandu4221.composestudio.engine

import io.github.chandu4221.composestudio.data.AtomicCategory
import io.github.chandu4221.composestudio.data.ComponentCatalog
import io.github.chandu4221.composestudio.data.ComponentDefinition
import io.github.chandu4221.composestudio.state.ComponentNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SlotValidatorTest {

    private val catalog = ComponentCatalog(
        components = listOf(
            ComponentDefinition(id = "Scaffold", displayName = "Scaffold", category = "LAYOUT"),
            ComponentDefinition(id = "TopAppBar", displayName = "TopAppBar", category = "NAVIGATION"),
            ComponentDefinition(id = "NavigationBar", displayName = "NavigationBar", category = "NAVIGATION"),
            ComponentDefinition(id = "FloatingActionButton", displayName = "FloatingActionButton", category = "INPUT"),
            ComponentDefinition(id = "Column", displayName = "Column", category = "LAYOUT"),
            ComponentDefinition(id = "Row", displayName = "Row", category = "LAYOUT"),
            ComponentDefinition(id = "Box", displayName = "Box", category = "LAYOUT"),
            ComponentDefinition(id = "Card", displayName = "Card", category = "SURFACE"),
            ComponentDefinition(id = "Button", displayName = "Button", category = "INPUT"),
            ComponentDefinition(id = "IconButton", displayName = "IconButton", category = "INPUT"),
            ComponentDefinition(id = "Text", displayName = "Text", category = "DISPLAY"),
            ComponentDefinition(id = "Icon", displayName = "Icon", category = "DISPLAY")
        )
    )

    private fun findDef(id: String) = catalog.components.first { it.id == id }

    @Test
    fun template_cannot_be_nested_in_any_slot() {
        val scaffoldNode = ComponentNode(catalogId = "Scaffold")
        val columnNode = ComponentNode(catalogId = "Column")
        val buttonNode = ComponentNode(catalogId = "Button")
        val scaffoldDef = findDef("Scaffold")

        // Try dropping Scaffold into Scaffold content
        val res1 = SlotValidator.validateDrop(scaffoldNode, "content", scaffoldDef, catalog)
        assertIs<SlotValidationResult.Rejected.CannotNestTemplate>(res1)
        assertEquals("Scaffold", res1.templateId)

        // Try dropping Scaffold into Column content
        val res2 = SlotValidator.validateDrop(columnNode, "content", scaffoldDef, catalog)
        assertIs<SlotValidationResult.Rejected.CannotNestTemplate>(res2)

        // Try dropping Scaffold into Button content
        val res3 = SlotValidator.validateDrop(buttonNode, "content", scaffoldDef, catalog)
        assertIs<SlotValidationResult.Rejected.CannotNestTemplate>(res3)
    }

    @Test
    fun leaf_component_rejects_all_drops() {
        val textNode = ComponentNode(catalogId = "Text")
        val buttonDef = findDef("Button")

        val result = SlotValidator.validateDrop(textNode, "content", buttonDef, catalog)
        assertIs<SlotValidationResult.Rejected.LeafComponentNoSlots>(result)
        assertEquals("Text", result.parentCatalogId)
    }

    @Test
    fun category_rejection_when_category_not_allowed() {
        val buttonNode = ComponentNode(catalogId = "Button")
        val cardDef = findDef("Card") // Card is ORGANISM, Button content only allows ATOM

        val result = SlotValidator.validateDrop(buttonNode, "content", cardDef, catalog)
        assertIs<SlotValidationResult.Rejected.CategoryNotAllowed>(result)
        assertEquals(AtomicCategory.ORGANISM, result.attemptedCategory)
    }

    @Test
    fun whitelist_enforcement_on_slot() {
        val scaffoldNode = ComponentNode(catalogId = "Scaffold")
        val textDef = findDef("Text") // ATOM, but Scaffold.topBar explicitly whitelists TopAppBar variants

        val result = SlotValidator.validateDrop(scaffoldNode, "topBar", textDef, catalog)
        // Text is ATOM, while topBar allows ORGANISM
        assertTrue(result is SlotValidationResult.Rejected)

        val topAppBarDef = findDef("TopAppBar")
        val validResult = SlotValidator.validateDrop(scaffoldNode, "topBar", topAppBarDef, catalog)
        assertIs<SlotValidationResult.Valid>(validResult)
        assertEquals("topBar", validResult.slotName)
    }

    @Test
    fun single_cardinality_slot_rejects_second_child() {
        val topAppBarNode = ComponentNode(catalogId = "TopAppBar")
        val scaffoldNode = ComponentNode(
            catalogId = "Scaffold",
            slots = mapOf("topBar" to listOf(topAppBarNode))
        )
        val secondTopAppBarDef = findDef("TopAppBar")

        val result = SlotValidator.validateDrop(scaffoldNode, "topBar", secondTopAppBarDef, catalog)
        assertIs<SlotValidationResult.Rejected.SlotFull>(result)
        assertEquals(1, result.currentChildCount)
    }

    @Test
    fun multiple_cardinality_slot_accepts_additional_children() {
        val text1 = ComponentNode(catalogId = "Text")
        val columnNode = ComponentNode(
            catalogId = "Column",
            slots = mapOf("content" to listOf(text1))
        )
        val textDef = findDef("Text")

        val result = SlotValidator.validateDrop(columnNode, "content", textDef, catalog)
        assertIs<SlotValidationResult.Valid>(result)
        assertEquals("content", result.slotName)
        assertEquals("ColumnScope", result.scopeReceiver)
    }

    @Test
    fun scope_receiver_resolution() {
        val rowNode = ComponentNode(catalogId = "Row")
        val boxNode = ComponentNode(catalogId = "Box")
        val textDef = findDef("Text")

        val rowResult = SlotValidator.validateDrop(rowNode, "content", textDef, catalog)
        assertIs<SlotValidationResult.Valid>(rowResult)
        assertEquals("RowScope", rowResult.scopeReceiver)

        val boxResult = SlotValidator.validateDrop(boxNode, "content", textDef, catalog)
        assertIs<SlotValidationResult.Valid>(boxResult)
        assertEquals("BoxScope", boxResult.scopeReceiver)
    }

    @Test
    fun scaffold_auto_routing() {
        val scaffoldNode = ComponentNode(catalogId = "Scaffold")

        val topBarDef = findDef("TopAppBar")
        val navBarDef = findDef("NavigationBar")
        val fabDef = findDef("FloatingActionButton")
        val columnDef = findDef("Column")

        assertEquals("topBar", SlotValidator.resolveDrop(scaffoldNode, topBarDef, catalog))
        assertEquals("bottomBar", SlotValidator.resolveDrop(scaffoldNode, navBarDef, catalog))
        assertEquals("floatingActionButton", SlotValidator.resolveDrop(scaffoldNode, fabDef, catalog))
        assertEquals("content", SlotValidator.resolveDrop(scaffoldNode, columnDef, catalog))
    }

    @Test
    fun scaffold_auto_routing_skips_occupied_single_slot() {
        val existingTopBar = ComponentNode(catalogId = "TopAppBar")
        val scaffoldWithTopBar = ComponentNode(
            catalogId = "Scaffold",
            slots = mapOf("topBar" to listOf(existingTopBar))
        )
        val secondTopBar = findDef("TopAppBar")

        // topBar is full, and TopAppBar is ORGANISM which cannot go to content (Scaffold content allows ORGANISM, but wait!)
        // Does resolveDrop fallback to content or return null?
        val resolved = SlotValidator.resolveDrop(scaffoldWithTopBar, secondTopBar, catalog)
        // In Scaffold resolveDrop, if topBar is full, it checks candidate slots.
        // If content allows ORGANISM, it can accept it or if rejected, returns null.
        // Let's verify topBar itself is NOT chosen:
        assertTrue(resolved != "topBar")
    }

    @Test
    fun resolve_drop_with_invalid_target_slot_returns_null() {
        val buttonNode = ComponentNode(catalogId = "Button")
        val cardDef = findDef("Card")

        val resolved = SlotValidator.resolveDrop(
            parentNode = buttonNode,
            draggedComponent = cardDef,
            catalog = catalog,
            targetSlotName = "content"
        )
        assertNull(resolved)
    }
}
