package io.github.lightman314.lightmanscurrency.common.blockentity;

import io.github.lightman314.lightmanscurrency.common.blockentity.interfaces.tickable.IServerTicker;
import io.github.lightman314.lightmanscurrency.common.blocks.interfaces.IDeprecatedBlock;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxManager;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.interfaces.IOwnableBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
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
import java.util.List;

public abstract class TraderBlockEntity<D extends TraderData> extends EasyBlockEntity implements IOwnableBlockEntity, IServerTicker {

	private long traderID = -1;
	public long getTraderID() { return this.traderID; }
	@Deprecated
	public void setTraderID(long traderID) { this.traderID = traderID; }

	private CompoundTag customTrader = null;
	private boolean ignoreCustomTrader = false;

	private boolean legitimateBreak = false;
	public void flagAsLegitBreak() { this.legitimateBreak = true; }
	public boolean legitimateBreak() { return this.legitimateBreak; }

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

		D newTrader = this.buildTrader(owner, placementStack);
		//Register to the trading office
		this.traderID = TraderSaveData.RegisterTrader(newTrader, owner);
		List<TaxEntry> taxes = TaxManager.GetPossibleTaxesForTrader(newTrader);
		taxes.forEach(e -> e.acceptTaxes(newTrader));
		if(taxes.size() > 0)
		{
			Component firstMessage = EasyText.translatable("lightmanscurrency.tax_entry.placement_notification.trader.1");
			if(taxes.size() == 1 && taxes.get(0).isServerEntry())
				firstMessage = EasyText.translatable("lightmanscurrency.tax_entry.placement_notification.trader.server_only");
			EasyText.sendMessage(owner, firstMessage);
			EasyText.sendMessage(owner, EasyText.translatable("lightmanscurrency.tax_entry.placement_notification.trader.2"));
		}
		//Send update packet to connected clients, so that they'll have the new trader id.
		this.markDirty();
	}

	public TraderData getRawTraderData() { return TraderSaveData.GetTrader(this.isClient(), this.traderID); }

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
	public void saveAdditional(@NotNull CompoundTag compound) {
		super.saveAdditional(compound);
		compound.putLong("TraderID", this.traderID);
		if(this.customTrader != null)
			compound.put("CustomTrader", this.customTrader);
	}

	public void load(@NotNull CompoundTag compound)
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
			//Check if this block should be replaced
			BlockState bs = this.level.getBlockState(this.worldPosition);
			if(bs.getBlock() instanceof IDeprecatedBlock block)
				block.replaceBlock(this.level, this.worldPosition, bs);
			//Update the traders block position to this position just in case we got moved by another block
			this.moveCustomTrader(this.getTraderData());
		}
	}

	@Override
	@NotNull
	public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
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

	public boolean canBreak(Player player)
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
