package io.github.lightman314.lightmanscurrency.client.gui.widget.text;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget.IScrollable;
import io.github.lightman314.lightmanscurrency.client.util.PlayerSuggestionsUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TextComponent;

public class PlayerInputBox extends EditBox implements IScrollable{
	
	public int maxSuggestions;
	public boolean suggestAbove = true;
	
	private final Font font;
	
	private int suggestionScroll = 0;
	
	private static final int HEIGHT_PER_SUGGESTION = 10;
	
	
	public PlayerInputBox(int x, int y, int width, int height, Font font) { this(x, y, width, height, 5, font); }
	
	public PlayerInputBox(int x, int y, int width, int height, int maxSuggestions, Font font) {
		super(font, x, y, width, height, new TextComponent(""));
		this.maxSuggestions = maxSuggestions;
		this.font = font;
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		if(this.visible && mouseX >= (double)this.x && mouseX < (double)(this.x + this.width))
		{
			if(this.suggestAbove)
				return mouseY >= this.y - this.getSuggestionHeight() && mouseY < this.y + this.height;
			else
				return mouseY >= this.y && mouseY < this.y + this.height + this.getSuggestionHeight();
		}
		else
			return false;
	}
	
	private int getSuggestionHeight() {
		return Math.min(this.getSuggestions().size(), this.maxSuggestions) * HEIGHT_PER_SUGGESTION;
	}
	
	@Override
	public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		if(this.isVisible())
		{
			List<String> suggestions = this.getSuggestions();
			if(suggestions.size() > 0)
			{
				int suggestionHeight = this.getSuggestionHeight();
				int startHeight = this.suggestAbove ? this.y - suggestionHeight : this.y + this.height;
				GuiComponent.fill(pose, this.x, startHeight, this.x + this.width, startHeight + suggestionHeight, 0x000000);
				for(int i = this.suggestionScroll; i < suggestions.size() && i < this.suggestionScroll + this.maxSuggestions; ++i)
				{
					this.font.draw(pose, TextRenderUtil.fitString(suggestions.get(i), this.width - 4), this.x + 2, startHeight + (i - this.suggestionScroll) * HEIGHT_PER_SUGGESTION + 1, 0xFFFFFF);
				}
			}
		}
		super.renderButton(pose, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void tick() {
		super.tick();
		this.validateScroll();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if(this.handleScrollWheel(delta))
			return true;
		return super.mouseScrolled(mouseX, mouseY, delta);
	}
	
	private void validateScroll() { this.suggestionScroll = MathUtil.clamp(this.suggestionScroll, 0, this.getMaxScroll()); }
	
	private List<String> getSuggestions() {
		return PlayerSuggestionsUtil.getSuggestions(this.getValue());
	}

	@Override
	public int currentScroll() { return this.suggestionScroll; }

	@Override
	public void setScroll(int newScroll) {
		this.suggestionScroll = newScroll;
		this.validateScroll();
	}

	@Override
	public int getMaxScroll() {
		return IScrollable.calculateMaxScroll(this.maxSuggestions, this.getSuggestions().size());
	}
	
}
