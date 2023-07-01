package com.tvkdevelopment.titanirc.bridge

interface BridgeClient {

    fun addRelayMessageListener(listener: MessageListener)

    fun relayMessage(channel: String, nick: String, message: String)

    fun interface MessageListener {
        fun onMessage(channel: String, nick: String, message: String)
    }
}