package io.github.lightman314.lightmanscurrency.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.custom_models.CustomModelDataManager;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

//Render Helper to easily render items in-world while following client config options such as custom scales, model overrides, and render blacklists
public class ItemRenderHelper {

    public static void renderItem(@Nullable BlockEntity be, ItemRenderer renderer, ItemStack item, int lightLevel, PoseStack pose, MultiBufferSource buffer, int id)
    {
        //Get custom scale for the item
        float scale = LCConfig.CLIENT.itemScaleOverrides.get().getCustomScale(item);
        pose.scale(scale,scale,scale);
        //Check if the item is blacklisted, and if so replace with a trading core
        if(LCConfig.CLIENT.itemRenderBlacklist.contains(item))
            item = new ItemStack(ModItems.TRADING_CORE.get());
        //Check for custom model for the item
        ModelResourceLocation customModel = CustomModelDataManager.getCustomModel(be,item);
        if(customModel == null)
            renderer.renderStatic(item, ItemDisplayContext.FIXED, lightLevel, OverlayTexture.NO_OVERLAY, pose, buffer, be.getLevel(), id);
        else
        {
            BakedModel model = Minecraft.getInstance().getModelManager().getModel(customModel);
            renderer.render(item, ItemDisplayContext.FIXED, false, pose, buffer, lightLevel, OverlayTexture.NO_OVERLAY, model);
        }
    }

}