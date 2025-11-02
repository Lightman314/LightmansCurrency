package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings;

import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.core.TraderSettingsTab;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SettingsSubTab extends EasyTab {

    public final TraderSettingsClientTab parent;
    public final TraderSettingsTab commonTab;
    public final ITraderStorageScreen screen;
    public final ITraderStorageMenu menu;

    private final List<Object> children = new ArrayList<>();

    protected SettingsSubTab(TraderSettingsClientTab parent)
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