package io.github.lightman314.lightmanscurrency.client.gui.screen.easy.options;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public interface IEasyScreenOptions {
    Component getTitle();
    int getWidth();
    int getHeight();
    ResourceLocation getTexture();

}
