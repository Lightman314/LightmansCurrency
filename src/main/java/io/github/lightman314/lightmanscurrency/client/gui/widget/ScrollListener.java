package io.github.lightman314.lightmanscurrency.client.gui.widget;

import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IScrollListener;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.jetbrains.annotations.NotNull;

public class ScrollListener implements IScrollListener {

	public ScreenArea area;
	private final IScrollListener listener;
	
	public boolean active = true;

	public ScrollListener(ScreenPosition position, int width, int height, IScrollListener listener) { this(ScreenArea.of(position, width, height), listener); }

	public ScrollListener(int x, int y, int width, int height, IScrollListener listener) { this(ScreenArea.of(x, y, width, height), listener); }
	public ScrollListener(ScreenArea area, IScrollListener listener) {
		this.area = area;
		this.listener = listener;
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta)
	{
		if(this.area.isMouseInArea(mouseX, mouseY))
			return this.listener.mouseScrolled(mouseX, mouseY, delta);
		return false;
	}

}
