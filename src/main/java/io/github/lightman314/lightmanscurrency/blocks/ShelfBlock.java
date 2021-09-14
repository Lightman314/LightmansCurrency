package io.github.lightman314.lightmanscurrency.blocks;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import io.github.lightman314.lightmanscurrency.blockentity.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.TickableBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.blocks.util.TickerUtil;
import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ShelfBlock extends RotatableBlock implements IItemTraderBlock, EntityBlock{
	
	public static final int TRADECOUNT = 1;
	
	private static final VoxelShape SHAPE_NORTH = box(0d,0d,0d,16d,16d,5d);
	private static final VoxelShape SHAPE_SOUTH = box(0d,0d,11d,16d,16d,16d);
	private static final VoxelShape SHAPE_EAST = box(11d,0d,0d,16d,16d,16d);
	private static final VoxelShape SHAPE_WEST = box(0d,0d,0d,5d,16d,16d);
	private static final Function<Direction,VoxelShape> LAZY_SHAPE = LazyShapes.lazyDirectionalShape(SHAPE_NORTH, SHAPE_EAST, SHAPE_SOUTH, SHAPE_WEST);
	
	public ShelfBlock(Properties properties)
	{
		super(properties);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
		return TickerUtil.createTickerHelper(type, ModBlockEntities.ITEM_TRADER, TickableBlockEntity::tickHandler);
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new ItemTraderBlockEntity(pos, state, TRADECOUNT);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		if(!level.isClientSide)
		{
			//Open UI
			BlockEntity tileEntity = level.getBlockEntity(pos);
			if(tileEntity instanceof ItemTraderBlockEntity)
			{
				ItemTraderBlockEntity trader = (ItemTraderBlockEntity)tileEntity;
				//Update the owner
				if(trader.isOwner(player) && !trader.isCreative())
				{
					//CurrencyMod.LOGGER.info("Updating the owner name.");
					trader.setOwner(player);
				}
				TileEntityUtil.sendUpdatePacket(tileEntity);
				trader.openTradeMenu(player);
			}
		}
		return InteractionResult.SUCCESS;
	}
	
	/*@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos posIn, Random randomIn) {
        
	}*/
	
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		if(!level.isClientSide)
		{
			ItemTraderBlockEntity tileEntity = (ItemTraderBlockEntity)level.getBlockEntity(pos);
			if(tileEntity != null)
			{
				tileEntity.setOwner(player);
				if(stack.hasCustomHoverName())
					tileEntity.setCustomName(stack.getDisplayName().getString());
			}
		}
	}
	
	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
	{
		
		//Prevent client-side multi-block destruction & breaking animations if they aren't allowed to break this trader
		TraderBlockEntity blockEntity = (TraderBlockEntity)getTileEntity(state, level, pos);
		if(blockEntity != null)
		{
			if(!blockEntity.canBreak(player))
				return;
			else
				blockEntity.dumpContents(level, pos);
		}
		
		super.playerWillDestroy(level, pos, state, player);
		
	}

	
	@Override
	@OnlyIn(Dist.CLIENT)
	public List<Vector3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isBlock) {
		List<Vector3f> posList = new ArrayList<Vector3f>(1);
		Direction facing = this.getFacing(state);
		//Define directions for easy positional handling
		Vector3f forward = this.getForwardVect(facing);
		Vector3f right = this.getRightVect(facing);
		Vector3f up = Vector3f.YP;
		Vector3f offset = this.getOffsetVect(facing);
		
		if(tradeSlot == 0)
		{
			Vector3f firstPosition = MathUtil.VectorAdd(offset, MathUtil.VectorMult(right, 0.5f), MathUtil.VectorMult(forward, 14.5f/16f), MathUtil.VectorMult(up, 9f/16f));
			posList.add(firstPosition); 
			for(float distance = isBlock ? -3.2f : -1f; distance >= -3f; distance -= isBlock ? 3.2f : 1f)
			{
				posList.add(MathUtil.VectorAdd(firstPosition, MathUtil.VectorMult(forward, distance/16f)));
			}
		}
		
		return posList;
	}
	
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public List<Quaternion> GetStackRenderRot(int tradeSlot, BlockState state, boolean isBlock)
	{
		//Return null for automatic rotation
		List<Quaternion> rotation = new ArrayList<>();
		int facing = MathUtil.getHorizontalFacing(state.getValue(FACING));
		rotation.add(Vector3f.YP.rotationDegrees(facing * -90f));
		return rotation;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Vector3f GetStackRenderScale(int tradeSlot, BlockState state, boolean isBlock){
		return new Vector3f(14f/16f, 14f/16f, 14f/16f);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public int maxRenderIndex()
	{
		return TRADECOUNT;
	}

	@Override
	public BlockEntity getTileEntity(BlockState state, LevelAccessor level, BlockPos pos) {
		return level.getBlockEntity(pos);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		return LAZY_SHAPE.apply(state.getValue(FACING));
	}
	
}
