package io.github.lightman314.lightmanscurrency.client.renderer.blockentity;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.common.blockentity.AuctionStandBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class AuctionStandBlockEntityRenderer extends TileEntityRenderer<AuctionStandBlockEntity> {


    private final ItemRenderer itemRenderer;
    public AuctionStandBlockEntityRenderer(TileEntityRendererDispatcher dispatcher) { super(dispatcher); this.itemRenderer = Minecraft.getInstance().getItemRenderer(); }

    @Override
    public void render(@Nonnull AuctionStandBlockEntity blockEntity, float partialTicks, @Nonnull MatrixStack pose, @Nonnull IRenderTypeBuffer buffer, int lightLevel, int id) {

        ImmutableList<ItemStack> displayItems = AuctionStandBlockEntity.getDisplayItems();
        if(displayItems.size() < 1)
            return;

        pose.pushPose();
        pose.translate(0.5f, 0.75f, 0.5f);
        pose.mulPose(ItemTraderBlockEntityRenderer.getRotation(partialTicks));
        pose.scale(0.4f,0.4f,0.4f);

        if(displayItems.size() < 2)
        {
            //Only render 1 item
            this.itemRenderer.renderStatic(displayItems.get(0), ItemCameraTransforms.TransformType.FIXED, lightLevel, id, pose, buffer);
        }
        else
        {
            //Render Item 1
            pose.pushPose();
            pose.translate(-0.55f,0f,0f);
            this.itemRenderer.renderStatic(displayItems.get(0), ItemCameraTransforms.TransformType.FIXED, lightLevel, id, pose, buffer);
            pose.popPose();

            //Render Item 2
            pose.pushPose();
            pose.translate(0.55f, 0f, 0f);
            this.itemRenderer.renderStatic(displayItems.get(1), ItemCameraTransforms.TransformType.FIXED, lightLevel, id, pose, buffer);
            pose.popPose();
        }
        pose.popPose();

    }

}