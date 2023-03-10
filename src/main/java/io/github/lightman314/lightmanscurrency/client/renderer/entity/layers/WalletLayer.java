package io.github.lightman314.lightmanscurrency.client.renderer.entity.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.lightman314.lightmanscurrency.client.model.ModelWallet;
import io.github.lightman314.lightmanscurrency.common.capability.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class WalletLayer<T extends PlayerEntity, M extends BipedModel<T>> extends LayerRenderer<T,M> {

	private final ModelWallet<T> model;
	
	public WalletLayer(IEntityRenderer<T,M> renderer, ModelWallet<T> model)
	{
		super(renderer);
		this.model = model;
	}

	@Override
	public void render(@Nonnull MatrixStack poseStack, @Nonnull IRenderTypeBuffer bufferSource, int light, @Nonnull T entity, float limbSwing,
					   float limbSwingAmount,
					   float partialTicks,
					   float ageInTicks,
					   float netHeadYaw,
					   float headPitch) {
		
		IWalletHandler handler = WalletCapability.lazyGetWalletHandler(entity);
		if(handler == null || !handler.visible())
			return;
		
		ItemStack wallet = handler.getWallet();
		if(wallet.getItem() instanceof WalletItem)
		{
			WalletItem walletItem = (WalletItem)wallet.getItem();
			this.model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
			this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			this.getParentModel().copyPropertiesTo(this.model);
			IVertexBuilder vertexConsumer = ItemRenderer
					.getFoilBuffer(bufferSource, this.model.renderType(walletItem.getModelTexture()), false, wallet.hasFoil());
			this.model.renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
		}
		
	}
	
}
