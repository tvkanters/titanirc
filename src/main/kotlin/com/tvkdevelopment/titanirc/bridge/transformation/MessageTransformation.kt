package com.tvkdevelopment.titanirc.bridge.transformation

fun interface MessageTransformation {
    fun transform(channel: String, message: String): String
}