package io.github.lightman314.lightmanscurrency.blocks;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import io.github.lightman314.lightmanscurrency.blockentity.ArmorDisplayTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.DummyBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.TickableBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.util.TickerUtil;
import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ArmorDisplayBlock extends TallRotatableTraderBlock implements IItemTraderBlock, EntityBlock{
	
	public static final int TRADECOUNT = 4;
	
	protected static final BooleanProperty ISBOTTOM = BlockStateProperties.BOTTOM;
	
	public ArmorDisplayBlock(Properties properties)
	{
		super(properties, TRADECOUNT);
		this.flagAsTransparent();
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
		return TickerUtil.createTickerHelper(type, ModBlockEntities.ARMOR_TRADER, TickableBlockEntity::tickHandler);
	}
	
	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
	{
		
		//Default Tall Block Functionality
		super.playerWillDestroy(level, pos, state, player);
		
		//Abort if the player wasn't able to break it.
		if(!this.playerCanBreak(level, pos, state, player))
			return;
		
		//Destroy the armor stand
		TraderBlockEntity traderBlockEntity = (TraderBlockEntity)getTileEntity(state, level, pos);
		if(traderBlockEntity instanceof ArmorDisplayTraderBlockEntity)
		{
			((ArmorDisplayTraderBlockEntity)traderBlockEntity).destroyArmorStand();
		}
		
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		if(state.getValue(ISBOTTOM))
			return new ArmorDisplayTraderBlockEntity(pos, state);
		return new DummyBlockEntity(pos, state);
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
		return 0;
	}
	
}
