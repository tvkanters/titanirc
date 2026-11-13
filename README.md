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

To relay URL-only messages from known IRC bots to Discord without the `**<nick>**` prefix,
add their IRC login/ident values to your private configuration implementation:

```kotlin
override val ircUrlOnlyBotUsernames = setOf("botident")
```

Alternatively, use `ircUrlOnlyBotNicks` to match nicknames. Both options are case-insensitive;
all other messages retain their normal nickname prefix. The username here is the IRC
login/ident (as seen in `nick!ident@host`), **not** the authenticated IRC services account.
Use the exact ident from the hostmask, including a leading `~` if present.
