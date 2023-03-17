package io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.renderers.NormalBookRenderer;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.BookRestriction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class BookRenderer {

    protected final ItemStack book;
    protected BookRenderer(ItemStack book) { this.book = book; }

    private static final List<BookRendererGenerator> GENERATORS = new ArrayList<>();

    public static void register(@Nonnull BookRendererGenerator generator) { GENERATORS.add(generator); }

    public static BookRenderer GetRenderer(@Nonnull ItemStack bookStack) {
        for(BookRendererGenerator generator : GENERATORS)
        {
            BookRenderer renderer = generator.createRendererForItem(bookStack);
            if(renderer != null)
                return renderer;
        }
        //If a valid book, but no renderer was found, default to the normal book renderer
        if(BookRestriction.CanSellAsBook(bookStack))
            return NormalBookRenderer.INSTANCE;
        return null;
    }

    public abstract void render(BlockEntity blockEntity, float partialTicks, @NotNull PoseStack pose, @NotNull MultiBufferSource buffer, int lightLevel, int id);

    protected final void renderModel(ResourceLocation modelResource, PoseStack pose, MultiBufferSource buffer, int lightLevel) {
        Minecraft mc = Minecraft.getInstance();
        BakedModel model = mc.getModelManager().getModel(modelResource);
        ItemRenderer itemRenderer = mc.getItemRenderer();
        itemRenderer.render(this.book, ItemTransforms.TransformType.FIXED, false, pose, buffer, lightLevel, OverlayTexture.NO_OVERLAY, model);
    }

}