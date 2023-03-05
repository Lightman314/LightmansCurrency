package io.github.lightman314.lightmanscurrency.client.gui.team;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageDisbandTeam;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageEditTeam;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class TeamOwnerTab extends TeamTab{

	public static final TeamOwnerTab INSTANCE = new TeamOwnerTab();
	
	private TeamOwnerTab() { }
	
	@Override
	public @NotNull IconData getIcon() { return IconData.of(Items.WRITABLE_BOOK); }

	@Override
	public MutableComponent getTooltip() { return Component.translatable("tooltip.lightmanscurrency.team.owner"); }

	@Override
	public boolean allowViewing(Player player, Team team) {
		return team != null && team.isOwner(player);
	}
	
	EditBox newOwnerName;
	Button buttonChangeOwner;
	
	Button buttonDisbandTeam;

	@Override
	public void initTab() {
		
		if(this.getActiveTeam() == null)
		{
			this.getScreen().changeTab(0);
			return;
		}	
		
		TeamManagerScreen screen = this.getScreen();
		
		this.newOwnerName = screen.addRenderableTabWidget(new EditBox(this.getFont(), screen.guiLeft() + 20, screen.guiTop() + 20, 160, 20, Component.empty()));
		this.newOwnerName.setMaxLength(16);
		
		this.buttonChangeOwner = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 20, screen.guiTop() + 45, 160, 20, Component.translatable("gui.button.lightmanscurrency.set_owner"), this::setNewOwner));
		this.buttonChangeOwner.active = false;
		
		this.buttonDisbandTeam = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 20, screen.guiTop() + 160, 160, 20, Component.translatable("gui.button.lightmanscurrency.team.disband"), this::disbandTeam));
		
	}

	@Override
	public void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		if(this.getActiveTeam() == null)
			return;
		
		TeamManagerScreen screen = this.getScreen();
		
		this.getFont().draw(pose, Component.translatable("gui.button.lightmanscurrency.team.owner", this.getActiveTeam().getOwner().getName(true)), screen.guiLeft() + 20, screen.guiTop() + 10, 0x404040);
		
	}

	@Override
	public void postRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		if(this.buttonChangeOwner.isMouseOver(mouseX, mouseY) || this.buttonDisbandTeam.isMouseOver(mouseX, mouseY))
		{
			this.getScreen().renderTooltip(pose, Component.translatable("tooltip.lightmanscurrency.warning").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW), mouseX, mouseY);
		}
		
	}

	@Override
	public void tick() {
		
		this.newOwnerName.tick();
		this.buttonChangeOwner.active = !this.newOwnerName.getValue().isBlank();
		
	}

	@Override
	public void closeTab() {
		
	}
	
	private void setNewOwner(Button button)
	{
		if(this.newOwnerName.getValue().isBlank() || this.getActiveTeam() == null)
			return;
		
		Team team = this.getActiveTeam();
		team.changeOwner(this.getPlayer(), this.newOwnerName.getValue());
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageEditTeam(team.getID(), this.newOwnerName.getValue(), Team.CATEGORY_OWNER));
		this.newOwnerName.setValue("");
		
	}
	
	private void disbandTeam(Button button)
	{
		if(this.getActiveTeam() == null)
			return;
		
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageDisbandTeam(this.getActiveTeam().getID()));
	}

}
