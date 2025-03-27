package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.data.ClientPlayerNameCache;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountSelectionWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import io.github.lightman314.lightmanscurrency.network.message.bank.CPacketBankTransferPlayer;
import io.github.lightman314.lightmanscurrency.network.message.bank.CPacketBankTransferAccount;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.UUID;

public class TransferTab extends ATMTab {

	public TransferTab(ATMScreen screen) { super(screen); }

	//Response should be 100 ticks or 5 seconds
	public static final int RESPONSE_DURATION = 100;

	private int responseTimer = 0;

	MoneyValueWidget amountWidget;

	BankAccountSelectionWidget accountSelectionWidget;

	EditBox playerInput;
	IconButton buttonToggleMode;

	EasyButton buttonTransfer;

	boolean playerMode = false;
	BankReference selectedAccount = null;

	@Nonnull
	@Override
	public IconData getIcon() { return IconUtil.ICON_STORE_COINS; }

	@Override
	public MutableComponent getTooltip() { return LCText.TOOLTIP_ATM_TRANSFER.get(); }

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {

		this.screen.setCoinSlotsActive(false);

		this.responseTimer = 0;
		if(firstOpen)
			this.screen.getMenu().clearMessage();

		this.amountWidget = this.addChild(MoneyValueWidget.builder()
				.position(screenArea.pos)
				.oldIfNotFirst(firstOpen,this.amountWidget)
				.blockFreeInputs()
				.build());

		this.accountSelectionWidget = this.addChild(BankAccountSelectionWidget.builder()
				.position(screenArea.pos.offset(10,84))
				.width(screenArea.width - 20)
				.rows(3)
				.filter(this::allowAccount)
				.selected(() -> this.selectedAccount)
				.handler(r -> this.selectedAccount = r)
				.build());
		this.accountSelectionWidget.visible = !this.playerMode;

		this.playerInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 10, screenArea.y + 104, screenArea.width - 20, 20, Component.empty()));
		this.playerInput.visible = this.playerMode;

		this.buttonToggleMode = this.addChild(IconButton.builder()
				.position(screenArea.pos.offset(screenArea.width, 84))
				.pressAction(this::ToggleMode)
				.icon(() -> this.playerMode ? IconData.of(Items.PLAYER_HEAD) : IconUtil.ICON_ALEX_HEAD)
				.addon(EasyAddonHelper.toggleTooltip(() -> this.playerMode, LCText.TOOLTIP_ATM_TRANSFER_MODE_LIST.get(), LCText.TOOLTIP_ATM_TRANSFER_MODE_PLAYER.get()))
				.build());
		this.buttonTransfer = this.addChild(IconButton.builder()
				.position(screenArea.pos.offset(screenArea.width, 124))
				.pressAction(this::PressTransfer)
				.icon(IconUtil.ICON_STORE_COINS)
				.color(this::getTransferColor)
				.addon(EasyAddonHelper.activeCheck(this::canTriggerTransfer))
				.addon(EasyAddonHelper.tooltip(this::getTransferTooltip))
				.build());

	}

	private boolean allowAccount(BankReference reference)
	{
		return reference != null && !reference.equals(this.menu.getBankAccountReference());
	}

	private int getTransferColor(IconButton button)
	{
		if(button.isActive())
		{
			if(this.playerMode)
			{
				//Check if selected account exists
				UUID playerID = ClientPlayerNameCache.lookupID(this.playerInput.getValue());
				if(playerID == null)
					return ChatFormatting.GOLD.getColor();
			}
			return 0x00FF00;
		}
		return 0xFFFFFF;
	}

	private Component getTransferTooltip()
	{
		if(this.playerMode)
		{
			UUID playerID = ClientPlayerNameCache.lookupID(this.playerInput.getValue());
			if(playerID == null)
				return null;
			String playerName = ClientPlayerNameCache.lookupName(playerID);
			if(playerName == null)
				playerName = this.playerInput.getValue();
			return LCText.TOOLTIP_ATM_TRANSFER_TRIGGER.get(this.amountWidget.getCurrentValue().getText(),LCText.GUI_BANK_ACCOUNT_NAME.get(playerName));
		}
		else if(this.selectedAccount != null)
		{
			IBankAccount account = this.selectedAccount.get();
			if(account != null)
				return LCText.TOOLTIP_ATM_TRANSFER_TRIGGER.get(this.amountWidget.getCurrentValue().getText(),this.selectedAccount.get().getName());
		}
		return null;
	}

	private void PressTransfer(EasyButton button)
	{
		if(this.playerMode)
		{
			//Check for bank account for the player
			new CPacketBankTransferPlayer(this.playerInput.getValue(), this.amountWidget.getCurrentValue()).send();
			this.playerInput.setValue("");
			this.amountWidget.changeValue(MoneyValue.empty());
		}
		else if(this.selectedAccount != null)
		{
			new CPacketBankTransferAccount(this.selectedAccount, this.amountWidget.getCurrentValue()).send();
			this.amountWidget.changeValue(MoneyValue.empty());
		}
	}

	private void ToggleMode(EasyButton button) {
		this.playerMode = !this.playerMode;
		this.accountSelectionWidget.visible = !this.playerMode;
		this.playerInput.visible = this.playerMode;
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {

		//this.screen.getFont().draw(pose, this.getTooltip(), this.screen.getGuiLeft() + 8f, this.screen.getGuiTop() + 6f, 0x404040);
		Component balance = this.screen.getMenu().getBankAccount() == null ? LCText.GUI_BANK_NO_SELECTED_ACCOUNT.get() : this.screen.getMenu().getBankAccount().getBalanceText();
		gui.drawString(balance, 8, 72, 0x404040);

		if(this.hasMessage())
		{
			//Draw a message background
			TextRenderUtil.drawCenteredMultilineText(gui, this.getMessage(), 2, this.screen.getXSize() - 4, 5, 0x404040);
			this.amountWidget.visible = false;
		}
		else
			this.amountWidget.visible = true;
	}

	private boolean canTriggerTransfer() {
		if(this.amountWidget.getCurrentValue().isEmpty())
			return false;
		if(this.playerMode)
		{
			UUID playerID = ClientPlayerNameCache.lookupID(this.playerInput.getValue());
			if(playerID == null)
				return false;
			if(this.menu.getBankAccountReference() instanceof PlayerBankReference prb)
				return !prb.getPlayer().is(playerID);
			else
				return true;
		}
		else
			return this.selectedAccount != null;
	}

	@Override
	public void tick() {
		if(this.hasMessage())
		{
			this.responseTimer++;
			if(this.responseTimer >= RESPONSE_DURATION)
			{
				this.responseTimer = 0;
				this.screen.getMenu().clearMessage();
			}
		}
	}

	private boolean hasMessage() { return this.screen.getMenu().hasTransferMessage(); }

	private Component getMessage() { return this.screen.getMenu().getTransferMessage(); }

	@Override
	public void closeAction() {
		this.screen.setCoinSlotsActive(true);
		this.responseTimer = 0;
		this.screen.getMenu().clearMessage();
	}

	@Override
	public boolean blockInventoryClosing() { return this.playerMode; }

}