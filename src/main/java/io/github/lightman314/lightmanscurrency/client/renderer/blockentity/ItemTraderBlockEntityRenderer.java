package io.github.lightman314.lightmanscurrency.client.renderer.blockentity;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.ItemTradeData;
import io.github.lightman314.lightmanscurrency.blockentity.ItemTraderBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class ItemTraderBlockEntityRenderer implements BlockEntityRenderer<ItemTraderBlockEntity>{

	public ItemTraderBlockEntityRenderer(BlockEntityRendererProvider.Context context)
	{
	}
	
	/*protected int getLightLevel(BlockEntity blockEntity)
	{
		return getLightLevel(blockEntity.getLevel(), blockEntity.getBlockPos());
	}
	
	protected int getLightLevel(Level level, BlockPos pos)
	{
		LightmansCurrency.LogInfo("Light at " + pos.toString() + ": " + level.getRawBrightness(pos, 0));
		return level.getRawBrightness(pos, level.getSkyDarken());
	}*/
	
	@Override
	public void render(ItemTraderBlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int lightLevel, int id)
	{
		
		for(int tradeSlot = 0; tradeSlot < blockEntity.getTradeCount() && tradeSlot < blockEntity.maxRenderIndex(); tradeSlot++)
		{
			
			ItemTradeData trade = blockEntity.getTrade(tradeSlot);
			if(!trade.getSellItem().isEmpty())
			{
				
				ItemStack stack = trade.getSellItem();
				
				boolean isBlock = stack.getItem() instanceof BlockItem;
				if(isBlock && Config.CLIENT.renderBlocksAsItems.get().contains(stack.getItem().getRegistryName().toString()))
				{
					//LightmansCurrency.LOGGER.info("Rendering '" + stack.getItem().getRegistryName().toString() + "' as an item.");
					isBlock = false;
				}
				
				//Get positions
				List<Vector3f> positions = blockEntity.GetStackRenderPos(tradeSlot, isBlock);
				
				//Get rotation
				List<Quaternion> rotation = blockEntity.GetStackRenderRot(tradeSlot, partialTicks, isBlock);
				
				//Get scale
				Vector3f scale = blockEntity.GetStackRenderScale(tradeSlot, isBlock);

				for(int pos = 0; pos < positions.size() && pos < blockEntity.getTradeStock(tradeSlot); pos++)
				{
					
					poseStack.pushPose();;
					
					Vector3f position = positions.get(pos);
					
					//Translate, rotate, and scale the matrix stack
					poseStack.translate(position.x(), position.y(), position.z());
					for(Quaternion rot : rotation)
					{
						poseStack.mulPose(rot);
					}
					poseStack.scale(scale.x(), scale.y(), scale.z());
					
					//Render the item
					//BakedModel bakedModel = Minecraft.getInstance().getItemRenderer().getModel(stack, blockEntity.getLevel(), null, i1);
					//LightmansCurrency.LogInfo("Light level for block at " + blockEntity.getBlockPos().toString() + " is " + lightLevel);
					Minecraft.getInstance().getItemRenderer().renderStatic(stack, TransformType.FIXED, lightLevel, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, id);
					//(stack,  TransformType.FIXED, i, i1, poseStack, renderTypeBuffer);
				
					poseStack.popPose();
					
				}
				
				
			}
			
		}
	}
	
}
