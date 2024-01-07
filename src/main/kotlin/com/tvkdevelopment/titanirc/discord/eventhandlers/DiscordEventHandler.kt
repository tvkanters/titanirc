package com.tvkdevelopment.titanirc.discord.eventhandlers

import com.tvkdevelopment.titanirc.TitanircConfiguration
import com.tvkdevelopment.titanirc.util.Log
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.channel.Channel
import dev.kord.core.event.Event
import dev.kord.core.on

interface DiscordEventHandler {

    fun Registrar.register()

    class Registrar(val kord: Kord, val configuration: TitanircConfiguration) {
        inline fun <reified T : Event> on(
            crossinline getGuildId: T.() -> Snowflake?,
            noinline consumer: suspend T.() -> Unit,
        ) {
            kord.on<T>(
                consumer = {
                    val guildId = getGuildId()
                    if (guildId == null || configuration.discordGuilds.contains(guildId.toString())) {
                        try {
                            consumer()
                        } catch (e: Exception) {
                            Log.e("Error handling event ${T::class.simpleName}", e)
                        }
                    }
                }
            )
        }

        inline fun <reified T : Event> onChannel(
            crossinline getChannel: T.() -> Channel,
            noinline consumer: suspend T.() -> Unit,
        ) {
            on<T>({ getChannel().data.guildId.value }, consumer)
        }
    }
}

class DiscordEventHandlers(private vararg val handlers: DiscordEventHandler) {

    fun register(kord: Kord, configuration: TitanircConfiguration) {
        with(DiscordEventHandler.Registrar(kord, configuration)) {
            handlers.forEach {
                it.apply { register() }
            }
        }
    }
}