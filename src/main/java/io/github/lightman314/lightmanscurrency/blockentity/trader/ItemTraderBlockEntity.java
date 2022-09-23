package io.github.lightman314.lightmanscurrency.blockentity.trader;

import java.util.ArrayList;
import java.util.List;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.interfaces.IItemTraderBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemTraderBlockEntity extends TraderBlockEntity<ItemTraderData>{
	
	protected long rotationTime = 0;
	protected int tradeCount;
	protected boolean networkTrader = false;
	
	public ItemTraderBlockEntity(BlockPos pos, BlockState state) { this(ModBlockEntities.ITEM_TRADER.get(), pos, state, 1, false); }
	
	public ItemTraderBlockEntity(BlockPos pos, BlockState state, int tradeCount) { this(ModBlockEntities.ITEM_TRADER.get(), pos, state, tradeCount, false); }
	
	public ItemTraderBlockEntity(BlockPos pos, BlockState state, int tradeCount, boolean networkTrader) { this(ModBlockEntities.ITEM_TRADER.get(), pos, state, tradeCount, networkTrader); }
	
	protected ItemTraderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { this(type, pos, state, 1, false);}
	
	protected ItemTraderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int tradeCount) { this(type, pos, state, tradeCount, false); }
	
	protected ItemTraderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int tradeCount, boolean networkTrader)
	{
		super(type, pos, state);
		this.tradeCount = tradeCount;
		this.networkTrader = networkTrader;
	}
	
	public ItemTraderData buildNewTrader() {
		ItemTraderData trader = new ItemTraderData(this.tradeCount, this.level, this.worldPosition);
		if(this.networkTrader)
			trader.setAlwaysShowOnTerminal();
		return trader;
	}
	
	@OnlyIn(Dist.CLIENT)
	public List<Vector3f> GetStackRenderPos(int tradeSlot, boolean isDoubleTrade)
	{
		Block block = this.getBlockState().getBlock();
		if(block instanceof IItemTraderBlock)
		{
			IItemTraderBlock traderBlock = (IItemTraderBlock)block;
			return traderBlock.GetStackRenderPos(tradeSlot, this.getBlockState(), isDoubleTrade);
		}
		else
		{
			List<Vector3f> posList = new ArrayList<>();
			posList.add(new Vector3f(0.0f, 0.0f, 0.0f));
			return posList;
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public List<Quaternion> GetStackRenderRot(int tradeSlot, float partialTicks)
	{
		Block block = this.getBlockState().getBlock();
		if(block instanceof IItemTraderBlock)
		{
			IItemTraderBlock traderBlock = (IItemTraderBlock)block;
			List<Quaternion> rotation = traderBlock.GetStackRenderRot(tradeSlot, this.getBlockState());
			//If null received. Rotate item based on world time
			if(rotation == null)
			{
				rotation = new ArrayList<>();
				rotation.add(Vector3f.YP.rotationDegrees((this.rotationTime + partialTicks) * 2.0F));
			}
			return rotation;
		}
		else
		{
			List<Quaternion> rotation = new ArrayList<>();
			rotation.add(Vector3f.YP.rotationDegrees(0f));
			return rotation;
		}
			
	}
	
	@OnlyIn(Dist.CLIENT)
	public float GetStackRenderScale(int tradeSlot)
	{
		Block block = this.getBlockState().getBlock();
		if(block instanceof IItemTraderBlock)
		{
			IItemTraderBlock traderBlock = (IItemTraderBlock)block;
			return traderBlock.GetStackRenderScale(tradeSlot, this.getBlockState());
		}
		else
			return 0f;
	}
	
	@OnlyIn(Dist.CLIENT)
	public int maxRenderIndex()
	{
		Block block = this.getBlockState().getBlock();
		if(block instanceof IItemTraderBlock)
		{
			IItemTraderBlock traderBlock = (IItemTraderBlock)block;
			return traderBlock.maxRenderIndex();
		}
		else
			return 0;
	}
	
	@Override
	public void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);
		compound.putInt("TradeCount", this.tradeCount);
		compound.putBoolean("NetworkTrader", this.networkTrader);
	}
	
	@Override
	public void load(CompoundTag compound)
	{
		super.load(compound);
		this.tradeCount = compound.getInt("TradeCount");
		this.networkTrader = compound.getBoolean("NetworkTrader");
	}

	@Override
	public void clientTick() {
		
		super.clientTick();
		this.rotationTime++;
		
	}

	@Override @Deprecated
	protected ItemTraderData createTraderFromOldData(CompoundTag compound) {
		ItemTraderData newTrader = new ItemTraderData(1, this.level, this.worldPosition);
		newTrader.loadOldBlockEntityData(compound);
		this.tradeCount = newTrader.getTradeCount();
		return newTrader;
	}
	
}
