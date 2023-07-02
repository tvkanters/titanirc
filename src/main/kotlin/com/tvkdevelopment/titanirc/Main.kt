package com.tvkdevelopment.titanirc

import com.tvkdevelopment.titanirc.bridge.Bridge
import com.tvkdevelopment.titanirc.bridge.ChannelLink
import com.tvkdevelopment.titanirc.bridge.ChannelMapping
import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformationLink
import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformationMapping
import com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations.AddSmolFiMessageTransformation
import com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations.AddTwitterFixMessageTransformation
import com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations.StripIrcFormattingMessageTransformation
import com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations.StripSmolFiMessageTransformation
import com.tvkdevelopment.titanirc.discord.Discord
import com.tvkdevelopment.titanirc.discord.TopicRoles
import com.tvkdevelopment.titanirc.irc.Irc
import org.apache.log4j.BasicConfigurator
import org.slf4j.simple.SimpleLogger


object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        val configuration = TitanircConfigurationPrivate

        if (configuration.isDevEnv) {
            System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO")
            BasicConfigurator.configure()
        }

        val irc = Irc.connect(configuration)
        val discord = Discord.startBot(
            configuration,
            TopicRoles("418911279625797652" to "806471250406670367")
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
                        StripIrcFormattingMessageTransformation(),
                        StripSmolFiMessageTransformation(),
                        AddTwitterFixMessageTransformation(),
                    )
                ),
                MessageTransformationLink(
                    discord to irc,
                    listOf(
                        AddSmolFiMessageTransformation(),
                    )
                )
            )
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