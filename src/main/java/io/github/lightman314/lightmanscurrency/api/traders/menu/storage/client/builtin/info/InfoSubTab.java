package io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.info;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.ITraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.client.gui.EasyTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin.TraderInfoTab;

import java.util.ArrayList;
import java.util.List;

public abstract class InfoSubTab extends EasyTab {

    public final TraderInfoClientTab parent;
    public final TraderInfoTab commonTab;
    public final ITraderStorageScreen screen;
    public final ITraderStorageMenu menu;

    private final List<Object> children = new ArrayList<>();

    protected InfoSubTab(TraderInfoClientTab parent)
    {
        super(parent.screen);
        this.parent = parent;
        this.commonTab = this.parent.commonTab;
        this.screen = this.parent.screen;
        this.menu = this.parent.menu;
    }

    public abstract boolean canOpen();

    public final void sendMessage(LazyPacketData.Builder message) { this.menu.SendMessage(message); }

    public boolean shouldRenderInventoryText() { return true; }

    @Override
    public <T> T addChild(T child)
    {
        if(!this.children.contains(child))
            this.children.add(child);
        return this.parent.addChild(child);
    }

    @Override
    public void removeChild(Object child)
    {
        this.children.remove(child);
        this.parent.removeChild(child);
    }

    @Override
    protected final void closeAction() {
        this.children.forEach(this.parent::removeChild);
        this.children.clear();
        this.onSubtabClose();
    }

    protected void onSubtabClose() {}
}