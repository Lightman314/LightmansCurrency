package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.menus.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.menus.traderinterface.base.OwnershipTab;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

public class OwnershipClientTab extends TraderInterfaceClientTab<OwnershipTab>{

	public OwnershipClientTab(TraderInterfaceScreen screen, OwnershipTab tab) { super(screen, tab); }
	
	@Override
	public IconData getIcon() { return IconData.of(Items.PLAYER_HEAD); }

	@Override
	public MutableComponent getTooltip() { return Component.translatable("tooltip.lightmanscurrency.settings.owner"); }
	
	@Override
	public boolean blockInventoryClosing() { return true; }
	
	EditBox newOwnerInput;
	Button buttonSetOwner;
	TeamSelectWidget teamSelection;
	Button buttonSetTeamOwner;
	
	UUID selectedTeam = null;
	List<Team> teamList = Lists.newArrayList();
	
	IconButton buttonToggleMode;
	
	boolean playerMode = true;
	
	@Override
	public void onOpen() {
		
		this.newOwnerInput = this.screen.addRenderableTabWidget(new EditBox(this.font, this.screen.getGuiLeft() + 23, this.screen.getGuiTop() + 26, 160, 20, Component.empty()));
		this.newOwnerInput.setMaxLength(16);
		
		this.buttonSetOwner = this.screen.addRenderableTabWidget(new Button(this.screen.getGuiLeft() + 23, this.screen.getGuiTop() + 47, 160, 20, Component.translatable("gui.button.lightmanscurrency.set_owner"), this::setOwner));
		this.buttonSetOwner.active = false;
		
		this.teamSelection = this.screen.addRenderableTabWidget(new TeamSelectWidget(this.screen.getGuiLeft() + 13, this.screen.getGuiTop() + 25, 4, () -> this.teamList, this::getSelectedTeam, this::selectTeam));
		this.teamSelection.init(screen::addRenderableTabWidget, this.font);
		
		this.buttonSetTeamOwner = this.screen.addRenderableTabWidget(new Button(this.screen.getGuiLeft() + 23, this.screen.getGuiTop() + 117, 160, 20, Component.translatable("gui.button.lightmanscurrency.set_owner"), this::setTeamOwner));
		this.buttonSetTeamOwner.active = false;
		
		this.buttonToggleMode = this.screen.addRenderableTabWidget(new IconButton(this.screen.getGuiLeft() + this.screen.getXSize() - IconButton.SIZE - 3, this.screen.getGuiTop() + 3, this::toggleMode, this::getModeIcon, new IconAndButtonUtil.ToggleTooltip(() -> this.playerMode, Component.translatable("tooltip.lightmanscurrency.settings.owner.player"), Component.translatable("tooltip.lightmanscurrency.settings.owner.team"))));
		
		this.tick();
		
	}
	
	private IconData getModeIcon() { return this.playerMode ? IconData.of(Items.PLAYER_HEAD) : IconData.of(Items.WRITABLE_BOOK); }
	
	private Team getTeam(int teamIndex)
	{
		if(teamIndex < this.teamList.size())
			return this.teamList.get(teamIndex);
		return null;
	}
	
	private Team getSelectedTeam()
	{
		if(this.selectedTeam == null)
			return null;
		return ClientTradingOffice.getTeam(this.selectedTeam);
	}
	
	private void refreshTeamList()
	{
		this.teamList = Lists.newArrayList();
		List<Team> allTeams = ClientTradingOffice.getTeamList();
		allTeams.forEach(team ->{
			if(team.isMember(this.menu.player))
				this.teamList.add(team);
		});
		this.teamList.sort(Team.sorterFor(this.menu.player));
	}

	@Override
	public void renderBG(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		if(this.menu.getBE() == null)
			return;
		
		this.font.draw(pose, TextRenderUtil.fitString(Component.translatable("gui.button.lightmanscurrency.team.owner", this.menu.getBE().getOwnerName()), this.screen.getXSize() - 20), this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 10, 0x404040);
		
	}
	
	public boolean changeInTrades() {
		TradeData referencedTrade = this.menu.getBE().getReferencedTrade();
		TradeData trueTrade = this.menu.getBE().getTrueTrade();
		if(referencedTrade == null)
			return false;
		if(trueTrade == null)
			return true;
		return !referencedTrade.compare(trueTrade).Identical();
	}

	@Override
	public void renderTooltips(PoseStack pose, int mouseX, int mouseY) {
		
		if(this.menu.getBE() == null)
			return;
		
		//Render button tooltips
		if(this.buttonSetOwner.isMouseOver(mouseX, mouseY) || this.buttonSetTeamOwner.isMouseOver(mouseX, mouseY))
		{
			this.screen.renderTooltip(pose, Component.translatable("tooltip.lightmanscurrency.warning").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW), mouseX, mouseY);
		}
		
		IconAndButtonUtil.renderButtonTooltips(pose, mouseX, mouseY, Lists.newArrayList(this.buttonToggleMode));
		
	}
	
	private void toggleMode(Button button) {
		this.playerMode = !this.playerMode;
	}
	
	private void setOwner(Button button)
	{
		if(this.newOwnerInput.getValue().isBlank())
			return;
		this.commonTab.setNewOwner(this.newOwnerInput.getValue());
		this.newOwnerInput.setValue("");
	}
	
	private void setTeamOwner(Button button) {
		if(this.selectedTeam == null)
			return;
		this.commonTab.setNewTeam(this.selectedTeam);
		this.selectedTeam = null;
	}
	
	private void selectTeam(int teamIndex)
	{
		Team newTeam = this.getTeam(teamIndex);
		if(newTeam != null)
		{
			if(newTeam.getID().equals(this.selectedTeam))
				this.selectedTeam = null;
			else
				this.selectedTeam = newTeam.getID();
		}
	}
	
	@Override
	public void tick() {
		this.refreshTeamList();
		
		this.newOwnerInput.tick();
		
		this.buttonSetOwner.visible = this.newOwnerInput.visible = this.playerMode;
		this.buttonSetTeamOwner.visible = this.teamSelection.visible = !this.playerMode;
		
		this.buttonSetOwner.active = !this.newOwnerInput.getValue().isBlank();
		this.buttonSetTeamOwner.active = this.getSelectedTeam() != null;
	}

	@Override
	public void onClose() {
		//Reset the selected team & team list to save space
		this.selectedTeam = null;
		this.teamList = Lists.newArrayList();
	}

}
