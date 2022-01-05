package io.github.lightman314.lightmanscurrency.blocks;

import java.util.List;

import io.github.lightman314.lightmanscurrency.tileentity.ItemInterfaceTileEntity.IItemHandlerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IItemTraderBlock extends ITraderBlock, IItemHandlerBlock{
	
	/**
	 * Gets the item display render transform/position for the trade at the given tradeSlot.
	 */
	@OnlyIn(Dist.CLIENT)
	public List<Vector3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isBlock);
	
	/**
	 * Gets the item display render rotation for the trade at the given tradeSlot.
	 */
	@OnlyIn(Dist.CLIENT)
	public List<Quaternion> GetStackRenderRot(int tradeSlot, BlockState state, boolean isBlock);
	
	/**
	 * Gets the item display render scale for the trade at the given tradeSlot.
	 */
	@OnlyIn(Dist.CLIENT)
	public Vector3f GetStackRenderScale(int tradeSlot, BlockState state, boolean isBlock);
	
	@OnlyIn(Dist.CLIENT)
	public int maxRenderIndex();
	
}
