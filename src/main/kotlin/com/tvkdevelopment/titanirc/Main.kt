package com.tvkdevelopment.titanirc

import com.tvkdevelopment.titanirc.bridge.Bridge
import com.tvkdevelopment.titanirc.bridge.ChannelLink
import com.tvkdevelopment.titanirc.bridge.ChannelMapping
import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformationLink
import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformationMapping
import com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations.*
import com.tvkdevelopment.titanirc.discord.Discord
import com.tvkdevelopment.titanirc.discord.TopicRoles
import com.tvkdevelopment.titanirc.irc.Irc
import com.tvkdevelopment.titanirc.util.TopicUtil
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

        val irc = Irc(configuration)
        val discord = Discord(
            configuration,
            nick = "\uD83D\uDCAC",
            TopicRoles(
                "418911279625797652" to { topic -> "806471250406670367".takeIf { TopicUtil.getStreamInfo(topic) != null } },
            )
        )

        Bridge.connect(
            ChannelMapping(
                ChannelLink(discord to "418911279625797652", irc to "#dopefish_lives"),
                ChannelLink(discord to "381040418436939777", irc to "#freamonsmind"),
                ChannelLink(discord to "399598970860732416", irc to "#dopefish_gdq"),
                ChannelLink(discord to "1089194862915637412", irc to "#titanirc"),
            ),
            MessageTransformationMapping(
                MessageTransformationLink(
                    irc to discord,
                    listOf(
                        EscapeDiscordFormattingMessageTransformation(),
                        IrcFormattingToDiscordMessageTransformation(),
                        StripIrcFormattingMessageTransformation(),
                        StripSmolFiMessageTransformation(),
                        AddTwitterFixMessageTransformation(),
                        NicknameToDiscordMemberMessageTransformation(discord.memberRegistry),
                    )
                ),
                MessageTransformationLink(
                    discord to irc,
                    listOf(
                        DiscordFormattingToIrcMessageTransformation(),
                        AddSmolFiMessageTransformation(),
                        DiscordMemberToNicknameMessageTransformation(discord.memberRegistry),
                    )
                )
            ),
            topicTransformationMapping = MessageTransformationMapping(
                MessageTransformationLink(
                    irc to discord,
                    listOf(
                        EscapeDiscordFormattingMessageTransformation(),
                        IrcFormattingToDiscordMessageTransformation(),
                        StripIrcFormattingMessageTransformation(),
                    )
                ),
                MessageTransformationLink(
                    discord to irc,
                    listOf(
                        DiscordFormattingToIrcMessageTransformation(),
                        DiscordMemberToNicknameMessageTransformation(discord.memberRegistry),
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