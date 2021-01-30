package de.clique.westwood.chat.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef

data class ParticipantMessage @JsonCreator constructor(
    @JsonProperty("action") val action: Action,
    @JsonProperty("user") var user: Map<String,String>,
)
{
    companion object {
        const val TYPE = "PARTICIPANT"
        fun toJson(msg: ParticipantMessage): String = jacksonObjectMapper().writeValueAsString(msg)
        fun fromJson(json: String): ParticipantMessage = jacksonObjectMapper().readValue(json, jacksonTypeRef<ParticipantMessage>())
    }

    enum class Action {JOIN, LEAVE}
}