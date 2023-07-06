package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation
import com.tvkdevelopment.titanirc.discord.GuildSnowflakeRegistry
import com.tvkdevelopment.titanirc.discord.SnowflakeRegistry

class NicknameToDiscordMemberMessageTransformation(
    private val snowflakeRegistry: SnowflakeRegistry,
) : MessageTransformation {

    override fun transform(sourceChannel: String, targetChannel: String, message: String): String =
        snowflakeRegistry.forChannel(targetChannel)
            ?.run {
                message.replace(REGEX_POTENTIAL_NAME) { match ->
                    resolvePotentialName(match.groupValues[1], false)
                        ?: resolvePotentialName(match.groupValues[2], true)
                        ?: match.groupValues[0]
                }
            }
            ?: message

    private fun GuildSnowflakeRegistry.resolvePotentialName(potentialName: String, allowRoles: Boolean): String? =
        potentialName
            .takeIf { it.isNotEmpty() }
            ?.lowercase()
            ?.let { normalizedName ->
                memberRegistry.itemsByNormalizedName[normalizedName]?.let { "<@$it>" }
                    ?: roleRegistry.takeIf { allowRoles }?.itemsByNormalizedName?.get(normalizedName)?.let { "<@&$it>" }
            }

    companion object {
        private val REGEX_POTENTIAL_NAME =
            Regex("""^([a-z0-9_-]+)(?=[:,])|(?<=^| )@([a-z0-9_-]+)""", RegexOption.IGNORE_CASE)
    }
}
