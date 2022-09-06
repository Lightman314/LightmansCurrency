package io.github.lightman314.lightmanscurrency.blocks.traderblocks.interfaces;

import java.util.List;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import io.github.lightman314.lightmanscurrency.blockentity.ItemInterfaceBlockEntity.IItemHandlerBlock;
import io.github.lightman314.lightmanscurrency.blockentity.ItemInterfaceBlockEntity.IItemHandlerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IItemTraderBlock extends ITraderBlock, IItemHandlerBlock{
	
	/**
	 * Gets the item display render transform/position for the trade at the given tradeSlot.
	 */
	@OnlyIn(Dist.CLIENT)
	public List<Vector3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isDoubleTrade);
	
	/**
	 * Gets the item display render rotation for the trade at the given tradeSlot.
	 */
	@OnlyIn(Dist.CLIENT)
	public List<Quaternion> GetStackRenderRot(int tradeSlot, BlockState state);
	
	/**
	 * Gets the item display render scale for the trade at the given tradeSlot.
	 */
	@OnlyIn(Dist.CLIENT)
	public float GetStackRenderScale(int tradeSlot, BlockState state);
	
	@OnlyIn(Dist.CLIENT)
	public int maxRenderIndex();
	
	public default IItemHandlerBlockEntity getItemHandlerEntity(BlockState state, Level level, BlockPos pos) {
		if(level == null)
			return null;
		BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
		if(blockEntity instanceof IItemHandlerBlockEntity)
			return (IItemHandlerBlockEntity)blockEntity;
		return null;
	}
	
}
