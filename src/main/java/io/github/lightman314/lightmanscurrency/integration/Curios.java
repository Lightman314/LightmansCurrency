package io.github.lightman314.lightmanscurrency.integration;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import io.github.lightman314.lightmanscurrency.client.ModLayerDefinitions;
import io.github.lightman314.lightmanscurrency.client.model.ModelWallet;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.capability.CurioItemCapability;

public class Curios {
	
	public static ItemStack getWalletStack(Player player)
	{
		AtomicReference<ItemStack> wallet = new AtomicReference<>(ItemStack.EMPTY);
		LazyOptional<ICuriosItemHandler> optional = CuriosApi.getCuriosHelper().getCuriosHandler(player);
		optional.ifPresent(itemHandler ->
		{
			Optional<ICurioStacksHandler> stacksOptional = itemHandler.getStacksHandler(SlotTypePreset.BELT.getIdentifier());
			stacksOptional.ifPresent(stacksHandler ->{
				//Go through every belt slot just in case there's more than 1 belt slot.
				for(int i = 0; i < stacksHandler.getStacks().getSlots(); i++)
				{
					ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
					if(stack.getItem() instanceof WalletItem)
					{
						wallet.set(stack);
					}
				}
				
			});
		});
		
		return wallet.get();
	}
	
	public static void RegisterCuriosRenderers()
	{
		CuriosRendererRegistry.register(ModItems.WALLET_COPPER, WalletCuriosRenderer::new);
		CuriosRendererRegistry.register(ModItems.WALLET_IRON, WalletCuriosRenderer::new);
		CuriosRendererRegistry.register(ModItems.WALLET_GOLD, WalletCuriosRenderer::new);
		CuriosRendererRegistry.register(ModItems.WALLET_EMERALD, WalletCuriosRenderer::new);
		CuriosRendererRegistry.register(ModItems.WALLET_DIAMOND, WalletCuriosRenderer::new);
		CuriosRendererRegistry.register(ModItems.WALLET_NETHERITE, WalletCuriosRenderer::new);
	}
	
	public static ICapabilityProvider createWalletProvider(ItemStack walletStack)
	{
		return CurioItemCapability.createProvider(new WalletCuriosCapability(walletStack));
	}
	
	public static class WalletCuriosCapability implements ICurio
	{
		
		//private final WalletItem walletItem;
		private final ItemStack walletStack;
		//private Object model;
		
		public WalletCuriosCapability(ItemStack walletStack)
		{
			this.walletStack = walletStack;
			//this.walletItem = (WalletItem)walletStack.getItem();
		}
		
		@Override
		public ItemStack getStack() { return this.walletStack; }
		
		@Override
        public boolean canRightClickEquip()
        {
            return false;
        }

        @Override
        public boolean canSync(String identifier, int index, LivingEntity livingEntity)
        {
            return true;
        }
        
        @Nonnull
        @Override
        public DropRule getDropRule(LivingEntity livingEntity)
        {
            return DropRule.DEFAULT;
        }
        
        /*@Override
        @OnlyIn(Dist.CLIENT)
        public boolean canRender(String identifier, int index, LivingEntity entity)
        {
        	return true;
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public void render(String identifier, int index, MatrixStack matrix, IRenderTypeBuffer renderTypeBuffer, int light, LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
        {
        	BipedModel<LivingEntity> model = getModel();
        	model.setRotationAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        	model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
        	ICurio.RenderHelper.followBodyRotations(entity, model);
        	IVertexBuilder vertexBuilder = ItemRenderer.getBuffer(renderTypeBuffer, model.getRenderType(walletItem.getModelTexture()), false, walletStack.hasEffect());
        	model.render(matrix, vertexBuilder, light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        }
        
        @SuppressWarnings("unchecked")
		@OnlyIn(Dist.CLIENT)
        private BipedModel<LivingEntity> getModel()
        {
        	if(model == null)
        		model = createModel();
        	
        	return (BipedModel<LivingEntity>)model;
        }
        
        @OnlyIn(Dist.CLIENT)
        private BipedModel<LivingEntity> createModel()
        {
        	return new ModelWallet<LivingEntity>();
        }*/
        
	}
	
	public static class WalletCuriosRenderer implements ICurioRenderer
	{
		
		private final ModelWallet<LivingEntity> model;
		
		public WalletCuriosRenderer()
		{
			this.model = new ModelWallet<LivingEntity>(Minecraft.getInstance().getEntityModels().bakeLayer(ModLayerDefinitions.WALLET));
		}

		@Override
		public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack,
				SlotContext slotContext,
				PoseStack poseStack,
				RenderLayerParent<T, M> renderLayerParent,
				MultiBufferSource bufferSource,
				int light, float limbSwing,
				float limbSwingAmount,
				float partialTicks,
				float ageInTicks,
				float netHeadYaw,
				float headPitch) {
			
			WalletItem walletItem = (WalletItem)stack.getItem();
			LivingEntity entity = slotContext.entity();
			this.model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
			this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			ICurioRenderer.followBodyRotations(entity, this.model);
			VertexConsumer vertexConsumer = ItemRenderer
					.getFoilBuffer(bufferSource, this.model.renderType(walletItem.getModelTexture()), false, stack.hasFoil());
			this.model.renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
			
		}
	}
	
}
