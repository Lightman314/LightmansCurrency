package io.github.lightman314.lightmanscurrency.client.gui.screen.easy.options;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class EasyScreenOptions implements IEasyScreenOptions {

    private EasyScreenOptions() {}
    public static EasyScreenOptions create() { return new EasyScreenOptions(); }

    private Component title = Component.empty();
    public final Component getTitle() { return this.title; }
    public final EasyScreenOptions withTitle(@NotNull Component title) { this.title = title; return this; }

    private int width = 176;
    private int height = 176;
    public final int getWidth() { return this.width; }
    public final int getHeight() { return this.height; }
    public final EasyScreenOptions withSize(int width, int height) { this.width = width; this.height = height; return this; }

    private ResourceLocation texture = AbstractContainerScreen.INVENTORY_LOCATION;
    public final ResourceLocation getTexture() { return this.texture; }
    public final EasyScreenOptions withTexture(@NotNull ResourceLocation texture) { this.texture = texture; return this; }



}
