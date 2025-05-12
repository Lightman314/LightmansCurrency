package io.github.lightman314.lightmanscurrency.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.ItemTraderBlockEntityRenderer;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class GachaBallRenderer {

    public static final ResourceLocation MODEL = VersionUtil.lcResource("item/gacha_ball_model");

    public static void renderGachaBall(ItemStack ball, PoseStack pose, MultiBufferSource buffer, int lightLevel, int id)
    {

        pose.pushPose();

        //Render the ball
        pose.translate(0.5d,0.5d,0.5d);
        Minecraft mc = Minecraft.getInstance();
        BakedModel model = mc.getModelManager().getModel(MODEL);
        ItemRenderer itemRenderer = mc.getItemRenderer();
        itemRenderer.render(ball,ItemDisplayContext.FIXED,false,pose,buffer,lightLevel,OverlayTexture.NO_OVERLAY,model);

        pose.popPose();

        if(!LCConfig.CLIENT.drawGachaBallItem.get())
            return;

        //Render the balls contents
        CompoundTag tag = ball.getTag();
        if(tag == null || !tag.contains("GachaItem"))
            return;
        ItemStack contents = ItemStack.of(tag.getCompound("GachaItem"));
        if(contents.isEmpty())
            return;

        pose.pushPose();
        //Move into position
        //Model is 14/16 meters tall, so move up 7/16 of a meter
        pose.translate(0.5d,0.4375d,0.5d);
        pose.scale(0.6f,0.6f,0.6f);
        //Rotate
        pose.mulPose(ItemTraderBlockEntityRenderer.getRotation(0f));

        float scale = LCConfig.CLIENT.itemScaleOverrides.get().getCustomScale(contents);
        pose.scale(scale,scale,scale);

        itemRenderer.renderStatic(contents,ItemDisplayContext.FIXED,lightLevel,OverlayTexture.NO_OVERLAY,pose,buffer,null,id);

        pose.popPose();

    }

}