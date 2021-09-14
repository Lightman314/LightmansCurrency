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
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DisplayCaseBlock extends Block implements IItemTraderBlock, EntityBlock{
	
	public static final int TRADECOUNT = 1;
	
	public DisplayCaseBlock(Properties properties)
	{
		super(properties);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
		return TickerUtil.createTickerHelper(type, ModBlockEntities.ITEM_TRADER, TickableBlockEntity::tickHandler);
	}
	
	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		return Shapes.empty();
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		return LazyShapes.BOX_T;
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
			BlockEntity blockEntity = this.getTileEntity(state, level, pos);
			if(blockEntity instanceof ItemTraderBlockEntity)
			{
				ItemTraderBlockEntity trader = (ItemTraderBlockEntity)blockEntity;
				//Update the owner
				if(trader.isOwner(player) && !trader.isCreative())
				{
					//CurrencyMod.LOGGER.info("Updating the owner name.");
					trader.setOwner(player);
				}
				TileEntityUtil.sendUpdatePacket(blockEntity);
				trader.openTradeMenu(player);
			}
		}
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		if(!level.isClientSide)
		{
			ItemTraderBlockEntity traderEntity = (ItemTraderBlockEntity)level.getBlockEntity(pos);
			if(traderEntity != null)
			{
				traderEntity.setOwner(player);
				if(stack.hasCustomHoverName())
					traderEntity.setCustomName(stack.getDisplayName().getString());
			}
		}
	}
	
	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
	{
		
		//Prevent client-side multi-block destruction & breaking animations if they aren't allowed to break this trader
		BlockEntity blockEntity = this.getTileEntity(state, level, pos);
		if(blockEntity instanceof TraderBlockEntity)
		{
			TraderBlockEntity traderEntity = (TraderBlockEntity)blockEntity;
			if(traderEntity != null)
			{
				if(!traderEntity.canBreak(player))
					return;
				else
					traderEntity.dumpContents(level, pos);
			}
		}
		
		
		super.playerWillDestroy(level, pos, state, player);
		
	}

	
	@Override
	@OnlyIn(Dist.CLIENT)
	public List<Vector3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isBlock) {
		List<Vector3f> posList = new ArrayList<Vector3f>(1);
		posList.add(new Vector3f(0.5F, 0.5F + 2F/16F, 0.5F));
		return posList;
	}
	
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public List<Quaternion> GetStackRenderRot(int tradeSlot, BlockState state, boolean isBlock)
	{
		//Return null for automatic rotation
		return null;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Vector3f GetStackRenderScale(int tradeSlot, BlockState state, boolean isBlock){
		return new Vector3f(0.75F, 0.75F, 0.75F);
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
