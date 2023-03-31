package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.input;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;

public abstract class InputTabAddon {

    public abstract void onInit(SettingsSubTab tab);

    public abstract void renderBG(SettingsSubTab tab, PoseStack pose, int mouseX, int mouseY, float partialTicks);
    public abstract void renderTooltips(SettingsSubTab tab, PoseStack pose, int mouseX, int mouseY);

    public abstract void tick(SettingsSubTab tab);

    public abstract void onClose(SettingsSubTab tab);

}