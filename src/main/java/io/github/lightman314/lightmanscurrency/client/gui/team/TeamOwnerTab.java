package io.github.lightman314.lightmanscurrency.client.gui.team;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageDisbandTeam;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageEditTeam;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class TeamOwnerTab extends TeamTab{

	public static final TeamOwnerTab INSTANCE = new TeamOwnerTab();
	
	private TeamOwnerTab() { }
	
	@Override
	public IconData getIcon() {
		return IconData.of(Items.WRITABLE_BOOK);
	}

	@Override
	public ITextComponent getTooltip() {
		return new TranslationTextComponent("tooltip.lightmanscurrency.team.owner");
	}

	@Override
	public boolean allowViewing(PlayerEntity player, Team team) {
		return team != null && team.isOwner(player);
	}
	
	TextFieldWidget newOwnerName;
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
		
		this.newOwnerName = screen.addRenderableTabWidget(new TextFieldWidget(this.getFont(), screen.guiLeft() + 20, screen.guiTop() + 20, 160, 20, new StringTextComponent("")));
		this.newOwnerName.setMaxStringLength(16);
		
		this.buttonChangeOwner = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 20, screen.guiTop() + 45, 160, 20, new TranslationTextComponent("gui.button.lightmanscurrency.set_owner"), this::setNewOwner));
		this.buttonChangeOwner.active = false;
		
		this.buttonDisbandTeam = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 20, screen.guiTop() + 160, 160, 20, new TranslationTextComponent("gui.button.lightmanscurrency.team.disband"), this::disbandTeam));
		
	}

	@Override
	public void preRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
		if(this.getActiveTeam() == null)
			return;
		
		TeamManagerScreen screen = this.getScreen();
		
		this.getFont().drawString(pose, new TranslationTextComponent("gui.button.lightmanscurrency.team.owner", this.getActiveTeam().getOwner().lastKnownName()).getString(), screen.guiLeft() + 20, screen.guiTop() + 10, 0x404040);
		
	}

	@Override
	public void postRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
		if(this.buttonChangeOwner.isMouseOver(mouseX, mouseY) || this.buttonDisbandTeam.isMouseOver(mouseX, mouseY))
		{
			this.getScreen().renderTooltip(pose, new TranslationTextComponent("tooltip.lightmanscurrency.warning").mergeStyle(TextFormatting.BOLD, TextFormatting.YELLOW), mouseX, mouseY);
		}
		
	}

	@Override
	public void tick() {
		
		this.newOwnerName.tick();
		this.buttonChangeOwner.active = !this.newOwnerName.getText().isEmpty();
		
	}

	@Override
	public void closeTab() {
		
	}
	
	private void setNewOwner(Button button)
	{
		if(this.newOwnerName.getText().isEmpty() || this.getActiveTeam() == null)
			return;
		
		Team team = this.getActiveTeam();
		team.changeOwner(this.getPlayer(), this.newOwnerName.getText());
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageEditTeam(team.getID(), this.newOwnerName.getText(), Team.CATEGORY_OWNER));
		this.newOwnerName.setText("");
		
	}
	
	private void disbandTeam(Button button)
	{
		if(this.getActiveTeam() == null)
			return;
		
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageDisbandTeam(this.getActiveTeam().getID()));
	}

}
