package io.github.lightman314.lightmanscurrency.client.renderer.blockentity;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.blockentity.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class ItemTraderBlockEntityRenderer implements BlockEntityRenderer<ItemTraderBlockEntity>{

	public static int positionLimit()
	{
		switch(Config.CLIENT.traderRenderType.get())
		{
		case PARTIAL:
			return 1;
		case NONE:
			return 0;
			default:
				return Integer.MAX_VALUE;
		}
	}
	
	public ItemTraderBlockEntityRenderer(BlockEntityRendererProvider.Context dispatcher)
	{
		//dispatcher.
		//super(dispatcher);
	}
	
	@Override
	public void render(ItemTraderBlockEntity tileEntity, float partialTicks, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int lightLevel, int id)
	{
		
		for(int tradeSlot = 0; tradeSlot < tileEntity.getTradeCount() && tradeSlot < tileEntity.maxRenderIndex(); tradeSlot++)
		{
			
			ItemTradeData trade = tileEntity.getTrade(tradeSlot);
			if(!trade.getSellItem().isEmpty())
			{
				
				ItemStack stack = trade.getSellItem();
				
				boolean isBlock = stack.getItem() instanceof BlockItem;
				if(isBlock && Config.CLIENT.renderBlocksAsItems.get().contains(stack.getItem().getRegistryName().toString()))
				{
					isBlock = false;
				}
				
				//Get positions
				List<Vector3f> positions = tileEntity.GetStackRenderPos(tradeSlot, isBlock);
				
				//Get rotation
				List<Quaternion> rotation = tileEntity.GetStackRenderRot(tradeSlot, partialTicks, isBlock);
				
				//Get scale
				Vector3f scale = tileEntity.GetStackRenderScale(tradeSlot, isBlock);

				for(int pos = 0; pos < positions.size() && pos < tileEntity.getTradeStock(tradeSlot) && pos < positionLimit(); pos++)
				{
					
					matrixStack.pushPose();
					
					Vector3f position = positions.get(pos);
					
					//Translate, rotate, and scale the matrix stack
					matrixStack.translate(position.x(), position.y(), position.z());
					for(Quaternion rot : rotation)
					{
						matrixStack.mulPose(rot);
					}
					matrixStack.scale(scale.x(), scale.y(), scale.z());
					
					//Render the item
					Minecraft.getInstance().getItemRenderer().renderStatic(stack,  ItemTransforms.TransformType.FIXED, lightLevel, OverlayTexture.NO_OVERLAY, matrixStack, renderTypeBuffer, id);
				
					matrixStack.popPose();
					
				}
				
				
			}
			
		}
	}
	
}
