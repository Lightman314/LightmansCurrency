package io.github.lightman314.lightmanscurrency.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.data.ClientTaxData;
import io.github.lightman314.lightmanscurrency.client.util.OutlineUtil;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import javax.annotation.Nonnull;

@EventBusSubscriber(value = Dist.CLIENT, modid = LightmansCurrency.MODID)
public class TaxBlockRenderer {

    @SubscribeEvent
    public static void onLevelRender(RenderLevelStageEvent event)
    {
        if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES)
        {
            Player player = Minecraft.getInstance().player;
            Level level = Minecraft.getInstance().level;
            PoseStack pose = event.getPoseStack();
            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            double cameraX = event.getCamera().getPosition().x();
            double cameraY = event.getCamera().getPosition().y();
            double cameraZ = event.getCamera().getPosition().z();
            for(TaxEntry entry : ClientTaxData.GetAllTaxEntries())
            {
                if(shouldRenderEntry(entry, level, player, event.getCamera().getPosition()))
                {
                    //LightmansCurrency.LogDebug("Rendering Tax Entry (" + entry.getID() + ") area at " + entry.getCenter().getPos().toShortString());
                    pose.pushPose();
                    int radius = entry.getRadius();
                    int height = entry.getHeight();
                    int vertOffset = entry.getVertOffset();
                    AABB renderArea = new AABB(-radius, vertOffset, -radius, radius + 1d, vertOffset + height, radius + 1d);
                    BlockPos center = entry.getCenter().getPos();
                    //Use same pose translation as used in LevelRenderer when rendering block entities
                    pose.translate(center.getX() - cameraX, center.getY() - cameraY, center.getZ() - cameraZ);
                    //Render the outline
                    OutlineUtil.renderBox(pose, buffer, renderArea, entry.getRenderColor(player), 0.1f);
                    pose.popPose();
                }
            }
        }
    }

    private static boolean shouldRenderEntry(@Nonnull TaxEntry entry, @Nonnull Level level, @Nonnull Player player, @Nonnull Vec3 cameraPos) {
        BlockPos center = entry.getCenter().getPos();
        double renderDistance = 256d + entry.getRadius();
        return entry.shouldRender(player) &&
                entry.getCenter().sameDimension(level) &&
                center.distToCenterSqr(cameraPos.x, cameraPos.y, cameraPos.z) <= renderDistance * renderDistance;
    }
}
