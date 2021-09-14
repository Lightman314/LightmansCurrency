package io.github.lightman314.lightmanscurrency.integration;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import io.github.lightman314.lightmanscurrency.client.model.ModelWallet;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.capability.CurioItemCapability;

public class Curios {
	
	public static ItemStack getWalletStack(PlayerEntity player)
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
	
	public static boolean isWalletVisible(PlayerEntity player)
	{
		AtomicReference<Boolean> visible = new AtomicReference<>(true);
        LazyOptional<ICuriosItemHandler> optional = CuriosApi.getCuriosHelper().getCuriosHandler(player);
        optional.ifPresent(itemHandler -> {
            Optional<ICurioStacksHandler> stacksOptional = itemHandler.getStacksHandler(SlotTypePreset.BELT.getIdentifier());
            stacksOptional.ifPresent(stacksHandler -> {
            	for(int i = 0; i < stacksHandler.getStacks().getSlots(); i++)
            	{
            		ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
            		if(stack.getItem() instanceof WalletItem)
            		{
            			visible.set(stacksHandler.getRenders().get(i));
            		}
            	}
            });
        });
        return visible.get();
	}
	
	public static ICapabilityProvider createWalletProvider(ItemStack walletStack)
	{
		return CurioItemCapability.createProvider(new WalletCuriosCapability(walletStack));
	}
	
	public static class WalletCuriosCapability implements ICurio
	{
		
		private final WalletItem walletItem;
		private final ItemStack walletStack;
		private Object model;
		
		public WalletCuriosCapability(ItemStack walletStack)
		{
			this.walletStack = walletStack;
			this.walletItem = (WalletItem)walletStack.getItem();
		}
		
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
        
        @Override
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
        }
        
	}
	
}
