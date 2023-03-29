package io.github.lightman314.lightmanscurrency.client.gui.screen;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.List;

/**
 * Keeping around to keep LC Tech Settings addons from crashing.
 */
@Deprecated(since = "2.1.1.0", forRemoval = true)
public class TraderSettingsScreen {

    private final SettingsSubTab tab;

    public int guiLeft() { return tab.screen.getGuiLeft(); }
    public int guiTop() { return tab.screen.getGuiTop(); }
    public final int xSize;
    public final int ySize = 140;

    public TraderData getTrader() { return this.tab.menu.getTrader(); }

    public TraderSettingsScreen(SettingsSubTab tab) { this.tab = tab; this.xSize = this.tab.screen.getXSize(); }

    public <T extends AbstractWidget> T addRenderableTabWidget(T widget) { return this.tab.addWidget(widget); }

    public void removeRenderableTabWidget(AbstractWidget widget) { this.tab.removeWidget(widget); }

    public <T extends GuiEventListener> T addTabListener(T listener) { return this.tab.addWidget(listener); }

    public void removeTabListener(GuiEventListener listener) { this.tab.removeWidget(listener); }

    public boolean hasPermission(String permission) { return this.tab.menu.hasPermission(permission); }

    public int getPermissionLevel(String permission) { return this.tab.menu.getPermissionLevel(permission); }

    public boolean hasPermissions(List<String> permissions)
    {
        for (String permission : permissions) {
            if (!this.hasPermission(permission))
                return false;
        }
        return true;
    }

}
