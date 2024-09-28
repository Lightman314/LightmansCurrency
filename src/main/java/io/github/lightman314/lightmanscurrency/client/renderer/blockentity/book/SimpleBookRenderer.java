package io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

public abstract class SimpleBookRenderer extends BookRenderer {

    protected SimpleBookRenderer(ItemStack book) { super(book); }

    protected abstract ResourceLocation getBookModel();

    @Override
    public final void render(BlockEntity blockEntity, float partialTicks, @Nonnull PoseStack pose, @Nonnull MultiBufferSource buffer, int lightLevel, int id) {
        this.renderModel(this.getBookModel(), pose, buffer, lightLevel);
    }

}
