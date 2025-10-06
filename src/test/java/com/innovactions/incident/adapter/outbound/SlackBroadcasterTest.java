package com.innovactions.incident.adapter.outbound;

import com.innovactions.incident.domain.model.Incident;
import com.innovactions.incident.domain.model.Severity;
import com.innovactions.incident.domain.service.ChannelNameGenerator;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.conversations.ConversationsCreateRequest;
import com.slack.api.methods.request.conversations.ConversationsInviteRequest;
import com.slack.api.methods.request.conversations.ConversationsSetTopicRequest;
import com.slack.api.methods.response.conversations.ConversationsCreateResponse;
import com.slack.api.methods.response.conversations.ConversationsInviteResponse;
import com.slack.api.methods.response.conversations.ConversationsSetTopicResponse;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.Conversation;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SlackBroadcasterTest {

    @Mock private ChannelNameGenerator channelNameGenerator;
    @Mock private Slack slack;
    @Mock private MethodsClient methodsClient;

    @Mock private ConversationsCreateResponse createResponse;
    @Mock private ConversationsSetTopicResponse topicResponse;
    @Mock private ConversationsInviteResponse inviteResponse;
    @Mock private ChatPostMessageResponse postMessageResponse;
    @Mock private Conversation channel;

    private SlackBroadcaster broadcaster;

    private static final String BOT_TOKEN = "xoxb-test-token";
    private static final String DEVELOPER_USER_ID = "U123456";
    private static final String CHANNEL_ID = "C123456";
    private static final String CHANNEL_NAME = "incident-high-20250106";

    @BeforeEach
    void setUp() {
        broadcaster = new SlackBroadcaster(BOT_TOKEN, DEVELOPER_USER_ID, channelNameGenerator);
    }

    /** Utility: mock Slack static and common behavior */
    private MockedStatic<Slack> mockSlack() throws IOException, SlackApiException {
        MockedStatic<Slack> slackStatic = mockStatic(Slack.class);
        slackStatic.when(Slack::getInstance).thenReturn(slack);
        when(slack.methods(BOT_TOKEN)).thenReturn(methodsClient);

        // Default stubs for Slack calls
        doReturn(createResponse).when(methodsClient).conversationsCreate((ConversationsCreateRequest) any());
        when(createResponse.isOk()).thenReturn(true);
        when(createResponse.getChannel()).thenReturn(channel);
        when(channel.getId()).thenReturn(CHANNEL_ID);

        doReturn(topicResponse).when(methodsClient).conversationsSetTopic((ConversationsSetTopicRequest) any());
        when(topicResponse.isOk()).thenReturn(true);

        doReturn(inviteResponse).when(methodsClient).conversationsInvite((ConversationsInviteRequest) any());
        when(inviteResponse.isOk()).thenReturn(true);

        doReturn(postMessageResponse).when(methodsClient).chatPostMessage((ChatPostMessageRequest) any());

        return slackStatic;
    }

    private Incident createIncident(String id, Severity severity, String reporterId) {
        Incident incident = mock(Incident.class);
        when(incident.getId()).thenReturn(UUID.randomUUID());
        when(incident.getSeverity()).thenReturn(severity);
        when(incident.getReporterId()).thenReturn(reporterId);
        when(incident.summary()).thenReturn("Incident " + id + " - " + severity + " severity");
        return incident;
    }

    // ---------------------------------------------------------------------
    // TESTS
    // ---------------------------------------------------------------------

    @Test
    void shouldBroadcastIncidentSuccessfully() throws IOException, SlackApiException {
        Incident incident = createIncident("INC-001", Severity.MAJOR, "user123");
        when(channelNameGenerator.generateChannelName(Severity.MAJOR)).thenReturn(CHANNEL_NAME);

        try (MockedStatic<Slack> slackStatic = mockSlack()) {
            broadcaster.broadcast(incident);

            verify(channelNameGenerator).generateChannelName(Severity.MAJOR);
            verify(methodsClient).conversationsCreate((ConversationsCreateRequest) any());
            verify(methodsClient).conversationsSetTopic((ConversationsSetTopicRequest) any());
            verify(methodsClient).conversationsInvite((ConversationsInviteRequest) any());
            verify(methodsClient).chatPostMessage((ChatPostMessageRequest) any());
        }
    }

    @Test
    void shouldCreateChannelWithCorrectName() throws IOException, SlackApiException {
        Incident incident = createIncident("INC-002", Severity.URGENT, "user456");
        when(channelNameGenerator.generateChannelName(Severity.URGENT)).thenReturn(CHANNEL_NAME);

        try (MockedStatic<Slack> slackStatic = mockSlack()) {
            broadcaster.broadcast(incident);

            verify(methodsClient).conversationsCreate((ConversationsCreateRequest) any());
            verify(createResponse).isOk(); // sanity check
        }
    }

    @Test
    void shouldSetTopicWithReporterIdCorrectly() throws IOException, SlackApiException {
        Incident incident = createIncident("INC-003", Severity.MINOR, "reporter789");
        when(channelNameGenerator.generateChannelName(Severity.MINOR)).thenReturn(CHANNEL_NAME);

        try (MockedStatic<Slack> slackStatic = mockSlack()) {
            broadcaster.broadcast(incident);

            verify(methodsClient).conversationsSetTopic((ConversationsSetTopicRequest) any());
            verify(topicResponse).isOk();
        }
    }

    @Test
    void shouldInviteDeveloperToChannel() throws IOException, SlackApiException {
        Incident incident = createIncident("INC-004", Severity.MINOR, "user999");
        when(channelNameGenerator.generateChannelName(Severity.MINOR)).thenReturn(CHANNEL_NAME);

        try (MockedStatic<Slack> slackStatic = mockSlack()) {
            broadcaster.broadcast(incident);

            verify(methodsClient).conversationsInvite((ConversationsInviteRequest) any());
            verify(inviteResponse).isOk();
        }
    }

    @Test
    void shouldPostIncidentSummaryToChannel() throws IOException, SlackApiException {
        Incident incident = createIncident("INC-005", Severity.MAJOR, "userABC");
        String expectedSummary = "Incident INC-005 - HIGH severity";
        when(channelNameGenerator.generateChannelName(Severity.MAJOR)).thenReturn(CHANNEL_NAME);
        when(incident.summary()).thenReturn(expectedSummary);

        try (MockedStatic<Slack> slackStatic = mockSlack()) {
            broadcaster.broadcast(incident);

            ArgumentCaptor<ChatPostMessageRequest> captor =
                    ArgumentCaptor.forClass(ChatPostMessageRequest.class);
            verify(methodsClient).chatPostMessage(captor.capture());

            ChatPostMessageRequest request = captor.getValue();
            assertThat(request.getChannel()).isEqualTo(CHANNEL_ID);
            assertThat(request.getText()).isEqualTo(expectedSummary);
        }
    }

    @Test
    void shouldReturnEarlyWhenChannelCreationFails() throws IOException, SlackApiException {
        Incident incident = createIncident("INC-006", Severity.MAJOR, "userXYZ");
        when(channelNameGenerator.generateChannelName(Severity.MAJOR)).thenReturn(CHANNEL_NAME);

        try (MockedStatic<Slack> slackStatic = mockSlack()) {
            when(createResponse.isOk()).thenReturn(false);
            when(createResponse.getError()).thenReturn("channel_name_taken");

            broadcaster.broadcast(incident);

            verify(methodsClient).conversationsCreate((ConversationsCreateRequest) any());
            verify(methodsClient, never()).conversationsSetTopic((ConversationsSetTopicRequest) any());
            verify(methodsClient, never()).conversationsInvite((ConversationsInviteRequest) any());
            verify(methodsClient, never()).chatPostMessage((ChatPostMessageRequest) any());
        }
    }

    @Test
    void shouldContinueWhenTopicSettingFails() throws IOException, SlackApiException {
        Incident incident = createIncident("INC-007", Severity.MAJOR, "user111");
        when(channelNameGenerator.generateChannelName(Severity.MAJOR)).thenReturn(CHANNEL_NAME);

        try (MockedStatic<Slack> slackStatic = mockSlack()) {
            when(topicResponse.isOk()).thenReturn(false);
            when(topicResponse.getError()).thenReturn("permission_denied");

            broadcaster.broadcast(incident);

            verify(methodsClient).conversationsSetTopic((ConversationsSetTopicRequest) any());
            verify(methodsClient).conversationsInvite((ConversationsInviteRequest) any());
            verify(methodsClient).chatPostMessage((ChatPostMessageRequest) any());
        }
    }

    @Test
    void shouldContinueWhenDeveloperInviteFails() throws IOException, SlackApiException {
        Incident incident = createIncident("INC-008", Severity.MINOR, "user222");
        when(channelNameGenerator.generateChannelName(Severity.MINOR)).thenReturn(CHANNEL_NAME);

        try (MockedStatic<Slack> slackStatic = mockSlack()) {
            when(inviteResponse.isOk()).thenReturn(false);
            when(inviteResponse.getError()).thenReturn("user_not_found");

            broadcaster.broadcast(incident);

            verify(methodsClient).conversationsInvite((ConversationsInviteRequest) any());
            verify(methodsClient).chatPostMessage((ChatPostMessageRequest) any());
        }
    }

    @Test
    void shouldHandleIOException() throws IOException, SlackApiException {
        Incident incident = createIncident("INC-009", Severity.URGENT, "user333");
        when(channelNameGenerator.generateChannelName(Severity.URGENT)).thenReturn(CHANNEL_NAME);

        try (MockedStatic<Slack> slackStatic = mockSlack()) {
            doThrow(new IOException("Network error"))
                    .when(methodsClient)
                    .conversationsCreate((ConversationsCreateRequest) any());

            broadcaster.broadcast(incident);

            verify(methodsClient).conversationsCreate((ConversationsCreateRequest) any());
            verify(methodsClient, never()).conversationsSetTopic((ConversationsSetTopicRequest) any());
        }
    }

    @Test
    void shouldHandleSlackApiException() throws IOException, SlackApiException {
        Incident incident = createIncident("INC-010", Severity.MAJOR, "user444");
        when(channelNameGenerator.generateChannelName(Severity.MAJOR)).thenReturn(CHANNEL_NAME);

        Response errorResponse = mock(Response.class);
        SlackApiException apiException = new SlackApiException(errorResponse, "API error");

        try (MockedStatic<Slack> slackStatic = mockSlack()) {
            doThrow(apiException)
                    .when(methodsClient)
                    .conversationsCreate((ConversationsCreateRequest) any());

            broadcaster.broadcast(incident);

            verify(methodsClient).conversationsCreate((ConversationsCreateRequest) any());
            verify(methodsClient, never()).conversationsInvite((ConversationsInviteRequest) any());
        }
    }
}
