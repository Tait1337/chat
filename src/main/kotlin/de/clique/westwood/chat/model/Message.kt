package de.clique.westwood.chat.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import java.util.*

data class Message @JsonCreator constructor(
    @JsonProperty("type") val type: String,
    @JsonProperty("timestamp") var timestamp: Date,
    @JsonProperty("sender") var sender: String,
    @JsonProperty("payload") val payload: String
) {
    companion object {
        fun toJson(msg: Message): String = jacksonObjectMapper().writeValueAsString(msg)
        fun fromJson(json: String): Message = jacksonObjectMapper().readValue(json, jacksonTypeRef<Message>())
    }
}