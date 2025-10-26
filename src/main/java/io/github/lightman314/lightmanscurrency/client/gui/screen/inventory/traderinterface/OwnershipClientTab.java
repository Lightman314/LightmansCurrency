package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.PotentialOwner;
import io.github.lightman314.lightmanscurrency.client.gui.widget.OwnerSelectionWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base.OwnershipTab;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OwnershipClientTab extends TraderInterfaceClientTab<OwnershipTab> {

	public OwnershipClientTab(Object screen, OwnershipTab tab) { super(screen, tab); }
	
	@Nonnull
	@Override
	public IconData getIcon() { return ItemIcon.ofItem(Items.PLAYER_HEAD); }

	@Override
	public MutableComponent getTooltip() { return LCText.TOOLTIP_SETTINGS_OWNER.get(); }
	
	@Override
	public boolean blockInventoryClosing() { return true; }

	private boolean manualMode = false;

	private OwnerSelectionWidget ownerSelectionWidget;
	private EditBox playerOwnerInput;
	private EasyButton playerOwnerButton;
	
	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {

		this.playerOwnerInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 23, screenArea.y + 26, 160, 20, EasyText.empty()));
		this.playerOwnerInput.setMaxLength(16);

		this.playerOwnerButton = this.addChild(EasyTextButton.builder()
				.position(screenArea.pos.offset(23,47))
				.width(160)
				.text(LCText.BUTTON_OWNER_SET_PLAYER)
				.pressAction(this::SetOwnerPlayer)
				.addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_WARNING_CANT_BE_UNDONE.getWithStyle(ChatFormatting.YELLOW,ChatFormatting.BOLD)))
				.build());

		this.ownerSelectionWidget = this.addChild(OwnerSelectionWidget.builder()
				.position(screenArea.pos.offset(22,26))
				.width(153)
				.rows(5)
				.selected(this::getCurrentOwner)
				.handler(this.commonTab::setOwner)
				.oldWidget(this.ownerSelectionWidget)
				.filter(this::hasBankAccount)
				.build());

		this.addChild(IconButton.builder()
				.position(screenArea.pos.offset(screenArea.width - 25,5))
				.pressAction(this::toggleInputMode)
				.icon(this::getModeIcon)
				.addon(EasyAddonHelper.tooltip(this::getModeTooltip))
				.build());

		this.updateMode();
		
	}

	private boolean hasBankAccount(@Nonnull PotentialOwner owner)
	{
		Owner result = owner.asOwner();
        return result != null && result.asBankReference() != null;
    }

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {
		
		if(this.menu.getBE() == null)
			return;

		gui.drawString(TextRenderUtil.fitString(LCText.GUI_OWNER_CURRENT.get(this.menu.getBE().getOwnerName()), this.screen.getXSize() - 20), 10, 10, 0x404040);
		
	}

	@Override
	public void tick() {
		if(this.manualMode)
			this.playerOwnerButton.active = !this.playerOwnerInput.getValue().isBlank();
	}

	@Nullable
	protected OwnerData getCurrentOwner()
	{
		return this.menu.getBE().owner;
	}

	private void toggleInputMode(EasyButton button) { this.manualMode = !this.manualMode; this.updateMode(); }

	private void updateMode()
	{
		this.playerOwnerInput.visible = this.playerOwnerButton.visible = this.manualMode;
		if(this.manualMode)
			this.playerOwnerButton.active = !this.playerOwnerInput.getValue().isBlank();
		this.ownerSelectionWidget.setVisible(!this.manualMode);
	}

	private IconData getModeIcon() { return this.manualMode ? ItemIcon.ofItem(Items.COMMAND_BLOCK) : IconUtil.ICON_ALEX_HEAD; }

	private Component getModeTooltip() { return this.manualMode ? LCText.TOOLTIP_OWNERSHIP_MODE_SELECTION.get() : LCText.TOOLTIP_OWNERSHIP_MODE_MANUAL.get(); }

	private void SetOwnerPlayer()
	{
		if(this.playerOwnerInput != null)
		{
			this.commonTab.setPlayerOwner(this.playerOwnerInput.getValue());
			this.playerOwnerInput.setValue("");
		}
	}

}
