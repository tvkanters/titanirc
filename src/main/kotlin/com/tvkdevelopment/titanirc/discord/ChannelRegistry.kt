package com.tvkdevelopment.titanirc.discord

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.channel.Channel

interface ChannelRegistry {
    val channelsById: Map<Snowflake, ChannelInfo>
    val channelsByNormalizedName: Map<String, Snowflake>
}

class MutableChannelRegistry : ChannelRegistry {
    private val index = MutableIndexedRegistry<Snowflake, ChannelInfo, String> { it.normalizedName }

    override val channelsByNormalizedName
        get() = index.itemsByNormalizedValue

    override val channelsById: Map<Snowflake, ChannelInfo> = index.itemsByKey

    operator fun plusAssign(channel: Channel) {
        channel.data.name.value?.let { channelName ->
            index[channel.id] = ChannelInfo(channelName)
        }
    }
}

data class ChannelInfo(val name: String) {
    val normalizedName: String? =
        name.lowercase()
            .let { REGEX_NORMALIZE_NAME.matchAt(it, 0)?.groupValues?.get(1) }
            ?.takeIf { it.isNotEmpty() }

    companion object {
        private val REGEX_NORMALIZE_NAME = Regex("^([a-z0-9_-]+)")
    }
}
