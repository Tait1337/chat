package de.clique.westwood.chat.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import java.util.*

data class ChatMessage @JsonCreator constructor(
    @JsonProperty("receiver") var receiver: String,
    @JsonProperty("text") var text: String
)
{
    companion object {
        const val TYPE = "CHAT"
        fun toJson(msg: ChatMessage): String = jacksonObjectMapper().writeValueAsString(msg)
        fun fromJson(json: String): ChatMessage = jacksonObjectMapper().readValue(json, jacksonTypeRef<ChatMessage>())
    }
}
