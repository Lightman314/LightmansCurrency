package io.github.lightman314.lightmanscurrency.client.gui.screen;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed.EasyTabbedMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.util.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.TeamManagementMenu;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementTab;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class TeamManagerScreen extends EasyTabbedMenuScreen<TeamManagementMenu,TeamManagementTab,TeamManagerScreen>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation("lightmanscurrency", "textures/gui/teammanager.png");

	public TeamManagerScreen(@Nonnull TeamManagementMenu menu, @Nonnull Inventory inventory, @Nonnull Component title) { super(menu, inventory); this.resize(200,200); }

	@Nonnull
	@Override
	protected IWidgetPositioner getTabButtonPositioner() { return LazyWidgetPositioner.create(this,LazyWidgetPositioner.createClockwiseWraparound(this.getArea(),0),25); }

	@Override
	protected void init(ScreenArea screenArea) { }

	@Override
	protected void renderBackground(@Nonnull EasyGuiGraphics gui) {
		gui.renderNormalBackground(GUI_TEXTURE, this);
	}

}