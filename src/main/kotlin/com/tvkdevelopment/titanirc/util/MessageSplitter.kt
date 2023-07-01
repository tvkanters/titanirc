package com.tvkdevelopment.titanirc.util

fun String.splitMessageForIrc(lineBasicCharMax: Int, prefix: String? = null): String {
    val messageChars = this.toCharArray()
    val charSize = if (messageChars.any { it.code >= 128 }) 2 else 1

    val splitMessageBuilder = StringBuilder()
    val wordBuilder = StringBuilder()

    var lineBasicCharCount = 0
    var wordBasicCharCount = 0
    var wordAddedToLine = false
    var lastCharNewLine = false

    fun prepareNewLine() {
        prefix?.let { splitMessageBuilder.append(it) }
        lineBasicCharCount = prefix?.length ?: 0
        lastCharNewLine = true
        wordAddedToLine = false
    }
    prepareNewLine()

    this.toCharArray().forEach {
        // On word splits, try to add the buffered word to the line
        when (it) {
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

            '\n' -> {
                if (!lastCharNewLine) {
                    splitMessageBuilder.append(wordBuilder)
                    wordBasicCharCount = 0
                    wordBuilder.clear()

                    splitMessageBuilder.append(it)

                    prepareNewLine()
                }
            }

            else -> {
                // Add the character, counting extended ASCII chars (such as Cyrillic) as double
                wordBasicCharCount += charSize
                if (wordBasicCharCount < lineBasicCharMax) {
                    wordBuilder.append(it)
                }
                lastCharNewLine = false
            }
        }
    }

    // Add the final word
    lineBasicCharCount += wordBasicCharCount
    if (lineBasicCharCount > lineBasicCharMax) {
        splitMessageBuilder.append('\n')
        prepareNewLine()
    } else if (splitMessageBuilder.isNotEmpty()) {
        splitMessageBuilder.append(' ')
    }

    splitMessageBuilder.append(wordBuilder)

    return splitMessageBuilder.toString()
}