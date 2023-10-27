package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

public class TeamBankAccountTab extends TeamTab {
	
	public TeamBankAccountTab(TeamManagerScreen screen) { super(screen); }
	
	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(ModBlocks.COINPILE_GOLD); }
	
	@Override
	public MutableComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.team.bank"); }

	@Override
	public boolean allowViewing(Player player, Team team) { return team != null && team.isOwner(player); }

	EasyButton buttonCreateBankAccount;
	EasyButton buttonToggleAccountLimit;
	
	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		
		this.buttonCreateBankAccount = this.addChild(new EasyTextButton(screenArea.pos.offset(20, 20), 160, 20, EasyText.translatable("gui.button.lightmanscurrency.team.bank.create"), this::createBankAccount));
		
		this.buttonToggleAccountLimit = this.addChild(new EasyTextButton(screenArea.pos.offset(20, 60), 160, 20, EasyText.empty(), this::toggleBankLimit));
		this.updateBankLimitText();
		
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {
		
		if(this.getActiveTeam() == null)
			return;

		if(this.getActiveTeam() != null && this.getActiveTeam().hasBankAccount())
			gui.drawString(EasyText.translatable("gui.lightmanscurrency.bank.balance", this.getActiveTeam().getBankAccount().getCoinStorage().getString("0")), 20, 46, 0x404040);
		
	}

	@Override
	public void tick() {
		
		if(this.getActiveTeam() == null)
			return;
		
		this.buttonCreateBankAccount.active = !this.getActiveTeam().hasBankAccount();
		
	}

	private void createBankAccount(EasyButton button)
	{
		if(this.getActiveTeam() == null || !this.getActiveTeam().isOwner(this.getPlayer()))
			return;
		
		this.getActiveTeam().createBankAccount(this.getPlayer());
		this.RequestChange(LazyPacketData.simpleFlag("CreateBankAccount"));
		
	}
	
	private void toggleBankLimit(EasyButton button)
	{
		if(this.getActiveTeam() == null || !this.getActiveTeam().isOwner(this.getPlayer()))
			return;
		
		int newLimit = Team.NextBankLimit(this.getActiveTeam().getBankLimit());
		this.getActiveTeam().changeBankLimit(this.getPlayer(), newLimit);
		this.RequestChange(LazyPacketData.simpleInt("ChangeBankLimit", newLimit));
		
		this.updateBankLimitText();
		
	}
	
	private void updateBankLimitText()
	{
		Component message = EasyText.translatable("gui.button.lightmanscurrency.team.bank.limit", EasyText.translatable("gui.button.lightmanscurrency.team.bank.limit." + this.getActiveTeam().getBankLimit()));
		this.buttonToggleAccountLimit.setMessage(message);
	}
	
}
