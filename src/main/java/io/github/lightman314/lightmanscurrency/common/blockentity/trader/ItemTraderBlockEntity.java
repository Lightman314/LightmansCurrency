package io.github.lightman314.lightmanscurrency.common.blockentity.trader;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.ItemTraderBlockEntityRenderer;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IItemTraderBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class ItemTraderBlockEntity extends TraderBlockEntity<ItemTraderData> {

	protected int tradeCount;
	protected boolean networkTrader;
	
	public ItemTraderBlockEntity() { this(ModBlockEntities.ITEM_TRADER.get(), 1, false); }
	
	public ItemTraderBlockEntity(int tradeCount) { this(ModBlockEntities.ITEM_TRADER.get(), tradeCount, false); }
	
	public ItemTraderBlockEntity(int tradeCount, boolean networkTrader) { this(ModBlockEntities.ITEM_TRADER.get(), tradeCount, networkTrader); }
	
	protected ItemTraderBlockEntity(TileEntityType<?> type) { this(type, 1, false);}
	
	protected ItemTraderBlockEntity(TileEntityType<?> type, int tradeCount) { this(type, tradeCount, false); }
	
	protected ItemTraderBlockEntity(TileEntityType<?> type, int tradeCount, boolean networkTrader)
	{
		super(type);
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
			return Lists.newArrayList(new Vector3f(0f,0f,0f));
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
				rotation.add(ItemTraderBlockEntityRenderer.getRotation(partialTicks));
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
	
	@Nonnull
	@Override
	public CompoundNBT save(@Nonnull CompoundNBT compound)
	{
		compound = super.save(compound);
		compound.putInt("TradeCount", this.tradeCount);
		compound.putBoolean("NetworkTrader", this.networkTrader);
		return compound;
	}
	
	@Override
	public void load(@Nonnull BlockState state, @Nonnull CompoundNBT compound)
	{
		super.load(state, compound);
		this.tradeCount = compound.getInt("TradeCount");
		this.networkTrader = compound.getBoolean("NetworkTrader");
	}

	@Override @Deprecated
	protected ItemTraderData createTraderFromOldData(CompoundNBT compound) {
		ItemTraderData newTrader = new ItemTraderData(1, this.level, this.worldPosition);
		newTrader.loadOldBlockEntityData(compound);
		this.tradeCount = newTrader.getTradeCount();
		return newTrader;
	}

	@Override
	protected void loadAsFormerNetworkTrader(ItemTraderData trader, CompoundNBT compound) {
		this.tradeCount = trader == null ? 1 : trader.getTradeCount();
		this.networkTrader = true;
	}
}