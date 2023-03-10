package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TeamButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TeamButton.Size;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;

import javax.annotation.Nonnull;

public class TeamSelectWidget extends Widget {

	private final int rows;
	private final Size size;
	private final Supplier<List<Team>> teamSource;
	private final Supplier<Team> selectedTeam;
	private final Consumer<Integer> onPress;
	private List<TeamButton> teamButtons = Lists.newArrayList();
	
	public TeamSelectWidget(int x, int y, int rows, Supplier<List<Team>> teamSource, Supplier<Team> selectedTeam, Consumer<Integer> onPress) {
		this(x, y, rows, Size.WIDE, teamSource, selectedTeam, onPress);
	}
	
	public TeamSelectWidget(int x, int y, int rows, Size size, Supplier<List<Team>> teamSource, Supplier<Team> selectedTeam, Consumer<Integer> onPress) {
		super(x, y, size.width, TeamButton.HEIGHT * rows, EasyText.empty());
		this.rows = rows;
		this.size = size;
		this.teamSource = teamSource;
		this.selectedTeam = selectedTeam;
		this.onPress = onPress;
	}
	
	public void init(Consumer<Button> addButton, FontRenderer font)
	{
		for(int i = 0; i < this.rows; ++i)
		{
			int index = i;
			TeamButton button = new TeamButton(this.x, this.y + i * TeamButton.HEIGHT, this.size, this::onTeamSelect, font, () -> this.getTeam(index), () -> this.isSelected(index));
			this.teamButtons.add(button);
			addButton.accept(button);
		}
	}
	
	@Override
	public void render(@Nonnull MatrixStack pose, int mouseX, int mouseY, float partialTicks)
	{
		this.teamButtons.forEach(b -> b.visible = this.visible);
		if(!this.visible)
			return;
		fill(pose, x, y, x + this.width, y + this.height, 0xFF000000);
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

	private void onTeamSelect(Button button)
	{
		int index = this.teamButtons.indexOf(button);
		if(index < 0)
			return;
		this.onPress.accept(this.scroll + index);
	}
	
	@Override
	protected boolean isValidClickButton(int button) { return false; }
	
	@Override
	public void playDownSound(@Nonnull SoundHandler soundManager) { }
	
}
