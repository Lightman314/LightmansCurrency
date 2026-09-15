package io.github.lightman314.lightmanscurrency.client.features.atm;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.TabbedMenuScreen;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.WidgetPositioner;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.features.atm.ATMMenu;
import io.github.lightman314.lightmanscurrency.features.atm.ATMTab;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ATMScreen extends TabbedMenuScreen<ATMMenu,ATMTab,ATMClientTab<?>> {

    public static final Identifier GUI_TEXTURE = LCApi.id("textures/gui/container/atm.png");

    public ATMScreen(ATMMenu menu, Inventory inventory) { super(menu, inventory,176,243); }

    @Override
    protected IWidgetPositioner createTabPositioner() { return WidgetPositioner.leftEdge(this,20); }

    @Override
    protected void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        gui.blitBackground(GUI_TEXTURE,area);
        gui.blitSlots(this.menu.getMoneySlots());
        super.extractBackground(gui, area);
    }

}
