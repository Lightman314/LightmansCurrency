package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TeamButton.Size;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageATMSetAccount;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Items;

public class SelectionTab extends ATMTab{

	public SelectionTab(ATMScreen screen) { super(screen); }

	Button buttonPersonalAccount;
	TeamSelectWidget teamSelection;
	
	@Override
	public IconData getIcon() { return IconData.of(Items.PAPER); }

	@Override
	public Component getTooltip() { return new TranslatableComponent("tooltip.lightmanscurrency.atm.selection"); }

	@Override
	public void init() {
		
		SimpleSlot.SetInactive(this.screen.getMenu());
		
		this.teamSelection = this.screen.addRenderableTabWidget(new TeamSelectWidget(this.screen.getGuiLeft() + 79, this.screen.getGuiTop() + 15, 5, Size.NARROW, this::getTeamList, this::selectedTeam, this::SelectTeam));
		this.teamSelection.init(this.screen::addRenderableTabWidget, this.screen.getFont());
		
		this.buttonPersonalAccount = this.screen.addRenderableTabWidget(new Button(this.screen.getGuiLeft() + 7, this.screen.getGuiTop() + 15, 70, 20, new TranslatableComponent("gui.button.bank.playeraccount"), this::PressPersonalAccount));
		this.buttonPersonalAccount.active = this.selectedTeam != null;
		
	}
	
	UUID selectedTeam = null;
	
	private List<Team> getTeamList()
	{
		List<Team> results = Lists.newArrayList();
		for(Team team : ClientTradingOffice.getTeamList())
		{
			if(team.hasBankAccount() && team.canAccessBankAccount(this.screen.getMenu().getPlayer()))
				results.add(team);
		}
		return results;
	}
	
	public Team selectedTeam()
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
			this.screen.getMenu().SetAccount(accountSource);
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageATMSetAccount(accountSource));
		} catch(Exception e) { }
	}
	
	private void PressPersonalAccount(Button button)
	{
		this.selectedTeam = null;
		AccountReference accountSource = BankAccount.GenerateReference(this.screen.getMenu().getPlayer());
		this.screen.getMenu().SetAccount(accountSource);
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageATMSetAccount(accountSource));
	}

	@Override
	public void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		RenderSystem.setShaderTexture(0, ATMScreen.GUI_TEXTURE);
		this.screen.blit(pose, this.screen.getGuiLeft() + 7, this.screen.getGuiTop() + 97, 7, 79, 162, 18);
		
		this.screen.getFont().draw(pose, this.getTooltip(), this.screen.getGuiLeft() + 8f, this.screen.getGuiTop() + 6f, 0x404040);
		
	}

	@Override
	public void postRender(PoseStack pose, int mouseX, int mouseY) { }
	
	@Override
	public void tick() {
		this.buttonPersonalAccount.active = this.selectedTeam != null;
	}

	@Override
	public void onClose() {
		SimpleSlot.SetActive(this.screen.getMenu());
	}

}
