package io.github.lightman314.lightmanscurrency.client.renderer.entity.layers;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import io.github.lightman314.lightmanscurrency.client.model.ModelWallet;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.util.WalletUtil;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class WalletLayer<T extends Player, M extends HumanoidModel<T>> extends RenderLayer<T,M>{

	private final ModelWallet<T> model;
	
	public WalletLayer(RenderLayerParent<T,M> parent, ModelWallet<T> model)
	{
		super(parent);
		this.model = model;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, T entity, float limbSwing,
			float limbSwingAmount,
			float partialTicks,
			float ageInTicks,
			float netHeadYaw,
			float headPitch) {
		
		List<ItemStack> wallets = WalletUtil.getEquippedWallets(entity);
		ItemStack wallet = wallets.size() > 0 ? wallets.get(0) : ItemStack.EMPTY;
		if(wallet.getItem() instanceof WalletItem)
		{
			
			WalletItem walletItem = (WalletItem)wallet.getItem();
			this.model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
			this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			this.getParentModel().copyPropertiesTo(this.model);
        	VertexConsumer vertexConsumer = ItemRenderer
        			.getFoilBuffer(bufferSource, this.model.renderType(walletItem.getModelTexture()), false, wallet.hasFoil());
        	this.model.renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
			
		}
		
	}
	
}
