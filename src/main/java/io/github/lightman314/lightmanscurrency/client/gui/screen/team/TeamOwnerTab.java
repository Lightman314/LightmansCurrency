package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
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
	public MutableComponent getTooltip() { return LCText.TOOLTIP_TEAM_OWNER.get(); }

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
		
		this.buttonChangeOwner = this.addChild(new EasyTextButton(screenArea.pos.offset(20, 45), 160, 20, LCText.BUTTON_OWNER_SET_PLAYER.get(), this::setNewOwner)
				.withAddons(EasyAddonHelper.tooltip(LCText.TOOLTIP_WARNING_CANT_BE_UNDONE.getWithStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW))));
		this.buttonChangeOwner.active = false;
		
		this.buttonDisbandTeam = this.addChild(new EasyTextButton(screenArea.pos.offset(20, 160),160, 20, LCText.BUTTON_TEAM_DISBAND.get(), this::disbandTeam)
				.withAddons(EasyAddonHelper.tooltip(LCText.TOOLTIP_WARNING_CANT_BE_UNDONE.getWithStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW))));
		
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {

		Team team = this.getActiveTeam();
		if(team == null)
			return;

		gui.drawString(LCText.GUI_OWNER_CURRENT.get(team.getOwner().getName(true)), 20, 10, 0x404040);

		TextRenderUtil.drawCenteredText(gui, LCText.GUI_TEAM_ID.get(team.getID()), this.screen.getXSize() / 2, 184, 0x404040);

	}

	@Override
	public void tick() {

		this.buttonChangeOwner.active = !this.newOwnerName.getValue().isBlank();
		
	}
	
	private void setNewOwner(EasyButton button)
	{
		if(this.newOwnerName.getValue().isBlank() || this.getActiveTeam() == null)
			return;
		
		Team team = this.getActiveTeam();
		team.changeOwner(this.getPlayer(), this.newOwnerName.getValue());
		this.RequestChange(this.builder().setString("ChangeOwner", this.newOwnerName.getValue()));
		this.newOwnerName.setValue("");
		
	}
	
	private void disbandTeam(EasyButton button)
	{
		if(this.getActiveTeam() == null)
			return;

		this.RequestChange(this.builder().setFlag("Disband"));
	}

}
