package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class PlainButton extends EasyButton {

	private NonNullSupplier<Sprite> sprite;
	
	public PlainButton(@Nonnull ScreenPosition pos, @Nonnull Consumer<EasyButton> pressable, @Nonnull Sprite sprite) { this(pos.x, pos.y, pressable, sprite); }
	public PlainButton(int x, int y, @Nonnull Consumer<EasyButton> pressable, @Nonnull Sprite sprite) { this(x, y, pressable, () -> sprite); }

	public PlainButton(@Nonnull ScreenPosition pos, @Nonnull Consumer<EasyButton> pressable, @Nonnull NonNullSupplier<Sprite> sprite) { this(pos.x, pos.y, pressable, sprite); }
	public PlainButton(int x, int y, @Nonnull Consumer<EasyButton> pressable, @Nonnull NonNullSupplier<Sprite> sprite)
	{
		super(x, y, sprite.get().width, sprite.get().height, pressable);
		this.sprite = sprite;
	}
	
	public void setSprite(@Nonnull Sprite sprite) { this.setSprite(() -> sprite); }
	
	public void setSprite(@Nonnull NonNullSupplier<Sprite> sprite) { this.sprite = sprite; }
	
	@Override
	public void renderWidget(@Nonnull EasyGuiGraphics gui)
	{
		gui.resetColor();
        if(!this.active)
			gui.setColor(0.5f,0.5f,0.5f);
		gui.blitSprite(this.sprite.get(), 0, 0, this.isHovered);
		gui.resetColor();
	}

	@Override
	public PlainButton withAddons(WidgetAddon... addons) { this.withAddonsInternal(addons); return this; }

}
