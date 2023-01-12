package io.github.lightman314.lightmanscurrency.client.gui.screen.easy.options;

import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.tabs.TabOverflowHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class EasyScreenTabbedOptions implements IEasyScreenTabbedOptions {

    private EasyScreenTabbedOptions() {}

    public static EasyScreenTabbedOptions create() { return new EasyScreenTabbedOptions(); }

    private Component title = Component.empty();
    public final Component getTitle() { return this.title; }
    public final EasyScreenTabbedOptions withTitle(@NotNull Component title) { this.title = title; return this; }

    private int width = 176;
    private int height = 176;
    public final int getWidth() { return this.width; }
    public final int getHeight() { return this.height; }
    public final EasyScreenTabbedOptions withSize(int width, int height) { this.width = width; this.height = height; return this; }

    private ResourceLocation texture = AbstractContainerScreen.INVENTORY_LOCATION;
    public final ResourceLocation getTexture() { return this.texture; }
    public final EasyScreenTabbedOptions withTexture(@NotNull ResourceLocation texture) { this.texture = texture; return this; }

    private TabOverflowHandler tabOverflowHandler = TabOverflowHandler.CreateBasic();
    public final TabOverflowHandler getTabOverflowHandler() { return this.tabOverflowHandler; }
    public final EasyScreenTabbedOptions withTabOverflowHandler(TabOverflowHandler tabOverflowHandler) { this.tabOverflowHandler = tabOverflowHandler; return this; }

}