package io.github.lightman314.lightmanscurrency.client.renderer.entity.layers;

import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import io.github.lightman314.lightmanscurrency.common.core.ModAttachmentTypes;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
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
		
		WalletHandler handler = entity.getData(ModAttachmentTypes.WALLET_HANDLER);
		if(handler == null || !handler.visible())
			return;
		
		ItemStack wallet = handler.getWallet();
		if(wallet.getItem() instanceof WalletItem walletItem)
		{

			pose.pushPose();
			pose.translate(12f/16f, 19.5f/16f, 6f/16f);
			//Debug Positioning to align with old model position
			//pose.translate(LCConfig.CLIENT.xOff.get(),LCConfig.CLIENT.yOff.get(),LCConfig.CLIENT.zOff.get());

			Minecraft mc = Minecraft.getInstance();
			BakedModel model = mc.getModelManager().getModel(ModelResourceLocation.standalone(wallet.getOrDefault(ModDataComponents.WALLET_MODEL, VersionUtil.lcResource("item/wallet/null"))));
			mc.getItemRenderer().render(wallet, ItemDisplayContext.FIXED, false, pose, bufferSource, light, OverlayTexture.NO_OVERLAY, model);

			pose.popPose();

		}
		
	}
	
}
