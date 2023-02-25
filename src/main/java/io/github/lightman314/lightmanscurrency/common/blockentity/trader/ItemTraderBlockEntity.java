package io.github.lightman314.lightmanscurrency.common.blockentity.trader;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.ItemTraderBlockEntityRenderer;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IItemTraderBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ItemTraderBlockEntity extends TraderBlockEntity<ItemTraderData> {

	protected int tradeCount;
	protected boolean networkTrader;
	
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
		if(block instanceof IItemTraderBlock traderBlock)
			return traderBlock.GetStackRenderPos(tradeSlot, this.getBlockState(), isDoubleTrade);
		else
			return Lists.newArrayList(new Vector3f(0.0f, 0.0f, 0.0f));
	}
	
	@OnlyIn(Dist.CLIENT)
	public List<Quaternionf> GetStackRenderRot(int tradeSlot, float partialTicks)
	{
		Block block = this.getBlockState().getBlock();
		if(block instanceof IItemTraderBlock traderBlock)
		{
			List<Quaternionf> rotation = traderBlock.GetStackRenderRot(tradeSlot, this.getBlockState());
			//If null received. Rotate item based on world time
			if(rotation == null)
			{
				rotation = new ArrayList<>();
				rotation.add(ItemTraderBlockEntityRenderer.getRotation(partialTicks));
			}
			return rotation;
		}
		else
		{
			List<Quaternionf> rotation = new ArrayList<>();
			rotation.add(new Quaternionf().fromAxisAngleDeg(new Vector3f(0f,1f,0f), 0f));
			return rotation;
		}
			
	}
	
	@OnlyIn(Dist.CLIENT)
	public float GetStackRenderScale(int tradeSlot)
	{
		Block block = this.getBlockState().getBlock();
		if(block instanceof IItemTraderBlock traderBlock)
			return traderBlock.GetStackRenderScale(tradeSlot, this.getBlockState());
		else
			return 0f;
	}
	
	@OnlyIn(Dist.CLIENT)
	public int maxRenderIndex()
	{
		Block block = this.getBlockState().getBlock();
		if(block instanceof IItemTraderBlock traderBlock)
			return traderBlock.maxRenderIndex();
		else
			return 0;
	}
	
	@Override
	public void saveAdditional(@NotNull CompoundTag compound)
	{
		super.saveAdditional(compound);
		compound.putInt("TradeCount", this.tradeCount);
		compound.putBoolean("NetworkTrader", this.networkTrader);
	}
	
	@Override
	public void load(@NotNull CompoundTag compound)
	{
		super.load(compound);
		this.tradeCount = compound.getInt("TradeCount");
		this.networkTrader = compound.getBoolean("NetworkTrader");
	}

	@Override @Deprecated
	protected ItemTraderData createTraderFromOldData(CompoundTag compound) {
		ItemTraderData newTrader = new ItemTraderData(1, this.level, this.worldPosition);
		newTrader.loadOldBlockEntityData(compound);
		this.tradeCount = newTrader.getTradeCount();
		return newTrader;
	}
	
}
