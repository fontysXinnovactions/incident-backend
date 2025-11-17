package com.innovactions.incident.application;

import static org.mockito.Mockito.*;

import com.innovactions.incident.application.command.CloseIncidentCommand;
import com.innovactions.incident.application.command.CreateIncidentCommand;
import com.innovactions.incident.application.command.UpdateIncidentCommand;
import com.innovactions.incident.domain.model.Incident;
import com.innovactions.incident.domain.model.Platform;
import com.innovactions.incident.domain.model.Severity;
import com.innovactions.incident.domain.service.IncidentService;
import com.innovactions.incident.port.outbound.IncidentBroadcasterPort;
import com.innovactions.incident.port.outbound.IncidentClosurePort;
import com.innovactions.incident.port.outbound.SeverityClassifierPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class IncidentApplicationServiceTest {

  private IncidentService incidentService;
  private IncidentBroadcasterPort broadcaster;
  private SeverityClassifierPort classifier;
  private IncidentClosurePort closurePort;
  private ConversationContextService contextService;
  private IncidentApplicationService appService;

  @BeforeEach
  void setUp() {
    incidentService = mock(IncidentService.class);
    broadcaster = mock(IncidentBroadcasterPort.class);
    classifier = mock(SeverityClassifierPort.class);
    closurePort = mock(IncidentClosurePort.class);
    contextService = mock(ConversationContextService.class);

    appService =
        new IncidentApplicationService(
            incidentService, broadcaster, classifier, closurePort, contextService);
  }

  @Nested
  @DisplayName("reportIncident()")
  class ReportIncidentTests {

    @Test
    @DisplayName("should create and broadcast new incident when no active context")
    void shouldCreateAndBroadcastNewIncident() {
      // Given
      var command =
          CreateIncidentCommand.builder()
              .reporterId("user-1")
              .reporterName("Alice")
              .message("Database is down")
              .platform(Platform.SLACK)
              .build();

      when(contextService.hasActiveContext(command)).thenReturn(false);
      when(classifier.classify("Database is down")).thenReturn(Severity.MAJOR);

      var fakeIncident = mock(Incident.class);
      when(incidentService.createIncident(command, Severity.MAJOR)).thenReturn(fakeIncident);
      when(broadcaster.initSlackDeveloperWorkspace(fakeIncident, command.platform()))
          .thenReturn("channel-123");

      // When
      appService.reportIncident(command);

      // Then
      InOrder inOrder = inOrder(contextService, classifier, incidentService, broadcaster);
      inOrder.verify(contextService).hasActiveContext(command);
      inOrder.verify(classifier).classify("Database is down");
      inOrder.verify(incidentService).createIncident(command, Severity.MAJOR);
      inOrder.verify(broadcaster).initSlackDeveloperWorkspace(fakeIncident, command.platform());
      inOrder.verify(contextService).saveNewIncident(command, "channel-123");
      verifyNoMoreInteractions(contextService, classifier, incidentService, broadcaster);
    }

    @Test
    @DisplayName("should delegate to updateExistingIncident() when active context found")
    void shouldDelegateToUpdateExistingIncident() {
        // Given
        var command = mock(CreateIncidentCommand.class);
        when(command.reporterId()).thenReturn("U12345");
        when(contextService.hasActiveContext(command)).thenReturn(true);

        var updateCmd = mock(UpdateIncidentCommand.class);
        when(contextService.findValidUpdateContext(command)).thenReturn(updateCmd);
        when(updateCmd.channelId()).thenReturn("C12345");

        var updatedIncident = mock(Incident.class);
        when(incidentService.updateIncident(updateCmd, command)).thenReturn(updatedIncident);

        var spyService = spy(appService);

        // When
        spyService.reportIncident(command);

        // Then
        verify(spyService).updateExistingIncident(command);
        verify(incidentService).updateIncident(updateCmd, command);
        verify(broadcaster).updateIncidentToDeveloper(updatedIncident, "C12345");
        verifyNoInteractions(classifier);
    }
  }

  @Nested
  @DisplayName("closeIncident()")
  class CloseIncidentTests {

    @Test
    @DisplayName("should call closure port with correct parameters")
    void shouldCloseIncident() {
      // Given
      var cmd = new CloseIncidentCommand("dev-1", "channel-42", "Resolved manually");

      // When
      appService.closeIncident(cmd);

      // Then
      verify(closurePort).closeIncident("dev-1", "channel-42", "Resolved manually");
      verifyNoMoreInteractions(closurePort);
    }
  }

  @Nested
  @DisplayName("updateExistingIncident()")
  class UpdateIncidentTests {

    @Test
    @DisplayName("should start new flow if no valid update context found")
    void shouldStartNewFlowIfNoValidUpdateContext() {
        // Given
        var createCmd = mock(CreateIncidentCommand.class);
        when(createCmd.reporterId()).thenReturn("U12345"); // Add this
        when(contextService.findValidUpdateContext(createCmd)).thenReturn(null);

        // When
        appService.updateExistingIncident(createCmd);

        // Then
        verify(contextService).findValidUpdateContext(createCmd);
        verify(broadcaster).warnUserOfUnlinkedIncident("U12345");
        verifyNoInteractions(incidentService);
    }

    @Test
    @DisplayName("should update and broadcast when valid update context found")
    void shouldUpdateAndBroadcast() {
      // Given
      var createCmd = mock(CreateIncidentCommand.class);
      var updateCmd = mock(UpdateIncidentCommand.class);
      var incident = mock(Incident.class);

      when(contextService.findValidUpdateContext(createCmd)).thenReturn(updateCmd);
      when(incidentService.updateIncident(updateCmd, createCmd)).thenReturn(incident);
      when(updateCmd.channelId()).thenReturn("channel-99");

      // When
      appService.updateExistingIncident(createCmd);

      // Then
      InOrder inOrder = inOrder(contextService, incidentService, broadcaster);
      inOrder.verify(contextService).findValidUpdateContext(createCmd);
      inOrder.verify(incidentService).updateIncident(updateCmd, createCmd);
      inOrder.verify(broadcaster).updateIncidentToDeveloper(incident, "channel-99");
    }
  }
}
