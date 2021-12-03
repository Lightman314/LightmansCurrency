package io.github.lightman314.lightmanscurrency.client.renderer.entity.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import io.github.lightman314.lightmanscurrency.client.model.ModelWallet;
import io.github.lightman314.lightmanscurrency.integration.Curios;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class WalletLayer<T extends PlayerEntity, M extends BipedModel<T>> extends LayerRenderer<T,M>{

	private ModelWallet<T> model;
	
	public WalletLayer(IEntityRenderer<T,M> renderer, ModelWallet<T> model)
	{
		super(renderer);
		this.model = model;
	}

	@Override
	public void render(MatrixStack stack, IRenderTypeBuffer renderTypeBuffer, int arg2, T player, float arg4, float arg5, float arg6,
			float arg7, float arg8, float arg9) {
		
		ItemStack wallet = LightmansCurrency.getWalletStack(player);
		if(wallet.getItem() instanceof WalletItem)
		{
			
			//Abort rendering if the slot is hidden via curios
			//Obsolete, as curios now handles the rendering of wallets via the ICurio capability
			if(LightmansCurrency.isCuriosLoaded() && !Curios.isWalletVisible(player))
			{
				return;
			}
			
			stack.push();
			this.getEntityModel().setModelAttributes(this.model);
			this.model.setupAngles(this.getEntityModel());
			WalletItem item = (WalletItem) wallet.getItem();
			IVertexBuilder builder = ItemRenderer.getBuffer(renderTypeBuffer, this.model.getRenderType(item.getModelTexture()), false, wallet.hasEffect());
			this.model.render(stack, builder, arg2, OverlayTexture.NO_OVERLAY, 1f, 2f, 2f, 2f);
			stack.pop();
			
		}
		
	}
	
}
