package com.innovactions.incident.application;

import com.innovactions.incident.application.command.CreateIncidentCommand;
import com.innovactions.incident.application.command.UpdateIncidentCommand;
import com.innovactions.incident.port.inbound.IncidentInboundPort;
import com.innovactions.incident.port.outbound.ConversationRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationContextService {
//    private final IncidentInboundPort incidentInboundPort;
    private final ConversationRepositoryPort conversationRepository;

    public UpdateIncidentCommand isNewOrExpired(CreateIncidentCommand command) {
        var context = conversationRepository.findActiveByUser(command.reporterId());
        if (context.isPresent()) {
            var ctx = context.get();
            boolean expired = command.timestamp()
                    .isAfter(context.get().lastMessageAt().plus(Duration.ofHours(24)));

            // If it has not expired, we update it and send the new message to the end-user conversation
            if (!expired) {
                conversationRepository.update(
                        ctx.userId(),
                        ctx.incidentId(),
                        ctx.channelId(),
                        command.timestamp()
                );

                return UpdateIncidentCommand.builder()
                        .channelId(context.get().channelId())
                        .message(command.message())
                        .updatedAt(command.timestamp())
                        .build();
            }
        }

        //If it is new or expired, we return null and go through the new incident flow
        return null;
    }

    public void saveNewIncident(CreateIncidentCommand command, String channelId) {
        conversationRepository.saveNew(command.reporterId(),"INCIDENT-" + command.timestamp(), channelId, command.timestamp());
    }
    //FIXME: Refactor this is only to restore functionality
    public boolean hasActiveContext(CreateIncidentCommand command) {
        var context = conversationRepository.findActiveByUser(command.reporterId());
        if (context.isEmpty()) {
            log.debug("No active context found for {}", command.reporterId());
            return false;
        }

        var ctx = context.get();
        boolean expired = command.timestamp()
                .isAfter(ctx.lastMessageAt().plus(Duration.ofHours(24)));

        boolean active = !expired;
        log.debug("Active context for {} → {}", ctx.userId(), active);
        return active;
    }

}


