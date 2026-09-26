package io.github.chandu4221.composestudio.state

import io.github.chandu4221.composestudio.data.ComponentCatalog
import io.github.chandu4221.composestudio.data.ComponentDefinition
import kotlinx.serialization.json.JsonElement

/**
 * Sealed interface representing all user intents dispatched in ComposeStudio.
 * Guarantees a strict Unidirectional Data Flow (UDF).
 */
sealed interface StudioIntent {
    /**
     * Initializes or updates the active component catalog.
     */
    data class LoadCatalog(val catalog: ComponentCatalog) : StudioIntent

    /**
     * Drops/adds a component from the catalog into a parent node at an optional slot name.
     * If [targetSlotName] is null, the slot routing engine resolves the optimal compatible slot.
     */
    data class DropComponent(
        val targetParentId: String,
        val targetSlotName: String? = null,
        val component: ComponentDefinition
    ) : StudioIntent

    /**
     * Selects a component node in the canvas/inspector. Passing null clears the selection.
     * Optionally targets a specific [slotName] on that component.
     */
    data class SelectNode(
        val nodeId: String?,
        val slotName: String? = null
    ) : StudioIntent

    /**
     * Specifically targets or clears an active slot on a node for insertion.
     */
    data class SelectSlot(
        val nodeId: String,
        val slotName: String?
    ) : StudioIntent

    /**
     * Updates or sets a property on a component node.
     */
    data class UpdateProperty(
        val nodeId: String,
        val key: String,
        val value: JsonElement
    ) : StudioIntent

    /**
     * Appends a modifier to the node's modifier chain.
     */
    data class AddModifier(
        val nodeId: String,
        val modifier: ModifierNode
    ) : StudioIntent

    /**
     * Removes a modifier from the node's modifier chain.
     */
    data class RemoveModifier(
        val nodeId: String,
        val modifierId: String
    ) : StudioIntent

    /**
     * Removes a node from the canvas tree.
     */
    data class DeleteNode(val nodeId: String) : StudioIntent

    /**
     * Replaces the root node with a new tree.
     */
    data class SetRootNode(val node: ComponentNode) : StudioIntent

    /**
     * Resets the canvas to the default starting template.
     */
    data object ResetCanvas : StudioIntent
}
