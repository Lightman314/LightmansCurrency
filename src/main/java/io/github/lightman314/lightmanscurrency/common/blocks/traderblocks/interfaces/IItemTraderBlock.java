package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces;

import java.util.List;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IItemTraderBlock extends ITraderBlock {
	
	/**
	 * Gets the item display render transform/position for the trade at the given tradeSlot.
	 */
	@OnlyIn(Dist.CLIENT)
	List<Vector3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isDoubleTrade);
	
	/**
	 * Gets the item display render rotation for the trade at the given tradeSlot.
	 */
	@OnlyIn(Dist.CLIENT)
	List<Quaternion> GetStackRenderRot(int tradeSlot, BlockState state);
	
	/**
	 * Gets the item display render scale for the trade at the given tradeSlot.
	 */
	@OnlyIn(Dist.CLIENT)
	float GetStackRenderScale(int tradeSlot, BlockState state);
	
	@OnlyIn(Dist.CLIENT)
	int maxRenderIndex();
	
}