package de.clique.westwood.chat

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.util.*

@Component
class Router(@Value("\${chat.history}") val chatHistory: Int) : WebSocketHandler {

    @Bean
    fun routes() = SimpleUrlHandlerMapping(mapOf("/chat" to this), 1)

    data class ChatMessage @JsonCreator constructor(
        @JsonProperty("timestamp") var timestamp: Date,
        @JsonProperty("sender") var sender: String,
        @JsonProperty("receiver") var receiver: String?,
        @JsonProperty("text") var text: String
    )

    private fun toJson(chatMessage: ChatMessage): String = jacksonObjectMapper().writeValueAsString(chatMessage)
    private fun fromJson(json: String): ChatMessage = jacksonObjectMapper().readValue(json, jacksonTypeRef<ChatMessage>())

    private val receivedMessages = Sinks.many().replay().limit<ChatMessage>(chatHistory)
    private val outgoingMessages: Flux<ChatMessage> = receivedMessages.asFlux()

    override fun handle(session: WebSocketSession): Mono<Void> {
        session.receive()
            .map { fromJson(it.payloadAsText) }
            .doOnNext { println(it) }
            .subscribe(
                { message: ChatMessage -> receivedMessages.tryEmitNext(message) },
                { error: Throwable -> receivedMessages.tryEmitError(error) }
            )
        return session.send(
            outgoingMessages
                //               .filter { it.receiver == session.id }
                // filter here to send the message only to a subset of consumers
                .map { session.textMessage(toJson(it)) }
        )
    }

}