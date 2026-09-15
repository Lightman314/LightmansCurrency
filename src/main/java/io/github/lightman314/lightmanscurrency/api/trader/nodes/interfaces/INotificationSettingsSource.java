package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.ownership.MemberLevel;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;

public interface INotificationSettingsSource {

    boolean sendsNotificationsToMembers();
    MemberLevel getNotificationMemberLevel();
    boolean sendsNotificationsToChat();

    static boolean getSendNotificationsToMembers(INodeAccess trader) { return trader.getNodes(INotificationSettingsSource.class).stream().anyMatch(INotificationSettingsSource::sendsNotificationsToMembers); }

    static MemberLevel getNotificationMemberLevel(INodeAccess trader) {
        MemberLevel level = MemberLevel.MEMBERS;
        for(INotificationSettingsSource node : trader.getNodes(INotificationSettingsSource.class)) {
            MemberLevel newLevel = node.getNotificationMemberLevel();
            if(newLevel.ordinal() > level.ordinal())
                level = newLevel;
        }
        return level;
    }

    static boolean getPushNotificationsToChat(INodeAccess trader) { return trader.getNodes(INotificationSettingsSource.class).stream().anyMatch(INotificationSettingsSource::sendsNotificationsToChat); }

}
