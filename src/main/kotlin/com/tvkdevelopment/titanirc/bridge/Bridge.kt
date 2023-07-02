package com.tvkdevelopment.titanirc.bridge

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformationMapping
import com.tvkdevelopment.titanirc.util.Log

class Bridge private constructor(
    private val channelMapping: ChannelMapping,
    private val messageTransformationMapping: MessageTransformationMapping,
) {

    private fun connect() {
        channelMapping.clients.forEach { sourceClient ->
            val targetClients = channelMapping.clients - sourceClient

            sourceClient.addRelayMessageListener { sourceChannel, nick, message ->
                targetClients.forEach { targetClient ->
                    relayMessage(sourceClient, targetClient, sourceChannel, nick, message)
                }
            }

            sourceClient.addTopicListener { sourceChannel, topic ->
                Log.i("Topic update: client=${sourceClient.name}, channel=$sourceChannel, topic=$topic")
                targetClients.forEach { targetClient ->
                    setTopic(sourceClient, targetClient, sourceChannel, topic)
                }
            }
        }
    }

    private fun relayMessage(
        sourceClient: BridgeClient,
        targetClient: BridgeClient,
        sourceChannel: String,
        nick: String,
        message: String,
    ) {
        val targetChannel = channelMapping.getTargetChannel(sourceClient, targetClient, sourceChannel) ?: return
        val transformedMessage = messageTransformationMapping.transform(sourceClient, targetClient, message)

        targetClient.relayMessage(targetChannel, nick, transformedMessage)
    }

    private fun setTopic(
        sourceClient: BridgeClient,
        targetClient: BridgeClient,
        sourceChannel: String,
        topic: String
    ) {
        val targetChannel = channelMapping.getTargetChannel(sourceClient, targetClient, sourceChannel) ?: return

        targetClient.setTopic(targetChannel, topic)
    }

    companion object {
        fun connect(
            channelMapping: ChannelMapping,
            transformationMapping: MessageTransformationMapping = MessageTransformationMapping(),
        ) = Bridge(channelMapping, transformationMapping)
            .apply { connect() }
    }
}
