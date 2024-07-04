package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
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
	public MutableComponent getTooltip() { return LCText.TOOLTIP_TEAM_BANK.get(); }

	@Override
	public boolean allowViewing(Player player, Team team) { return team != null && team.isOwner(player); }

	EasyButton buttonCreateBankAccount;
	EasyButton buttonToggleAccountLimit;
	
	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		
		this.buttonCreateBankAccount = this.addChild(new EasyTextButton(screenArea.pos.offset(20, 20), 160, 20, LCText.BUTTON_TEAM_BANK_CREATE.get(), this::createBankAccount));
		
		this.buttonToggleAccountLimit = this.addChild(new EasyTextButton(screenArea.pos.offset(20, 60), 160, 20, EasyText.empty(), this::toggleBankLimit));
		this.updateBankLimitText();
		
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {
		
		if(this.getActiveTeam() == null)
			return;

		if(this.getActiveTeam() != null && this.getActiveTeam().hasBankAccount())
		{
			IBankAccount account = this.getActiveTeam().getBankAccount();
			if(account != null)
				gui.drawString(account.getBalanceText(), 20, 46, 0x404040);
		}

		
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
		this.RequestChange(this.builder().setFlag("CreateBankAccount"));
		
	}
	
	private void toggleBankLimit(EasyButton button)
	{
		if(this.getActiveTeam() == null || !this.getActiveTeam().isOwner(this.getPlayer()))
			return;
		
		int newLimit = Team.NextBankLimit(this.getActiveTeam().getBankLimit());
		this.getActiveTeam().changeBankLimit(this.getPlayer(), newLimit);
		this.RequestChange(this.builder().setInt("ChangeBankLimit", newLimit));
		
		this.updateBankLimitText();
		
	}
	
	private void updateBankLimitText()
	{
		Component message = LCText.BUTTON_TEAM_BANK_LIMIT.get(Owner.getOwnerLevelBlurb(this.getActiveTeam().getBankLimit()));
		this.buttonToggleAccountLimit.setMessage(message);
	}
	
}
