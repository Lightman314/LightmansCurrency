package io.github.lightman314.lightmanscurrency.api.trader.client.nodes.builtin;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.interfaces.ISettingTabProvider;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.SettingsTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.builtin.DisplaySettingsTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.simple.SimpleCheckmarkSetting;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.simple.SimpleSettingCategory;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.DisplayNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionAccess;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;

import java.util.function.BiConsumer;

public class ClientDisplayNode extends ClientTraderNode implements ISettingTabProvider {

    public static final ClientDisplayNode INSTANCE = new ClientDisplayNode();
    private ClientDisplayNode() {}

    @Override
    public void addSettingsTab(INodeAccess trader, IPermissionAccess perms, BiConsumer<TraderNodeType<?>,FancyPacketMap> sender, SettingsTabBuilder builder) {
        builder.addTab(-1000,DisplaySettingsTab::new);
        builder.addSimpleSettingLabel(SimpleSettingCategory.MISC,DisplayNode.NAME);
        builder.addSimpleSetting(SimpleSettingCategory.MISC,SimpleCheckmarkSetting.builder()
                .withCurrentValue(trader,DisplayNode.TYPE,DisplayNode::alwaysShowSearchBox)
                .canEdit(perms,LCPermissions.EDIT_SETTINGS)
                .onPress(sender,DisplayNode.TYPE,"alwaysShowSearch")
                .withLabel(DisplayNode.GUI_ALWAYS_SHOW_SEARCH)
                .build());
    }

}