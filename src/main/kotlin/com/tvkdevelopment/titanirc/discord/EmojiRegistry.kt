package com.tvkdevelopment.titanirc.discord

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.GuildEmoji

interface EmojiRegistry {
    val emojisById: Map<Snowflake, EmojiInfo>
    val emojisByNormalizedName: Map<String, Snowflake>
}

class MutableEmojiRegistry : EmojiRegistry {
    private val index = MutableIndexedRegistry<Snowflake, EmojiInfo, String> { it.normalizedName }

    override val emojisByNormalizedName
        get() = index.itemsByNormalizedValue

    override val emojisById: Map<Snowflake, EmojiInfo> = index.itemsByKey

    operator fun plusAssign(emoji: GuildEmoji) {
        emoji.name?.let { name ->
            index[emoji.id] = EmojiInfo(name)
        }
    }
}

data class EmojiInfo(val name: String) {
    val normalizedName: String? =
        name.lowercase()
            .let { REGEX_NORMALIZE_NAME.matchAt(it, 0)?.groupValues?.get(1) }
            ?.takeIf { it.isNotEmpty() }

    companion object {
        private val REGEX_NORMALIZE_NAME = Regex("^([a-z0-9_-]+)")
    }
}
