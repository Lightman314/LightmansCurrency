package io.github.lightman314.lightmanscurrency.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.CoinChestBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class CoinChestRenderer implements BlockEntityRenderer<CoinChestBlockEntity> {

    public static final Material COIN_CHEST_MATERIAL = new Material(Sheets.CHEST_SHEET, new ResourceLocation(LightmansCurrency.MODID, "entity/chest/coin_chest"));

    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;

    public CoinChestRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart modelpart = context.bakeLayer(ModelLayers.CHEST);
        this.bottom = modelpart.getChild("bottom");
        this.lid = modelpart.getChild("lid");
        this.lock = modelpart.getChild("lock");
    }

    @Override
    public void render(@Nonnull CoinChestBlockEntity blockEntity, float partialTicks, @NotNull PoseStack pose, @NotNull MultiBufferSource buffer, int lightLevel, int id) {
        Level level = blockEntity.getLevel();
        boolean flag = level != null;
        BlockState blockstate = flag ? blockEntity.getBlockState() : ModBlocks.COIN_CHEST.get().defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH);
        Block block = blockstate.getBlock();
        if (block instanceof CoinChestBlock abstractchestblock) {
            pose.pushPose();
            float f = blockstate.getValue(ChestBlock.FACING).getOpposite().toYRot();
            pose.translate(0.5F, 0.5F, 0.5F);
            pose.mulPose(Axis.YP.rotationDegrees(-f));
            pose.translate(-0.5F, -0.5F, -0.5F);

            float f1 = blockEntity.getOpenNess(partialTicks);
            f1 = 1.0F - f1;
            f1 = 1.0F - f1 * f1 * f1;
            VertexConsumer vertexconsumer = COIN_CHEST_MATERIAL.buffer(buffer, RenderType::entityCutout);
            this.render(pose, vertexconsumer, this.lid, this.lock, this.bottom, f1, lightLevel, id);

            pose.popPose();
        }
    }

    private void render(PoseStack pose, VertexConsumer vertex, ModelPart lid, ModelPart lock, ModelPart bottom, float lidAngle, int lightLevel, int id) {
        lid.xRot = -(lidAngle * ((float)Math.PI / 2F));
        lock.xRot = lid.xRot;
        lid.render(pose, vertex, lightLevel, id);
        lock.render(pose, vertex, lightLevel, id);
        bottom.render(pose, vertex, lightLevel, id);
    }

}
