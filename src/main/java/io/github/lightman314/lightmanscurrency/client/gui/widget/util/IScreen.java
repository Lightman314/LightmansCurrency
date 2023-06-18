package io.github.lightman314.lightmanscurrency.client.gui.widget.util;

public interface IScreen {

	int getGuiLeft();
	int getGuiTop();
	int getXSize();
	int getYSize();
	void addTickListener(Runnable r);
	
}
