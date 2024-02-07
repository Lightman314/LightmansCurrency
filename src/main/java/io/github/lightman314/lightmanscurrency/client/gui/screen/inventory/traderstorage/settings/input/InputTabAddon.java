package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.input;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;

import javax.annotation.Nonnull;

public abstract class InputTabAddon {
	
	public abstract void onOpen(@Nonnull SettingsSubTab tab, @Nonnull ScreenArea screenArea, boolean firstOpen);
	
	public abstract void renderBG(@Nonnull SettingsSubTab tab, @Nonnull EasyGuiGraphics gui);
	public abstract void renderAfterWidgets(@Nonnull SettingsSubTab tab, @Nonnull EasyGuiGraphics gui);
	
	public abstract void tick(@Nonnull SettingsSubTab tab);
	
	public abstract void onClose(@Nonnull SettingsSubTab tab);
	
}
