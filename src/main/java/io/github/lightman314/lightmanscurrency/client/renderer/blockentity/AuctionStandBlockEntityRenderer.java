package io.github.lightman314.lightmanscurrency.client.renderer.blockentity;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.renderer.ItemRenderHelper;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.ItemPositionBlockManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.ItemPositionData;
import io.github.lightman314.lightmanscurrency.common.blockentity.AuctionStandBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.List;

public class AuctionStandBlockEntityRenderer implements BlockEntityRenderer<AuctionStandBlockEntity> {


    private final ItemRenderer itemRenderer;
    private AuctionStandBlockEntityRenderer(BlockEntityRendererProvider.Context context) { this.itemRenderer = context.getItemRenderer(); }

    public static AuctionStandBlockEntityRenderer create(BlockEntityRendererProvider.Context context) { return new AuctionStandBlockEntityRenderer(context); }

    @Override
    public void render(@Nonnull AuctionStandBlockEntity blockEntity, float partialTicks, @Nonnull PoseStack pose, @Nonnull MultiBufferSource buffer, int lightLevel, int id) {

        ImmutableList<ItemStack> displayItems = AuctionStandBlockEntity.getDisplayItems();
        if(displayItems.isEmpty())
            return;

        BlockState state = blockEntity.getBlockState();

        ItemPositionData data = ItemPositionBlockManager.getDataForBlock(state);
        List<Vector3f> positions = data.getPositions(state, 0);
        if(positions.isEmpty())
            return;

        pose.pushPose();
        Vector3f pos = positions.getFirst();
        pose.translate(pos.x, pos.y, pos.z);

        for(Quaternionf r : data.getRotation(state, 0, partialTicks))
            pose.mulPose(r);

        float scale = data.getScale(0);
        pose.scale(scale,scale,scale);

        int itemLight = lightLevel;
        Level level = blockEntity.getLevel();
        BlockPos blockPos = blockEntity.getBlockPos();
        int minLight = data.getMinLight(0);
        if(level.getBrightness(LightLayer.BLOCK,blockPos) < data.getMinLight(0))
            itemLight = LightTexture.pack(Math.min(minLight,level.getMaxLightLevel()),level.getBrightness(LightLayer.SKY,blockPos));

        if(displayItems.size() < 2)
        {
            //Only renderBG 1 item
            ItemRenderHelper.renderItem(blockEntity,this.itemRenderer,displayItems.getFirst(),itemLight,pose,buffer,id);
        }
        else
        {
            //Render Item 1
            pose.pushPose();
            pose.translate(-0.55f,0f,0f);
            ItemRenderHelper.renderItem(blockEntity,this.itemRenderer,displayItems.getFirst(),itemLight,pose,buffer,id);
            pose.popPose();

            //Render Item 2
            pose.pushPose();
            pose.translate(0.55f, 0f, 0f);
            ItemRenderHelper.renderItem(blockEntity,this.itemRenderer,displayItems.get(1),itemLight,pose,buffer,id);
            pose.popPose();
        }
        pose.popPose();

    }

}
