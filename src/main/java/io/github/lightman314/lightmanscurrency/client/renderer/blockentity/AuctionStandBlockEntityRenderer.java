package io.github.lightman314.lightmanscurrency.client.renderer.blockentity;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.ItemPositionBlockManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.ItemPositionData;
import io.github.lightman314.lightmanscurrency.common.blockentity.AuctionStandBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class AuctionStandBlockEntityRenderer implements BlockEntityRenderer<AuctionStandBlockEntity> {


    private final ItemRenderer itemRenderer;
    private AuctionStandBlockEntityRenderer(BlockEntityRendererProvider.Context context) { this.itemRenderer = context.getItemRenderer(); }

    public static AuctionStandBlockEntityRenderer create(BlockEntityRendererProvider.Context context) { return new AuctionStandBlockEntityRenderer(context); }

    @Override
    public void render(@NotNull AuctionStandBlockEntity blockEntity, float partialTicks, @NotNull PoseStack pose, @NotNull MultiBufferSource buffer, int lightLevel, int id) {

        ImmutableList<ItemStack> displayItems = AuctionStandBlockEntity.getDisplayItems();
        if(displayItems.isEmpty())
            return;

        BlockState state = blockEntity.getBlockState();

        ItemPositionData data = ItemPositionBlockManager.getDataForBlock(state);
        List<Vector3f> positions = data.getPositions(state, 0);
        if(positions.isEmpty())
            return;

        pose.pushPose();
        Vector3f pos = positions.get(0);
        pose.translate(pos.x, pos.y, pos.z);

        for(Quaternionf r : data.getRotation(state, 0, partialTicks))
            pose.mulPose(r);

        float scale = data.getScale(0);
        pose.scale(scale,scale,scale);

        int itemLight = lightLevel;
        Level level = blockEntity.getLevel();
        BlockPos blockPos = blockEntity.getBlockPos();
        int minLight = data.getMinLight(0);
        if(level.getBrightness(LightLayer.BLOCK,blockPos) < minLight)
            itemLight = LightTexture.pack(Math.min(minLight,level.getMaxLightLevel()),level.getBrightness(LightLayer.SKY,blockPos));

        if(displayItems.size() < 2)
        {
            //Only renderBG 1 item
            scale = LCConfig.CLIENT.itemScaleOverrides.get().getCustomScale(displayItems.get(0));
            pose.scale(scale,scale,scale);
            this.itemRenderer.renderStatic(displayItems.get(0), ItemDisplayContext.FIXED, itemLight, OverlayTexture.NO_OVERLAY, pose, buffer, blockEntity.getLevel(), id);
        }
        else
        {
            //Render Item 1
            pose.pushPose();
            pose.translate(-0.55f,0f,0f);
            scale = LCConfig.CLIENT.itemScaleOverrides.get().getCustomScale(displayItems.get(0));
            pose.scale(scale,scale,scale);
            this.itemRenderer.renderStatic(displayItems.get(0), ItemDisplayContext.FIXED, itemLight, OverlayTexture.NO_OVERLAY, pose, buffer, blockEntity.getLevel(), id);
            pose.popPose();

            //Render Item 2
            pose.pushPose();
            pose.translate(0.55f, 0f, 0f);
            scale = LCConfig.CLIENT.itemScaleOverrides.get().getCustomScale(displayItems.get(1));
            pose.scale(scale,scale,scale);
            this.itemRenderer.renderStatic(displayItems.get(1),  ItemDisplayContext.FIXED, itemLight, OverlayTexture.NO_OVERLAY, pose, buffer, blockEntity.getLevel(), id);
            pose.popPose();
        }
        pose.popPose();

    }

}
