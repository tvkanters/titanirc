package com.tvkdevelopment.titanirc

import com.tvkdevelopment.titanirc.bridge.Bridge
import com.tvkdevelopment.titanirc.bridge.ChannelLink
import com.tvkdevelopment.titanirc.bridge.ChannelMapping
import com.tvkdevelopment.titanirc.discord.Discord
import com.tvkdevelopment.titanirc.irc.Irc
import org.apache.log4j.BasicConfigurator
import org.slf4j.simple.SimpleLogger


object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        val configuration = TitanircConfigurationPrivate

        if (configuration.isDevEnv) {
            System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "ERROR")
            BasicConfigurator.configure()
        }

        val irc = Irc.connect(configuration)
        val discord = Discord.startBot(configuration)

        Bridge.connect(
            irc,
            discord,
            ChannelMapping(
                ChannelLink("#dopefish_lives", "418911279625797652"),
                ChannelLink("#freamonsmind", "381040418436939777"),
                ChannelLink("#dopefish_gdq", "399598970860732416"),
                ChannelLink("#titanirc", "1089194862915637412"),
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