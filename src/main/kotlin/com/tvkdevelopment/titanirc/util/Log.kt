package com.tvkdevelopment.titanirc.util

import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import java.util.logging.*


object Log {
    private const val FILE_SIZE_B = 5 * 1024 * 1024

    private val simpleFormatter = object : SimpleFormatter() {
        override fun format(logRecord: LogRecord) =
            String.format(
                "[%1\$tF %1\$tT] [%2\$-7s] %3\$s%n",
                Date(logRecord.millis),
                logRecord.level.localizedName,
                logRecord.message
            )
    }

    private val logger = Logger.getLogger("main").apply {
        level = Level.ALL
        useParentHandlers = false
        addHandler(ConsoleHandler().apply {
            formatter = simpleFormatter
        })
        addHandler(FileHandler("titanirc.log", FILE_SIZE_B, 1, true).apply {
            formatter = simpleFormatter
        })
    }

    fun i(message: Any?) {
        logger.info(message.toString())
    }

    fun w(message: Any?) {
        logger.warning(message.toString())
    }

    fun e(message: Any?) {
        logger.severe(message.toString())
    }

    fun e(e: Exception) {
        val exceptionAsString = StringWriter().let {
            e.printStackTrace(PrintWriter(it))
            it.toString()
        }
        e(exceptionAsString)
    }

    fun e(message: Any?, e: Exception) {
        val exceptionAsString = StringWriter().let {
            e.printStackTrace(PrintWriter(it))
            it.toString()
        }
        e("$message\n$exceptionAsString")
    }
}