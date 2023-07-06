package com.tvkdevelopment.titanirc.discord

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Entity

interface ItemIndexRegistry<Id, Name, NormalizedName> {
    val itemsById: Map<Id, Name>
    val itemsByNormalizedName: Map<NormalizedName, Id>
}

class MutableEntityItemRegistry<Item : Entity>(private val getName: (Item) -> String?) :
    ItemIndexRegistry<Snowflake, ItemInfo, String> {

    private val mutableItemsById = mutableMapOf<Snowflake, ItemInfo>()
    override val itemsById = mutableItemsById

    override val itemsByNormalizedName = mutableMapOf<String, Snowflake>()
        get() {
            if (itemsByNormalizedValueInvalidated) {
                mutableItemsById.forEach { (key, value) ->
                    value.normalizedName?.let { field[it] = key }
                }
                itemsByNormalizedValueInvalidated = false
            }
            return field
        }

    private var itemsByNormalizedValueInvalidated = false

    operator fun plusAssign(item: Item) {
        getName(item)?.let { mutableItemsById[item.id] = ItemInfo(it) }
        itemsByNormalizedValueInvalidated = true
    }
}

data class ItemInfo(val originalName: String) {
    val normalizedName: String? =
        originalName
            .lowercase()
            .let { REGEX_NORMALIZE_NAME.matchAt(it, 0)?.groupValues?.get(1) }
            ?.takeIf { it.isNotEmpty() }

    companion object {
        private val REGEX_NORMALIZE_NAME = Regex("^([a-z0-9_-]+)")
    }
}