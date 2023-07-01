package com.tvkdevelopment.titanirc.bridge

class Bridge(
    private val channelMapping: ChannelMapping,
) {

    private fun connect() {
        channelMapping.clients.forEach { sourceClient ->
            val targetClients = channelMapping.clients - sourceClient
            sourceClient.addRelayMessageListener { sourceChannel, nick, message ->
                targetClients.forEach { targetClient ->
                    channelMapping.getTargetChannel(sourceClient, targetClient, sourceChannel)
                        ?.let { targetChannel ->
                            targetClient.relayMessage(targetChannel, nick, message)
                        }
                }
            }
        }
    }

    companion object {
        fun connect(
            channelMapping: ChannelMapping,
        ) = Bridge(channelMapping)
            .apply { connect() }
    }
}

class ChannelMapping(vararg links: ChannelLink) {

    val clients: Set<BridgeClient> = links.flatMap { link -> link.channels.map { it.client } }.toSet()

    private val clientMapping: Map<BridgeClient, Map<BridgeClient, Map<String, String>>> =
        mutableMapOf<BridgeClient, MutableMap<BridgeClient, MutableMap<String, String>>>().apply {
            links.forEach { link ->
                link.channels.forEach { channel ->
                    link.channels
                        .minus(channel)
                        .forEach { otherChannel ->
                            getOrPut(channel.client) { mutableMapOf() }
                                .getOrPut(otherChannel.client) { mutableMapOf() }
                                .put(channel.id, otherChannel.id)
                        }
                }
            }
        }

    fun getTargetChannel(sourceClient: BridgeClient, targetClient: BridgeClient, sourceChannel: String): String? =
        clientMapping[sourceClient]?.get(targetClient)?.get(sourceChannel)
}

data class ChannelLink(val channels: Set<Channel>) {
    constructor(vararg channels: Pair<BridgeClient, String>) :
        this(channels.map { Channel(it.first, it.second) }.toSet())
}

data class Channel(val client: BridgeClient, val id: String)




