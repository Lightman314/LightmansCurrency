package io.github.lightman314.lightmanscurrency.client.gui.widget;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.function.Function;

public class ScrollListener extends EasyWidget implements IScrollListener {

	private final Function<Double,Boolean> listener;

	private final boolean inverted;

	private static Function<Double,Boolean> convertConsumer(Consumer<Double> consumer) { return d -> { consumer.accept(d); return false; }; }

	private ScrollListener(@Nonnull Builder builder)
	{
		super(builder);
		this.listener = builder.listener;
		this.inverted = builder.inverted;
	}

	@Override
	protected void renderWidget(@Nonnull EasyGuiGraphics gui) { }

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta)
	{
		if(this.isActive() && this.getArea().isMouseInArea(mouseX, mouseY))
			return this.listener.apply(this.inverted ? -delta : delta);
		return false;
	}

	@Nonnull
	public static Builder builder() { return new Builder(); }

	@MethodsReturnNonnullByDefault
	@ParametersAreNonnullByDefault
	@FieldsAreNonnullByDefault
	public static class Builder extends EasySizableBuilder<Builder>
	{

		private Builder() { }

		@Override
		protected Builder getSelf() { return this; }

		private Function<Double,Boolean> listener = d -> false;
		private boolean inverted = false;

		public Builder listener(Function<Double,Boolean> listener) { this.listener = listener; return this; }
		public Builder listener(IScrollable listener) { this.listener = listener::handleScrollWheel; return this; }
		public Builder invert() { this.inverted = true; return this; }

		public ScrollListener build() { return new ScrollListener(this); }

	}

}
