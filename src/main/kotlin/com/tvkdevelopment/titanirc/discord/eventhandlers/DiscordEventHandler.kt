package com.tvkdevelopment.titanirc.discord.eventhandlers

import dev.kord.core.Kord

interface DiscordEventHandler {

    fun Kord.register()
}