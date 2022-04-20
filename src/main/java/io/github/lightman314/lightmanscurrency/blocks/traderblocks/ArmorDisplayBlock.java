package io.github.lightman314.lightmanscurrency.blocks.traderblocks;

import java.util.List;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import io.github.lightman314.lightmanscurrency.blockentity.ArmorDisplayTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.templates.TraderBlockTallRotatable;
import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.blockentity.ItemInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.ItemInterfaceBlockEntity.IItemHandlerBlock;
import io.github.lightman314.lightmanscurrency.blockentity.ItemInterfaceBlockEntity.IItemHandlerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ArmorDisplayBlock extends TraderBlockTallRotatable implements IItemTraderBlock{
	
	public static final int TRADECOUNT = 4;
	
	public ArmorDisplayBlock(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public BlockEntity makeTrader(BlockPos pos, BlockState state) { return new ArmorDisplayTraderBlockEntity(pos, state); }
	
	@Override
	protected BlockEntity makeDummy(BlockPos pos, BlockState state) { return new ItemInterfaceBlockEntity(pos, state); }
	
	@Override
	public BlockEntityType<?> traderType() { return ModBlockEntities.ARMOR_TRADER; }
	
	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
	{
		
		BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
		if(blockEntity instanceof ArmorDisplayTraderBlockEntity)
		{
			ArmorDisplayTraderBlockEntity trader = (ArmorDisplayTraderBlockEntity)blockEntity;
			if(trader.canBreak(player))
				trader.destroyArmorStand();
		}
		
		super.playerWillDestroy(level, pos, state, player);
		
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof ArmorDisplayTraderBlockEntity)
			((ArmorDisplayTraderBlockEntity)blockEntity).destroyArmorStand();
		super.onRemove(state, level, pos, newState, isMoving);
	}
	
	@Override
	public List<Vector3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isBlock) {
		return null;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public List<Quaternion> GetStackRenderRot(int tradeSlot, BlockState state, boolean isBlock)
	{
		return null;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Vector3f GetStackRenderScale(int tradeSlot, BlockState state, boolean isBlock){
		return null;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public int maxRenderIndex()
	{
		return -1;
	}

	@Override
	public Direction getRelativeSide(BlockState state, Direction side) {
		return IItemHandlerBlock.getRelativeSide(this.getFacing(state), side);
	}

	@Override
	public IItemHandlerBlockEntity getItemHandlerEntity(BlockState state, Level level, BlockPos pos) {
		BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
		if(blockEntity instanceof IItemHandlerBlockEntity)
			return (IItemHandlerBlockEntity)blockEntity;
		return null;
	}
	
}
