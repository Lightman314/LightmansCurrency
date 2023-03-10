package io.github.lightman314.lightmanscurrency.client.gui.team;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageDisbandTeam;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageEditTeam;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;

public class TeamOwnerTab extends TeamTab{

	public static final TeamOwnerTab INSTANCE = new TeamOwnerTab();
	
	private TeamOwnerTab() { }
	
	@Nonnull
    @Override
	public IconData getIcon() {
		return IconData.of(Items.WRITABLE_BOOK);
	}

	@Override
	public ITextComponent getTooltip() {
		return EasyText.translatable("tooltip.lightmanscurrency.team.owner");
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
		
		this.newOwnerName = screen.addRenderableTabWidget(new TextFieldWidget(this.getFont(), screen.guiLeft() + 20, screen.guiTop() + 20, 160, 20, EasyText.empty()));
		this.newOwnerName.setMaxLength(16);
		
		this.buttonChangeOwner = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 20, screen.guiTop() + 45, 160, 20, EasyText.translatable("gui.button.lightmanscurrency.set_owner"), this::setNewOwner));
		this.buttonChangeOwner.active = false;
		
		this.buttonDisbandTeam = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 20, screen.guiTop() + 160, 160, 20, EasyText.translatable("gui.button.lightmanscurrency.team.disband"), this::disbandTeam));
		
	}

	@Override
	public void preRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
		if(this.getActiveTeam() == null)
			return;
		
		TeamManagerScreen screen = this.getScreen();
		
		this.getFont().draw(pose, EasyText.translatable("gui.button.lightmanscurrency.team.owner", this.getActiveTeam().getOwner().getName(true)), screen.guiLeft() + 20, screen.guiTop() + 10, 0x404040);
		
	}

	@Override
	public void postRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
		if(this.buttonChangeOwner.isMouseOver(mouseX, mouseY) || this.buttonDisbandTeam.isMouseOver(mouseX, mouseY))
		{
			this.getScreen().renderTooltip(pose, EasyText.translatable("tooltip.lightmanscurrency.warning").withStyle(TextFormatting.BOLD, TextFormatting.YELLOW), mouseX, mouseY);
		}
		
	}

	@Override
	public void tick() {
		
		this.newOwnerName.tick();
		this.buttonChangeOwner.active = !this.newOwnerName.getValue().isEmpty();
		
	}

	@Override
	public void closeTab() {
		
	}
	
	private void setNewOwner(Button button)
	{
		if(this.newOwnerName.getValue().isEmpty() || this.getActiveTeam() == null)
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
