package io.github.chandu4221.composestudio.state

import kotlin.random.Random
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Generates an RFC-4122 compliant UUID v4 string safely across all KMP targets (JVM, WasmJs, JS).
 */
fun generateUuid(): String {
    val hexChars = "0123456789abcdef"
    return buildString(36) {
        repeat(8) { append(hexChars[Random.nextInt(16)]) }
        append('-')
        repeat(4) { append(hexChars[Random.nextInt(16)]) }
        append("-4")
        repeat(3) { append(hexChars[Random.nextInt(16)]) }
        append('-')
        append(hexChars[8 + Random.nextInt(4)])
        repeat(3) { append(hexChars[Random.nextInt(16)]) }
        append('-')
        repeat(12) { append(hexChars[Random.nextInt(16)]) }
    }
}

/**
 * Representation of an individual modifier applied to a component node.
 */
@Serializable
data class ModifierNode(
    val id: String = generateUuid(),
    val modifierId: String,
    val parameters: Map<String, JsonElement> = emptyMap()
)

/**
 * Representation of a Composable in the visual builder tree.
 * Linked to a component definition in `compose-catalog.json` via [catalogId].
 */
@Serializable
data class ComponentNode(
    val id: String = generateUuid(),
    val catalogId: String,
    val properties: Map<String, JsonElement> = emptyMap(),
    val slots: Map<String, List<ComponentNode>> = emptyMap(),
    val modifiers: List<ModifierNode> = emptyList()
) {
    /**
     * Recursively searches for a node by its ID.
     */
    fun findNode(targetId: String): ComponentNode? {
        if (id == targetId) return this
        for (children in slots.values) {
            for (child in children) {
                val found = child.findNode(targetId)
                if (found != null) return found
            }
        }
        return null
    }

    /**
     * Finds the parent node and the slot name containing the target child ID.
     */
    fun findParent(targetId: String): Pair<ComponentNode, String>? {
        for ((slotName, children) in slots) {
            if (children.any { it.id == targetId }) {
                return this to slotName
            }
            for (child in children) {
                val found = child.findParent(targetId)
                if (found != null) return found
            }
        }
        return null
    }

    /**
     * Recursively transforms a node matching [targetId] and returns the updated tree.
     */
    fun updateNode(targetId: String, transform: (ComponentNode) -> ComponentNode): ComponentNode {
        if (id == targetId) {
            return transform(this)
        }
        val updatedSlots = slots.mapValues { (_, children) ->
            children.map { it.updateNode(targetId, transform) }
        }
        return copy(slots = updatedSlots)
    }

    /**
     * Recursively removes a node matching [targetId] from any slot in the tree.
     * Returns null if this node itself is the target, or the updated node otherwise.
     */
    fun removeNode(targetId: String): ComponentNode? {
        if (id == targetId) return null
        val updatedSlots = slots.mapValues { (_, children) ->
            children.mapNotNull { it.removeNode(targetId) }
        }
        return copy(slots = updatedSlots)
    }

    /**
     * Appends a child node to the specified slot.
     */
    fun addChild(slotName: String, child: ComponentNode): ComponentNode {
        val currentSlotChildren = slots[slotName].orEmpty()
        val updatedSlots = slots + (slotName to (currentSlotChildren + child))
        return copy(slots = updatedSlots)
    }

    /**
     * Returns a flat list of all nodes in this subtree.
     */
    fun flatten(): List<ComponentNode> {
        val list = mutableListOf(this)
        for (children in slots.values) {
            for (child in children) {
                list.addAll(child.flatten())
            }
        }
        return list
    }
}

/**
 * Root state for the visual editor document.
 */
@Serializable
data class ProjectState(
    val rootNode: ComponentNode? = null,
    val selectedNodeId: String? = null
) {
    /**
     * Helper to find the currently selected node.
     */
    val selectedNode: ComponentNode?
        get() = selectedNodeId?.let { rootNode?.findNode(it) }
}
