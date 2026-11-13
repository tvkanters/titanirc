package com.tvkdevelopment.titanirc.bridge

interface BridgeClient {

    val name: String

    fun connect()

    fun addBridgeListener(listener: Listener)

    interface Listener {
        fun onMessage(channel: String, username: String, nick: String, message: String)
        fun onSlashMe(channel: String, username: String, nick: String, message: String)
        fun onTopicChanged(channel: String, topic: String)
    }

    fun relayMessage(channel: String, username: String, nick: String, message: String)

    fun relaySlashMe(channel: String, username: String, nick: String, message: String)

    fun setTopic(channel: String, topic: String)
}