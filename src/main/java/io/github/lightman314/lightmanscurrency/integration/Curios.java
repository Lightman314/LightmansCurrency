package io.github.lightman314.lightmanscurrency.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
		List<ItemStack> foundWallets = getEquippedWallets(player);
		return foundWallets.size() > 0 ? foundWallets.get(0) : ItemStack.EMPTY;
	}
	
	public static List<ItemStack> getEquippedWallets(Player player)
	{
		List<ItemStack> foundWallets = new ArrayList<>();
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
						foundWallets.add(stack);
					}
				}
				
			});
		});
		return foundWallets;
	}
	
	public static List<ItemStack> extractEquippedWallets(Player player)
	{
		List<ItemStack> equippedWallets = new ArrayList<>();
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
						equippedWallets.add(stack);
						stacksHandler.getStacks().setStackInSlot(i, ItemStack.EMPTY);
					}
				}
			});
		});
		return equippedWallets;
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
		
		private final ItemStack walletStack;
		
		public WalletCuriosCapability(ItemStack walletStack) { this.walletStack = walletStack; }
		
		@Override
        public boolean canRightClickEquip() { return false; }

        @Override
        public boolean canSync(String identifier, int index, LivingEntity livingEntity) { return true; }
        
        @Nonnull
        @Override
        public DropRule getDropRule(LivingEntity livingEntity)
        {
        	//WalletDropMode dropMode = Config.SERVER.walletDropMode.get();
        	//if(dropMode == WalletDropMode.KEEP || dropMode == WalletDropMode.DROP_COINS)
        	//	return DropRule.ALWAYS_KEEP;
            return DropRule.DEFAULT;
        }

		@Override
		public ItemStack getStack() { return walletStack; }
        
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
				float headPitch)
		{
			WalletItem walletItem = (WalletItem)stack.getItem();
			LivingEntity entity = slotContext.entity();
			this.model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
			this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			ICurioRenderer.followBodyRotations(entity, this.model);
        	VertexConsumer vertexConsumer = ItemRenderer
        			.getFoilBuffer(bufferSource, this.model.renderType(walletItem.getModelTexture()), false, stack.hasFoil());
        	this.model.renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
		}
		
	}
	
}
