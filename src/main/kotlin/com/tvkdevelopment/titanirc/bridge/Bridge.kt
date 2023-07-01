package com.tvkdevelopment.titanirc.bridge

class Bridge(
    private val ircClient: BridgeClient,
    private val discordClient: BridgeClient,
    private val channelMapping: ChannelMapping,
) {

    private fun connect() {
        ircClient.addRelayMessageListener { channel, nick, message ->
            val targetChannel = channelMapping.getDiscordChannel(channel) ?: return@addRelayMessageListener
            discordClient.relayMessage(targetChannel, nick, message)
        }
        discordClient.addRelayMessageListener { channel, nick, message ->
            val targetChannel = channelMapping.getIrcChannel(channel) ?: return@addRelayMessageListener
            ircClient.relayMessage(targetChannel, nick, message)
        }
    }

    companion object {
        fun connect(
            ircClient: BridgeClient,
            discordClient: BridgeClient,
            channelMapping: ChannelMapping,
        ) = Bridge(ircClient, discordClient, channelMapping)
            .apply { connect() }
    }
}

class ChannelMapping(vararg channels: ChannelLink) {
    private val ircToDiscord = channels.associate { it.ircChannel to it.discordChannel }
    private val discordToIrc = channels.associate { it.discordChannel to it.ircChannel }

    fun getIrcChannel(discordChannelId: String) = discordToIrc[discordChannelId]
    fun getDiscordChannel(ircChannel: String) = ircToDiscord[ircChannel]
}

data class ChannelLink(val ircChannel: String, val discordChannel: String)


