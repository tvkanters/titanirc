package com.tvkdevelopment.titanirc

object Platform {

    val isWindows = System.getProperty("os.name").lowercase().contains("win")

    val isUnitTest = Thread.currentThread().stackTrace.any { it.className.startsWith("org.junit.") }
}
