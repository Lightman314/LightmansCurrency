package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class TeamButton extends EasyButton {

	public static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource("textures/gui/buttons.png");

	public static final int HEIGHT = 20;
	public static final int TEXT_COLOR = 0xFFFFFF;

	private final Supplier<ITeam> teamSource;
	public ITeam getTeam() { return this.teamSource.get(); }
	private final Supplier<Boolean> selectedSource;

	private TeamButton(@Nonnull Builder builder)
	{
		super(builder);
		this.teamSource = builder.team;
		this.selectedSource = builder.selected;
	}

	@Override
	public void renderWidget(@Nonnull EasyGuiGraphics gui)
	{
		if(!this.visible || this.getTeam() == null)
			return;

		//Render Background
		gui.resetColor();
		gui.blitNineSplit(GUI_TEXTURE,0,0,this.width,this.height,0,this.selectedSource.get() ? 20 : 0,256,20,4);

		//Render Team Name
		gui.drawString(TextRenderUtil.fitString(this.getTeam().getName(), this.width - 4), 2, 2, TEXT_COLOR);
		//Render Owner Name
		gui.drawString(TextRenderUtil.fitString(LCText.GUI_OWNER_CURRENT.get(this.getTeam().getOwner().getName(true)), this.width - 4), 2, 10, TEXT_COLOR);

	}

	@Override
	public void playDownSound(@Nonnull SoundManager soundManager) {
		if(!this.visible || this.getTeam() == null)
			return;
		super.playDownSound(soundManager);
	}

	@Nonnull
	public static Builder builder() { return new Builder(); }

	@MethodsReturnNonnullByDefault
	@FieldsAreNonnullByDefault
	public static class Builder extends EasyButtonBuilder<Builder>
	{
		private Builder() { super(256,HEIGHT); }
		@Override
		protected Builder getSelf() { return this; }

		private Supplier<ITeam> team = () -> null;
		private Supplier<Boolean> selected = () -> false;

		public Builder width(int width) { this.changeWidth(width); return this; }
		public Builder team(Supplier<ITeam> team) { this.team = team; return this; }
		public Builder selected(Supplier<Boolean> selected) { this.selected = selected; return this; }

		public TeamButton build() { return new TeamButton(this); }

	}

}