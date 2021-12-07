package io.github.lightman314.lightmanscurrency.blocks.traderblocks;

import java.util.List;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import io.github.lightman314.lightmanscurrency.blockentity.ArmorDisplayTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.templates.TraderBlockTallRotatable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
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
	
}
