package com.tvkdevelopment.titanirc

import com.tvkdevelopment.titanirc.bridge.Bridge
import com.tvkdevelopment.titanirc.bridge.ChannelLink
import com.tvkdevelopment.titanirc.bridge.ChannelMapping
import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformationLink
import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformationMapping
import com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations.*
import com.tvkdevelopment.titanirc.discord.Discord
import com.tvkdevelopment.titanirc.irc.Irc
import com.tvkdevelopment.titanirc.util.Log
import org.apache.log4j.BasicConfigurator
import org.slf4j.simple.SimpleLogger


object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        val configuration = TitanircConfigurationPrivate

        if (configuration.isDevEnv) {
            System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "VERBOSE")
            BasicConfigurator.configure()
        }

        Log.i("Starting titanirc")

        val irc = Irc(configuration)
        val discord = Discord(configuration)

        Bridge.connect(
            ChannelMapping(
                setOf(
                    ChannelLink(discord to "418911279625797652", irc to "#dopefish_lives"),
                    ChannelLink(discord to "381040418436939777", irc to "#freamonsmind"),
                    ChannelLink(discord to "399598970860732416", irc to "#dopefish_gdq"),
                    ChannelLink(discord to "1089194862915637412", irc to "#titanirc"),
                )
                    .filter { link ->
                        link.channels.none { it.client == irc && !configuration.ircChannels.contains(it.id) }
                    }
            ),
            MessageTransformationMapping(
                MessageTransformationLink(
                    irc to discord,
                    listOf(
                        FormattingDiscordEscapeMessageTransformation(),
                        FormattingIrcToDiscordMessageTransformation(),
                        FormattingIrcStripMessageTransformation(),
                        SmolFiStripMessageTransformation(),
                        VxMessageTransformation(),
                        // Tracking is not stripped because URLs don't work without it anymore.
                        //DiscordUrlTrackingStripMessageTransformation(),
                        SnowflakeEncodeMemberMessageTransformation(discord.snowflakeRegistry),
                        SnowflakeEncodeChannelMessageTransformation(discord.snowflakeRegistry),
                        SnowflakeEncodeEmojiMessageTransformation(discord.snowflakeRegistry),
                    )
                ),
                MessageTransformationLink(
                    discord to irc,
                    listOf(
                        FormattingDiscordToIrcMessageTransformation(),
                        SmolFiPrependMessageTransformation(),
                        VxStripMessageTransformation(),
                        // Tracking is not stripped because URLs don't work without it anymore.
                        //DiscordUrlTrackingStripMessageTransformation(),
                        SnowflakeDecodeMessageTransformation(discord.snowflakeRegistry),
                    )
                )
            ),
            topicTransformationMapping = MessageTransformationMapping(
                MessageTransformationLink(
                    irc to discord,
                    listOf(
                        FormattingDiscordEscapeMessageTransformation(),
                        FormattingIrcToDiscordMessageTransformation(),
                        FormattingIrcStripMessageTransformation(),
                    )
                ),
                MessageTransformationLink(
                    discord to irc,
                    listOf(
                        FormattingDiscordToIrcMessageTransformation(),
                        SnowflakeDecodeMessageTransformation(discord.snowflakeRegistry),
                    )
                )
            ),
        )

        stayAlive()
    }

    private fun stayAlive() {
        try {
            while (true) {
                Thread.sleep(10000)
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}