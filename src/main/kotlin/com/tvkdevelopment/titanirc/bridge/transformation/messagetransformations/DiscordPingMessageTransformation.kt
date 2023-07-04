package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation
import com.tvkdevelopment.titanirc.discord.MemberRegistry

class DiscordPingMessageTransformation(private val memberRegistry: MemberRegistry) : MessageTransformation {

    override fun transform(channel: String, message: String): String {
        val members = memberRegistry.getMembers(channel)
        return message.replace(REGEX_POTENTIAL_MEMBER_NAME) { match ->
            match.groupValues
                .drop(1)
                .firstOrNull { it.isNotEmpty() }
                ?.lowercase()
                ?.let { members[it] }
                ?.let { "<@$it>" }
                ?: match.groupValues[0]
        }
    }

    companion object {
        private val REGEX_POTENTIAL_MEMBER_NAME =
            Regex("""(?:^([a-z0-9_-]+)(?=[:,])|(?<=^| )@([a-z0-9_-]+))""", RegexOption.IGNORE_CASE)
    }
}
