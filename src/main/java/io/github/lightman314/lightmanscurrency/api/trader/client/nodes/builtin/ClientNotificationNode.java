package io.github.lightman314.lightmanscurrency.api.trader.client.nodes.builtin;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.ownership.MemberLevel;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.interfaces.IInfoTabProvider;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.interfaces.ISettingTabProvider;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.info.InfoTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.info.builtin.NotificationTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.SettingsTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.simple.SimpleButtonSetting;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.simple.SimpleCheckmarkSetting;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.simple.SimpleSettingCategory;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.NotificationNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionAccess;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;

import java.util.function.BiConsumer;

public class ClientNotificationNode extends ClientTraderNode implements IInfoTabProvider, ISettingTabProvider {

    public static final ClientNotificationNode INSTANCE = new ClientNotificationNode();

    private ClientNotificationNode() {}

    @Override
    public void addInfoTab(INodeAccess trader,InfoTabBuilder builder) {
        builder.addTab(-1000,t -> new NotificationTab(t,false));
        builder.addTab(1000,t -> new NotificationTab(t,true));
    }

    @Override
    public void addSettingsTab(INodeAccess trader,IPermissionAccess perms,BiConsumer<TraderNodeType<?>,FancyPacketMap> sender,SettingsTabBuilder builder) {
        //Add push to chat settings option
        builder.addSimpleSettingLabel(SimpleSettingCategory.MISC,NotificationNode.NAME);
        builder.addSimpleSetting(SimpleSettingCategory.MISC,SimpleCheckmarkSetting.builder()
                .withCurrentValue(trader, NotificationNode.TYPE,NotificationNode::sendsNotificationsToChat)
                .canEdit(perms,LCPermissions.EDIT_SETTINGS)
                .onPress(sender,NotificationNode.TYPE,"pushToChat")
                .withLabel(NotificationNode.VALUE_PUSH_TO_CHAT)
                .build());
        TraderData t = trader.getTrader();
        if(t != null && t.getOwner().hasMemberLevels()) {
            //Add Member-Related settings options
            //Notify Member toggle
            builder.addSimpleSetting(SimpleSettingCategory.MISC,SimpleCheckmarkSetting.builder()
                    .withCurrentValue(trader,NotificationNode.TYPE,NotificationNode::sendsNotificationsToMembers)
                    .canEdit(perms,LCPermissions.EDIT_SETTINGS)
                    .onPress(sender,NotificationNode.TYPE,"pushToMembers")
                    .withLabel(NotificationNode.VALUE_NOTIFY_MEMBERS)
                    .build());
            //Notify Member Level
            builder.addSimpleSetting(SimpleSettingCategory.MISC, SimpleButtonSetting.builder()
                    .withText(trader,NotificationNode.TYPE,NotificationNode::getMemberLevelBlurb)
                    .canEdit(perms,LCPermissions.EDIT_SETTINGS)
                    .onPress(() -> {
                        MemberLevel currentLevel = trader.getNodeValue(NotificationNode.TYPE,NotificationNode::getNotificationMemberLevel);
                        if(currentLevel != null)
                            sender.accept(NotificationNode.TYPE,FancyPacketMap.map().setEnum("memberLevel",currentLevel.next()));
                    }).build());
        }

    }

}
