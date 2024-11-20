package io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Function;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class IconButton extends EasyButton {

	public static final int SIZE = 20;

	private Function<IconButton, IconData> iconSource;

	private final Function<IconButton,Integer> color;

	protected IconButton(@Nonnull Builder builder)
	{
		super(builder);
		this.setIcon(builder.icon);
		this.color = builder.color;
	}

	public void setIcon(@Nonnull IconData icon) { this.iconSource = b -> icon; }

	public void setIcon(@Nonnull Supplier<IconData> iconSource) { this.iconSource = b -> iconSource.get(); }

	public void setIcon(@Nonnull Function<IconButton,IconData> iconSource) { this.iconSource = iconSource; }

	//Copy/pasted from AbstractButton.getTextureY()
	private int getTextureY() {
		int i = 1;
		if (!this.active) {
			i = 0;
		} else if (this.isHoveredOrFocused()) {
			i = 2;
		}

		return 46 + (i * 20);
	}

	@Override
	public void renderWidget(@Nonnull EasyGuiGraphics gui)
	{

		gui.renderButtonBG(0,0,this.getWidth(), this.getHeight(), this.alpha, this.getTextureY(), this.color.apply(this));

		if(!this.active)
			gui.setColor(0.5f, 0.5f, 0.5f,this.alpha);

		this.iconSource.apply(this).render(gui, 2, 2);

		gui.resetColor();

	}

	@Nonnull
	public static Builder builder() { return new Builder(); }

	@MethodsReturnNonnullByDefault
	@FieldsAreNonnullByDefault
	public static class Builder extends EasyButtonBuilder<Builder>
	{

		private Function<IconButton,IconData> icon = b -> IconData.Null();
		private Function<IconButton,Integer> color = b -> 0xFFFFFF;

		protected Builder() {}

		@Override
		protected Builder getSelf() { return this; }

		public Builder icon(IconData icon) { this.icon = b -> icon; return this; }
		public Builder icon(Supplier<IconData> icon) { this.icon = b -> icon.get(); return this; }
		public Builder icon(Function<IconButton,IconData> icon) { this.icon = icon; return this; }
		public Builder color(int color) { this.color = b -> color; return this; }
		public Builder color(Supplier<Integer> color) { this.color = b -> color.get(); return this; }
		public Builder color(Function<IconButton,Integer> color) { this.color = color; return this; }

		public IconButton build() { return new IconButton(this); }

	}

}