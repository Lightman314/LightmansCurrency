package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.EjectionRecoveryMenu;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class EjectionRecoveryScreen extends EasyMenuScreen<EjectionRecoveryMenu> {

	public static final ResourceLocation GUI_TEXTURE = VersionUtil.vanillaResource("textures/gui/container/generic_54.png");
	
	public EjectionRecoveryScreen(EjectionRecoveryMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.resize(176,222);
	}
	
	EasyButton buttonLeft;
	EasyButton buttonRight;

	EasyButton buttonSplit;
	
	@Override
	protected void initialize(ScreenArea screenArea) {
		
		this.buttonLeft = this.addChild(IconButton.builder()
				.position(screenArea.pos.offset(-20,0))
				.pressAction(() -> this.changeSelection(-1))
				.icon(IconUtil.ICON_LEFT)
				.addon(EasyAddonHelper.activeCheck(() -> this.menu.getSelectedIndex() > 0))
				.build());
		this.buttonRight = this.addChild(IconButton.builder()
				.position(screenArea.pos.offset(screenArea.width,0))
				.pressAction(() -> this.changeSelection(1))
				.icon(IconUtil.ICON_RIGHT)
				.addon(EasyAddonHelper.activeCheck(() -> this.menu.getSelectedIndex() < this.menu.getValidEjectionData().size() - 1))
				.build());

		this.buttonSplit = this.addChild(IconButton.builder()
				.position(screenArea.pos.offset(-20,20))
				.pressAction(this::splitData)
				.icon(this::getSplitIcon)
				.addon(EasyAddonHelper.visibleCheck(this::canSplit))
				.addon(EasyAddonHelper.tooltips(this::getSplitTooltip))
				.build());

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
			return data.getName();
		return LCText.GUI_EJECTION_NO_DATA.get();
	}
	
	private void changeSelection(int delta) {
		int newSelection = this.menu.getSelectedIndex() + delta;
		this.menu.changeSelection(newSelection);
	}

	private boolean canSplit()
	{
		EjectionData data = this.menu.getSelectedData();
		return data != null && data.canSplit();
	}

	private IconData getSplitIcon()
	{
		EjectionData data = this.menu.getSelectedData();
		return data != null ? data.getSplitButtonIcon() : IconUtil.ICON_X;
	}

	private List<Component> getSplitTooltip()
	{
		EjectionData data = this.menu.getSelectedData();
		return data != null ? data.getSplitButtonTooltip() : new ArrayList<>();
	}

	private void splitData(EasyButton button) { this.menu.splitSelectedData(); }

}
