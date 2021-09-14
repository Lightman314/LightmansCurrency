package io.github.lightman314.lightmanscurrency.blocks;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CardDisplayBlock extends RotatableBlock implements IItemTraderBlock, EntityBlock{
	
	public static final int TRADECOUNT = 4;
	
	public CardDisplayBlock(Properties properties)
	{
		super(properties, LazyShapes.BOX_T);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
		return TickerUtil.createTickerHelper(type, ModBlockEntities.ITEM_TRADER, TickableBlockEntity::tickHandler);
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ItemTraderBlockEntity(pos, state, TRADECOUNT);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player playerEntity, InteractionHand hand, BlockHitResult result)
	{
		if(!level.isClientSide)
		{
			//Open UI
			BlockEntity blockEntity = this.getTileEntity(state, level, pos);
			if(blockEntity instanceof ItemTraderBlockEntity)
			{
				ItemTraderBlockEntity trader = (ItemTraderBlockEntity)blockEntity;
				//Update the owner
				if(trader.isOwner(playerEntity) && !trader.isCreative())
				{
					//CurrencyMod.LOGGER.info("Updating the owner name.");
					trader.setOwner(playerEntity);
				}
				TileEntityUtil.sendUpdatePacket(blockEntity);
				trader.openTradeMenu(playerEntity);
			}
		}
		return InteractionResult.SUCCESS;
	}
	
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
	public List<Vector3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isBlock) {
		//Get facing
		Direction facing = this.getFacing(state);
		//Define directions for easy positional handling
		Vector3f forward = this.getForwardVect(facing);
		Vector3f right = this.getRightVect(facing);
		Vector3f up = Vector3f.YP;
		Vector3f offset = this.getOffsetVect(facing);
		
		Vector3f firstPosition = null;
		
		if(tradeSlot == 0)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 9f/16f);
			Vector3f forwardOffset = MathUtil.VectorMult(forward, 4.5f/16f);
			firstPosition = MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 1)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 11f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 9f/16f);
			Vector3f forwardOffset = MathUtil.VectorMult(forward, 4.5f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 2)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 12f/16f);
			Vector3f forwardOffset = MathUtil.VectorMult(forward, 12f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 3)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 11f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 12f/16f);
			Vector3f forwardOffset = MathUtil.VectorMult(forward, 12f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		
		List<Vector3f> posList = new ArrayList<>(3);
		if(firstPosition != null)
		{
			posList.add(firstPosition);
			for(float distance = isBlock ? 3.2f : 0.5f; distance < 4f; distance += isBlock ? 3.2f : 0.5f)
			{
				posList.add(MathUtil.VectorAdd(firstPosition, MathUtil.VectorMult(up, distance/16F)));
			}
		}
		else
		{
			posList.add(new Vector3f(0F, 1f, 0F));
		}
		return posList;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public List<Quaternion> GetStackRenderRot(int tradeSlot, BlockState state, boolean isBlock)
	{
		List<Quaternion> rotation = new ArrayList<>();
		int facing = MathUtil.getHorizontalFacing(state.getValue(FACING));
		rotation.add(Vector3f.YP.rotationDegrees(facing * -90f));
		rotation.add(Vector3f.XP.rotationDegrees(90f));
		return rotation;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Vector3f GetStackRenderScale(int tradeSlot, BlockState state, boolean isBlock){
		return new Vector3f(0.4f, 0.4f, 0.4f);
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
	
}
