package io.github.lightman314.lightmanscurrency.client.gui.team;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class TeamMemberListTab extends TeamTab {

	public static final TeamMemberListTab INSTANCE = new TeamMemberListTab();
	
	private TeamMemberListTab() { }
	
	@Override
	public IconData getIcon() {
		return IconData.of(Items.PLAYER_HEAD);
	}

	@Override
	public ITextComponent getTooltip() {
		return new TranslationTextComponent("tooltip.lightmanscurrency.team.members");
	}

	@Override
	public boolean allowViewing(PlayerEntity player, Team team) {
		return team != null;
	}
	
	ScrollTextDisplay memberDisplay;

	@Override
	public void initTab() {
		TeamManagerScreen screen = this.getScreen();
		
		this.memberDisplay = screen.addRenderableTabWidget(new ScrollTextDisplay(screen.guiLeft() + 10, screen.guiTop() + 10, screen.xSize - 20, screen.ySize - 20, this.getFont(), this::getMemberList));
		this.memberDisplay.setColumnCount(2);
		
	}
	
	private List<ITextComponent> getMemberList()
	{
		List<ITextComponent> list = Lists.newArrayList();
		Team team = this.getActiveTeam();
		if(team != null)
		{
			//List Owner
			list.add(new StringTextComponent(team.getOwner().lastKnownName()).mergeStyle(TextFormatting.GREEN));
			//List Admins
			team.getAdmins().forEach(admin -> list.add(new StringTextComponent(admin.lastKnownName()).mergeStyle(TextFormatting.DARK_GREEN)));
			//List members
			team.getMembers().forEach(member -> list.add(new StringTextComponent(member.lastKnownName())));
		}
		
		return list;
	}

	@Override
	public void preRender(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
		
	}

	@Override
	public void postRender(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
		
	}

	@Override
	public void tick() {
		
	}

	@Override
	public void closeTab() {
		
	}

}
