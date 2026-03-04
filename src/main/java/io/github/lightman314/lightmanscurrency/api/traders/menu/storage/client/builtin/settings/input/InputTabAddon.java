package io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.input;

import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;

public abstract class InputTabAddon {
	
	public abstract void onOpen(SettingsSubTab tab, ScreenArea screenArea, boolean firstOpen);
	
	public abstract void renderBG(SettingsSubTab tab, EasyGuiGraphics gui);
	public abstract void renderAfterWidgets(SettingsSubTab tab, EasyGuiGraphics gui);
	
	public abstract void tick(SettingsSubTab tab);
	
	public abstract void onClose(SettingsSubTab tab);
	
}
