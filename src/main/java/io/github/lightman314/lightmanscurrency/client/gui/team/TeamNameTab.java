package io.github.lightman314.lightmanscurrency.client.gui.team;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageRenameTeam;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TeamNameTab extends TeamTab {

	public static final TeamNameTab INSTANCE = new TeamNameTab();
	
	private TeamNameTab() { }
	
	@Override
	public IconData getIcon() {
		return IconData.of(new TranslationTextComponent("gui.button.lightmanscurrency.changename"));
	}

	@Override
	public ITextComponent getTooltip() {
		return new TranslationTextComponent("tooltip.lightmanscurrency.team.name");
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
		
		this.nameInput = screen.addRenderableTabWidget(new TextFieldWidget(this.getFont(), screen.guiLeft() + 20, screen.guiTop() + 20, 160, 20, new StringTextComponent("")));
		this.nameInput.setMaxStringLength(Team.MAX_NAME_LENGTH);
		this.nameInput.setText(this.getActiveTeam().getName());
		
		this.buttonChangeName = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 20, screen.guiTop() + 45, 160, 20, new TranslationTextComponent("gui.button.lightmanscurrency.team.rename"), this::changeName));
		this.buttonChangeName.active = false;
	}

	@Override
	public void preRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
		TeamManagerScreen screen = this.getScreen();
		
		String currentName = "NULL";
		if(this.getActiveTeam() != null)
			currentName = this.getActiveTeam().getName();
		this.getFont().drawString(pose, new TranslationTextComponent("gui.lightmanscurrency.team.name.current", currentName).getString(), screen.guiLeft() + 20, screen.guiTop() + 10, 0x404040);
		
	}

	@Override
	public void postRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
	}

	@Override
	public void tick() {
		
		this.nameInput.tick();
		this.buttonChangeName.active = !this.nameInput.getText().isEmpty() && !this.nameInput.getText().contentEquals(this.getActiveTeam().getName());
		
	}

	@Override
	public void closeTab() {
		
	}

	private void changeName(Button button)
	{
		if(this.nameInput.getText().isEmpty() || this.getActiveTeam() == null)
			return;
		
		this.getActiveTeam().changeName(this.getPlayer(), this.nameInput.getText());
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageRenameTeam(this.getActiveTeam().getID(), this.nameInput.getText()));
		
	}
	
}
