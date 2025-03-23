package io.github.lightman314.lightmanscurrency.client.gui.widget.dropdown;

import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ILateRender;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class DropdownButton extends EasyWidget implements ILateRender, IMouseListener {
	
	private final Component optionText;
	private final Runnable onPress;

	private DropdownButton(@Nonnull Builder builder)
	{
		super(builder);
		this.onPress = builder.pressAction;
		this.optionText = builder.text;
	}

	@Override
	public void lateRender(@Nonnull EasyGuiGraphics gui) {
		if(this.isVisible())
		{
			gui.pushOffset(this);
			gui.pushPose().TranslateToForeground();
			//Draw the background
			int offset = (this.isHovered ? this.height : 0) + (DropdownWidget.HEIGHT * 2);
			if(!this.active)
				gui.setColor(0.5f,0.5f,0.5f);
			else
				gui.resetColor();
			gui.blitHorizSplit(DropdownWidget.GUI_TEXTURE, 0,0,this.width,this.height,0,offset,156,12);
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
	public void renderWidget(@Nonnull EasyGuiGraphics gui) {}

	@Nonnull
	public static Builder builder() { return new Builder(); }

	@MethodsReturnNonnullByDefault
	@FieldsAreNonnullByDefault
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
