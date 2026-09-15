package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.SettingsTab;

import javax.annotation.Nullable;

public abstract class SettingsSubTab extends TraderStorageClientTab<SettingsTab> {

    private final SettingsClientTab parentTab;
    public final SettingsClientTab getParentTab() { return this.parentTab; }
    protected SettingsSubTab(SettingsClientTab parent) {
        super(parent.getMenu(),parent.getCommonTab(),parent.getScreen());
        this.parentTab = parent;
    }

    @Override
    @Deprecated
    public void sendMessage(FancyPacketMap message) { }

    public boolean displayTitle() { return true; }

    public final void sendSettingRequest(TraderNodeType<?> node,FancyPacketMap message) {
        this.getCommonTab().assembleAndHandleSettingRequst(node,message);
    }

    public abstract static class ForNode<T extends TraderNode> extends SettingsSubTab {

        protected final TraderNodeType<T> nodeType;
        protected ForNode(SettingsClientTab parent,TraderNodeType<T> nodeType) { super(parent); this.nodeType = nodeType; }

        @Nullable
        protected final T getNode() { return this.getNode(this.nodeType); }

        public final void sendSettingRequest(FancyPacketMap message) { this.sendSettingRequest(this.nodeType,message); }

    }

}