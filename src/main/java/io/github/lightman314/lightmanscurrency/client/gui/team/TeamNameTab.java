package io.github.lightman314.lightmanscurrency.client.gui.team;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageRenameTeam;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class TeamNameTab extends TeamTab {

	public static final TeamNameTab INSTANCE = new TeamNameTab();
	
	private TeamNameTab() { }
	
	@Nonnull
    @Override
	public IconData getIcon() {
		return IconData.of(EasyText.translatable("gui.button.lightmanscurrency.changename"));
	}

	@Override
	public ITextComponent getTooltip() {
		return EasyText.translatable("tooltip.lightmanscurrency.team.name");
	}

	@Override
	public boolean allowViewing(PlayerEntity player, Team team) {
		return team != null && team.isAdmin(player);
	}

	TextFieldWidget nameInput;
	Button buttonChangeName;
	
	@Override
	public void initTab() {
		
		TeamManagerScreen screen = this.getScreen();
		
		this.nameInput = screen.addRenderableTabWidget(new TextFieldWidget(this.getFont(), screen.guiLeft() + 20, screen.guiTop() + 20, 160, 20, EasyText.empty()));
		this.nameInput.setMaxLength(Team.MAX_NAME_LENGTH);
		this.nameInput.setValue(this.getActiveTeam().getName());
		
		this.buttonChangeName = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 20, screen.guiTop() + 45, 160, 20, EasyText.translatable("gui.button.lightmanscurrency.team.rename"), this::changeName));
		this.buttonChangeName.active = false;
	}

	@Override
	public void preRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
		TeamManagerScreen screen = this.getScreen();
		
		String currentName = "NULL";
		if(this.getActiveTeam() != null)
			currentName = this.getActiveTeam().getName();
		this.getFont().draw(pose, EasyText.translatable("gui.lightmanscurrency.team.name.current", currentName), screen.guiLeft() + 20, screen.guiTop() + 10, 0x404040);
		
	}

	@Override
	public void postRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
	}

	@Override
	public void tick() {
		
		this.nameInput.tick();
		this.buttonChangeName.active = !this.nameInput.getValue().isEmpty() && !this.nameInput.getValue().contentEquals(this.getActiveTeam().getName());
		
	}

	@Override
	public void closeTab() {
		
	}

	private void changeName(Button button)
	{
		if(this.nameInput.getValue().isEmpty() || this.getActiveTeam() == null)
			return;
		
		this.getActiveTeam().changeName(this.getPlayer(), this.nameInput.getValue());
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageRenameTeam(this.getActiveTeam().getID(), this.nameInput.getValue()));
		
	}
	
}
