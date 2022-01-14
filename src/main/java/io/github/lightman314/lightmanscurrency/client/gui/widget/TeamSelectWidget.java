package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TeamButton;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;

public class TeamSelectWidget extends AbstractWidget {

	private final int rows;
	private final Supplier<List<Team>> teamSource;
	private final Supplier<Team> selectedTeam;
	private final Consumer<Integer> onPress;
	private List<TeamButton> teamButtons = Lists.newArrayList();
	
	public TeamSelectWidget(int x, int y, int rows, Supplier<List<Team>> teamSource, Supplier<Team> selectedTeam, Consumer<Integer> onPress) {
		super(x, y, TeamButton.WIDTH, TeamButton.HEIGHT * rows, new TextComponent(""));
		this.rows = rows;
		this.teamSource = teamSource;
		this.selectedTeam = selectedTeam;
		this.onPress = onPress;
	}
	
	public void init(Consumer<Button> addButton, Font font)
	{
		for(int i = 0; i < this.rows; ++i)
		{
			int index = i;
			TeamButton button = new TeamButton(this.x, this.y + i * TeamButton.HEIGHT, this::onTeamSelect, font, () -> this.getTeam(index), () -> this.isSelected(index));
			this.teamButtons.add(button);
			addButton.accept(button);
		}
	}
	
	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks)
	{
		fill(pose, x, y, x + this.width, y + this.height, 0xFF000000);
	}

	private int scroll = 0;
	
	private Team getTeam(int index)
	{
		index += scroll;
		List<Team> teamList = teamSource.get();
		this.validateScroll(teamList.size());
		if(index >= 0 && index < teamList.size())
			return teamList.get(index);
		return null;
	}
	
	private boolean isSelected(int index)
	{
		Team team = getTeam(index);
		if(team == null)
			return false;
		return team == this.selectedTeam.get();
	}
	
	private void validateScroll(int teamListSize)
	{
		this.scroll = MathUtil.clamp(scroll, 0, teamListSize - this.teamButtons.size());
	}
	
	private int maxScroll()
	{
		return MathUtil.clamp(this.teamButtons.size() - 4, 0, Integer.MAX_VALUE);
	}
	
	private boolean canScrollDown()
	{
		return scroll < this.maxScroll();
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta)
	{
		if(!this.visible)
			return false;
		
		if(delta < 0)
		{			
			if(this.canScrollDown())
				scroll++;
			else
				return false;
		}
		else if(delta > 0)
		{
			if(scroll > 0)
				scroll--;
			else
				return false;
		}
		
		return true;
	}

	private void onTeamSelect(Button button)
	{
		int index = this.teamButtons.indexOf(button);
		if(index < 0)
			return;
		this.onPress.accept(scroll + index);
	}
	
	@Override
	public void updateNarration(NarrationElementOutput narrator) { }
	
	@Override
	protected boolean isValidClickButton(int button) { return false; }
	
}
