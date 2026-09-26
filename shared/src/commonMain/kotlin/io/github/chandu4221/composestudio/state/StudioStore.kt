package io.github.chandu4221.composestudio.state

import androidx.compose.runtime.staticCompositionLocalOf
import io.github.chandu4221.composestudio.data.AtomicCategory
import io.github.chandu4221.composestudio.data.CatalogRepository
import io.github.chandu4221.composestudio.data.ComponentCatalog
import io.github.chandu4221.composestudio.data.ComponentDefinition
import io.github.chandu4221.composestudio.engine.SlotValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive

/**
 * CompositionLocal providing access to the visual editor store.
 */
val LocalStudioStore = staticCompositionLocalOf<StudioStore> {
    error("No StudioStore provided")
}

/**
 * Single source of truth Store for ComposeStudio canvas document.
 * Manages StateFlow<ProjectState> and executes pure state reductions.
 */
class StudioStore(
    initialCatalog: ComponentCatalog? = null,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private var catalog: ComponentCatalog = initialCatalog ?: ComponentCatalog()
    val currentCatalog: ComponentCatalog get() = catalog

    private val _state = MutableStateFlow(createInitialProjectState())
    val state: StateFlow<ProjectState> = _state.asStateFlow()

    init {
        if (initialCatalog == null) {
            coroutineScope.launch {
                val loadedCatalog = CatalogRepository.getComponentCatalog()
                catalog = loadedCatalog
            }
        }
    }

    /**
     * Dispatches an intent to trigger a pure state mutation.
     */
    fun dispatch(intent: StudioIntent) {
        _state.update { currentState -> reduce(currentState, intent) }
    }

    /**
     * Pure reducer function: (CurrentState, Intent) -> NextState
     */
    private fun reduce(currentState: ProjectState, intent: StudioIntent): ProjectState {
        return when (intent) {
            is StudioIntent.LoadCatalog -> {
                catalog = intent.catalog
                currentState
            }

            is StudioIntent.DropComponent -> {
                handleDrop(currentState, intent)
            }

            is StudioIntent.SelectNode -> {
                currentState.copy(
                    selectedNodeId = intent.nodeId,
                    selectedSlotName = if (intent.nodeId == null) null else intent.slotName
                )
            }

            is StudioIntent.SelectSlot -> {
                currentState.copy(
                    selectedNodeId = intent.nodeId,
                    selectedSlotName = intent.slotName
                )
            }

            is StudioIntent.UpdateProperty -> {
                val updatedRoot = currentState.rootNode?.updateNode(intent.nodeId) { node ->
                    node.copy(properties = node.properties + (intent.key to intent.value))
                }
                currentState.copy(rootNode = updatedRoot)
            }

            is StudioIntent.AddModifier -> {
                val updatedRoot = currentState.rootNode?.updateNode(intent.nodeId) { node ->
                    node.copy(modifiers = node.modifiers + intent.modifier)
                }
                currentState.copy(rootNode = updatedRoot)
            }

            is StudioIntent.RemoveModifier -> {
                val updatedRoot = currentState.rootNode?.updateNode(intent.nodeId) { node ->
                    node.copy(modifiers = node.modifiers.filterNot { it.id == intent.modifierId })
                }
                currentState.copy(rootNode = updatedRoot)
            }

            is StudioIntent.DeleteNode -> {
                if (currentState.rootNode?.id == intent.nodeId) {
                    currentState.copy(rootNode = null, selectedNodeId = null, selectedSlotName = null)
                } else {
                    val updatedRoot = currentState.rootNode?.removeNode(intent.nodeId)
                    val isDeletedSelected = currentState.selectedNodeId == intent.nodeId
                    currentState.copy(
                        rootNode = updatedRoot,
                        selectedNodeId = if (isDeletedSelected) null else currentState.selectedNodeId,
                        selectedSlotName = if (isDeletedSelected) null else currentState.selectedSlotName
                    )
                }
            }

            is StudioIntent.SetRootNode -> {
                currentState.copy(rootNode = intent.node, selectedNodeId = intent.node.id, selectedSlotName = null)
            }

            is StudioIntent.ResetCanvas -> {
                createInitialProjectState()
            }
        }
    }

    /**
     * Resolves the target slot and drops the component into the component tree.
     */
    private fun handleDrop(state: ProjectState, intent: StudioIntent.DropComponent): ProjectState {
        val root = state.rootNode
        val newNode = createDefaultNode(intent.component)

        // Case 1: Tree is currently empty, dropped component becomes root
        if (root == null) {
            return state.copy(rootNode = newNode, selectedNodeId = newNode.id)
        }

        // Case 2: Find target parent node
        val targetParent = root.findNode(intent.targetParentId) ?: root

        // Resolve compatible slot on the target parent
        var resolvedSlot = SlotValidator.resolveDrop(
            parentNode = targetParent,
            draggedComponent = intent.component,
            catalog = catalog,
            targetSlotName = intent.targetSlotName
        )

        var finalParentId = targetParent.id

        // If target parent cannot accept the component (e.g. user clicked a Text leaf node),
        // try to drop into targetParent's own container parent
        if (resolvedSlot == null) {
            val parentInfo = root.findParent(targetParent.id)
            if (parentInfo != null) {
                val grandParent = parentInfo.first
                val fallbackSlot = SlotValidator.resolveDrop(
                    parentNode = grandParent,
                    draggedComponent = intent.component,
                    catalog = catalog,
                    targetSlotName = null
                )
                if (fallbackSlot != null) {
                    resolvedSlot = fallbackSlot
                    finalParentId = grandParent.id
                }
            }
        }

        if (resolvedSlot == null) {
            // Strict rejection if no compatible slot found
            return state
        }

        // Insert child into resolved slot of final parent
        val updatedRoot = root.updateNode(finalParentId) { parent ->
            parent.addChild(resolvedSlot, newNode)
        }

        return state.copy(
            rootNode = updatedRoot,
            selectedNodeId = newNode.id,
            selectedSlotName = null
        )
    }

    /**
     * Creates a new ComponentNode with sensible default properties from catalog.
     */
    private fun createDefaultNode(component: ComponentDefinition): ComponentNode {
        val defaultProperties = mutableMapOf<String, kotlinx.serialization.json.JsonElement>()

        when (component.id) {
            "Text" -> {
                defaultProperties["text"] = JsonPrimitive("New Text")
            }
            "Button", "ElevatedButton", "FilledTonalButton", "OutlinedButton", "TextButton" -> {
                defaultProperties["text"] = JsonPrimitive(component.displayName)
                defaultProperties["enabled"] = JsonPrimitive(true)
            }
            "TopAppBar", "CenterAlignedTopAppBar" -> {
                defaultProperties["title"] = JsonPrimitive("Screen Title")
            }
            "Card", "ElevatedCard", "OutlinedCard" -> {
                defaultProperties["enabled"] = JsonPrimitive(true)
            }
            "Switch" -> {
                defaultProperties["checked"] = JsonPrimitive(false)
            }
            "Checkbox" -> {
                defaultProperties["checked"] = JsonPrimitive(true)
            }
            "Slider" -> {
                defaultProperties["value"] = JsonPrimitive(0.5f)
            }
        }

        return ComponentNode(
            catalogId = component.id,
            category = component.atomicCategory,
            properties = defaultProperties
        )
    }

    companion object {
        /**
         * Builds an initial default project template matching the Compose preview frame.
         */
        fun createInitialProjectState(): ProjectState {
            val buttonNode = ComponentNode(
                catalogId = "Button",
                category = AtomicCategory.ATOM,
                properties = mapOf(
                    "text" to JsonPrimitive("Get Started"),
                    "enabled" to JsonPrimitive(true)
                )
            )

            val bodyText = ComponentNode(
                catalogId = "Text",
                category = AtomicCategory.ATOM,
                properties = mapOf("text" to JsonPrimitive("Build beautiful apps with Jetpack Compose and Material 3."))
            )

            val headingText = ComponentNode(
                catalogId = "Text",
                category = AtomicCategory.ATOM,
                properties = mapOf("text" to JsonPrimitive("Welcome to Compose"))
            )

            val contentColumn = ComponentNode(
                catalogId = "Column",
                category = AtomicCategory.ORGANISM,
                slots = mapOf(
                    "content" to listOf(headingText, bodyText, buttonNode)
                )
            )

            val topBarNode = ComponentNode(
                catalogId = "TopAppBar",
                category = AtomicCategory.ORGANISM,
                properties = mapOf("title" to JsonPrimitive("Compose App"))
            )

            val scaffoldRoot = ComponentNode(
                catalogId = "Scaffold",
                category = AtomicCategory.TEMPLATE,
                slots = mapOf(
                    "topBar" to listOf(topBarNode),
                    "content" to listOf(contentColumn)
                )
            )

            return ProjectState(
                rootNode = scaffoldRoot,
                selectedNodeId = buttonNode.id
            )
        }
    }
}
