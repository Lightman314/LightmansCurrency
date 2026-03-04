package io.github.lightman314.lightmanscurrency.api.traders.blockentity;

import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.CapabilityBlockHelper;
import io.github.lightman314.lightmanscurrency.api.misc.ticker.IServerTicker;
import io.github.lightman314.lightmanscurrency.api.misc.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.api.misc.world.WorldPosition;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.PlayerOwner;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxCollector;
import io.github.lightman314.lightmanscurrency.api.taxes.TaxAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.tracking.BlockEntityTraderTrackingHolder;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderState;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.TraderBlockBase;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.DisplayNode;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeable;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeableBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.items.data.TraderItemData;
import io.github.lightman314.lightmanscurrency.network.message.trader.SPacketTaxInfo;
import net.minecraft.core.component.DataComponents;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.blockentity.IOwnableBlockEntity;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@EventBusSubscriber
public abstract class TraderBlockEntity<D extends TraderData> extends EasyBlockEntity implements IOwnableBlockEntity, IServerTicker, IUpgradeableBlockEntity {

    private final BlockEntityTraderTrackingHolder trackingHolder = new BlockEntityTraderTrackingHolder(this);
    public void clearAllTracking() { this.trackingHolder.clearAll(); }

	private long traderID = -1;
	public long getTraderID() { return this.traderID; }
	protected void setTraderID(long traderID) {
        if(this.traderID == traderID)
            return;
        if(this.traderID >= 0)
            this.trackingHolder.endTracking(this.traderID);
        this.traderID = traderID;
        //Start tracking for all players within range
        if(this.level instanceof ServerLevel serverLevel)
        {
            TraderData trader = this.getTraderData();
            if(trader != null)
            {
                for(Player player : serverLevel.getChunkSource().chunkMap.getPlayers(new ChunkPos(this.worldPosition),true))
                    this.trackingHolder.requestTracking(trader,player);
            }
        }
    }

	private CompoundTag customTrader = null;
	private boolean ignoreCustomTrader = false;

	private boolean selfPickup = false;
	public void flagAsPickup() { this.selfPickup = true; }
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
	public ItemStack PickupTrader(Player player, TraderData trader)
	{
		if(!this.supportsTraderPickup() || trader.getID() != this.traderID)
			return ItemStack.EMPTY;
		BlockState state = this.getBlockState();
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
		if(placementStack.has(DataComponents.CUSTOM_NAME))
			newTrader.ifNodePresent(DisplayNode.TYPE,node -> node.setCustomName(null,placementStack.getHoverName()));
		return newTrader;
	}

	protected final D initCustomTrader()
	{
		try {
			return (D)TraderData.CODEC.decode(RegistryOps.create(NbtOps.INSTANCE,this.level.registryAccess()),this.customTrader).getOrThrow().getFirst();
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
			this.customTrader = trader.save(DataContext.createNBT(this.level.registryAccess()));
			this.ignoreCustomTrader = true;
			this.markDirty();
		}
	}

	@Nullable
	private CompoundTag getCurrentTraderAsTag() {
		TraderData trader = this.getRawTraderData();
		if(trader != null)
			return trader.save(DataContext.createNBT(this.level.registryAccess()));
		return null;
	}

	public void initialize(Player owner, ItemStack placementStack)
	{
		if(this.getTraderData() != null)
			return;

		//If Item Stack has data pointing back to an actual trader
		if(placementStack.has(ModDataComponents.TRADER_ITEM_DATA))
		{
			TraderItemData data = placementStack.get(ModDataComponents.TRADER_ITEM_DATA);
			TraderData trader = TraderAPI.getApi().GetTrader(this, data.traderID());
			if(trader != null && this.castOrNullify(trader) != null && trader.isRecoverable())
			{
				//Flag this block as that trader
				this.setTraderID(data.traderID());
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
		//Update with the latest block variant data
		if(this.getCurrentVariant() != null)
			newTrader.setTraderBlockVariant(this.getCurrentVariant(),this.isVariantLocked());
		//Register to the trading office
		this.setTraderID(TraderAPI.getApi().CreateTrader(newTrader, owner));
		this.checkTaxes(owner,newTrader);
		//Send update packet to connected clients, so that they'll have the new trader id.
		this.markDirty();
		//Invalidate the capabilities
		this.level.invalidateCapabilities(this.worldPosition);
	}

	private void checkTaxes(Player player, TraderData trader)
	{
		List<ITaxCollector> taxes = TaxAPI.getApi().AcknowledgeTaxCollectors(trader);
		if(!taxes.isEmpty())
			SPacketTaxInfo.sendPacket(taxes,player);
	}

	public TraderData getRawTraderData() { return TraderAPI.getApi().GetTrader(this.isClient(), this.traderID); }

	public D getTraderData()
	{
		//Get from trading office
		TraderData rawData = this.getRawTraderData();
		if(rawData == null)
			return null;
		else
			return castOrNullify(rawData);
	}

	@Nullable
	protected abstract D castOrNullify(TraderData trader);

	@Override
	public void saveAdditional(CompoundTag compound,DataContext<Tag> context) {
		super.saveAdditional(compound,context);
		compound.putLong("TraderID", this.traderID);
		if(this.customTrader != null)
			compound.put("CustomTrader", this.customTrader);
	}

	@Override
	protected void loadAdditional(CompoundTag compound,DataContext<Tag> context) {
		super.loadAdditional(compound,context);
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
            WorldPosition position = customTrader.getWorldPosition();
			//Check if the custom trader is this position & dimension
			if(position.getDimension() == this.level.dimension() && this.worldPosition.equals(position.getPos()))
				this.ignoreCustomTrader = true;
			else
			{
				//If the dimension and position don't match exactly, assume it's been moved and load the custom trader
				this.moveCustomTrader(customTrader);
				this.setTraderID(TraderAPI.getApi().CreateTrader(customTrader,null));
				this.customTrader = null;
				this.ignoreCustomTrader = true;
				this.markDirty();
				//Invalidate capabilities as a new trader was made
				this.level.invalidateCapabilities(this.worldPosition);
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
		if(this.isServer())
		{
			//Update the traders block position to this position just in case we got moved by another block
			this.moveCustomTrader(this.getTraderData());
		}
		super.onLoad();
	}

	public static <X> void easyRegisterCapProvider(RegisterCapabilitiesEvent event, BlockCapability<X,Direction> cap, BiFunction<TraderData,Direction,X> getter, Block... blocks)
	{
		event.registerBlock(cap,CapabilityBlockHelper.wrapProvider((level,pos,state,be,side) -> {
			if(be instanceof TraderBlockEntity<?> traderBE)
			{
				TraderData trader = traderBE.getRawTraderData();
				if(trader != null)
				{
					Direction relativeSide = side;
					if(state.getBlock() instanceof IRotatableBlock rb)
						relativeSide = IRotatableBlock.getRelativeSide(rb.getFacing(state),side);
					return getter.apply(trader, relativeSide);
				}
			}
			return null;
		}), blocks);
	}

	public boolean canBreak(@Nullable Player player)
	{
		TraderData trader = this.getTraderData();
		if(trader != null)
			return trader.hasPermission(player, Permissions.BREAK_TRADER);
		return true;
	}

	public void onBreak() { TraderAPI.getApi().DeleteTrader(this.traderID); }

	@Nullable
	public static AABB getRenderBoundingBox(TraderBlockEntity<?> be)
	{
		if(be.getBlockState() != null)
			return be.getBlockState().getCollisionShape(be.level, be.worldPosition).bounds().move(be.worldPosition);
		return null;
	}

	@Nullable
	@Override
	public IUpgradeable getUpgradeable() { return this.getTraderData(); }

	@Override
	public void setVariant(@Nullable ResourceLocation variant, boolean locked) {
		super.setVariant(variant,locked);
		TraderData t = this.getTraderData();
		if(t != null)
			t.setTraderBlockVariant(variant,locked);
	}

    //Start tracking for the given player when it's now being watched
    @SubscribeEvent
    private static void trackBlockEntity(ChunkWatchEvent.Sent event)
    {
        for(BlockEntity be : new ArrayList<>(event.getChunk().getBlockEntities().values()))
        {
            if(be instanceof TraderBlockEntity<?> traderBlockEntity)
            {
                TraderData trader = traderBlockEntity.getTraderData();
                if(trader != null)
                {
                    traderBlockEntity.trackingHolder.requestTracking(trader,event.getPlayer());
                }
            }
        }
    }

    //Clear tracking for the given player when it's no longer being watched
    @SubscribeEvent
    private static void untrackBlockEntity(ChunkWatchEvent.UnWatch event)
    {
        ChunkPos pos = event.getPos();
        ServerLevel level = event.getLevel();
        if(level.hasChunk(pos.x,pos.z))
        {
            LevelChunk chunk = level.getChunk(pos.x,pos.z);
            for(BlockEntity be : new ArrayList<>(chunk.getBlockEntities().values()))
            {
                if(be instanceof TraderBlockEntity<?> traderBlockEntity)
                    traderBlockEntity.trackingHolder.clearPlayer(event.getPlayer());
            }
        }
    }

    //Clear all tracking when the chunk is unloaded
    @SubscribeEvent
    private static void untrackChunk(ChunkEvent.Unload event)
    {
        ChunkAccess chunk = event.getChunk();
        for(BlockPos pos : chunk.getBlockEntitiesPos())
        {
            if(chunk.getBlockEntity(pos) instanceof TraderBlockEntity<?> traderBlockEntity)
                traderBlockEntity.trackingHolder.clearAll();
        }
    }

}
