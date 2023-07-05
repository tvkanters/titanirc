package com.tvkdevelopment.titanirc.bridge

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformationMapping
import com.tvkdevelopment.titanirc.util.Log

class Bridge private constructor(
    private val channelMapping: ChannelMapping,
    private val messageTransformationMapping: MessageTransformationMapping,
    private val topicTransformationMapping: MessageTransformationMapping,
) {

    private fun connect() {
        channelMapping.clients.forEach { sourceClient ->
            val targetClients = channelMapping.clients - sourceClient

            sourceClient.addRelayMessageListener { sourceChannel, nick, message ->
                targetClients.forEach { targetClient ->
                    relayMessage(sourceClient, targetClient, sourceChannel, nick, message)
                }
            }

            sourceClient.addRelaySlashMeListener { sourceChannel, nick, message ->
                targetClients.forEach { targetClient ->
                    relaySlashMe(sourceClient, targetClient, sourceChannel, nick, message)
                }
            }

            sourceClient.addTopicListener { sourceChannel, topic ->
                Log.i("Bridge topic update: client=${sourceClient.name}, channel=$sourceChannel, topic=$topic")
                targetClients.forEach { targetClient ->
                    setTopic(sourceClient, targetClient, sourceChannel, topic)
                }
            }
        }

        channelMapping.clients.forEach {
            Log.i("Bridge connecting client: ${it.name}")
            it.connect()
        }
    }

    private fun relayMessage(
        sourceClient: BridgeClient,
        targetClient: BridgeClient,
        sourceChannel: String,
        nick: String,
        message: String,
    ) {
        try {
            val targetChannel = channelMapping.getTargetChannel(sourceClient, targetClient, sourceChannel) ?: return
            val transformedMessage =
                messageTransformationMapping.transform(
                    sourceClient,
                    targetClient,
                    sourceChannel,
                    targetChannel,
                    message,
                )

            targetClient.relayMessage(targetChannel, nick, transformedMessage)
        } catch (e: Exception) {
            Log.e(
                "Bridge exception in relayMessage: " +
                    "sourceClient=${sourceClient.name}, " +
                    "targetClient=${targetClient.name}, " +
                    "sourceChannel=$sourceChannel, " +
                    "nick=$nick, " +
                    "message=$message",
                e
            )
        }
    }

    private fun relaySlashMe(
        sourceClient: BridgeClient,
        targetClient: BridgeClient,
        sourceChannel: String,
        nick: String,
        message: String,
    ) {
        try {
            val targetChannel = channelMapping.getTargetChannel(sourceClient, targetClient, sourceChannel) ?: return
            val transformedMessage =
                messageTransformationMapping.transform(
                    sourceClient,
                    targetClient,
                    sourceChannel,
                    targetChannel,
                    message,
                )

            targetClient.relaySlashMe(targetChannel, nick, transformedMessage)
        } catch (e: Exception) {
            Log.e(
                "Bridge exception in relaySlashMe: " +
                    "sourceClient=${sourceClient.name}, " +
                    "targetClient=${targetClient.name}, " +
                    "sourceChannel=$sourceChannel, " +
                    "nick=$nick, " +
                    "message=$message",
                e
            )
        }
    }

    private fun setTopic(
        sourceClient: BridgeClient,
        targetClient: BridgeClient,
        sourceChannel: String,
        topic: String
    ) {
        try {
            val targetChannel = channelMapping.getTargetChannel(sourceClient, targetClient, sourceChannel) ?: return
            val transformedTopic =
                topicTransformationMapping.transform(sourceClient, targetClient, sourceChannel, targetChannel, topic)

            targetClient.setTopic(targetChannel, transformedTopic)
        } catch (e: Exception) {
            Log.e(
                "Bridge exception in relayMessage: " +
                    "sourceClient=${sourceClient.name}, " +
                    "targetClient=${targetClient.name}, " +
                    "sourceChannel=$sourceChannel, " +
                    "topic=$topic",
                e
            )
        }
    }

    companion object {
        fun connect(
            channelMapping: ChannelMapping,
            messageTransformationMapping: MessageTransformationMapping = MessageTransformationMapping(),
            topicTransformationMapping: MessageTransformationMapping = MessageTransformationMapping(),
        ) = Bridge(channelMapping, messageTransformationMapping, topicTransformationMapping)
            .apply { connect() }
    }
}
