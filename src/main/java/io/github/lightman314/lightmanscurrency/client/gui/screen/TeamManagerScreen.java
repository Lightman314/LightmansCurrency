package io.github.lightman314.lightmanscurrency.client.gui.screen;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.client.gui.tabbed.EasyTabbedMenuScreen;
import io.github.lightman314.lightmanscurrency.api.client.widgets.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.WidgetRotation;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.TeamManagementMenu;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementTab;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TeamManagerScreen extends EasyTabbedMenuScreen<TeamManagementMenu,TeamManagementTab,TeamManagerScreen>{

    public static final ResourceLocation GUI_TEXTURE = LightmansCurrency.id("textures/gui/teammanager.png");

    public TeamManagerScreen(TeamManagementMenu menu, Inventory inventory, Component title) { super(menu, inventory); this.resize(200,200); }

    @Override
    protected IWidgetPositioner getTabButtonPositioner() { return LazyWidgetPositioner.create(this,LazyWidgetPositioner.createClockwiseWraparound(this.getArea(), WidgetRotation.TOP), TabButton.SIZE); }

    @Override
    protected void init(ScreenArea screenArea) { }

    @Override
    protected void renderBackground(EasyGuiGraphics gui) { gui.renderNormalBackground(GUI_TEXTURE, this); }

}
