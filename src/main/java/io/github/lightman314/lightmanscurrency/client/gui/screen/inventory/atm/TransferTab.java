package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TeamButton.Size;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.TeamBankReference;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import io.github.lightman314.lightmanscurrency.network.message.bank.CPacketBankTransferPlayer;
import io.github.lightman314.lightmanscurrency.network.message.bank.CPacketBankTransferTeam;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;

public class TransferTab extends ATMTab {

	public TransferTab(ATMScreen screen) { super(screen); }
	
	//Response should be 100 ticks or 5 seconds
	public static final int RESPONSE_DURATION = 100;
	
	private int responseTimer = 0;
	
	MoneyValueWidget amountWidget;
	
	EditBox playerInput;
	TeamSelectWidget teamSelection;
	
	IconButton buttonToggleMode;
	EasyButton buttonTransfer;
	
	long selectedTeam = -1;
	
	boolean playerMode = true;
	
	@Nonnull
	@Override
	public IconData getIcon() { return IconUtil.ICON_STORE_COINS; }

	@Override
	public MutableComponent getTooltip() { return LCText.TOOLTIP_ATM_TRANSFER.get(); }

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {

		EasySlot.SetInactive(this.screen.getMenu());
		
		this.responseTimer = 0;
		if(firstOpen)
			this.screen.getMenu().clearMessage();
		
		this.amountWidget = this.addChild(new MoneyValueWidget(screenArea.pos, firstOpen ? null : this.amountWidget, MoneyValue.empty(), MoneyValueWidget.EMPTY_CONSUMER));
		this.amountWidget.allowFreeInput = false;
		this.amountWidget.drawBG = false;
		
		this.buttonToggleMode = this.addChild(new IconButton(screenArea.pos.offset(screenArea.width - 30, 64), this::ToggleMode, () -> this.playerMode ? IconData.of(Items.PLAYER_HEAD) : IconUtil.ICON_ALEX_HEAD)
				.withAddons(EasyAddonHelper.toggleTooltip(() -> this.playerMode, LCText.TOOLTIP_ATM_TRANSFER_MODE_TEAM.get(), LCText.TOOLTIP_ATM_TRANSFER_MODE_PLAYER.get())));
		
		this.playerInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 10, screenArea.y + 104, screenArea.width - 20, 20, Component.empty()));
		this.playerInput.visible = this.playerMode;
		
		this.teamSelection = this.addChild(new TeamSelectWidget(screenArea.pos.offset(10, 84), 2, Size.NORMAL, this::getTeamList, this::selectedTeam, this::SelectTeam));
		this.teamSelection.visible = !this.playerMode;
		
		this.buttonTransfer = this.addChild(new EasyTextButton(screenArea.pos.offset(10, 126), screenArea.width - 20, 20, () -> this.playerMode ? LCText.BUTTON_ATM_TRANSFER_PLAYER.get() : LCText.BUTTON_ATM_TRANSFER_TEAM.get(), this::PressTransfer));
		this.buttonTransfer.active = false;
		
	}
	
	private List<ITeam> getTeamList()
	{
		List<ITeam> results = Lists.newArrayList();
		BankReference source = this.screen.getMenu().getBankAccountReference();
		ITeam blockTeam = null;
		if(source instanceof TeamBankReference teamBankReference)
			blockTeam = TeamSaveData.GetTeam(true, teamBankReference.teamID);
		for(Team team : TeamSaveData.GetAllTeams(true))
		{
			if(team.hasBankAccount() && team != blockTeam)
				results.add(team);
		}
		return results;
	}
	
	public Team selectedTeam()
	{
		if(this.selectedTeam >= 0)
			return TeamSaveData.GetTeam(true, this.selectedTeam);
		return null;
	}
	
	public void SelectTeam(int teamIndex)
	{
		try {
			ITeam team = this.getTeamList().get(teamIndex);
			if(team.getID() == this.selectedTeam)
				return;
			this.selectedTeam = team.getID();
		} catch(Throwable ignored) { }
	}
	
	private void PressTransfer(EasyButton button)
	{
		if(this.playerMode)
		{
			new CPacketBankTransferPlayer(this.playerInput.getValue(), this.amountWidget.getCurrentValue()).send();
			this.playerInput.setValue("");
			this.amountWidget.changeValue(MoneyValue.empty());
		}
		else if(this.selectedTeam >= 0)
		{
			new CPacketBankTransferTeam(this.selectedTeam, this.amountWidget.getCurrentValue()).send();
			this.amountWidget.changeValue(MoneyValue.empty());
		}
	}

	private void ToggleMode(EasyButton button) {
		this.playerMode = !this.playerMode;
		this.teamSelection.visible = !this.playerMode;
		this.playerInput.visible = this.playerMode;
	}
	
	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {
		
		this.hideCoinSlots(gui);
		
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
	
	@Override
	public void tick() {
		
		if(this.playerMode)
			this.buttonTransfer.active = !this.playerInput.getValue().isBlank() && !this.amountWidget.getCurrentValue().isEmpty();
		else
		{
			Team team = this.selectedTeam();
			this.buttonTransfer.active = team != null && team.hasBankAccount() && !this.amountWidget.getCurrentValue().isEmpty();
		}
		
		
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
		EasySlot.SetActive(this.screen.getMenu());
		this.responseTimer = 0;
		this.screen.getMenu().clearMessage();
	}

	@Override
	public boolean blockInventoryClosing() { return this.playerMode; }
	
}
