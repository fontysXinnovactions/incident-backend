package com.innovactions.incident.application.listner;

import com.innovactions.incident.domain.event.IncidentClosedEvent;
import com.innovactions.incident.port.outbound.IncidentReporterNotifierPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
/**
 * Application-layer listener that handles {@link IncidentClosedEvent} notifications.
 * <p>
 * When an incident is closed, this listener asynchronously identifies the appropriate
 * reporter notifier based on the originating platform (e.g., Slack, WhatsApp)
 * and sends a closure notification to the reporter.
 * <p>
 * Each platform-specific implementation of {@link IncidentReporterNotifierPort}
 * is injected and filtered dynamically at runtime.
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class IncidentClosedEventListener {

    private final List<IncidentReporterNotifierPort> notifierPorts;

    /**
     * Use case: Handles the incident closure notification for the corresponding platform.
     * Listens to {@link IncidentClosedEvent} and notifies the reporter
     * <p>
     * The distribution of the closure notification is determined by the platform
     * This method runs asynchronously to avoid blocking the main incident workflow.
     * @param event the domain event published when an incident is closed
     */
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
