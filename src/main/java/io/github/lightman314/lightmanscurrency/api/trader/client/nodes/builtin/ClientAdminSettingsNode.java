package io.github.lightman314.lightmanscurrency.api.trader.client.nodes.builtin;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.interfaces.ISettingTabProvider;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.SettingsTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.simple.SimpleCheckmarkSetting;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.simple.SimpleSettingCategory;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.AdminSettingsNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionAccess;

import java.util.function.BiConsumer;

public class ClientAdminSettingsNode extends ClientTraderNode implements ISettingTabProvider {

    public static final ClientAdminSettingsNode INSTANCE = new ClientAdminSettingsNode();
    private ClientAdminSettingsNode() {}

    @Override
    public void addSettingsTab(INodeAccess trader,IPermissionAccess perms,BiConsumer<TraderNodeType<?>,FancyPacketMap> sender,SettingsTabBuilder builder) {
        builder.addSimpleSetting(SimpleSettingCategory.ADMIN,SimpleCheckmarkSetting.builder()
                .withCurrentValue(trader,AdminSettingsNode.TYPE,AdminSettingsNode::hasUnlimitedStock)
                .canEdit(perms::isAdminPlayer)
                .onPress(sender,AdminSettingsNode.TYPE,"unlimitedStock")
                .withLabel(AdminSettingsNode.VALUE_UNLIMITED_STOCK)
                .build());
        builder.addSimpleSetting(SimpleSettingCategory.ADMIN,SimpleCheckmarkSetting.builder()
                .withCurrentValue(trader,AdminSettingsNode.TYPE,AdminSettingsNode::shouldStorePrice)
                .canEdit(perms::isAdminPlayer)
                .onPress(sender,AdminSettingsNode.TYPE,"storePrice")
                .withLabel(AdminSettingsNode.VALUE_STORE_PRICE)
                .build());
    }

}
