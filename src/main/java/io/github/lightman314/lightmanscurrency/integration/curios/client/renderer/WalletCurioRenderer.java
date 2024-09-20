package io.github.lightman314.lightmanscurrency.integration.curios.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.renderer.entity.layers.WalletLayer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.function.Supplier;

public class WalletCurioRenderer implements ICurioRenderer {

    public static final ICurioRenderer INSTANCE = new WalletCurioRenderer();
    public static Supplier<ICurioRenderer> supplier() { return () -> INSTANCE; }

    private WalletCurioRenderer() { }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack itemStack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource multiBufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        //Don't render on a player as they already have a wallet layer
        if(slotContext.entity() instanceof Player)
            return;
        WalletLayer<T,M> layer = new WalletLayer<>(renderLayerParent);
        try{ layer.render(poseStack, multiBufferSource, light, (T)slotContext.entity(), limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        } catch(Throwable ignored) { }

    }
}
