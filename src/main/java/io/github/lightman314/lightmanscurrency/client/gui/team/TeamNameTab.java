package io.github.lightman314.lightmanscurrency.client.gui.team;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageRenameTeam;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

public class TeamNameTab extends TeamTab {

	public static final TeamNameTab INSTANCE = new TeamNameTab();
	
	private TeamNameTab() { }
	
	@Nonnull
    @Override
	public IconData getIcon() {
		return IconData.of(new TranslatableComponent("gui.button.lightmanscurrency.changename"));
	}

	@Override
	public Component getTooltip() {
		return new TranslatableComponent("tooltip.lightmanscurrency.team.name");
	}

	@Override
	public boolean allowViewing(Player player, Team team) {
		return team != null && team.isAdmin(player);
	}

	EditBox nameInput;
	Button buttonChangeName;
	
	@Override
	public void initTab() {
		
		TeamManagerScreen screen = this.getScreen();
		
		this.nameInput = screen.addRenderableTabWidget(new EditBox(this.getFont(), screen.guiLeft() + 20, screen.guiTop() + 20, 160, 20, new TextComponent("")));
		this.nameInput.setMaxLength(Team.MAX_NAME_LENGTH);
		this.nameInput.setValue(this.getActiveTeam().getName());
		
		this.buttonChangeName = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 20, screen.guiTop() + 45, 160, 20, new TranslatableComponent("gui.button.lightmanscurrency.team.rename"), this::changeName));
		this.buttonChangeName.active = false;
	}

	@Override
	public void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		TeamManagerScreen screen = this.getScreen();
		
		String currentName = "NULL";
		if(this.getActiveTeam() != null)
			currentName = this.getActiveTeam().getName();
		this.getFont().draw(pose, new TranslatableComponent("gui.lightmanscurrency.team.name.current", currentName), screen.guiLeft() + 20, screen.guiTop() + 10, 0x404040);
		
	}

	@Override
	public void postRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
	}

	@Override
	public void tick() {
		
		this.nameInput.tick();
		this.buttonChangeName.active = !this.nameInput.getValue().isBlank() && !this.nameInput.getValue().contentEquals(this.getActiveTeam().getName());
		
	}

	@Override
	public void closeTab() {
		
	}

	private void changeName(Button button)
	{
		if(this.nameInput.getValue().isBlank() || this.getActiveTeam() == null)
			return;
		
		this.getActiveTeam().changeName(this.getPlayer(), this.nameInput.getValue());
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageRenameTeam(this.getActiveTeam().getID(), this.nameInput.getValue()));
		
	}
	
}
