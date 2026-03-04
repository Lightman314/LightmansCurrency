package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin;

import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin.SettingsClipboardTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin.TraderSettingsTab;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.UnitNode;

public class NormalTraderNode extends UnitNode {

    public static final TraderNodeType<NormalTraderNode> TYPE = TraderNodeType.unit(NormalTraderNode::new);

    private NormalTraderNode() {}
    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void applyStorageTabs(ITraderStorageMenu menu) {
        menu.addTab(new TraderSettingsTab(menu));
        menu.addTab(new SettingsClipboardTab(menu));
    }

}
