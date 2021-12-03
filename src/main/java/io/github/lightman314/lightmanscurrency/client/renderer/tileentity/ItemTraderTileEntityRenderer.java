package io.github.lightman314.lightmanscurrency.client.renderer.tileentity;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.tileentity.ItemTraderTileEntity;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

public class ItemTraderTileEntityRenderer extends TileEntityRenderer<ItemTraderTileEntity>{

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
	
	public ItemTraderTileEntityRenderer(TileEntityRendererDispatcher dispatcher)
	{
		super(dispatcher);
	}
	
	@Override
	public void render(ItemTraderTileEntity tileEntity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int i, int i1)
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
					//LightmansCurrency.LOGGER.info("Rendering '" + stack.getItem().getRegistryName().toString() + "' as an item.");
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
					
					matrixStack.push();
					
					Vector3f position = positions.get(pos);
					
					//Translate, rotate, and scale the matrix stack
					matrixStack.translate(position.getX(), position.getY(), position.getZ());
					for(Quaternion rot : rotation)
					{
						matrixStack.rotate(rot);
					}
					matrixStack.scale(scale.getX(), scale.getY(), scale.getZ());
					
					//Render the item
					Minecraft.getInstance().getItemRenderer().renderItem(stack,  ItemCameraTransforms.TransformType.FIXED, i, i1, matrixStack, renderTypeBuffer);
				
					matrixStack.pop();
					
				}
				
				
			}
			
		}
	}
	
}
