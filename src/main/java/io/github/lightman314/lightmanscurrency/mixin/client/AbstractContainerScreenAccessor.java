package io.github.lightman314.lightmanscurrency.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {

    @Accessor
    @Mutable
    void setImageWidth(int imageWidth);
    @Accessor
    @Mutable
    void setImageHeight(int imageHeight);

}
