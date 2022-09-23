package io.github.lightman314.lightmanscurrency.client.gui.team;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.teams.MessageEditTeam;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

public class TeamMemberEditTab extends TeamTab {

	public static final TeamMemberEditTab INSTANCE = new TeamMemberEditTab();
	
	private TeamMemberEditTab() { }
	
	@Override
	public IconData getIcon() { return IconData.of(ItemRenderUtil.getAlexHead()); }

	@Override
	public MutableComponent getTooltip() { return Component.translatable("tooltip.lightmanscurrency.team.member_edit"); }

	@Override
	public boolean allowViewing(Player player, Team team) {
		return team != null;
	}
	
	ScrollTextDisplay memberDisplay;
	
	EditBox memberNameInput;
	Button buttonAddMember;
	Button buttonPromoteMember;
	Button buttonRemoveMember;

	@Override
	public void initTab() {
		TeamManagerScreen screen = this.getScreen();
		
		this.memberNameInput = screen.addRenderableTabWidget(new EditBox(this.getFont(), screen.guiLeft() + 11, screen.guiTop() + 9, 178, 20, Component.empty()));
		this.memberNameInput.setMaxLength(16);
		
		this.buttonAddMember = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 10, screen.guiTop() + 30, 60, 20, Component.translatable("gui.button.lightmanscurrency.team.member.add"), this::addMember));
		this.buttonPromoteMember = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 70, screen.guiTop() + 30, 60, 20, Component.translatable("gui.button.lightmanscurrency.team.member.promote"), this::addAdmin));
		this.buttonRemoveMember = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 130, screen.guiTop() + 30, 60, 20, Component.translatable("gui.button.lightmanscurrency.team.member.remove"), this::removeMember));
		this.buttonAddMember.active = this.buttonPromoteMember.active = this.buttonRemoveMember.active = false;
		
		this.memberDisplay = screen.addRenderableTabWidget(new ScrollTextDisplay(screen.guiLeft() + 10, screen.guiTop() + 55, screen.xSize - 20, screen.ySize - 65, this.getFont(), this::getMemberList));
		this.memberDisplay.setColumnCount(2);
		
	}
	
	private List<Component> getMemberList()
	{
		List<Component> list = Lists.newArrayList();
		Team team = this.getActiveTeam();
		if(team != null)
		{
			//Do NOT List Owner
			//list.add(new TextComponent(team.getOwner().lastKnownName()).withStyle(ChatFormatting.GREEN));
			//List Admins
			team.getAdmins().forEach(admin -> list.add(admin.getNameComponent(true).withStyle(ChatFormatting.DARK_GREEN)));
			//List members
			team.getMembers().forEach(member -> list.add(member.getNameComponent(true)));
		}
		
		return list;
	}

	@Override
	public void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
	}

	@Override
	public void postRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
	}

	@Override
	public void tick() {
		
		if(this.getActiveTeam().isAdmin(this.getPlayer()))
		{
			this.buttonAddMember.active = this.buttonPromoteMember.active = this.buttonRemoveMember.active = !this.memberNameInput.getValue().isBlank();
		}
		else
		{
			this.buttonAddMember.active = this.buttonPromoteMember.active = false;
			this.buttonRemoveMember.active = this.getPlayer().getGameProfile().getName().toLowerCase().equals(this.memberNameInput.getValue().toLowerCase());
		}
		
	}

	@Override
	public void closeTab() {
		
	}
	
	private void addMember(Button button)
	{
		if(this.memberNameInput.getValue().isBlank() || this.getActiveTeam() == null)
			return;
		
		Team team = this.getActiveTeam();
		team.changeAddMember(this.getPlayer(), this.memberNameInput.getValue());
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageEditTeam(team.getID(), this.memberNameInput.getValue(), Team.CATEGORY_MEMBER));
		this.memberNameInput.setValue("");
		
	}
	
	private void addAdmin(Button button)
	{
		if(this.memberNameInput.getValue().isBlank() || this.getActiveTeam() == null)
			return;
		
		Team team = this.getActiveTeam();
		team.changeAddAdmin(this.getPlayer(), this.memberNameInput.getValue());
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageEditTeam(team.getID(), this.memberNameInput.getValue(), Team.CATEGORY_ADMIN));
		this.memberNameInput.setValue("");
	}
	
	private void removeMember(Button button)
	{
		if(this.memberNameInput.getValue().isBlank() || this.getActiveTeam() == null)
			return;
		
		Team team = this.getActiveTeam();
		team.changeRemoveMember(this.getPlayer(), this.memberNameInput.getValue());
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageEditTeam(team.getID(), this.memberNameInput.getValue(), Team.CATEGORY_REMOVE));
		this.memberNameInput.setValue("");
	}

}
