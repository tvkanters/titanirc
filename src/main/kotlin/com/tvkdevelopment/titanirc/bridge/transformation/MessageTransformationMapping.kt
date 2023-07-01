package com.tvkdevelopment.titanirc.bridge.transformation

import com.tvkdevelopment.titanirc.bridge.BridgeClient

class MessageTransformationMapping(vararg links: MessageTransformationLink) {

    private val transformations: Map<BridgeClient, Map<BridgeClient, List<MessageTransformation>>> =
        mutableMapOf<BridgeClient, MutableMap<BridgeClient, MutableList<MessageTransformation>>>().apply {
            links.forEach { link ->
                getOrPut(link.sourceClient) { mutableMapOf() }
                    .getOrPut(link.targetClient) { mutableListOf() }
                    .addAll(link.messageTransformations)
            }
        }

    fun transform(sourceClient: BridgeClient, targetClient: BridgeClient, message: String): String =
        transformations[sourceClient]?.get(targetClient)
            ?.fold(message) { transformedMessage, transformation -> transformation.transform(transformedMessage) }
            ?: message

}

data class MessageTransformationLink(
    val sourceClient: BridgeClient,
    val targetClient: BridgeClient,
    val messageTransformations: List<MessageTransformation>,
) {
    constructor(clients: Pair<BridgeClient, BridgeClient>, messageTransformations: List<MessageTransformation>) :
        this(clients.first, clients.second, messageTransformations)
}