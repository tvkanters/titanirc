package com.tvkdevelopment.titanirc.bridge.transformation

fun interface MessageTransformation {
    fun transform(message: String): String
}