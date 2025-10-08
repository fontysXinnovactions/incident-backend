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

    public CreateIncidentCommand processIncoming(CreateIncidentCommand command){
        var context = conversationRepository.findActiveByUser(command.reporterId());
        if (context.isPresent()) {
            var ctx = context.get();
            boolean expired = command.timestamp()
                    .isAfter(context.get().lastMessageAt().plus(Duration.ofHours(24)));

            if (!expired) {
                // Update existing incident
                UpdateIncidentCommand cmd = new UpdateIncidentCommand(
                        context.get().channelId(),
                        command.text(),
                        command.timestamp()
                );

                conversationRepository.update(
                        ctx.userId(),
                        ctx.incidentId(),
                        ctx.channelId(),
                        command.timestamp()
                );
                return;
            }
        }

        conversationRepository.saveNew(command.reporterId(),"INCIDENT-" + command.timestamp(), channelId, command.timestamp());
        return command;
    }
}


