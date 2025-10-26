package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import java.util.List;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountSelectionWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.network.message.bank.CPacketSelectBankAccount;
import io.github.lightman314.lightmanscurrency.network.message.bank.CPacketATMSetPlayerAccount;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;

public class SelectionTab extends ATMTab {

	public SelectionTab(ATMScreen screen) { super(screen); }

	BankAccountSelectionWidget bankAccountSelectionWidget;
	
	EasyButton buttonToggleAdminMode;
	
	EditBox playerAccountSelect;
	EasyButton buttonSelectPlayerAccount;
	Component responseMessage = EasyText.empty();
	
	boolean adminMode = false;
	
	@Nonnull
	@Override
	public IconData getIcon() { return ItemIcon.ofItem(Items.PAPER); }

	@Override
	public MutableComponent getTooltip() { return LCText.TOOLTIP_ATM_SELECTION.get(); }

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		
		this.adminMode = false;
		if(firstOpen)
			this.responseMessage = EasyText.empty();

		this.screen.setCoinSlotsActive(false);

		this.bankAccountSelectionWidget = this.addChild(BankAccountSelectionWidget.builder()
				.position(screenArea.pos.offset(20,15))
				.width(screenArea.width - 40)
				.rows(6)
				.filter(this::canAccess)
				.selected(this::getBankReference)
				.handler(this::selectAccount)
				.build());

		this.buttonToggleAdminMode = this.addChild(IconButton.builder()
				.position(screenArea.pos.offset(screenArea.width,0))
				.pressAction(this::ToggleAdminMode)
				.icon(ItemIcon.ofItem(Items.COMMAND_BLOCK))
				.addon(EasyAddonHelper.visibleCheck(() -> LCAdminMode.isAdminPlayer(this.screen.getMenu().getPlayer())))
				.build());
		
		this.playerAccountSelect = this.addChild(new EditBox(this.screen.getFont(), screenArea.x + 7, screenArea.y + 20, 162, 20, EasyText.empty()));
		this.playerAccountSelect.visible = false;
		
		this.buttonSelectPlayerAccount = this.addChild(EasyTextButton.builder()
				.position(screenArea.pos.offset(7,45))
				.width(162)
				.text(LCText.BUTTON_BANK_PLAYER_ACCOUNT)
				.pressAction(this::PressSelectPlayerAccount)
				.build());
		this.buttonSelectPlayerAccount.visible = false;

		this.tick();

	}

	@Override
	public boolean blockInventoryClosing() { return true; }

	private boolean canAccess(@Nonnull BankReference reference) { return reference.allowedAccess(this.menu.player); }

	private BankReference getBankReference() { return this.screen.getMenu().getBankAccountReference(); }

	private boolean isSelfSelected() {
		return this.screen.getMenu().getBankAccount() == PlayerBankReference.of(this.screen.getMenu().getPlayer()).get();
	}
	
	private void ToggleAdminMode(EasyButton button) {
		this.adminMode = !this.adminMode;
		this.bankAccountSelectionWidget.visible = !this.adminMode;
		
		this.buttonSelectPlayerAccount.visible = this.adminMode;
		this.playerAccountSelect.visible = this.adminMode;
	}

	private void selectAccount(@Nonnull BankReference account)
	{
		new CPacketSelectBankAccount(account).send();
	}
	
	private void PressSelectPlayerAccount(EasyButton button) {
		String playerName = this.playerAccountSelect.getValue();
		this.playerAccountSelect.setValue("");
		if(!playerName.isBlank())
			new CPacketATMSetPlayerAccount(playerName).send();
	}
	
	public void ReceiveSelectPlayerResponse(Component message) {
		this.responseMessage = message;
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {

		gui.drawString(this.getTooltip(), 8, 6, 0x404040);

		if(this.adminMode)
		{
			List<FormattedText> lines = this.screen.getFont().getSplitter().splitLines(this.responseMessage, this.screen.getXSize() - 15, Style.EMPTY);
			for(int i = 0; i < lines.size(); ++i)
				gui.drawString(lines.get(i).getString(), 7, 70 + (gui.font.lineHeight * i), 0x404040);
		}
		
	}

	@Override
	public void closeAction() { this.screen.setCoinSlotsActive(true); }

}
