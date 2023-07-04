package com.tvkdevelopment.titanirc.bridge.transformation

fun interface MessageTransformation {
    fun transform(sourceChannel: String, targetChannel: String, message: String): String
}