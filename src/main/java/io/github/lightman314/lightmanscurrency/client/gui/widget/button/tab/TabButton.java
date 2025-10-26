package io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipSource;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.IRotatableWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.WidgetRotation;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class TabButton extends EasyButton implements ITooltipSource, IRotatableWidget {

    public static final int SIZE = 25;
    public static final int NEGATIVE_SIZE = 25 * -1;

	public static final Function<WidgetRotation,FixedSizeSprite> NORMAL = TabSpriteSource.createBuiltin("common/widgets/tabs/normal",SIZE);
	public static final Function<WidgetRotation,FixedSizeSprite> YELLOW = TabSpriteSource.createBuiltin("common/widgets/tabs/yellow",SIZE);
	public static final Function<WidgetRotation,FixedSizeSprite> RED = TabSpriteSource.createBuiltin("common/widgets/tabs/red",SIZE);

	public boolean hideTooltip = false;
	
	public final ITab tab;

	private WidgetRotation rotation;

	protected TabButton(Builder builder)
	{
		super(builder);
		this.tab = builder.tab;
		this.rotation = builder.rotation;
	}

	@Deprecated
	public void reposition(ScreenPosition pos, int rotation) { this.reposition(pos.x, pos.y, rotation); }
	@Deprecated
	public void reposition(int x, int y, int rotation)
	{
		this.setPosition(x, y);
		this.rotation = WidgetRotation.fromIndex(rotation);
	}

	@Override
	public void setRotation(@Nonnull WidgetRotation rotation) { this.rotation = rotation; }

	@Override
	public void renderWidget(@Nonnull EasyGuiGraphics gui)
	{
		//Set the texture & color for the button
		gui.resetColor();

        //Render the background
        FixedSizeSprite sprite = this.getSprite().apply(this.rotation);
        if(sprite.getWidth() != SIZE || sprite.getHeight() != SIZE)
        {
            LightmansCurrency.LogError("Tab Button received a tab sprite that isn't 25x25 pixels!",new Throwable());
            return;
        }
        sprite.render(gui,0,0,this);

        //Render the icon
		float m = this.active ? 1f : 0.5f;
		gui.setColor(m,m,m);
        this.tab.getIcon().render(gui, getIconOffset(this.rotation));

		gui.resetColor();

	}

	public static ScreenPosition getIconOffset(WidgetRotation rotation)
	{
		return switch (rotation) {
			case TOP -> ScreenPosition.of(4,5);
			case BOTTOM -> ScreenPosition.of(4,3);
			case LEFT -> ScreenPosition.of(5,4);
			case RIGHT -> ScreenPosition.of(3,4);
		};
	}

	protected Function<WidgetRotation,FixedSizeSprite> getSprite() {
        Function<WidgetRotation,FixedSizeSprite> result = this.tab.getSprite();
		return result == null ? NORMAL : result;
	}

	@Override
	public List<Component> getTooltipText(int mouseX, int mouseY) {
		if(this.hideTooltip || !this.isVisible())
			return null;
		if(this.getArea().isMouseInArea(mouseX,mouseY))
			return ImmutableList.of(this.tab.getTooltip());
		return null;
	}

	@Nonnull
	public static Builder builder() { return new Builder(); }

	@MethodsReturnNonnullByDefault
	@FieldsAreNonnullByDefault
	@ParametersAreNonnullByDefault
	public static class Builder extends EasyButtonBuilder<Builder>
	{

		protected Builder() { super(SIZE,SIZE); }
		@Override
		protected Builder getSelf() { return this; }

		@Nullable
		private ITab tab = null;
		private WidgetRotation rotation = WidgetRotation.TOP;

		public Builder tab(ITab tab) { this.tab = tab; return this; }
		public Builder rotation(WidgetRotation rotation) { this.rotation = rotation; return this; }

		public TabButton build() { return new TabButton(this); }

	}

}
