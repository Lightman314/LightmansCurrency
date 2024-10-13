package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.common.menus.EjectionRecoveryMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class EjectionRecoveryScreen extends EasyMenuScreen<EjectionRecoveryMenu> {

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
	
	public EjectionRecoveryScreen(EjectionRecoveryMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.resize(176,222);
	}
	
	EasyButton buttonLeft;
	EasyButton buttonRight;
	
	@Override
	protected void initialize(ScreenArea screenArea) {
		
		this.buttonLeft = this.addChild(new IconButton(screenArea.pos.offset(-20, 0), b -> this.changeSelection(-1), IconAndButtonUtil.ICON_LEFT)
				.withAddons(EasyAddonHelper.activeCheck(() -> this.menu.getSelectedIndex() > 0)));
		this.buttonRight = this.addChild(new IconButton(screenArea.pos.offset(screenArea.width, 0), b -> this.changeSelection(1), IconAndButtonUtil.ICON_RIGHT)
				.withAddons(EasyAddonHelper.activeCheck(() -> this.menu.getSelectedIndex() < this.menu.getValidEjectionData().size() - 1)));
		
	}
	
	@Override
	protected void renderBG(@Nonnull EasyGuiGraphics gui)
	{
		gui.renderNormalBackground(GUI_TEXTURE, this);
		gui.drawString(this.getTraderTitle(), this.titleLabelX, this.titleLabelY, 0x404040);
		gui.drawString(this.playerInventoryTitle, this.inventoryLabelX, this.imageHeight - 94, 0x404040);
	}
	
	private Component getTraderTitle() {
		EjectionData data = this.menu.getSelectedData();
		if(data != null)
			return data.getTraderName();
		return EasyText.translatable("gui.lightmanscurrency.trader_recovery.nodata");
	}
	
	private void changeSelection(int delta) {
		int newSelection = this.menu.getSelectedIndex() + delta;
		this.menu.changeSelection(newSelection);
	}

}
