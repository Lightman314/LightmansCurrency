package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.walletbank;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.WalletBankScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TeamButton.Size;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageSelectBankAccount;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Items;

public class SelectionTab extends WalletBankTab {

	public SelectionTab(WalletBankScreen screen) { super(screen); }
	
	Button buttonPersonalAccount;
	TeamSelectWidget teamSelection;
	
	@Override
	public IconData getIcon() { return IconData.of(Items.PAPER); }

	@Override
	public MutableComponent getTooltip() { return new TranslatableComponent("tooltip.lightmanscurrency.atm.selection"); }
	
	@Override
	public void init() {
		
		this.teamSelection = this.screen.addRenderableTabWidget(new TeamSelectWidget(this.screen.getGuiLeft() + 79, this.screen.getGuiTop() + 15, 5, Size.NARROW, this::getTeamList, this::selectedTeam, this::SelectTeam));
		this.teamSelection.init(this.screen::addRenderableTabWidget, this.screen.getFont());
		
		this.buttonPersonalAccount = this.screen.addRenderableTabWidget(new Button(this.screen.getGuiLeft() + 7, this.screen.getGuiTop() + 15, 70, 20, new TranslatableComponent("gui.button.bank.playeraccount"), this::PressPersonalAccount));
		
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
		} catch(Exception e) { }
	}
	
	private void PressPersonalAccount(Button button)
	{
		AccountReference account = BankAccount.GenerateReference(this.screen.getMenu().getPlayer());
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSelectBankAccount(account));
	}
	
	@Override
	public void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.screen.getFont().draw(pose, this.getTooltip(), this.screen.getGuiLeft() + 8f, this.screen.getGuiTop() + 6f, 0x404040);
		
	}
	
	@Override
	public void postRender(PoseStack pose, int mouseX, int mouseY) {
		//Render text in front of selection background
		if(this.getTeamList().size() == 0)
			TextRenderUtil.drawVerticallyCenteredMultilineText(pose, new TranslatableComponent("gui.lightmanscurrency.bank.noteamsavailable"), this.teamSelection.x + 1, Size.NARROW.width - 2, this.teamSelection.y + 1, this.teamSelection.getHeight() - 2, 0xFFFFFF);
	}
	
	@Override
	public void tick() {
		this.buttonPersonalAccount.active = !this.isSelfSelected();
	}
	
	@Override
	public void onClose() { }
	
}