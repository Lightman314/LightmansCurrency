package io.github.lightman314.lightmanscurrency.api.traders.blockentity;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.IServerTicker;
import io.github.lightman314.lightmanscurrency.api.misc.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.PlayerOwner;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxCollector;
import io.github.lightman314.lightmanscurrency.api.taxes.TaxAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderState;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.TraderBlockBase;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.blockentity.IOwnableBlockEntity;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class TraderBlockEntity<D extends TraderData> extends EasyBlockEntity implements IOwnableBlockEntity, IServerTicker {

	private long traderID = -1;
	public long getTraderID() { return this.traderID; }
	@Deprecated
	public void setTraderID(long traderID) { this.traderID = traderID; }

	private CompoundTag customTrader = null;
	private boolean ignoreCustomTrader = false;

	private boolean selfPickup = false;
	public boolean isSelfPickup() { return this.selfPickup; }
	private boolean legitimateBreak = false;
	public void flagAsLegitBreak() { this.legitimateBreak = true; }
	public boolean legitimateBreak() { return this.legitimateBreak; }

	/**
	 * I see no real reason not to have all traders not support this, but I'm leaving this available just in case an addon whishes to disable this feature
	 */
	public boolean supportsTraderPickup() { return true; }

	/**
	 * Attempts to collect the trader as an item.<br>
	 * Returns {@link ItemStack#EMPTY} if the trader collection failed,<br>
	 * otherwise returns an item stack ready to have the trader data assigned to it and then given to the player<br>
	 * <i>May</i> give additional items to the player should the block warrant it (such as a carpenter trader, etc.)
	 */
	public ItemStack PickupTrader(@Nonnull Player player, @Nonnull TraderData trader)
	{
		if(!this.supportsTraderPickup() || trader.getID() != this.traderID)
			return ItemStack.EMPTY;
		BlockState state = this.level.getBlockState(this.worldPosition);
		if(state.getBlock() instanceof TraderBlockBase block)
		{
			this.selfPickup = true;
			this.legitimateBreak = true;
			block.removeAllBlocks(this.level,state,this.worldPosition);
			return new ItemStack(state.getBlock());
		}
		return ItemStack.EMPTY;
	}

	public TraderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	private D buildTrader(Player owner, ItemStack placementStack)
	{
		if(this.customTrader != null)
		{
			D newTrader = this.fullyBuildCustomTrader();
			if(newTrader != null)
				return newTrader;
		}
		D newTrader = this.buildNewTrader();
		newTrader.getOwner().SetOwner(PlayerOwner.of(owner));
		if(placementStack.hasCustomHoverName())
			newTrader.setCustomName(null, placementStack.getHoverName().getString());
		return newTrader;
	}

	protected final D initCustomTrader()
	{
		try {
			return this.castOrNullify(TraderData.Deserialize(false, this.customTrader));
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

	@Nonnull
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

	@Nullable
	private CompoundTag getCurrentTraderAsTag() {
		TraderData trader = this.getTraderData();
		if(trader != null)
			return trader.save();
		return null;
	}

	public void initialize(@Nonnull Player owner, @Nonnull ItemStack placementStack)
	{
		if(this.getTraderData() != null)
			return;

		CompoundTag tag = placementStack.getTag();
		//If Item Stack has data pointing back to an actual trader
		if(tag != null && tag.contains("StoredTrader",Tag.TAG_LONG))
		{
			long traderID = tag.getLong("StoredTrader");
			TraderData trader = TraderAPI.API.GetTrader(this, traderID);
			if(trader != null && this.castOrNullify(trader) != null && trader.isRecoverable())
			{
				//Flag this block as that trader
				this.traderID = traderID;
				this.markDirty();
				//Move the trader to this position & reset its state
				trader.move(this.level,this.worldPosition);
				trader.setState(TraderState.NORMAL);
				//Check Taxes
				this.checkTaxes(owner,trader);
				return;
			}
		}

		D newTrader = this.buildTrader(owner, placementStack);
		//Register to the trading office
		this.traderID = TraderSaveData.RegisterTrader(newTrader, owner);
		this.checkTaxes(owner,newTrader);
		//Send update packet to connected clients, so that they'll have the new trader id.
		this.markDirty();
	}

	private void checkTaxes(@Nonnull Player player, @Nonnull TraderData trader) {
		List<ITaxCollector> taxes = TaxAPI.API.AcknowledgeTaxCollectors(trader);
		if(!taxes.isEmpty()) {
			TextEntry firstMessage = LCText.MESSAGE_TAX_COLLECTOR_PLACEMENT_TRADER;
			if (taxes.size() == 1 && taxes.get(0).isServerEntry())
				firstMessage = LCText.MESSAGE_TAX_COLLECTOR_PLACEMENT_TRADER_SERVER_ONLY;
			EasyText.sendMessage(player, firstMessage.get());
			EasyText.sendMessage(player, LCText.MESSAGE_TAX_COLLECTOR_PLACEMENT_TRADER_INFO.get());
		}
	}

	public TraderData getRawTraderData() { return TraderSaveData.GetTrader(this.isClient(), this.traderID); }

	@Nullable
	public D getTraderData()
	{
		//Get from trading office
		TraderData rawData = this.getRawTraderData();
		if(rawData == null)
			return null;
		else
			return this.castOrNullify(rawData);
	}

	@Nullable
	protected abstract D castOrNullify(@Nonnull TraderData trader);

	@Override
	public void saveAdditional(@Nonnull CompoundTag compound) {
		super.saveAdditional(compound);
		compound.putLong("TraderID", this.traderID);
		if(this.customTrader != null)
			compound.put("CustomTrader", this.customTrader);
	}

	public void load(@Nonnull CompoundTag compound)
	{
		super.load(compound);
		if(compound.contains("TraderID", Tag.TAG_LONG))
			this.traderID = compound.getLong("TraderID");
		if(compound.contains("CustomTrader"))
			this.customTrader = compound.getCompound("CustomTrader");

	}

	@Override
	public void serverTick() {
		if(this.level == null)
			return;
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

	@Override
	public void onLoad()
	{
		if(this.level.isClientSide)
			BlockEntityUtil.requestUpdatePacket(this);
		else
		{
			//Update the traders block position to this position just in case we got moved by another block
			this.moveCustomTrader(this.getTraderData());
		}
	}

	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
		TraderData trader = this.getTraderData();
		if(trader != null)
		{
			Direction relativeSide = side;
			if(this.getBlockState().getBlock() instanceof IRotatableBlock block)
			{
				relativeSide = IRotatableBlock.getRelativeSide(block.getFacing(this.getBlockState()), side);
			}
			return trader.getCapability(cap, relativeSide);
		}

		return super.getCapability(cap, side);
    }

	public boolean canBreak(@Nullable Player player)
	{
		TraderData trader = this.getTraderData();
		if(trader != null)
			return trader.hasPermission(player, Permissions.BREAK_TRADER);
		return true;
	}

	public void onBreak() { TraderSaveData.DeleteTrader(this.traderID); }

	@Override
	public AABB getRenderBoundingBox()
	{
		if(this.getBlockState() != null)
			return this.getBlockState().getCollisionShape(this.level, this.worldPosition).bounds().move(this.worldPosition);
		return super.getRenderBoundingBox();
	}

}
