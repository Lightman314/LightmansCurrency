package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TeamButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.Nonnull;

public class TeamSelectWidget extends EasyWidgetWithChildren {

	private final int rows;
	private final Supplier<List<ITeam>> teamSource;
	private final Supplier<ITeam> selectedTeam;
	private final Consumer<Integer> onPress;
	private final List<TeamButton> teamButtons = new ArrayList<>();

	private TeamSelectWidget(@Nonnull Builder builder)
	{
		super(builder);
		this.rows = builder.rows;
		this.teamSource = builder.teams;
		this.selectedTeam = builder.selected;
		this.onPress = builder.handler;
	}

	@Override
	public void addChildren(@Nonnull ScreenArea area) {
		this.teamButtons.clear();
		for(int i = 0; i < this.rows; ++i)
		{
			final int index = i;
			TeamButton button = this.addChild(TeamButton.builder()
					.position(area.pos.offset(0,i * TeamButton.HEIGHT))
					.width(this.width)
					.pressAction(this::onTeamSelect)
					.team(() -> this.getTeam(index))
					.selected(() -> this.isSelected(index))
					.build());
			this.teamButtons.add(button);
		}
	}

	@Override
	protected void renderTick() {
		for(TeamButton b : this.teamButtons)
			b.setVisible(this.visible);
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

	private ITeam getTeam(int index)
	{
		List<ITeam> teamList = teamSource.get();
		this.validateScroll(teamList.size());
		index += this.scroll;
		if(index >= 0 && index < teamList.size())
			return teamList.get(index);
		return null;
	}

	private boolean isSelected(int index)
	{
		ITeam team = this.getTeam(index);
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
	public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double delta)
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

	@Nonnull
	public static Builder builder() { return new Builder(); }

	@MethodsReturnNonnullByDefault
	@FieldsAreNonnullByDefault
	public static class Builder extends EasyBuilder<Builder>
	{
		private Builder() { super(156,TeamButton.HEIGHT); }
		@Override
		protected Builder getSelf() { return this; }

		private int rows = 1;
		private Supplier<List<ITeam>> teams = ArrayList::new;
		private Supplier<ITeam> selected = () -> null;
		private Consumer<Integer> handler = i -> {};

		public Builder width(int width) { this.changeWidth(width); return this; }
		public Builder rows(int rows) { this.rows = rows; this.changeHeight(rows * TeamButton.HEIGHT); return this; }
		public Builder teams(Supplier<List<ITeam>> teams) { this.teams = teams; return this; }
		public Builder selected(Supplier<ITeam> selected) { this.selected = selected; return this; }
		public Builder handler(Consumer<Integer> handler) { this.handler = handler; return this; }

		public TeamSelectWidget build() { return new TeamSelectWidget(this); }

	}

}