package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings;

import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.settings.TraderSettingsTab;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class SettingsSubTab extends EasyTab {

    public final TraderSettingsClientTab parent;
    public final TraderSettingsTab commonTab;
    public final ITraderStorageScreen screen;
    public final ITraderStorageMenu menu;

    private final List<Object> children = new ArrayList<>();

    protected SettingsSubTab(@Nonnull TraderSettingsClientTab parent)
    {
        super(parent.screen);
        this.parent = parent;
        this.commonTab = this.parent.commonTab;
        this.screen = this.parent.screen;
        this.menu = this.parent.menu;
    }

    @Override
    public int getColor() { return 0xFFFFFF; }

    public abstract boolean canOpen();

    public void tick() {}

    public final void sendMessage(@Nonnull LazyPacketData.Builder message) { this.menu.SendMessage(message); }

    @Deprecated(since = "2.1.2.4")
    public final void sendNetworkMessage(@Nonnull CompoundTag message) { this.commonTab.SendSettingsMessage(message); }

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