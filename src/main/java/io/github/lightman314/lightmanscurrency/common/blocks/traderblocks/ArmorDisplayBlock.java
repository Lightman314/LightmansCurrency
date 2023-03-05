package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import java.util.ArrayList;
import java.util.List;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates.TraderBlockTallRotatable;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ArmorDisplayTraderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.NotNull;

public class ArmorDisplayBlock extends TraderBlockTallRotatable implements IItemTraderBlock {
	
	public ArmorDisplayBlock(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public BlockEntity makeTrader(BlockPos pos, BlockState state) {
		ArmorDisplayTraderBlockEntity trader = new ArmorDisplayTraderBlockEntity(pos, state);
		trader.flagAsLoaded();
		return trader;
	}
	
	@Override
	public BlockEntityType<?> traderType() { return ModBlockEntities.ARMOR_TRADER.get(); }
	
	@Override
	public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof ArmorDisplayTraderBlockEntity)
			((ArmorDisplayTraderBlockEntity)blockEntity).destroyArmorStand();
		super.onRemove(state, level, pos, newState, isMoving);
	}
	
	@Override
	public List<Vector3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isDoubleTrade) { return new ArrayList<>(); }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public List<Quaternion> GetStackRenderRot(int tradeSlot, BlockState state) { return new ArrayList<>(); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public float GetStackRenderScale(int tradeSlot, BlockState state){ return 0f; }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public int maxRenderIndex() { return -1; }
	
	@Override
	protected NonNullSupplier<List<Component>> getItemTooltips() { return LCTooltips.ITEM_TRADER_ARMOR; }
	
}
