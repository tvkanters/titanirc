package com.tvkdevelopment.titanirc.discord

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Entity

interface ItemIndexRegistry<Id, Name, NormalizedName> {
    val itemsById: Map<Id, Name>
    val itemsByNormalizedName: Map<NormalizedName, Id>
}

class MutableEntityItemRegistry<Item : Entity>(
    private val getName: (Item) -> String?,
    private val getAdditionalNames: (Item) -> Set<String>,
) : ItemIndexRegistry<Snowflake, ItemInfo, String> {

    constructor(getName: (Item) -> String?) : this(getName, { emptySet() })

    private val mutableItemsById = mutableMapOf<Snowflake, ItemInfo>()
    override val itemsById = mutableItemsById

    override val itemsByNormalizedName = mutableMapOf<String, Snowflake>()
        get() {
            if (itemsByNormalizedValueInvalidated) {
                mutableItemsById.forEach { (key, value) ->
                    value.normalizedNames.forEach { field[it] = key }
                }
                itemsByNormalizedValueInvalidated = false
            }
            return field
        }

    private var itemsByNormalizedValueInvalidated = false

    operator fun plusAssign(item: Item) {
        getName(item)?.let { mutableItemsById[item.id] = ItemInfo(it, getAdditionalNames(item)) }
        itemsByNormalizedValueInvalidated = true
    }
}

data class ItemInfo(val originalName: String, val additionalNames: Set<String>) {
    val normalizedNames: Set<String> =
        (additionalNames + originalName)
            .mapNotNull { name ->
                name.lowercase()
                    .let { lowercaseName ->
                        REGEX_NORMALIZE_NAME
                            .matchAt(lowercaseName, 0)
                            ?.groupValues
                            ?.get(1)
                            ?.replace(REGEX_REMOVE_CHARS, "")
                    }
                    ?.takeIf { it.isNotEmpty() }
            }
            .toSet()

    companion object {
        private val REGEX_NORMALIZE_NAME = Regex("^([a-z0-9 _-]+)")
        private val REGEX_REMOVE_CHARS = Regex(" ")
    }
}