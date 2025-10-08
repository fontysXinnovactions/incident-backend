package com.innovactions.incident.port.outbound;

import java.util.List;

public interface ChannelAdministrationPort {
    String createPublicChannel(String name);
    void setChannelTopic(String channelId, String topic);
    void inviteUsers(String channelId, List<String> userIds);
    void kickUserFromChannel(String channelId, String userId);
    List<String> listMembers(String channelId);
}


