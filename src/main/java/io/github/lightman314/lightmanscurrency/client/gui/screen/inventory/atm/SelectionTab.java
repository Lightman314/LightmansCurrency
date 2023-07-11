package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TeamButton.Size;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.common.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageSelectBankAccount;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageATMSetPlayerAccount;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;

public class SelectionTab extends ATMTab {

	public SelectionTab(ATMScreen screen) { super(screen); }

	EasyButton buttonPersonalAccount;
	TeamSelectWidget teamSelection;
	
	EasyButton buttonToggleAdminMode;
	
	EditBox playerAccountSelect;
	EasyButton buttonSelectPlayerAccount;
	MutableComponent responseMessage = EasyText.empty();
	
	boolean adminMode = false;
	
	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(Items.PAPER); }

	@Override
	public MutableComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.atm.selection"); }

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		
		this.adminMode = false;
		if(firstOpen)
			this.responseMessage = EasyText.empty();
		
		SimpleSlot.SetInactive(this.screen.getMenu());
		
		this.teamSelection = this.addChild(new TeamSelectWidget(screenArea.pos.offset(79,15), 6, Size.NARROW, this::getTeamList, this::selectedTeam, this::SelectTeam));
		
		this.buttonPersonalAccount = this.addChild(new EasyTextButton(screenArea.pos.offset(7, 15), 70, 20, EasyText.translatable("gui.button.bank.playeraccount"), this::PressPersonalAccount));
		
		this.buttonToggleAdminMode = this.addChild(new IconButton(screenArea.pos.offset(screenArea.width, 0), this::ToggleAdminMode, IconData.of(Items.COMMAND_BLOCK)));
		this.buttonToggleAdminMode.visible = CommandLCAdmin.isAdminPlayer(this.screen.getMenu().getPlayer());
		
		this.playerAccountSelect = this.addChild(new EditBox(this.screen.getFont(), screenArea.x + 7, screenArea.y + 20, 162, 20, EasyText.empty()));
		this.playerAccountSelect.visible = false;
		
		this.buttonSelectPlayerAccount = this.addChild(new EasyTextButton(screenArea.pos.offset(7, 45), 162, 20, EasyText.translatable("gui.button.bank.admin.playeraccount"), this::PressSelectPlayerAccount));
		this.buttonSelectPlayerAccount.visible = false;

		this.tick();

	}

	private boolean isTeamSelected() {
		return this.screen.getMenu().getBankAccountReference().accountType == BankAccount.AccountType.Team;
	}

	private boolean isSelfSelected() {
		return this.screen.getMenu().getBankAccount() == BankAccount.GenerateReference(this.screen.getMenu().getPlayer()).get();
	}
	
	private List<Team> getTeamList()
	{
		List<Team> results = Lists.newArrayList();
		for(Team team : TeamSaveData.GetAllTeams(true))
		{
			if(team.hasBankAccount() && team.canAccessBankAccount(this.screen.getMenu().getPlayer()))
				results.add(team);
		}
		return results;
	}
	
	public Team selectedTeam()
	{
		if(this.isTeamSelected())
			return TeamSaveData.GetTeam(true, this.screen.getMenu().getBankAccountReference().teamID);
		return null;	
	}
	
	public void SelectTeam(int teamIndex)
	{
		try {
			Team team = this.getTeamList().get(teamIndex);
			Team selectedTeam = this.selectedTeam();
			if(selectedTeam != null && team.getID() == selectedTeam.getID())
				return;
			AccountReference account = BankAccount.GenerateReference(true, team);
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSelectBankAccount(account));
		} catch(Throwable ignored) { }
	}
	
	private void PressPersonalAccount(EasyButton button)
	{
		AccountReference account = BankAccount.GenerateReference(this.screen.getMenu().getPlayer());
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSelectBankAccount(account));
	}
	
	private void ToggleAdminMode(EasyButton button) {
		this.adminMode = !this.adminMode;
		this.buttonPersonalAccount.visible = !this.adminMode;
		this.teamSelection.visible = !this.adminMode;
		
		this.buttonSelectPlayerAccount.visible = this.adminMode;
		this.playerAccountSelect.visible = this.adminMode;
	}
	
	private void PressSelectPlayerAccount(EasyButton button) {
		String playerName = this.playerAccountSelect.getValue();
		this.playerAccountSelect.setValue("");
		if(!playerName.isBlank())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageATMSetPlayerAccount(playerName));
	}
	
	public void ReceiveSelectPlayerResponse(MutableComponent message) {
		this.responseMessage = message;
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {
		
		this.hideCoinSlots(gui);

		gui.drawString(this.getTooltip(), 8, 6, 0x404040);

		if(this.adminMode)
		{
			List<FormattedText> lines = this.screen.getFont().getSplitter().splitLines(this.responseMessage, this.screen.getXSize() - 15, Style.EMPTY);
			for(int i = 0; i < lines.size(); ++i)
				gui.drawString(lines.get(i).getString(), 7, 70 + (gui.font.lineHeight * i), 0x404040);
		}
		
	}

	@Override
	public void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
		//Render text in front of selection background
		if(this.getTeamList().size() == 0 && !this.adminMode)
		{
			gui.pushOffsetZero();
			TextRenderUtil.drawVerticallyCenteredMultilineText(gui, EasyText.translatable("gui.lightmanscurrency.bank.noteamsavailable"), this.teamSelection.getX() + 1, Size.NARROW.width - 2, this.teamSelection.getY() + 1, this.teamSelection.getHeight() - 2, 0xFFFFFF);
			gui.popOffset();
		}
	}
	
	@Override
	public void tick() {
		this.buttonPersonalAccount.active = !this.isSelfSelected();
		this.buttonToggleAdminMode.visible = CommandLCAdmin.isAdminPlayer(this.screen.getMenu().getPlayer());
		if(this.adminMode)
			this.playerAccountSelect.tick();
	}

	@Override
	public void closeAction() {
		SimpleSlot.SetActive(this.screen.getMenu());
	}

}
