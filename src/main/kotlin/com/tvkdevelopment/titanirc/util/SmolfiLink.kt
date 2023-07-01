package com.tvkdevelopment.titanirc.util

object SmolfiLink {

    private val REGEX = Regex("""https?://smol\.fi/e/\?v=(https?://)?([^ ]+)""")

    fun findAllIn(input: String): List<String> =
        REGEX.findAll(input)
            .toList()
            .map { wrapResult(it.groupValues[1], it.groupValues[2]) }

    fun strip(input: String) =
        input.replace(REGEX, wrapResult("$1", "$2"))

    private fun wrapResult(protocol: String, url: String): String =
        protocol.ifEmpty { "https://" } + url
}