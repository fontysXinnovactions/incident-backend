package com.innovactions.incident.application.listner;

import com.innovactions.incident.domain.event.IncidentClosedEvent;
import com.innovactions.incident.port.outbound.IncidentReporterNotifierPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class IncidentClosedEventListener {

    private final List<IncidentReporterNotifierPort> notifierPorts;

    @Async
    @EventListener
    public void onIncidentClosed(IncidentClosedEvent event) {
        log.info("Received IncidentClosedEvent: {}", event);
        notifierPorts.stream()
                .filter(n -> n.getPlatformName()
                        .equalsIgnoreCase(event.platform()))
                        .findFirst()
                .ifPresentOrElse( n -> {
                    log.info("Notifying reporter {} via {}", event.reporterId(), event.platform());
                    n.notifyReporter(event.reporterId(),
                            event.reason()); }, () -> log.warn("No notifier found for platform '{}'", event.platform())
                );
    }
}
