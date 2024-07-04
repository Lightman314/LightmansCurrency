package io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class IconButton extends EasyButton {
	
	public static final int SIZE = 20;
	
	private Function<IconButton, IconData> iconSource;

	public int bgColor = 0xFFFFFF;

	public IconButton(ScreenPosition pos, Consumer<EasyButton> pressable, @Nonnull IconData icon) { this(pos.x, pos.y, pressable, icon); }
	public IconButton(int x, int y, Consumer<EasyButton> pressable, @Nonnull IconData icon)
	{
		super(x, y, SIZE, SIZE, pressable);
		this.setIcon(icon);
	}
	
	public IconButton(ScreenPosition pos, Consumer<EasyButton> pressable, @Nonnull Supplier<IconData> iconSource) { this(pos.x, pos.y, pressable, iconSource); }
	public IconButton(int x, int y, Consumer<EasyButton> pressable, @Nonnull Supplier<IconData> iconSource)
	{
		super(x, y, SIZE, SIZE, pressable);
		this.setIcon(iconSource);
	}
	
	public IconButton(ScreenPosition pos, Consumer<EasyButton> pressable, @Nonnull Function<IconButton,IconData> iconSource) { this(pos.x, pos.y, pressable, iconSource); }
	public IconButton(int x, int y, Consumer<EasyButton> pressable, @Nonnull Function<IconButton,IconData> iconSource)
	{
		super(x,y,SIZE, SIZE, pressable);
		this.setIcon(iconSource);
	}

	@Override
	public IconButton withAddons(WidgetAddon... addons) { this.withAddonsInternal(addons); return this; }

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
	public void renderWidget(@NotNull EasyGuiGraphics gui)
	{

		gui.renderButtonBG(0,0,this.getWidth(), this.getHeight(), this.alpha, this, this.bgColor);

        if(!this.active)
            gui.setColor(0.5f, 0.5f, 0.5f,this.alpha);
        
        this.iconSource.apply(this).render(gui, 2, 2);

		gui.resetColor();

	}

}
