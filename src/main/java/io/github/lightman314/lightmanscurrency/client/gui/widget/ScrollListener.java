package io.github.lightman314.lightmanscurrency.client.gui.widget;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;

public class ScrollListener implements GuiEventListener, NarratableEntry {

	public int x;
	public int y;
	public int width;
	public int height;
	private final IScrollListener listener;
	
	public boolean active = true;
	
	public ScrollListener(int x, int y, int width, int height, IScrollListener listener) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.listener = listener;
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) { return this.listener.mouseScrolled(mouseX, mouseY, delta); }
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.active && mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;
	}
	
	public interface IScrollListener { public boolean mouseScrolled(double mouseX, double mouseY, double delta); }
	
	@Override
	public void updateNarration(NarrationElementOutput narrator) {}

	@Override
	public NarrationPriority narrationPriority() { return NarrationPriority.NONE; }
	
}
