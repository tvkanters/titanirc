package com.tvkdevelopment.titanirc.util

typealias IndexedWordPair = Pair<IndexedWords, IndexedWords>

private val WORDS_SEPARATOR = Regex("""[^a-z0-9]+""", RegexOption.IGNORE_CASE)

class IndexedWords(val index: Int, val words: List<String>) {
    constructor(words: List<String>) : this(0, words)

    val length = words.size

    fun isEmpty() = words.isEmpty()

    operator fun get(i: Int) = words[i]

    fun drop(n: Int): IndexedWords =
        IndexedWords(index + n, words.drop(n))

    override fun hashCode() = words.hashCode()
    override fun equals(other: Any?) = words == other
    override fun toString() = "[$index, ${words.joinToString(" ")}]"
}

fun calculateWordCorrection(original: String, edited: String): String? =
    calculateWordCorrection(
        original.split(WORDS_SEPARATOR).filter { it.isNotEmpty() },
        edited.split(WORDS_SEPARATOR).filter { it.isNotEmpty() },
    )

private fun calculateWordCorrection(originalWords: List<String>, editedWords: List<String>): String? =
    calculateWordCorrection(IndexedWords(originalWords), IndexedWords(editedWords), hashMapOf())
        .second
        .takeIf { it.words.isNotEmpty() }
        ?.let { corrections ->
            val joinedCorrections = mutableListOf<String>()
            val bundledCorrections = mutableListOf<String>()
            fun processBundledCorrections() {
                if (bundledCorrections.isNotEmpty()) {
                    joinedCorrections += bundledCorrections.joinToString(" ")
                    bundledCorrections.clear()
                }
            }

            var indexEdited = corrections.index
            var indexCorrections = 0

            while (indexEdited < editedWords.size && indexCorrections < corrections.words.size) {
                val editedWord = editedWords[indexEdited]
                val correctionWord = corrections.words[indexCorrections]

                if (editedWord == correctionWord) {
                    bundledCorrections += correctionWord
                    ++indexCorrections
                } else {
                    processBundledCorrections()
                }
                ++indexEdited
            }
            processBundledCorrections()

            joinedCorrections
                .distinct()
                .joinToString(" ") { "$it*" }
        }

private fun calculateWordCorrection(
    a: IndexedWords,
    b: IndexedWords,
    lookup: MutableMap<Long, IndexedWordPair>
): IndexedWordPair {
    val key = a.length.toLong() shl 32 or b.length.toLong()
    return lookup.getOrPut(key) {
        when {
            a.isEmpty() || b.isEmpty() ->
                IndexedWordPair(a, b)

            a[0] == b[0] ->
                calculateWordCorrection(a.drop(1), b.drop(1), lookup)

            else -> {
                val aa = calculateWordCorrection(a.drop(1), b, lookup)
                val bb = calculateWordCorrection(a, b.drop(1), lookup)
                if (aa.first.length + aa.second.length < bb.first.length + bb.second.length) {
                    IndexedWordPair(
                        IndexedWords(a.index, listOf(a[0]) + aa.first.words),
                        aa.second
                    )
                } else {
                    IndexedWordPair(
                        bb.first,
                        IndexedWords(b.index, listOf(b[0]) + bb.second.words)
                    )
                }
            }
        }
    }
}
