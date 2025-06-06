package io.github.lightman314.lightmanscurrency.common.blockentity.trader;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettings;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.IDirectionalSettingsHolder;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.PaygateBlock;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.OutputConflictHandling;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PaygateBlockEntity extends TraderBlockEntity<PaygateTraderData> implements IDirectionalSettingsHolder {
	
	private final Map<Direction,SidedData> data = new HashMap<>();
	public boolean allowOutputSide(Direction side) { return this.data.containsKey(side); }

	private final Map<Direction,Integer> simplifiedData = new HashMap<>();

	public PaygateBlockEntity(BlockPos pos, BlockState state) { this(ModBlockEntities.PAYGATE.get(), pos, state); }
	
	protected PaygateBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

	@Nullable
	@Override
	protected PaygateTraderData castOrNullify(@Nonnull TraderData trader) {
		if(trader instanceof PaygateTraderData pg)
			return pg;
		return null;
	}

	@Override
	public void saveAdditional(@Nonnull CompoundTag compound) {
		super.saveAdditional(compound);

		this.saveRedstoneData(compound);
	}

	public final void saveRedstoneData(CompoundTag compound) {
		CompoundTag dataTag = new CompoundTag();
		this.data.forEach((side,data) -> dataTag.put(side.toString(),data.save()));
		compound.put("OutputData",dataTag);
	}

	public final CompoundTag saveSimpleTimerData() {
		CompoundTag compound = new CompoundTag();
		CompoundTag dataTag = new CompoundTag();
		this.data.forEach((side,data) -> dataTag.putInt(side.toString(),data.timer));
		compound.put("SimpleTimer",dataTag);
		return compound;
	}
	
	@Override
	public void load(@Nonnull CompoundTag compound) {
		
		//Load the timer
		if(compound.contains("Timer", Tag.TAG_INT))
		{
			int timer = compound.getInt("Timer");
			DirectionalSettings sides = new DirectionalSettings(new DummyHolder());
			int power = 15;
			if(compound.contains("OutputSides") && compound.contains("Power"))
			{
				//Load output sides
				sides.load(compound,"OutputSides");
				power = compound.getInt("Power");
			}
			else
			{
				for(Direction side : Direction.values())
				{
					sides.setState(side, DirectionalSettingsState.OUTPUT);
				}
			}
			this.data.clear();
			for(Direction side : Direction.values())
			{
				if(sides.allowOutputs(side))
					this.data.put(side,new SidedData(power,timer,""));
			}
		}
		if(compound.contains("OutputData"))
		{
			this.data.clear();
			CompoundTag data = compound.getCompound("OutputData");
			for(Direction side : Direction.values())
			{
				if(data.contains(side.toString()))
					this.data.put(side,SidedData.load(data.getCompound(side.toString())));
			}
		}
		else if(compound.contains("SimpleTimer"))
		{
			this.simplifiedData.clear();
			CompoundTag data = compound.getCompound("SimpleTimer");
			for(Direction side : Direction.values())
			{
				if(data.contains(side.toString()))
					this.simplifiedData.put(side,data.getInt(side.toString()));
			}
		}
		
		super.load(compound);
		
	}

	public static List<OutputVisibilityData> parseVisibilityData(CompoundTag dataTag)
	{
		Map<Direction,SidedData> data = new HashMap<>();
		for(Direction side : Direction.values())
		{
			if(dataTag.contains(side.toString()))
				data.put(side,SidedData.load(dataTag.getCompound(side.toString())));
		}
		Multimap<OutputDataKey,Direction> temp = HashMultimap.create();
		data.forEach((side,d) -> temp.put(new OutputDataKey(d.powerLevel,d.timer,d.name),side));
		List<OutputVisibilityData> results = new ArrayList<>();
		temp.asMap().forEach((d,sides) -> results.add(new OutputVisibilityData(ImmutableList.copyOf(sides),d.name,d.power,d.timer)));
		//Sort by hash
		results.sort(Comparator.comparingInt(OutputVisibilityData::partialHash));
		return results;
	}
	
	public boolean isActive() { return this.isClient() ? !this.simplifiedData.isEmpty() : !this.data.isEmpty(); }
	public List<Direction> getActiveSides() { return this.isClient() ? new ArrayList<>(this.simplifiedData.keySet()) : new ArrayList<>(this.data.keySet()); }
	public int getTimeRemaining(DirectionalSettings sides)
	{
		AtomicInteger time = new AtomicInteger(0);
		if(this.isClient())
		{
			this.simplifiedData.forEach((side,timer) -> {
				if(sides.allowOutputs(side))
					time.set(Math.max(time.get(),timer));
			});
		}
		else
		{
			this.data.forEach((side,data) -> {
				if(sides.allowOutputs(side))
					time.set(Math.max(time.get(),data.timer));
			});
		}
		return time.get();
	}

	public int getPowerLevel(Direction relativeSide) {
		if(this.data.containsKey(relativeSide))
			return this.data.get(relativeSide).powerLevel;
		return 0;
	}

	public void activate(int duration, int level, DirectionalSettings outputSides, OutputConflictHandling conflictHandling, String name) {
		for(Direction side : Direction.values())
		{
			if(outputSides.allowOutputs(side))
			{
				int newTime = duration;
				if(this.data.containsKey(side) && conflictHandling == OutputConflictHandling.ADD_TIME)
					newTime += this.data.get(side).timer;
				this.data.put(side,new SidedData(level,newTime,name));
			}
		}
		//Update block state last as we need all data saved to the BE before notifying the neighbors about the change
		//Flag of two as we don't want to update neighbors with this change
		this.level.setBlock(this.worldPosition, this.level.getBlockState(this.worldPosition).setValue(PaygateBlock.POWERED,true),2);
		//Manually update neighbors here so that they'll always be updated even if the "powered" state didn't change
		this.level.updateNeighborsAt(this.worldPosition,this.getBlockState().getBlock());
		this.markTimerDirty();
	}
	
	@Override
	public void serverTick()
	{
		super.serverTick();
		List<Direction> cleanSides = new ArrayList<>();
		boolean changed = false;
		boolean removalChanged = false;
		for(Direction side : new ArrayList<>(this.data.keySet()))
		{
			changed = true;
			if(this.data.get(side).tickTimer())
			{
				this.data.remove(side);
				removalChanged = true;
			}
		}
		if(changed)
			this.markTimerDirty();
		if(removalChanged)
		{
			if(this.data.isEmpty()) //Update the block state to unpowered
				this.level.setBlock(this.worldPosition,this.level.getBlockState(this.worldPosition).setValue(PaygateBlock.POWERED,false),2);
			//Notify neighbors of the output state change
			this.level.updateNeighborsAt(this.worldPosition,this.level.getBlockState(this.worldPosition).getBlock());
		}
	}
	
	public void markTimerDirty() {
		this.setChanged();
		if(!this.level.isClientSide)
			BlockEntityUtil.sendUpdatePacket(this, this.saveSimpleTimerData());
	}
	
	public int getValidTicketTrade(Player player, ItemStack heldItem) {
		PaygateTraderData trader = this.getTraderData();
		if(TicketItem.isTicketOrPass(heldItem))
		{
			long ticketID = TicketItem.GetTicketID(heldItem);
			if(ticketID >= -1)
			{
				TradeContext context = TradeContext.create(trader,player).build();
				for(int i = 0; i < trader.getTradeCount(); ++i)
				{
					PaygateTradeData trade = trader.getTrade(i);
					if(trade.isTicketTrade() && trade.getTicketID() == ticketID)
					{
						//Confirm that the player is allowed to access the trade
						if(!trader.runPreTradeEvent(trade, context).isCanceled())
							return i;
					}
				}
			}
		}
		return -1;
	}

	@Nonnull
    @Override
	protected PaygateTraderData buildNewTrader() { return new PaygateTraderData(this.level, this.worldPosition); }

	private static class SidedData {
		private final int powerLevel;
		private int timer;
		private final String name;
		private SidedData(int power, int timer, @Nullable String name) {
			this.powerLevel = power;
			this.timer = timer;
			if(name == null || name.isBlank())
				this.name = null;
			else
				this.name = name;
		}
		boolean tickTimer() { return --this.timer <= 0; }
		CompoundTag save()
		{
			CompoundTag entry = new CompoundTag();
			entry.putInt("Power",this.powerLevel);
			entry.putInt("Timer",this.timer);
			if(this.name != null)
				entry.putString("Name",this.name);
			return entry;
		}
		static SidedData load(CompoundTag entry) {
			String name = null;
			if(entry.contains("Name"))
				name = entry.getString("Name");
			return new SidedData(entry.getInt("Power"),entry.getInt("Timer"),name);
		}
	}

	public record OutputVisibilityData(List<Direction> sides, @Nullable String name, int power, int timer) {
		//Non-timer dependent hash code for sorting purposes
		int partialHash() { return Objects.hash(this.sides,this.name,this.power); }
	}

	private record OutputDataKey(int power, int timer, String name) { }

	public static class DummyHolder implements IDirectionalSettingsHolder
	{
		@Override
		public boolean allowInputs() { return false; }
	}
	
}
