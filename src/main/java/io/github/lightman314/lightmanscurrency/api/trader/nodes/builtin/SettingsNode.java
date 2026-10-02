package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IStorageMenuTabProvider;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IUnitNode;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.StorageTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.SettingsClipboardTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.SettingsTab;

public class SettingsNode extends TraderNode implements IUnitNode, IStorageMenuTabProvider {

    public static final TraderNodeType<SettingsNode> TYPE = TraderNodeType.unit(SettingsNode::new);

    protected SettingsNode() {}
    @Override
    public TraderNodeType<?> getType() { return TYPE; }
    @Override
    public void addTabs(StorageTabBuilder builder) {
        builder.addTab(SettingsTab::new);
        builder.addTab(SettingsClipboardTab::new);
    }

}