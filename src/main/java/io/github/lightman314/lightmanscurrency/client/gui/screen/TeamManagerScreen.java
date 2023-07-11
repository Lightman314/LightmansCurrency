package io.github.lightman314.lightmanscurrency.client.gui.screen;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyScreen;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.team.TeamBankAccountTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.team.TeamMemberEditTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.team.TeamMemberListTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.team.TeamNameTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.team.TeamOwnerTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.team.TeamSelectionTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.team.TeamTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class TeamManagerScreen extends EasyScreen {

	public static final ResourceLocation GUI_TEXTURE =  new ResourceLocation(LightmansCurrency.MODID, "textures/gui/teammanager.png");

	public TeamManagerScreen() { super(); this.resize(200, 200);}
	
	private long activeTeamID = -1;
	public Team getActiveTeam()
	{
		if(this.activeTeamID < 0)
			return null;
		Team team = TeamSaveData.GetTeam(true, this.activeTeamID);
		if(team != null && team.isMember(this.getPlayer()))
			return team;
		return null;
	}
	public void setActiveTeam(long teamID) { this.activeTeamID = teamID; }
	
	private final List<TeamTab> tabs = ImmutableList.of(new TeamSelectionTab(this), new TeamMemberListTab(this), new TeamNameTab(this), new TeamMemberEditTab(this), new TeamBankAccountTab(this), new TeamOwnerTab(this));
	public TeamTab currentTab() { return tabs.get(MathUtil.clamp(currentTabIndex, 0, this.tabs.size() - 1)); }
	List<TabButton> tabButtons = Lists.newArrayList();
	int currentTabIndex = 0;
	
	@Override
	protected void initialize(ScreenArea screenArea)
	{
		//Initialize the tab buttons
		for(TeamTab tab : this.tabs)
		{
			TabButton button = this.addChild(new TabButton(this::clickedOnTab, tab)).withAddons(
					EasyAddonHelper.activeCheck(() -> this.tabs.indexOf(tab) != this.currentTabIndex),
					EasyAddonHelper.visibleCheck(() -> tab.allowViewing(this.getPlayer(), this.getActiveTeam())));
			this.tabButtons.add(button);
		}
		this.positionTabButtons();
		
		//Initialize the starting tab
		this.currentTab().onOpen();
		
	}
	
	private ScreenPosition getTabPos(int index)
	{
		if(index < 8)
			return this.getCorner().offset(25 * index, -25);
		if(index < 16)
			return this.getCorner().offset(this.getXSize(), 25 * (index - 8));
		if(index < 24)
			return this.getCorner().offset(this.getXSize() - 25 * (index - 16), this.getYSize());
		return this.getCorner().offset(this.getGuiLeft() - 25, this.getYSize() - 25 * (index - 24));
	}
	
	private int getTabRotation(int index)
	{
		if(index < 8)
			return 0;
		if(index < 16)
			return 1;
		if(index < 24)
			return 2;
		return 3;
	}
	
	private void positionTabButtons()
	{
		int index = 0;
		for (TabButton thisButton : this.tabButtons) {
			if (thisButton.visible) {
				thisButton.reposition(this.getTabPos(index), this.getTabRotation(index));
				index++;
			}
		}
	}

	@Override
	protected void renderBG(@Nonnull EasyGuiGraphics gui) {

		gui.renderNormalBackground(GUI_TEXTURE, this);

		try{ this.currentTab().renderBG(gui);
		} catch(Throwable t) { t.printStackTrace(); }
	}

	@Override
	protected void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
		super.renderAfterWidgets(gui);

		try{ this.currentTab().renderAfterWidgets(gui);
		} catch(Throwable t) { t.printStackTrace(); }

	}
	
	@Override
	protected void screenTick()
	{
		if(this.activeTeamID < 0 && this.currentTabIndex != 0)
		{
			this.changeTab(0);
		}
		//Update the tabs visibility
		boolean updateTabs = false;
		for(int i = 0; i < this.tabs.size(); ++i)
		{
			boolean visible = this.tabs.get(i).allowViewing(this.getPlayer(), this.getActiveTeam());
			if(visible != this.tabButtons.get(i).visible)
			{
				updateTabs = true;
				this.tabButtons.get(i).visible = visible;
			}
		}
		if(updateTabs)
			this.positionTabButtons();
		
		if(!this.currentTab().allowViewing(this.getPlayer(), this.getActiveTeam()) && this.currentTabIndex != 0)
			this.changeTab(0);
		
		//Tick the current tab
		this.currentTab().tick();
	}
	
	public void changeTab(int tabIndex)
	{
		//Confirm that the tab index is valid
		if(tabIndex < 0 || tabIndex >= this.tabs.size())
			return;
		//Confirm that the new tab can be viewed
		if(!this.tabs.get(tabIndex).allowViewing(this.getPlayer(), this.getActiveTeam()))
			return;
		
		//Close the old tab
		this.currentTab().onClose();
		this.tabButtons.get(this.currentTabIndex).active = true;
		this.currentTabIndex = tabIndex;
		this.tabButtons.get(this.currentTabIndex).active = false;
		
		//Initialize the new tab
		this.currentTab().onOpen();
	}
	
	private void clickedOnTab(EasyButton tab)
	{
		int tabIndex = -1;
		if(tab instanceof TabButton)
			tabIndex = this.tabButtons.indexOf(tab);
		if(tabIndex < 0)
			return;
		this.changeTab(tabIndex);
	}

	@Override
	public boolean blockInventoryClosing() { return this.currentTab().blockInventoryClosing(); }

}
