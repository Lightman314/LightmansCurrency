package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.walletbank;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.WalletBankScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TeamButton.Size;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageSelectBankAccount;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class SelectionTab extends WalletBankTab {

	public SelectionTab(WalletBankScreen screen) { super(screen); }
	
	EasyButton buttonPersonalAccount;
	TeamSelectWidget teamSelection;
	
	@Override
	public @NotNull IconData getIcon() { return IconData.of(Items.PAPER); }

	@Override
	public MutableComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.atm.selection"); }
	
	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		
		this.teamSelection = this.addChild(new TeamSelectWidget(screenArea.pos.offset(79, 15), 5, Size.NARROW, this::getTeamList, this::selectedTeam, this::SelectTeam));
		
		this.buttonPersonalAccount = this.addChild(new EasyTextButton(screenArea.pos.offset(7, 15), 70, 20, EasyText.translatable("gui.button.bank.playeraccount"), this::PressPersonalAccount));
		
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
			if(selectedTeam != null && team.getID() == this.selectedTeam().getID())
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
	
	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {

		gui.drawString(this.getTooltip(), 8, 6, 0x404040);
		
	}
	
	@Override
	public void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
		//Render text in front of selection background
		if(this.getTeamList().size() == 0)
		{
			gui.pushOffset(this.teamSelection);
			TextRenderUtil.drawVerticallyCenteredMultilineText(gui, EasyText.translatable("gui.lightmanscurrency.bank.noteamsavailable"), 1, Size.NARROW.width - 2, 1, this.teamSelection.getHeight() - 2, 0xFFFFFF);
			gui.popOffset();
		}
	}
	
	@Override
	public void tick() { this.buttonPersonalAccount.active = !this.isSelfSelected(); }
	
}
