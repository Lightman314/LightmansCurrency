package io.github.lightman314.lightmanscurrency.client.gui.widget;

import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces.IScrollListener;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.client.gui.IGuiEventListener;

public class ScrollListener implements IGuiEventListener {

	public int x;
	public int y;
	public int width;
	public int height;
	private final IScrollListener listener;
	
	public boolean active = true;

	public ScrollListener(ScreenPosition position, int width, int height, IScrollListener listener) { this(position.x, position.y, width, height, listener); }

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

}
