package io.github.lightman314.lightmanscurrency.client.features.player_data;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.TabbedMenuScreen;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.WidgetPositioner;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.features.player_data.PlayerDataMenu;
import io.github.lightman314.lightmanscurrency.features.player_data.PlayerDataTab;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Inventory;

public class PlayerDataScreen extends TabbedMenuScreen<PlayerDataMenu,PlayerDataTab,PlayerDataClientTab<?>> {

    public PlayerDataScreen(PlayerDataMenu menu,Inventory inventory) { super(menu,inventory,200,200); }

    @Override
    protected IWidgetPositioner createTabPositioner() { return WidgetPositioner.rightEdge(this,20); }

    @Override
    protected void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        gui.blitSprite(LCSprites.SQUARE_BACKGROUND,area);
        super.extractBackground(gui, area);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics,int xm,int ym) { }

}