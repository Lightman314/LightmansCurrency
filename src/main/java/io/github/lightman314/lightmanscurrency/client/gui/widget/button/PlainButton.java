package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.FieldsAreNonnullByDefault;
import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class PlainButton extends EasyButton {

	private Supplier<Sprite> sprite;
	private final boolean drawInForeground;

	protected PlainButton(@Nonnull Builder builder)
	{
		super(builder);
		this.sprite = builder.sprite;
		this.drawInForeground = builder.drawInForeground;
	}
	
	public void setSprite(@Nonnull Sprite sprite) { this.setSprite(() -> sprite); }
	
	public void setSprite(@Nonnull Supplier<Sprite> sprite) { this.sprite = sprite; }
	
	@Override
	public void renderWidget(@Nonnull EasyGuiGraphics gui)
	{
		gui.resetColor();
        if(!this.active)
			gui.setColor(0.5f,0.5f,0.5f);
		if(this.drawInForeground)
			gui.pushPose().TranslateToForeground();
		gui.blitSprite(this.sprite.get(), 0, 0, this.isHovered);
		gui.resetColor();
		if(this.drawInForeground)
			gui.popPose();
	}

	@Nonnull
	public static Builder builder() { return new Builder(); }

	@MethodsReturnNonnullByDefault
	@FieldsAreNonnullByDefault
	public static class Builder extends EasyButtonBuilder<Builder>
	{
		protected Builder() {}

		@Override
		protected Builder getSelf() { return this; }

		private boolean drawInForeground = false;
		private Supplier<Sprite> sprite = null;
		public Builder sprite(Sprite sprite) { this.sprite = () -> sprite; this.changeSize(sprite.width,sprite.height); return this; }
		public Builder sprite(Supplier<Sprite> sprite) {
			this.sprite = sprite;
			Sprite example = sprite.get();
			if(example != null)
				this.changeSize(example.width,example.height);
			return this;
		}

		public Builder drawInForeground() { this.drawInForeground = true; return this; }

		public PlainButton build() { return new PlainButton(this); }

	}
}
