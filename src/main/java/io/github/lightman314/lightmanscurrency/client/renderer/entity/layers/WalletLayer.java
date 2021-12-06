package io.github.lightman314.lightmanscurrency.client.renderer.entity.layers;

import io.github.lightman314.lightmanscurrency.client.model.ModelWallet;
import io.github.lightman314.lightmanscurrency.items.WalletItem;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class WalletLayer<T extends Player, M extends HumanoidModel<T>> extends RenderLayer<T,M>{

	private ModelWallet<T> model;
	
	public WalletLayer(RenderLayerParent<T,M> renderer, ModelWallet<T> model)
	{
		super(renderer);
		this.model = model;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, T entity, float limbSwing,
			float limbSwingAmount,
			float partialTicks,
			float ageInTicks,
			float netHeadYaw,
			float headPitch) {
		
		ItemStack wallet = LightmansCurrency.getWalletStack(entity);
		if(wallet.getItem() instanceof WalletItem)
		{
			
			WalletItem walletItem = (WalletItem)wallet.getItem();
			this.model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
			this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			this.getParentModel().copyPropertiesTo(this.model);
			VertexConsumer vertexConsumer = ItemRenderer
					.getFoilBuffer(bufferSource, this.model.renderType(walletItem.getModelTexture()), false, wallet.hasFoil());
			this.model.renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
			
		}
		
	}
	
}
