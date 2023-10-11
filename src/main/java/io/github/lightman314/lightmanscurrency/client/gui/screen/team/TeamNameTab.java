package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class TeamNameTab extends TeamTab {
	
	public TeamNameTab(TeamManagerScreen screen) { super(screen); }
	
	@Override
	public @NotNull IconData getIcon() { return IconData.of(Component.translatable("gui.button.lightmanscurrency.changename")); }

	@Override
	public MutableComponent getTooltip() { return Component.translatable("tooltip.lightmanscurrency.team.name"); }

	@Override
	public boolean allowViewing(Player player, Team team) {
		return team != null && team.isAdmin(player);
	}

	EditBox nameInput;
	EasyButton buttonChangeName;

	@Override
	public boolean blockInventoryClosing() { return true; }

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		
		this.nameInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 20, screenArea.y + 20, 160, 20, Component.empty()));
		this.nameInput.setMaxLength(Team.MAX_NAME_LENGTH);
		this.nameInput.setValue(this.getActiveTeam().getName());
		
		this.buttonChangeName = this.addChild(new EasyTextButton(screenArea.pos.offset(20, 45), 160, 20, EasyText.translatable("gui.button.lightmanscurrency.team.rename"), this::changeName));
		this.buttonChangeName.active = false;
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {

		String currentName = "NULL";
		if(this.getActiveTeam() != null)
			currentName = this.getActiveTeam().getName();
		gui.drawString(EasyText.translatable("gui.lightmanscurrency.team.name.current", currentName), 20, 10, 0x404040);
		
	}

	@Override
	public void tick() {
		
		this.nameInput.tick();
		this.buttonChangeName.active = !this.nameInput.getValue().isBlank() && !this.nameInput.getValue().contentEquals(this.getActiveTeam().getName());
		
	}

	private void changeName(EasyButton button)
	{
		if(this.nameInput.getValue().isBlank() || this.getActiveTeam() == null)
			return;
		
		this.getActiveTeam().changeName(this.getPlayer(), this.nameInput.getValue());
		this.RequestChange(LazyPacketData.simpleString("ChangeName", this.nameInput.getValue()));
		
	}
	
}
