package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation
import com.tvkdevelopment.titanirc.discord.MemberRegistry
import dev.kord.common.entity.Snowflake

class DiscordMemberToNicknameMessageTransformation(private val memberRegistry: MemberRegistry) : MessageTransformation {

    override fun transform(sourceChannel: String, targetChannel: String, message: String): String {
        val members = memberRegistry.getMembersById(sourceChannel)
        return message.replace(REGEX) { match ->
            match.groupValues[1]
                .toLongOrNull()
                ?.let { members[Snowflake(it)] }
                ?.effectiveName
                ?: FALLBACK_NAME
        }
    }

    companion object {
        private val REGEX = Regex("""<@(\d+)>""", RegexOption.IGNORE_CASE)
        private const val FALLBACK_NAME = "???"
    }
}
