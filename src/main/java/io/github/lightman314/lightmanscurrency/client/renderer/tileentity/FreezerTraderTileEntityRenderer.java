package io.github.lightman314.lightmanscurrency.client.renderer.tileentity;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.tileentity.FreezerTraderTileEntity;
import io.github.lightman314.lightmanscurrency.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

public class FreezerTraderTileEntityRenderer extends TileEntityRenderer<FreezerTraderTileEntity>{

	public static final Item doorItem = ModItems.FREEZER_DOOR;
	
	public FreezerTraderTileEntityRenderer(TileEntityRendererDispatcher dispatcher)
	{
		super(dispatcher);
	}
	
	@Override
	public void render(FreezerTraderTileEntity tileEntity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int i, int i1)
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

				for(int pos = 0; pos < positions.size() && pos < tileEntity.getTradeStock(tradeSlot); pos++)
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
		
		//Render the door
		matrixStack.push();
		Vector3f corner = new Vector3f(0f,0f,0f);
		Vector3f right = new Vector3f(1f, 0f, 0f);
		Vector3f forward = new Vector3f(0f, 0f, 1f);
		Block freezerBlock = tileEntity.getBlockState().getBlock();
		Direction facing = Direction.SOUTH;
		if(freezerBlock instanceof IRotatableBlock)
		{
			IRotatableBlock block = (IRotatableBlock)freezerBlock;
			facing = block.getFacing(tileEntity.getBlockState());
			corner = block.getOffsetVect(facing);
			right = block.getRightVect(facing);
			forward = block.getForwardVect(facing);
		}
		//Calculate the hinge position
		Vector3f hinge = MathUtil.VectorAdd(corner, MathUtil.VectorMult(right, 15.5f/16f), MathUtil.VectorMult(forward, 3.5f/16f));
		
		Quaternion rotation = Vector3f.YP.rotationDegrees(facing.getHorizontalIndex() * -90f + (90f * tileEntity.getDoorAngle(partialTicks)));
		
		matrixStack.translate(hinge.getX(), hinge.getY(), hinge.getZ());
		matrixStack.rotate(rotation);
		
		ItemStack stack = new ItemStack(doorItem);
		Minecraft.getInstance().getItemRenderer().renderItem(stack,  ItemCameraTransforms.TransformType.FIXED, i, i1, matrixStack, renderTypeBuffer);
		
		matrixStack.pop();
		
	}
	
}
