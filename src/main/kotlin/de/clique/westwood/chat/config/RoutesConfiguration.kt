package de.clique.westwood.chat.config

import de.clique.westwood.chat.handler.MessageHandler
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping

@Component
class RoutesConfiguration {
    @Bean
    fun routes(messageHandler: MessageHandler) = SimpleUrlHandlerMapping(mapOf("/chat" to messageHandler), 1)
}