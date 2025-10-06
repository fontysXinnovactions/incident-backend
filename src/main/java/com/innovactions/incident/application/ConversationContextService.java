package com.innovactions.incident.application;

import com.innovactions.incident.application.command.CreateIncidentCommand;
import com.innovactions.incident.application.command.UpdateIncidentCommand;
import com.innovactions.incident.port.inbound.IncidentInboundPort;
import com.innovactions.incident.port.outbound.ConversationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Duration;


@Service
@RequiredArgsConstructor
public class ConversationContextService {
    private final IncidentInboundPort incidentInboundPort;
    private final ConversationRepositoryPort conversationRepository;

    public void processIncoming(InboundMessage message){
        var context = conversationRepository.findActiveByUser(message.from());
        if (context.isPresent()) {
            var ctx = context.get();
            boolean expired = message.timestamp()
                    .isAfter(context.get().lastMessageAt().plus(Duration.ofHours(24)));

            if (!expired) {
                // Update existing incident
                UpdateIncidentCommand cmd = new UpdateIncidentCommand(
                        context.get().channelId(),
                        message.text(),
                        message.timestamp()
                );
                incidentInboundPort.handle(cmd);

                conversationRepository.update(
                        ctx.userId(),
                        ctx.incidentId(),
                        ctx.channelId(),
                        message.timestamp()
                );
                return;
            }
        }

        // Create new incident (either no context or expired)
        CreateIncidentCommand cmd = new CreateIncidentCommand(
                message.from(),
                message.senderName(),
                message.text(),
                message.timestamp(),
                message.channel()//TODO: investigate what returns and handle it accordingly
        );
        // handle returns only the channelId
        String channelId = incidentInboundPort.handle(cmd);
        conversationRepository.saveNew(message.from(),"INCIDENT-" + System.currentTimeMillis(), channelId, message.timestamp());
    }
}


