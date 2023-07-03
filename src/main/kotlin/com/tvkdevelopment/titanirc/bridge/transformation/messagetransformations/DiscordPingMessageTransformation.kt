package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation
import com.tvkdevelopment.titanirc.discord.MemberRegistry

class DiscordPingMessageTransformation(private val memberRegistry: MemberRegistry) : MessageTransformation {

    override fun transform(channel: String, message: String): String =
        message.replace(REGEX_POTENTIAL_MEMBER_NAME) { originalMatch ->
            originalMatch.takeIf { it.groupValues[1].length > SHORT_NAME_MAX_LENGTH || it.groupValues[2].isNotEmpty() }
                ?.let { match ->
                    memberRegistry.getMembers(channel)
                        .get(match.groupValues[1].lowercase())
                        ?.let { memberId -> "<@$memberId>${match.groupValues[2]}" }
                }
                ?: originalMatch.groupValues[0]
        }

    companion object {
        private const val SHORT_NAME_MAX_LENGTH = 3
        private val REGEX_POTENTIAL_MEMBER_NAME = Regex("^([a-z0-9_-]+)([:,]?)", RegexOption.IGNORE_CASE)
    }
}
