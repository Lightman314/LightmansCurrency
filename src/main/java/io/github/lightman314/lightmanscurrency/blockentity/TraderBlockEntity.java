package io.github.lightman314.lightmanscurrency.blockentity;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.interfaces.IOwnableBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public abstract class TraderBlockEntity<D extends TraderData> extends TickableBlockEntity implements IOwnableBlockEntity {

	private long traderID = -1;
	public long getTraderID() { return this.traderID; }
	@Deprecated
	public void setTraderID(long traderID) { this.traderID = traderID; }
	
	private CompoundTag customTrader = null;
	private boolean ignoreCustomTrader = false; 
	
	private CompoundTag loadFromOldTag = null;
	
	private boolean legitimateBreak = false;
	public void flagAsLegitBreak() { this.legitimateBreak = true; }
	public boolean legitimateBreak() { return this.legitimateBreak; }
	
	public final boolean isClient() { return this.level == null ? false : this.level.isClientSide; }
	
	public TraderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	
	private final D buildTrader(Player owner, ItemStack placementStack)
	{
		if(this.customTrader != null)
		{
			D newTrader = this.fullyBuildCustomTrader();
			if(newTrader != null)
				return newTrader;
		}
		D newTrader = this.buildNewTrader();
		newTrader.getOwner().SetOwner(PlayerReference.of(owner));
		if(placementStack.hasCustomHoverName())
			newTrader.setCustomName(null, placementStack.getHoverName().getString());
		return newTrader;
	}
	
	@SuppressWarnings("unchecked")
	protected final D initCustomTrader()
	{
		try {
			return (D)TraderData.Deserialize(false, this.customTrader);
		} catch(Throwable t) { LightmansCurrency.LogError("Error while attempting to load the custom trader!", t); }
		return null;
	}
	
	
	protected final D fullyBuildCustomTrader()
	{
		try {
			D newTrader = this.initCustomTrader();
			this.moveCustomTrader(newTrader);
			return newTrader;
		} catch(Throwable t) { LightmansCurrency.LogError("Error while attempting to load the custom trader!", t); }
		return null;
	}
	
	protected final void moveCustomTrader(D customTrader)
	{
		if(customTrader != null)
			customTrader.move(this.level, this.worldPosition);
	}
	
	protected abstract D buildNewTrader();
	
	public final void saveCurrentTraderAsCustomTrader() {
		TraderData trader = this.getTraderData();
		if(trader != null)
		{
			this.customTrader = trader.save();
			this.ignoreCustomTrader = true;
			this.markDirty();
		}
	}
	
	public void initialize(Player owner, ItemStack placementStack)
	{
		if(this.getTraderData() != null)
			return;
		
		D newTrader = this.buildTrader(owner, placementStack);
		//Register to the trading office
		this.traderID = TraderSaveData.RegisterTrader(newTrader, owner);
		//Send update packet to connected clients, so that they'll have the new trader id.
		this.markDirty();
	}
	
	private TraderData getRawTraderData() { return TraderSaveData.GetTrader(this.isClient(), this.traderID); }
	
	@SuppressWarnings("unchecked")
	public D getTraderData()
	{
		//Get from trading office
		TraderData rawData = this.getRawTraderData();
		try {
			return (D)rawData;
		} catch(Throwable t) { t.printStackTrace(); return null; }
	}
	
	@Override
	public void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
		compound.putLong("TraderID", this.traderID);
		if(this.customTrader != null)
			compound.put("CustomTrader", this.customTrader);
	}
	
	public void load(CompoundTag compound)
	{
		super.load(compound);
		if(compound.contains("TraderID", Tag.TAG_LONG))
			this.traderID = compound.getLong("TraderID");
		if(compound.contains("CustomTrader"))
			this.customTrader = compound.getCompound("CustomTrader");
		
		//Convert from old trader types
		if(compound.contains("CoreSettings"))
			this.loadFromOldTag = compound;
		
	}
	
	@Override
	public void serverTick() {
		if(this.level == null)
			return;
		if(this.loadFromOldTag != null)
		{
			D newTrader = this.createTraderFromOldData(this.loadFromOldTag);
			this.loadFromOldTag = null;
			if(newTrader != null)
			{
				this.traderID = TraderSaveData.RegisterTrader(newTrader, null);
				this.markDirty();
			}
			else
			{
				LightmansCurrency.LogError("Failed to load trader from old data at " + this.worldPosition.toShortString());
			}
		}
		if(this.customTrader != null && !this.ignoreCustomTrader)
		{
			//Build the custom trader
			D customTrader = this.initCustomTrader();
			if(customTrader == null)
			{
				LightmansCurrency.LogWarning("The trader block at " + this.worldPosition.toShortString() + " could not properly load it's custom trader.");
				this.customTrader = null;
			}
			//Check if the custom trader is this position & dimension
			if(customTrader.getLevel() == this.level.dimension() && this.worldPosition.equals(customTrader.getPos()))
				this.ignoreCustomTrader = true;
			else
			{
				//If the dimension and position don't match exactly, assume it's been moved and load the custom trader
				this.moveCustomTrader(customTrader);
				this.traderID = TraderSaveData.RegisterTrader(customTrader, null);
				this.customTrader = null;
				this.ignoreCustomTrader = true;
				this.markDirty();
				LightmansCurrency.LogInfo("Successfully loaded custom trader at " + this.worldPosition.toShortString());
			}
		}
	}
	
	public final void markDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this);
	}
	
	protected abstract D createTraderFromOldData(CompoundTag compound);
	
	@Override
	public void onLoad()
	{
		if(this.level.isClientSide)
			BlockEntityUtil.requestUpdatePacket(this);
	}
	
	@Override
	public CompoundTag getUpdateTag() { return this.saveWithoutMetadata(); }
	
	@Override
	public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
		
		TraderData trader = this.getTraderData();
		if(trader != null)
		{
			
			Direction relativeSide = side;
			if(this.getBlockState().getBlock() instanceof IRotatableBlock)
			{
				IRotatableBlock block = (IRotatableBlock)this.getBlockState().getBlock();
				relativeSide = IRotatableBlock.getRelativeSide(block.getFacing(this.getBlockState()), side);
			}
			LazyOptional<T> result = trader.getCapability(cap, relativeSide);
			if(result != null)
				return result;
			
		}
		
		return super.getCapability(cap, side);
    }
	
	
	/**
	 * Deletes the trader from the registry, and returns the traders contents to be dropped.
	 */
	public Pair<MutableComponent,List<ItemStack>> onBlockBreak(boolean dropBlock) {
		TraderData trader = TraderSaveData.DeleteTrader(this.traderID);
		if(trader != null)
		{
			List<ItemStack> contents = trader.getContents(this.level, this.worldPosition, this.getBlockState(), dropBlock);
			return Pair.of(trader.getName(), contents);
		}
		else
			return Pair.of(Component.literal("NULL"), new ArrayList<>());
	}
	
	public boolean canBreak(Player player)
	{
		TraderData trader = this.getTraderData();
		if(trader != null)
			return trader.hasPermission(player, Permissions.BREAK_TRADER);
		return true;
	}
	
	public void onBreak() {
		if(this.getTraderData() != null)
			TraderSaveData.DeleteTrader(this.traderID);
	}
	
	
	
}
