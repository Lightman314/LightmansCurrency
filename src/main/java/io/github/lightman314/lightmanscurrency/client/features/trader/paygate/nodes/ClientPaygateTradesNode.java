package io.github.lightman314.lightmanscurrency.client.features.trader.paygate.nodes;

import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.interfaces.ISettingTabProvider;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.SettingsTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.simple.SimpleButtonSetting;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.simple.SimpleSettingCategory;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionAccess;
import io.github.lightman314.lightmanscurrency.client.features.tickets.ClientTicketStubStorageNode;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import io.github.lightman314.lightmanscurrency.features.trader.paygate.OutputConflictHandling;
import io.github.lightman314.lightmanscurrency.features.trader.paygate.nodes.PaygateTradesNode;

import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

public class ClientPaygateTradesNode extends ClientTraderNode implements ISettingTabProvider {

    public static final ClientTraderNode INSTANCE = new ClientTicketStubStorageNode();

    @Override
    public void addSettingsTab(INodeAccess trader,IPermissionAccess perms,BiConsumer<TraderNodeType<?>,FancyPacketMap> sender,SettingsTabBuilder builder) {
        builder.addSimpleSettingLabel(SimpleSettingCategory.MISC,PaygateTradesNode.SETTINGS_LABEL);
        builder.addSimpleSetting(SimpleSettingCategory.MISC, SimpleButtonSetting.builder()
                .withText(trader,PaygateTradesNode.TYPE,n -> n.getConflictHandling().getName())
                .canEdit(perms,LCPermissions.EDIT_SETTINGS)
                .onPress(this.onPress(trader,sender, EnumHelper::nextEnum))
                .onAltPress(this.onPress(trader,sender,EnumHelper::previousEnum))
                .build());
    }

    private Runnable onPress(INodeAccess trader,BiConsumer<TraderNodeType<?>,FancyPacketMap> sender,UnaryOperator<OutputConflictHandling> wrapper) {
        return () -> {
            OutputConflictHandling next = wrapper.apply(trader.getNodeValue(PaygateTradesNode.TYPE,PaygateTradesNode::getConflictHandling,OutputConflictHandling.DENY_SIDE_CONFLICT));
            sender.accept(PaygateTradesNode.TYPE,FancyPacketMap.map().setEnum("changeConflictMode",next));
        };
    }


}
