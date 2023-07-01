package com.tvkdevelopment.titanirc

interface TitanircConfiguration {
    val isDevEnv: Boolean

    val ircUsername: String
    val ircPassword: String?
    val ircNick: String

    val discordToken: String
}