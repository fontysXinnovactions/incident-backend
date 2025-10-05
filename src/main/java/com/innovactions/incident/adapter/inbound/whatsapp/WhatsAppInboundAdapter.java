package com.innovactions.incident.adapter.inbound.whatsapp;

import com.innovactions.incident.application.ConversationContextService;
import com.innovactions.incident.application.InboundMessage;
import com.innovactions.incident.port.inbound.WhatsAppMessageReceiverPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WhatsAppInboundAdapter implements WhatsAppMessageReceiverPort {

    private final ConversationContextService contextService;
        @Override
        public void handle(InboundMessage message) {
            contextService.processIncoming(message);
    }

    public void handle(WhatsAppPayload payload) {
        InboundMessage message = WhatsAppPayloadMapper.toInboundMessage(payload);
        handle(message);
    }

}



