package io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface BookRendererGenerator {

    @Nullable
    BookRenderer createRendererForItem(@Nonnull ItemStack book);

}