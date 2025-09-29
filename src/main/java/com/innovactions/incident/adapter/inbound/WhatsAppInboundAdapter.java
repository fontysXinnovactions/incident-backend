package com.innovactions.incident.adapter.inbound;

import com.innovactions.incident.application.command.CreateIncidentCommand;
import com.innovactions.incident.port.inbound.IncidentInboundPort;
import com.innovactions.incident.port.inbound.WhatsAppMessageReceiverPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WhatsAppInboundAdapter implements WhatsAppMessageReceiverPort {


    private final IncidentInboundPort incidentInboundPort;

    @Override
    public void handle(Map<String, Object> payload) {
        // Mapper builds a CreateIncidentCommand
        CreateIncidentCommand command = WhatsAppPayloadMapper.toIncidentCommand(payload);

        // Hand it off to the application layer
        incidentInboundPort.handle(command);
    }
}
