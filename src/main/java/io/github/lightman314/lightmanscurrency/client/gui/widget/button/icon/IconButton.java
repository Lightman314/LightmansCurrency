package io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.NonNullFunction;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class IconButton extends EasyButton {
	
	public static final int SIZE = 20;
	
	private NonNullFunction<IconButton, IconData> iconSource;
	
	public IconButton(ScreenPosition pos, Consumer<EasyButton> pressable, @Nonnull IconData icon) { this(pos.x, pos.y, pressable, icon); }
	public IconButton(int x, int y, Consumer<EasyButton> pressable, @Nonnull IconData icon)
	{
		super(x, y, SIZE, SIZE, pressable);
		this.setIcon(icon);
	}
	
	public IconButton(ScreenPosition pos, Consumer<EasyButton> pressable, @Nonnull NonNullSupplier<IconData> iconSource) { this(pos.x, pos.y, pressable, iconSource); }
	public IconButton(int x, int y, Consumer<EasyButton> pressable, @Nonnull NonNullSupplier<IconData> iconSource)
	{
		super(x, y, SIZE, SIZE, pressable);
		this.setIcon(iconSource);
	}
	
	public IconButton(ScreenPosition pos, Consumer<EasyButton> pressable, @Nonnull NonNullFunction<IconButton,IconData> iconSource) { this(pos.x, pos.y, pressable, iconSource); }
	public IconButton(int x, int y, Consumer<EasyButton> pressable, @Nonnull NonNullFunction<IconButton,IconData> iconSource)
	{
		super(x,y,SIZE, SIZE, pressable);
		this.setIcon(iconSource);
	}

	@Override
	public IconButton withAddons(WidgetAddon... addons) { this.withAddonsInternal(addons); return this; }

	public void setIcon(@Nonnull IconData icon) { this.iconSource = b -> icon; }
	
	public void setIcon(@Nonnull NonNullSupplier<IconData> iconSource) { this.iconSource = b -> iconSource.get(); }
	
	public void setIcon(@Nonnull NonNullFunction<IconButton,IconData> iconSource) { this.iconSource = iconSource; }


	@Override
	public void renderWidget(@NotNull EasyGuiGraphics gui)
	{

		gui.renderButtonBG(0,0,this.getWidth(), this.getHeight(), this.alpha, this.isHoveredOrFocused());

        if(!this.active)
            gui.setColor(0.5f,0.5f,0.5f);
        
        this.iconSource.apply(this).render(gui, 2, 2);

		RenderSystem.setShaderColor(1f,1f,1f,1f);

	}

}
