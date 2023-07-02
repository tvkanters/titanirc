package com.tvkdevelopment.titanirc.irc

fun String.splitMessageForIrc(lineBasicCharMax: Int, prefix: String? = null): String =
    split('\n')
        .filter { it.isNotBlank() }
        .joinToString("\n") { it.splitLineForIrc(lineBasicCharMax, prefix) }

private fun String.splitLineForIrc(lineBasicCharMax: Int, prefix: String?): String {
    val messageChars = this.toCharArray()
    val charSize = if (messageChars.any { it.code >= 128 }) 2 else 1
    val prefixLength = (prefix?.length ?: 0) * charSize
    val wordBasicCharMax = lineBasicCharMax - prefixLength * charSize

    val splitMessageBuilder = StringBuilder()
    val wordBuilder = StringBuilder()

    var lineBasicCharCount = 0
    var wordBasicCharCount = 0
    var wordAddedToLine = false

    fun prepareNewLine() {
        prefix?.let { splitMessageBuilder.append(it) }
        lineBasicCharCount = prefixLength
        wordAddedToLine = false
    }
    prepareNewLine()

    this.toCharArray().forEach {
        when (it) {
            // On word splits, try to add the buffered word to the line
            ' ' -> {
                lineBasicCharCount += wordBasicCharCount

                if (wordAddedToLine) {
                    // If the buffered word (plus space) doesn't fit on the line, add it to the next
                    if (lineBasicCharCount + 1 > lineBasicCharMax) {
                        splitMessageBuilder.append("\n")
                        prepareNewLine()
                        lineBasicCharCount += wordBasicCharCount
                    } else {
                        splitMessageBuilder.append(it)
                        lineBasicCharCount += 1

                    }
                }
                splitMessageBuilder.append(wordBuilder)
                wordAddedToLine = true

                wordBasicCharCount = 0

                wordBuilder.clear()
            }

            else -> {
                // Add the character, counting extended ASCII chars (such as Cyrillic) as double
                wordBasicCharCount += charSize
                if (wordBasicCharCount <= wordBasicCharMax) {
                    wordBuilder.append(it)
                } else {
                    if (wordBuilder.last() != '…') {
                        // Ellipsis is unicode and counts as 4 chars
                        wordBuilder.delete(wordBuilder.length - 3, wordBuilder.length)
                        wordBuilder.append('…')
                    }
                    wordBasicCharCount = wordBasicCharMax
                }
            }
        }
    }

    // Add the final word
    lineBasicCharCount += wordBasicCharCount
    if (lineBasicCharCount > lineBasicCharMax) {
        splitMessageBuilder.append('\n')
        prepareNewLine()
    } else if (splitMessageBuilder.length > prefixLength) {
        splitMessageBuilder.append(' ')
    }

    splitMessageBuilder.append(wordBuilder)

    return splitMessageBuilder.toString()
}