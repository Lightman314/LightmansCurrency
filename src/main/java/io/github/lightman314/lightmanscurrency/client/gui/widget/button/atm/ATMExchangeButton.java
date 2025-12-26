package io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.base.Predicates;
import com.mojang.blaze3d.FieldsAreNonnullByDefault;
import com.mojang.blaze3d.MethodsReturnNonnullByDefault;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FlexibleSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.renderer.ATMIconRenderer;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.data.ATMExchangeButtonData;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconData;

import javax.annotation.Nonnull;

public class ATMExchangeButton extends EasyButton {
	
	public final ATMExchangeButtonData data;

	private final Predicate<ATMExchangeButton> selected;

	private ATMExchangeButton(@Nonnull Builder builder)
	{
		super(builder);
		this.data = builder.data;
		this.selected = builder.selected;
	}

	@Override
	public void renderWidget(@Nonnull EasyGuiGraphics gui) {
		
		//Render background to width
		if(this.active)
			gui.resetColor();
		else
			gui.setColor(0.5f,0.5f,0.5f);

		//Draw background of size
        boolean highlighted = this.isHoveredOrFocused() != this.selected.test(this);
        FlexibleSizeSprite sprite = highlighted ? SpriteUtil.BUTTON_GRAY_HOVERED : SpriteUtil.BUTTON_GRAY;
        sprite.render(gui,0,0,this.width,this.height);
		
		//Draw the icons
		for(ATMIconData icon : this.data.getIcons())
		{
			try {
                ATMIconRenderer.renderIcon(this,icon,gui,highlighted);
			} catch(Throwable t) { LightmansCurrency.LogError("Error rendering ATM Conversion Button icon.", t); }
		}

		gui.resetColor();
		
	}

	@Nonnull
	public static Builder builder(@Nonnull ATMExchangeButtonData data) { return new Builder(data); }

	@MethodsReturnNonnullByDefault
	@FieldsAreNonnullByDefault
	public static class Builder extends EasyButtonBuilder<Builder>
	{
		private final ATMExchangeButtonData data;
		private Builder(ATMExchangeButtonData data) { super(data.width,data.height); this.data = data; }

		private Predicate<ATMExchangeButton> selected = Predicates.alwaysFalse();

		@Override
		protected Builder getSelf() { return this; }

		public Builder screenCorner(ScreenPosition corner) { return this.position(corner.offset(data.position)); }
		public Builder commandHandler(Consumer<String> commandHandler) { return this.pressAction(() -> commandHandler.accept(this.data.command)); }

		public Builder selected(Predicate<ATMExchangeButton> selected) { this.selected = selected; return this; }

		public ATMExchangeButton build() { return new ATMExchangeButton(this); }

	}
	
}
