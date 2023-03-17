package io.github.lightman314.lightmanscurrency.client.gui.widget.util;

public interface IScreen {

	int getGuiLeft();
	int getGuiTop();
	void addTickListener(Runnable r);
	
}
