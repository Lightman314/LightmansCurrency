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
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
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

		this.buttonLeft = this.addChild(new IconButton(screenArea.pos.offset(-20, 0), b -> this.changeSelection(-1), IconUtil.ICON_LEFT)
				.withAddons(EasyAddonHelper.activeCheck(() -> this.menu.getSelectedIndex() > 0)));
		this.buttonRight = this.addChild(new IconButton(screenArea.pos.offset(screenArea.width, 0), b -> this.changeSelection(1), IconUtil.ICON_RIGHT)
				.withAddons(EasyAddonHelper.activeCheck(() -> this.menu.getSelectedIndex() < this.menu.getValidEjectionData().size() - 1)));

		this.buttonSplit = this.addChild(new IconButton(screenArea.pos.offset(-20,20), this::splitData, this::getSplitIcon)
				.withAddons(EasyAddonHelper.visibleCheck(this::canSplit),
						EasyAddonHelper.tooltips(this::getSplitTooltip)));

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