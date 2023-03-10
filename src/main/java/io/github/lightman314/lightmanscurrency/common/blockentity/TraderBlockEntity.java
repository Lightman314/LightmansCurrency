package io.github.lightman314.lightmanscurrency.common.blockentity;

import io.github.lightman314.lightmanscurrency.common.blockentity.interfaces.IOwnableBlockEntity;
import io.github.lightman314.lightmanscurrency.common.data_updating.DataConverter;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public abstract class TraderBlockEntity<D extends TraderData> extends EasyBlockEntity implements IOwnableBlockEntity, ITickableTileEntity {

	private long traderID = -1;
	public long getTraderID() { return this.traderID; }
	@Deprecated
	public void setTraderID(long traderID) { this.traderID = traderID; }
	
	private CompoundNBT customTrader = null;
	private boolean ignoreCustomTrader = false; 
	
	private CompoundNBT loadFromOldTag = null;
	
	private boolean legitimateBreak = false;
	public void flagAsLegitBreak() { this.legitimateBreak = true; }
	public boolean legitimateBreak() { return this.legitimateBreak; }
	
	public TraderBlockEntity(TileEntityType<?> type) {
		super(type);
	}
	
	private D buildTrader(PlayerEntity owner, ItemStack placementStack)
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
	
	public void initialize(PlayerEntity owner, ItemStack placementStack)
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
	
	@Nonnull
	@Override
	public CompoundNBT save(@Nonnull CompoundNBT compound) {
		compound = super.save(compound);
		compound.putLong("TraderID", this.traderID);
		if(this.customTrader != null)
			compound.put("CustomTrader", this.customTrader);
		return compound;
	}
	
	public void load(@Nonnull BlockState state, @Nonnull CompoundNBT compound)
	{
		super.load(state, compound);
		if(compound.contains("TraderID", Constants.NBT.TAG_LONG))
			this.traderID = compound.getLong("TraderID");
		if(compound.contains("CustomTrader"))
			this.customTrader = compound.getCompound("CustomTrader");
		
		//Convert from old trader types
		if(compound.contains("CoreSettings") || compound.contains("ID"))
			this.loadFromOldTag = compound;
		
	}
	
	@Override
	public void tick() {
		if(this.isClient() || this.level == null)
			return;
		if(this.loadFromOldTag != null)
		{
			if(this.loadFromOldTag.contains("ID"))
			{
				UUID uuid = this.loadFromOldTag.getUUID("ID");
				this.traderID = DataConverter.getNewTraderID(uuid);
				D trader = this.getTraderData();
				this.loadAsFormerNetworkTrader(trader, this.loadFromOldTag);
				this.loadFromOldTag = null;
			}
			else
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
	
	protected abstract D createTraderFromOldData(CompoundNBT compound);

	protected abstract void loadAsFormerNetworkTrader(@Nullable D trader, CompoundNBT compound);
	
	@Override
	public <T> @Nonnull LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
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
			return trader.getCapability(cap, relativeSide);
		}
		
		return super.getCapability(cap, side);
    }
	
	public boolean canBreak(PlayerEntity player)
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
	
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(this.getBlockState() != null)
			return this.getBlockState().getCollisionShape(this.level, this.worldPosition).bounds().move(this.worldPosition);
		return super.getRenderBoundingBox();
	}
	
}