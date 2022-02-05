package io.github.lightman314.lightmanscurrency.client.gui.team;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageCreateTeamBankAccount;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageSetTeamBankLimit;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TeamBankAccountTab extends TeamTab {

	public static final TeamBankAccountTab INSTANCE = new TeamBankAccountTab();
	
	private TeamBankAccountTab() { }
	
	@Override
	public IconData getIcon() {
		return IconData.of(ModBlocks.COINPILE_GOLD);
	}

	@Override
	public ITextComponent getTooltip() {
		return new TranslationTextComponent("tooltip.lightmanscurrency.team.bank");
	}

	@Override
	public boolean allowViewing(PlayerEntity player, Team team) {
		return team != null && team.isOwner(player);
	}

	Button buttonCreateBankAccount;
	Button buttonToggleAccountLimit;
	
	
	@Override
	public void initTab() {
		
		TeamManagerScreen screen = this.getScreen();
		
		this.buttonCreateBankAccount = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 20, screen.guiTop() + 20, 160, 20, new TranslationTextComponent("gui.button.lightmanscurrency.team.bank.create"), this::createBankAccount));
		
		this.buttonToggleAccountLimit = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 20, screen.guiTop() + 60, 160, 20, new StringTextComponent(""), this::toggleBankLimit));
		this.updateBankLimitText();
		
	}

	@Override
	public void preRender(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
		
		if(this.getActiveTeam() == null)
			return;
		
		TeamManagerScreen screen = this.getScreen();
		if(this.getActiveTeam() != null && this.getActiveTeam().hasBankAccount())
			this.getFont().drawString(matrix, new TranslationTextComponent("gui.lightmanscurrency.bank.balance", this.getActiveTeam().getBankAccount().getCoinStorage().getString("0")).getString(), screen.guiLeft() + 20, screen.guiTop() + 100, 0x404040);
		
	}

	@Override
	public void postRender(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
		
	}

	@Override
	public void tick() {
		
		this.buttonCreateBankAccount.active = !this.getActiveTeam().hasBankAccount();
		
	}

	@Override
	public void closeTab() {
		
	}

	private void createBankAccount(Button button)
	{
		if(this.getActiveTeam() == null || !this.getActiveTeam().isOwner(this.getPlayer()))
			return;
		
		this.getActiveTeam().createBankAccount(this.getPlayer());
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCreateTeamBankAccount(this.getActiveTeam().getID()));
		
	}
	
	private void toggleBankLimit(Button button)
	{
		if(this.getActiveTeam() == null || !this.getActiveTeam().isOwner(this.getPlayer()))
			return;
		
		int newLimit = Team.NextBankLimit(this.getActiveTeam().getBankLimit());
		this.getActiveTeam().changeBankLimit(this.getPlayer(), newLimit);
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetTeamBankLimit(this.getActiveTeam().getID(), newLimit));
		
		this.updateBankLimitText();
		
	}
	
	private void updateBankLimitText()
	{
		ITextComponent message = new TranslationTextComponent("gui.button.lightmanscurrency.team.bank.limit", new TranslationTextComponent("gui.button.lightmanscurrency.team.bank.limit." + this.getActiveTeam().getBankLimit()));
		this.buttonToggleAccountLimit.setMessage(message);
	}
	
}
