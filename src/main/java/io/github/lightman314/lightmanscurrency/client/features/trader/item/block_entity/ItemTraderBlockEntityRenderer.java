package io.github.lightman314.lightmanscurrency.client.features.trader.item.block_entity;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.helpers.MathHelper;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.ItemPositionData;
import io.github.lightman314.lightmanscurrency.features.trader.item.blocks.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.client.features.trader.item.block_entity.ItemTraderRenderState.TradeItemDisplay;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

@EventBusSubscriber(Dist.CLIENT)
public class ItemTraderBlockEntityRenderer<T extends ItemTraderBlockEntity,S extends ItemTraderRenderState> implements BlockEntityRenderer<T,S> {

    public static final BlockEntityRendererProvider<ItemTraderBlockEntity,ItemTraderRenderState> PROVIDER = c -> new ItemTraderBlockEntityRenderer<>(c.itemModelResolver(),ItemTraderRenderState::new);

    protected final ItemModelResolver itemModelResolver;
    private final Supplier<S> stateFactory;
    protected ItemTraderBlockEntityRenderer(ItemModelResolver itemModelResolver,Supplier<S> stateFactory) {
        this.itemModelResolver = itemModelResolver;
        this.stateFactory = stateFactory;
    }

    @Override
    public S createRenderState() { return this.stateFactory.get(); }

    @Override
    public void extractRenderState(T blockEntity, S state, float partialTicks, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        ItemTraderRenderState.extractItemTraderRenderState(blockEntity,state,this.itemModelResolver,partialTicks);
    }

    @Override
    public void submit(S state,PoseStack poseStack,SubmitNodeCollector submitNodeCollector,CameraRenderState camera) {
        submitItems(state,poseStack,submitNodeCollector,camera);
    }

    public static void submitItems(ItemTraderRenderState state,PoseStack pose,SubmitNodeCollector submitNodeCollector,CameraRenderState camera) {
        final int renderLimit = LCConfig.CLIENT.itemRenderLimit.get();
        if(renderLimit <= 0)
            return;
        ItemPositionData positionData = state.positionData;
        if(positionData.isEmpty())
            return;

        final int maxIndex = positionData.getEntryCount();
        BlockState blockState = state.blockState;
        List<TradeItemDisplay> tradeDisplay = state.getTradeDisplay();
        for(int tradeSlot = 0; tradeSlot < tradeDisplay.size() && tradeSlot < maxIndex; ++tradeSlot) {
            TradeItemDisplay items = tradeDisplay.get(tradeSlot);
            if(items.isEmpty())
                continue;
            List<Vector3fc> positions = positionData.getPositions(blockState,tradeSlot);
            //Get rotation
            List<Quaternionfc> rotation = positionData.getRotation(blockState,tradeSlot,state.partialTicks);
            int itemLight = state.getPackedLight(positionData.getMinLight(tradeSlot));
            float scale = positionData.getScale(tradeSlot);
            for(int p = 0; p < renderLimit && p < positions.size() && p < items.stock; p++)
            {
                pose.pushPose();

                //Translate, rotate, and scale the pose stack
                pose.translate(MathHelper.toDoubleVec(positions.get(p)));
                for(Quaternionfc rot : rotation)
                    pose.mulPose(rot);
                pose.scale(scale,scale,scale);

                //Render the item
                List<ItemStackRenderState> renderItems = items.getSaleItems();
                if(renderItems.size() > 1) {
                    //Render the first item
                    pose.pushPose();

                    pose.translate(0.25,0.25,0);
                    pose.scale(0.5f,0.5f,0.5f);

                    renderItems.getFirst().submit(pose,submitNodeCollector,itemLight, OverlayTexture.NO_OVERLAY,0);

                    pose.popPose();

                    //Render the second item
                    pose.pushPose();

                    pose.translate(-0.25,-0.25,0.01);
                    pose.scale(0.5f,0.5f,0.5f);

                    renderItems.get(1).submit(pose,submitNodeCollector,itemLight,OverlayTexture.NO_OVERLAY,0);

                    pose.popPose();

                }
                else
                    renderItems.getFirst().submit(pose,submitNodeCollector,itemLight,OverlayTexture.NO_OVERLAY,0);

                pose.popPose();
            }
        }
    }

    @Override
    public AABB getRenderBoundingBox(T blockEntity) {
        return blockEntity.getBlockState().getCollisionShape(blockEntity.getLevel(),blockEntity.getBlockPos()).bounds().move(blockEntity.getBlockPos());
    }

    private static long rotationTime = 0;
    public static long getRotationTime() { return rotationTime; }
    public static Quaternionfc getRotation(float partialTicks) { return getRotation(partialTicks,2f); }
    public static Quaternionfc getRotation(float partialTicks,float mult) { return new Quaternionf().fromAxisAngleDeg(MathHelper.YP,(getRotationTime() + partialTicks) * mult); }

    @SubscribeEvent
    private static void onClientTick(ClientTickEvent.Pre event) { rotationTime++; }

}