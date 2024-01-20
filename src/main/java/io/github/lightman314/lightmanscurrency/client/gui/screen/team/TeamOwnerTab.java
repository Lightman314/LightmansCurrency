package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;

public class TeamOwnerTab extends TeamTab{
	
	public TeamOwnerTab(TeamManagerScreen screen) { super(screen); }
	
	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(Items.WRITABLE_BOOK); }

	@Override
	public MutableComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.team.owner"); }

	@Override
	public boolean allowViewing(Player player, Team team) { return team != null && team.isOwner(player); }
	
	EditBox newOwnerName;
	EasyButton buttonChangeOwner;

	EasyButton buttonDisbandTeam;

	@Override
	public boolean blockInventoryClosing() { return true; }

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		
		if(this.getActiveTeam() == null)
		{
			this.screen.changeTab(0);
			return;
		}
		
		this.newOwnerName = this.addChild(new EditBox(this.getFont(), screenArea.x + 20, screenArea.y + 20, 160, 20, EasyText.empty()));
		this.newOwnerName.setMaxLength(16);
		
		this.buttonChangeOwner = this.addChild(new EasyTextButton(screenArea.pos.offset(20, 45), 160, 20, EasyText.translatable("gui.button.lightmanscurrency.set_owner"), this::setNewOwner)
				.withAddons(EasyAddonHelper.tooltip(EasyText.translatable("tooltip.lightmanscurrency.warning").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW))));
		this.buttonChangeOwner.active = false;
		
		this.buttonDisbandTeam = this.addChild(new EasyTextButton(screenArea.pos.offset(20, 160),160, 20, EasyText.translatable("gui.button.lightmanscurrency.team.disband"), this::disbandTeam)
				.withAddons(EasyAddonHelper.tooltip(EasyText.translatable("tooltip.lightmanscurrency.warning").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW))));
		
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {
		
		if(this.getActiveTeam() == null)
			return;

		gui.drawString(EasyText.translatable("gui.button.lightmanscurrency.team.owner", this.getActiveTeam().getOwner().getName(true)), 20, 10, 0x404040);
		
	}

	@Override
	public void tick() {
		
		this.newOwnerName.tick();
		this.buttonChangeOwner.active = !this.newOwnerName.getValue().isBlank();
		
	}
	
	private void setNewOwner(EasyButton button)
	{
		if(this.newOwnerName.getValue().isBlank() || this.getActiveTeam() == null)
			return;
		
		Team team = this.getActiveTeam();
		team.changeOwner(this.getPlayer(), this.newOwnerName.getValue());
		this.RequestChange(LazyPacketData.simpleString("ChangeOwner", this.newOwnerName.getValue()));
		this.newOwnerName.setValue("");
		
	}
	
	private void disbandTeam(EasyButton button)
	{
		if(this.getActiveTeam() == null)
			return;

		this.RequestChange(LazyPacketData.simpleFlag("Disband"));
	}

}
