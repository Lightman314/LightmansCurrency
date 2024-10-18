package io.github.lightman314.lightmanscurrency.client.gui.widget.button.notifications;

import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class MarkAsSeenButton extends EasyTextButton {

	public static final int HEIGHT = 11;

	@Deprecated
	public MarkAsSeenButton(int rightPos, int yPos, Component text, Consumer<EasyButton> press) { super(rightPos - getWidth(text), yPos, getWidth(text), HEIGHT, text, press); }
	private MarkAsSeenButton(@Nonnull Builder builder) { super(builder,() -> builder.text); }

	private static int getWidth(Component text) { return TextRenderUtil.getFont().width(text) + 4; }

	@Nonnull
	public static Builder builder(@Nonnull Component text) { return new Builder(text); }
	public static Builder builder(@Nonnull TextEntry text) { return new Builder(text.get()); }

	@MethodsReturnNonnullByDefault
	@FieldsAreNonnullByDefault
	public static class Builder extends EasyButtonBuilder<Builder>
	{
		private final Component text;

		private Builder(Component text) { this.text = text; this.changeWidth(getWidth(this.text)); }

		@Override
		protected Builder getSelf() { return this; }

		@Override
		protected int getDefaultHeight() { return HEIGHT; }

		public Builder topRight(int rightPos, int yPos) { this.position(rightPos - getWidth(this.text), yPos); return this; }
		public Builder topRight(ScreenPosition pos) { return this.topRight(pos.x,pos.y); }

		public MarkAsSeenButton build() { return new MarkAsSeenButton(this); }

	}

}
