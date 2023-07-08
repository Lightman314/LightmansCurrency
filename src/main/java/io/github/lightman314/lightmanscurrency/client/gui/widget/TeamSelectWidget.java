package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TeamButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TeamButton.Size;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class TeamSelectWidget extends EasyWidgetWithChildren {

	private final int rows;
	private final Size size;
	private final Supplier<List<Team>> teamSource;
	private final Supplier<Team> selectedTeam;
	private final Consumer<Integer> onPress;
	private final List<TeamButton> teamButtons = new ArrayList<>();
	
	public TeamSelectWidget(ScreenPosition pos, int rows, Supplier<List<Team>> teamSource, Supplier<Team> selectedTeam, Consumer<Integer> onPress) { this(pos.x, pos.y, rows, teamSource, selectedTeam, onPress); }
	public TeamSelectWidget(int x, int y, int rows, Supplier<List<Team>> teamSource, Supplier<Team> selectedTeam, Consumer<Integer> onPress) {
		this(x, y, rows, Size.WIDE, teamSource, selectedTeam, onPress);
	}
	
	public TeamSelectWidget(ScreenPosition pos, int rows, Size size, Supplier<List<Team>> teamSource, Supplier<Team> selectedTeam, Consumer<Integer> onPress) { this(pos.x, pos.y, rows, size, teamSource, selectedTeam, onPress); }
	public TeamSelectWidget(int x, int y, int rows, Size size, Supplier<List<Team>> teamSource, Supplier<Team> selectedTeam, Consumer<Integer> onPress) {
		super(x, y, size.width, TeamButton.HEIGHT * rows);
		this.rows = rows;
		this.size = size;
		this.teamSource = teamSource;
		this.selectedTeam = selectedTeam;
		this.onPress = onPress;
	}

	@Override
	public TeamSelectWidget withAddons(WidgetAddon... addons) { this.withAddonsInternal(addons); return this; }

	@Override
	public void addChildren() {
		this.teamButtons.clear();
		for(int i = 0; i < this.rows; ++i)
		{
			int index = i;
			TeamButton button = this.addChild(new TeamButton(this.getPosition().offset(0, i * TeamButton.HEIGHT), this.size, this::onTeamSelect, () -> this.getTeam(index), () -> this.isSelected(index)));
			this.teamButtons.add(button);
		}
	}
	
	@Override
	public void renderWidget(@Nonnull EasyGuiGraphics gui)
	{
		this.teamButtons.forEach(b -> b.visible = this.visible);
		if(!this.visible)
			return;
		gui.fill(this.getArea().atPosition(ScreenPosition.ZERO), 0xFF000000);
	}

	private int scroll = 0;
	
	private Team getTeam(int index)
	{
		List<Team> teamList = teamSource.get();
		this.validateScroll(teamList.size());
		index += this.scroll;
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
		this.scroll = MathUtil.clamp(scroll, 0, this.maxScroll(teamListSize));
	}
	
	private int maxScroll(int teamListSize)
	{
		return MathUtil.clamp(teamListSize - this.rows, 0, Integer.MAX_VALUE);
	}
	
	private boolean canScrollDown()
	{
		return scroll < this.maxScroll(this.teamSource.get().size());
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

	private void onTeamSelect(EasyButton button)
	{
		int index = -1;
		if(button instanceof TeamButton)
			index = this.teamButtons.indexOf(button);
		if(index < 0)
			return;
		this.onPress.accept(this.scroll + index);
	}

	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narrator) { }

	@Override
	protected boolean isValidClickButton(int button) { return false; }
	
	@Override
	public void playDownSound(@NotNull SoundManager soundManager) { }
	
}
