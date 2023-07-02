package com.tvkdevelopment.titanirc.bridge

interface BridgeClient {

    val name: String

    fun relayMessage(channel: String, nick: String, message: String)

    fun addRelayMessageListener(listener: MessageListener)

    fun interface MessageListener {
        fun onMessage(channel: String, nick: String, message: String)
    }

    fun setTopic(channel: String, topic: String)

    fun addTopicListener(listener: TopicListener)

    fun interface TopicListener {
        fun onTopicChanged(channel: String, topic: String)
    }
}