package io.github.lightman314.lightmanscurrency.client.renderer.entity.layers;

import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletCapability;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class WalletLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T,M>{
	
	public WalletLayer(RenderLayerParent<T,M> renderer)
	{
		super(renderer);
	}

	@Override
	public void render(@Nonnull PoseStack pose, @Nonnull MultiBufferSource bufferSource, int light, @Nonnull T entity, float limbSwing,
					   float limbSwingAmount,
					   float partialTicks,
					   float ageInTicks,
					   float netHeadYaw,
					   float headPitch) {
		
		IWalletHandler handler = WalletCapability.lazyGetWalletHandler(entity);
		if(handler == null || !handler.visible())
			return;
		
		ItemStack wallet = handler.getVisibleWallet();
		if(wallet.getItem() instanceof WalletItem walletItem)
		{

			pose.pushPose();
			//Rotate 180 degrees so that the wallet is rendered right-side up
			pose.mulPose(MathUtil.fromAxisAngleDegree(MathUtil.getZP(),180f));
			pose.translate(2f/16f, -7.5f/16f, 6f/16f);

			Minecraft mc = Minecraft.getInstance();
			BakedModel model = mc.getModelManager().getModel(walletItem.model);
			mc.getItemRenderer().render(wallet, ItemDisplayContext.FIXED, false, pose, bufferSource, light, OverlayTexture.NO_OVERLAY, model);

			pose.popPose();

		}
		
	}
	
}
