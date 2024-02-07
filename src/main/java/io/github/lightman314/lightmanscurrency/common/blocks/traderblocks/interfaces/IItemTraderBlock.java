package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces;

import java.util.ArrayList;
import java.util.List;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.ITraderBlock;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.ItemPositionBlockManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.ItemPositionData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public interface IItemTraderBlock extends ITraderBlock {

	@OnlyIn(Dist.CLIENT)
	@Nonnull
	default ItemPositionData getItemPositionData() { return this instanceof Block b ? ItemPositionBlockManager.getDataForBlock(b) : ItemPositionData.EMPTY; }

	/**
	 * Gets the item display renderBG transform/position for the trade at the given tradeSlot.
	 */
	@OnlyIn(Dist.CLIENT)
	@Deprecated(since = "2.1.2.4")
	default List<Vector3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isDoubleTrade) { return new ArrayList<>(); }
	
	/**
	 * Gets the item display renderBG rotation for the trade at the given tradeSlot.
	 */
	@OnlyIn(Dist.CLIENT)
	@Deprecated(since = "2.1.2.4")
	default List<Quaternion> GetStackRenderRot(int tradeSlot, BlockState state) { return new ArrayList<>(); }
	
	/**
	 * Gets the item display renderBG scale for the trade at the given tradeSlot.
	 */
	@OnlyIn(Dist.CLIENT)
	@Deprecated(since = "2.1.2.4")
	default float GetStackRenderScale(int tradeSlot, BlockState state) { return 1f; }
	
	@OnlyIn(Dist.CLIENT)
	@Deprecated(since = "2.1.2.4")
	default int maxRenderIndex() { return 0; }
	
}
