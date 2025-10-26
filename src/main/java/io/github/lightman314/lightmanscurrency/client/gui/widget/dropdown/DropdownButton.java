package io.github.lightman314.lightmanscurrency.client.gui.widget.dropdown;

import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FlexibleWidthSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteSource;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.HorizontalSliceSprite;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ILateRender;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipSource;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DropdownButton extends EasyWidget implements ILateRender, IMouseListener, ITooltipSource {
	
	private final Component optionText;
	private final Runnable onPress;

    public static final FlexibleWidthSprite ENTRY_SPRITE = new HorizontalSliceSprite(SpriteSource.createTop(VersionUtil.lcResource("common/widgets/dropdown_entry"),128,12),12);
    public static final FlexibleWidthSprite ENTRY_HIGHLIGHTED_SPRITE = new HorizontalSliceSprite(SpriteSource.createBottom(VersionUtil.lcResource("common/widgets/dropdown_entry"),128,12),12);

	private DropdownButton(Builder builder)
	{
		super(builder);
		this.onPress = builder.pressAction;
		this.optionText = builder.text;
	}

	@Override
	public void lateRender(EasyGuiGraphics gui) {
		if(this.isVisible())
		{
			gui.pushOffset(this);
			gui.pushPose().TranslateToForeground();
			//Draw the background
			if(!this.active)
				gui.setColor(0.5f,0.5f,0.5f);
			else
				gui.resetColor();

            FlexibleWidthSprite sprite = this.isHoveredOrFocused() ? ENTRY_HIGHLIGHTED_SPRITE : ENTRY_SPRITE;
            sprite.render(gui,0,0,this.width);
			//Draw the option text
			gui.drawString(TextRenderUtil.fitString(this.optionText, this.width - 4), 2, 2, 0x404040);

			gui.resetColor();

			gui.popOffset().popPose();
		}
	}

	@Override
	protected boolean isValidClickButton(int button) { return button == 0; }

	@Override
	public boolean onMouseClicked(double mouseX, double mouseY, int button) {
		if(this.isActive() && this.clicked(mouseX, mouseY) && this.isValidClickButton(button))
		{
			EasyButton.playClick(Minecraft.getInstance().getSoundManager());
			this.onPress.run();
			return true;
		}
		return false;
	}

	@Override
	public void renderWidget(EasyGuiGraphics gui) {}

	public static Builder builder() { return new Builder(); }

    @Override
    public boolean renderTooltip(EasyGuiGraphics gui) { return this.isMouseOver(gui.mousePos); }

    @Override
    public List<Component> getTooltipText(int mouseX, int mouseY) { return null; }

	public static class Builder extends EasyBuilder<Builder>
	{
		private Builder() { super(20,DropdownWidget.HEIGHT); }
		@Override
		protected Builder getSelf() { return this; }

		private Runnable pressAction = () -> {};
		private Component text = EasyText.empty();

		public Builder width(int width) { this.changeWidth(width); return this; }
		public Builder pressAction(Runnable pressAction) { this.pressAction = pressAction; return this; }
		public Builder text(Component text) { this.text = text; return this; }

		public DropdownButton build() { return new DropdownButton(this); }

	}

}
