package io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public abstract class SimpleBookRenderer extends BookRenderer {

    protected SimpleBookRenderer(ItemStack book) { super(book); }

    protected abstract ModelResourceLocation getBookModel();

    protected static ModelResourceLocation modelLocation(@Nonnull ResourceLocation modelLocation) { return new ModelResourceLocation(modelLocation,ModelResourceLocation.STANDALONE_VARIANT); }

    @Override
    public final void render(BlockEntity blockEntity, float partialTicks, @NotNull PoseStack pose, @NotNull MultiBufferSource buffer, int lightLevel, int id) {
        this.renderModel(this.getBookModel(), pose, buffer, lightLevel);
    }

}
