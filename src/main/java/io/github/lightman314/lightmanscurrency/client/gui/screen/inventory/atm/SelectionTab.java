package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageATMSetAccount;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class SelectionTab extends ATMTab{

	public SelectionTab(ATMScreen screen) { super(screen); }

	Button buttonPersonalAccount;
	TeamSelectWidget teamSelection;
	
	@Override
	public IconData getIcon() { return IconData.of(Items.WRITABLE_BOOK); }

	@Override
	public ITextComponent getTooltip() { return new TranslationTextComponent("tooltip.lightmanscurrency.atm.selection"); }

	@Override
	public void init() {
		
		this.teamSelection = this.screen.addRenderableTabWidget(new TeamSelectWidget(this.screen.getGuiLeft() + 79, this.screen.getGuiTop() + 13, 4, true, this::getTeamList, this::selectedTeam, this::SelectTeam));
		this.teamSelection.init(this.screen::addRenderableTabWidget, this.screen.getFont());
		
		this.buttonPersonalAccount = this.screen.addRenderableTabWidget(new Button(this.screen.getGuiLeft() + 7, this.screen.getGuiTop() + 15, 70, 20, new TranslationTextComponent("gui.button.bank.playeraccount"), this::PressPersonalAccount));
		this.buttonPersonalAccount.active = this.selectedTeam != null;
		
	}
	
	UUID selectedTeam = null;
	
	private List<Team> getTeamList()
	{
		List<Team> results = Lists.newArrayList();
		for(Team team : ClientTradingOffice.getTeamList())
		{
			if(team.hasBankAccount() && team.canAccessBankAccount(this.screen.getContainer().getPlayer()))
				results.add(team);
		}
		return results;
	}
	
	private Team selectedTeam()
	{
		if(this.selectedTeam != null)
			return ClientTradingOffice.getTeam(this.selectedTeam);
		return null;
	}
	
	public void SelectTeam(int teamIndex)
	{
		try {
			Team team = this.getTeamList().get(teamIndex);
			if(team.getID().equals(this.selectedTeam))
				return;
			this.selectedTeam = team.getID();
			AccountReference accountSource = BankAccount.GenerateReference(true, team);
			this.screen.getContainer().SetAccount(accountSource);
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageATMSetAccount(accountSource));
		} catch(Exception e) { }
	}
	
	private void PressPersonalAccount(Button button)
	{
		this.selectedTeam = null;
		AccountReference accountSource = BankAccount.GenerateReference(this.screen.getContainer().getPlayer());
		this.screen.getContainer().SetAccount(accountSource);
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageATMSetAccount(accountSource));
	}

	@Override
	public void preRender(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) { }

	@Override
	public void postRender(MatrixStack matrix, int mouseX, int mouseY) { }
	
	@Override
	public void tick() {
		this.buttonPersonalAccount.active = this.selectedTeam != null;
	}

	@Override
	public void onClose() { }

}
