package io.github.lightman314.lightmanscurrency.client.gui.settings.input;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;

/**
 * Keeping around to keep LC Tech Settings addons from crashing.
 */
@Deprecated(since = "2.1.1.0", forRemoval = true)
public abstract class InputTabAddon extends io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.input.InputTabAddon {

	private static TraderSettingsScreen convert(SettingsSubTab tab) { return new TraderSettingsScreen(tab); }

	@Override
	public void onInit(SettingsSubTab tab) { this.onInit(convert(tab)); }

	@Override
	public void renderBG(SettingsSubTab tab, PoseStack pose, int mouseX, int mouseY, float partialTicks) { this.preRender(new TraderSettingsScreen(tab), pose, mouseX, mouseY, partialTicks); }

	@Override
	public void renderTooltips(SettingsSubTab tab, PoseStack pose, int mouseX, int mouseY) { this.postRender(convert(tab), pose, mouseX, mouseY, 0f); }

	@Override
	public void tick(SettingsSubTab tab) { this.tick(convert(tab)); }

	@Override
	public void onClose(SettingsSubTab tab) { this.onClose(convert(tab)); }

	public abstract void onInit(TraderSettingsScreen screen);

	public abstract void preRender(TraderSettingsScreen screen, PoseStack pose, int mouseX, int mouseY, float partialTicks);
	public abstract void postRender(TraderSettingsScreen screen, PoseStack pose, int mouseX, int mouseY, float partialTicks);

	public abstract void tick(TraderSettingsScreen screen);

	public abstract void onClose(TraderSettingsScreen screen);

}