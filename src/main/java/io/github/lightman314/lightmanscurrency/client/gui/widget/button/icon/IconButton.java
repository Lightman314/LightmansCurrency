package io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class IconButton extends EasyButton {
	
	public static final int SIZE = 20;
	
	private Function<IconButton, IconData> iconSource;

	public int bgColor = 0xFFFFFF;

	@Deprecated
	public IconButton(ScreenPosition pos, Consumer<EasyButton> pressable, @Nonnull IconData icon) { this(pos.x, pos.y, pressable, icon); }
	@Deprecated
	public IconButton(int x, int y, Consumer<EasyButton> pressable, @Nonnull IconData icon)
	{
		super(x, y, SIZE, SIZE, pressable);
		this.setIcon(icon);
	}
	@Deprecated
	public IconButton(ScreenPosition pos, Consumer<EasyButton> pressable, @Nonnull Supplier<IconData> iconSource) { this(pos.x, pos.y, pressable, iconSource); }
	@Deprecated
	public IconButton(int x, int y, Consumer<EasyButton> pressable, @Nonnull Supplier<IconData> iconSource)
	{
		super(x, y, SIZE, SIZE, pressable);
		this.setIcon(iconSource);
	}

	@Deprecated
	public IconButton(ScreenPosition pos, Consumer<EasyButton> pressable, @Nonnull Function<IconButton,IconData> iconSource) { this(pos.x, pos.y, pressable, iconSource); }
	@Deprecated
	public IconButton(int x, int y, Consumer<EasyButton> pressable, @Nonnull Function<IconButton,IconData> iconSource)
	{
		super(x,y,SIZE, SIZE, pressable);
		this.setIcon(iconSource);
	}
	private IconButton(@Nonnull Builder builder)
	{
		super(builder);
		this.setIcon(builder.icon);
	}

	@Override
	@Deprecated
	public IconButton withAddons(WidgetAddon... addons) { this.withAddonsInternal(addons); return this; }

	public void setIcon(@Nonnull IconData icon) { this.iconSource = b -> icon; }
	
	public void setIcon(@Nonnull Supplier<IconData> iconSource) { this.iconSource = b -> iconSource.get(); }
	
	public void setIcon(@Nonnull Function<IconButton,IconData> iconSource) { this.iconSource = iconSource; }

	@Override
	public void renderWidget(@Nonnull EasyGuiGraphics gui)
	{

		gui.renderButtonBG(0,0,this.getWidth(), this.getHeight(), this.alpha, this, this.bgColor);

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

		private Supplier<IconData> icon = IconData::Null;

		private Builder() {}

		@Override
		protected Builder getSelf() { return this; }

		public Builder icon(IconData icon) { this.icon = () -> icon; return this; }
		public Builder icon(Supplier<IconData> icon) { this.icon = icon; return this; }

		public IconButton build() { return new IconButton(this); }

	}

}
