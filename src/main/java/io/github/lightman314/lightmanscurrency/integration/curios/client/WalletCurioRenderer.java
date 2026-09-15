package io.github.lightman314.lightmanscurrency.integration.curios.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.features.wallet.WalletLayer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.function.Supplier;

public final class WalletCurioRenderer implements ICurioRenderer {

    private static final ICurioRenderer INSTANCE = new WalletCurioRenderer();
    public static final Supplier<ICurioRenderer> SOURCE = () -> INSTANCE;

    private WalletCurioRenderer() {}

    @Override
    public <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, S renderState, RenderLayerParent<S, M> renderLayerParent, EntityRendererProvider.Context context, float yRotation, float xRotation) {
        //Don't render on a player as they already have a wallet layer
        if(slotContext.entity() instanceof Player || !slotContext.visible())
            return;
        try {
            WalletLayer<LivingEntity,M,S> layer = new WalletLayer<>(renderLayerParent);
            layer.submit(WalletLayer.extractWalletState(stack,slotContext.entity()),poseStack,submitNodeCollector,packedLight,renderState,yRotation,xRotation);
        } catch (Exception ignored) {}
    }

}