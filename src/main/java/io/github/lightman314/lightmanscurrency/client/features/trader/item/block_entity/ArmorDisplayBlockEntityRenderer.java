package io.github.lightman314.lightmanscurrency.client.features.trader.item.block_entity;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.api.helpers.MathHelper;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.features.trader.item.blocks.armor.ArmorDisplayTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.features.trader.item.nodes.ArmorTradesNode;
import io.github.lightman314.lightmanscurrency.features.trader.item.trade.ArmorTradeData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public class ArmorDisplayBlockEntityRenderer implements BlockEntityRenderer<ArmorDisplayTraderBlockEntity, ArmorDisplayTraderRenderState> {

    public static final BlockEntityRendererProvider<ArmorDisplayTraderBlockEntity,ArmorDisplayTraderRenderState> PROVIDER = c -> new ArmorDisplayBlockEntityRenderer();

    protected ArmorDisplayBlockEntityRenderer() {}

    private static ArmorStandRenderer armorStandRenderer = null;

    @Nullable
    private static ArmorStandRenderer getArmorStandRenderer(ArmorStand stand) {
        if(armorStandRenderer == null && Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(stand) instanceof ArmorStandRenderer r)
            armorStandRenderer = r;
        return armorStandRenderer;
    }

    @Override
    public ArmorDisplayTraderRenderState createRenderState() {
        return new ArmorDisplayTraderRenderState();
    }

    @Override
    public void extractRenderState(ArmorDisplayTraderBlockEntity be, ArmorDisplayTraderRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);

        //Collect the Armor Stand's data
        TraderData trader = be.getTrader();
        ArmorStand stand = new ArmorStand(be.getLevel(),0,0,0);
        ArmorStandRenderer renderer = getArmorStandRenderer(stand);
        if(renderer != null) {
            if(trader != null) {
                ArmorTradesNode node = trader.getNode(ArmorTradesNode.TYPE);
                if(node != null) {
                    for(int i = 0; i < 4; i++) {
                        ArmorTradeData trade = node.getTrade(i);
                        if(trade != null)
                            stand.setItemSlot(trade.getEquipmentSlot(),this.extractItem(trader,trade));
                    }
                }
            }
            //Rotate the Armor Stand
            BlockState blockState = be.getBlockState();
            Direction facing = Direction.NORTH;
            if(blockState.getBlock() instanceof IRotatableBlock rb)
                facing = rb.getFacing(blockState);
            state.rotation = this.getStandRotation(facing);
            //Extract the Armor Stand Render State
            renderer.extractRenderState(stand,state.armorState,0f);
        }
    }

    private ItemStack extractItem(TraderData trader,ArmorTradeData trade) {
        try(TradeContext context = TradeContext.builder(trader).build()) {
            if(!trade.hasStock(context))
                return ItemStack.EMPTY;
            for(int i = 0; i < 2; ++i) {
                ItemStack temp = trade.getItem(i).getDisplayStack(context);
                if(!temp.isEmpty())
                    return temp;
            }
        }
        return ItemStack.EMPTY;
    }

    private float getStandRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 180f;
            case WEST -> -90f;
            case EAST -> 90f;
            default -> 0f;
        };
    }

    @Override
    public void submit(ArmorDisplayTraderRenderState state, PoseStack pose, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        //Render the Armor Stand
        if(armorStandRenderer != null) {
            pose.pushPose();
            pose.translate(0.5f,0f,0.5f);
            pose.mulPose(new Quaternionf().fromAxisAngleDeg(MathHelper.YP,state.rotation));
            armorStandRenderer.submit(state.armorState,pose,submitNodeCollector,camera);
            pose.popPose();
        }
    }

    @Override
    public AABB getRenderBoundingBox(ArmorDisplayTraderBlockEntity blockEntity) {
        return blockEntity.getBlockState().getCollisionShape(blockEntity.getLevel(),blockEntity.getBlockPos()).bounds().move(blockEntity.getBlockPos());
    }
}