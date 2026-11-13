# titanirc
A bridge between Dopelives' channels on Discord and IRC.

This is an alternative to https://github.com/i-h/discord-irc, written in Kotlin and tailored to Dopelives' needs.

## Getting started
* Open the Gradle project in IntelliJ.
* Create `/deploy.gradle`. (Can be left empty.)
* Sync Gradle.
* Create `/src/main/kotlin/com/tvkdevelopment/titanirc/TitanircConfigurationPrivate.kt` with contents:
  ```kotlin
  object TitanircConfigurationPrivate : TitanircConfiguration {
      // Implement interface here
  }
  ```
* Run `Main`.