package io.github.lightman314.lightmanscurrency.client.features.wallet;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.MathHelper;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

import javax.annotation.Nullable;

public class WalletLayer<T extends LivingEntity,M extends EntityModel<? super S>,S extends LivingEntityRenderState> extends RenderLayer<S,M> {

    public static final ContextKey<ItemStackRenderState> EQUIPPED_WALLET = new ContextKey<>(LCApi.id("equipped_wallet"));

    public WalletLayer(RenderLayerParent renderer) { super(renderer); }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector nodeCollector, int light, S state, float yRot, float xRot) {
        this.submit(state.getRenderData(EQUIPPED_WALLET),pose,nodeCollector,light,state,yRot,xRot);
    }

    //Item Render State sensitive version that allows curios to directly
    public void submit(@Nullable ItemStackRenderState wallet, PoseStack pose, SubmitNodeCollector nodeCollector, int light, S state, float yRot, float xRot) {
        if(wallet == null || wallet.isEmpty())
            return;

        pose.pushPose();
        //Rotate 180 degrees so that the wallet is rendered right-side up
        pose.mulPose(new Quaternionf().fromAxisAngleDeg(MathHelper.ZP,180f));
        pose.translate(2f/16f,-7.5f/16f,6f/16f);
        wallet.submit(pose,nodeCollector,light,OverlayTexture.NO_OVERLAY,0);

        pose.popPose();
    }

    public static ItemStackRenderState extractWalletState(ItemStack wallet,LivingEntity entity) {
        ItemStackRenderState state = new ItemStackRenderState();
        if(WalletItem.isWallet(wallet)) {
            //TODO perform model variant shenanigans?
            //Update the render state to match the wallets model
            wallet.set(DataComponents.ITEM_MODEL,wallet.get(LCDataComponents.WALLET_MODEL));
            Minecraft.getInstance().getItemModelResolver().appendItemLayers(state,wallet, ItemDisplayContext.FIXED,entity.level(),entity,0);
        }
        return state;
    }

}
