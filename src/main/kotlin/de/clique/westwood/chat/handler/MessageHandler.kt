package de.clique.westwood.chat.handler

import de.clique.westwood.chat.model.ChatMessage
import de.clique.westwood.chat.model.Message
import de.clique.westwood.chat.model.ParticipantMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.util.*

@Component
class MessageHandler(@Value("\${chat.history}") val chatHistory: Int) : WebSocketHandler {

    companion object {
        const val PUBLIC_CHANNEL_NAME = "All"
        const val PUBLIC_CHANNEL_KEY = "*"
    }

    private val participants = hashMapOf(PUBLIC_CHANNEL_KEY to PUBLIC_CHANNEL_NAME)
    private val receivedMessages = Sinks.many().replay().limit<Message>(chatHistory)
    private val outgoingMessages: Flux<Message> = receivedMessages.asFlux()

    override fun handle(session: WebSocketSession): Mono<Void> {
        session.receive()
            .map {
                Message.fromJson(it.payloadAsText).apply {
                    timestamp = Date()
                    sender = session.id
                }
            }
            .doOnNext {
                when (it.type) {
                    ChatMessage.TYPE -> handleChatMessage(it)
                    ParticipantMessage.TYPE -> handleParticipantMessage(it)
                }
            }
            .subscribe()
        return session.send(
            outgoingMessages
                .filter {
                    var deliveryAllowed = true
                    if (it.type == ChatMessage.TYPE) {
                        val chatMessage = ChatMessage.fromJson(it.payload)
                        deliveryAllowed = chatMessage.receiver == PUBLIC_CHANNEL_KEY || chatMessage.receiver == session.id || it.sender == session.id
                    }
                    deliveryAllowed
                }
                .map { session.textMessage(Message.toJson(it)) }
        )
    }

    private fun handleChatMessage(message: Message) {
        val chatMessage = ChatMessage.fromJson(message.payload)
        println(chatMessage)

        receivedMessages.tryEmitNext(message)
    }

    private fun handleParticipantMessage(message: Message) {
        val memberMessage = ParticipantMessage.fromJson(message.payload)
        println(memberMessage)

        when (memberMessage.action) {
            ParticipantMessage.Action.JOIN -> participants[message.sender] = memberMessage.user.values.first()
            ParticipantMessage.Action.LEAVE -> participants.remove(message.sender)
        }

        val memberUpdateMessage = ParticipantMessage(memberMessage.action, participants.toSortedMap())
        val updateMessage = Message(message.type, message.timestamp, message.sender, ParticipantMessage.toJson(memberUpdateMessage))
        receivedMessages.tryEmitNext(updateMessage)
    }

}