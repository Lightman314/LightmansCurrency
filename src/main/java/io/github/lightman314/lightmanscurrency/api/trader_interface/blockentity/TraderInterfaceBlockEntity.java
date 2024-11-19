package io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.api.ejection.builtin.BasicEjectionData;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.api.misc.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.misc.IServerTicker;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.PlayerOwner;
import io.github.lightman314.lightmanscurrency.api.stats.StatKeys;
import io.github.lightman314.lightmanscurrency.api.stats.StatTracker;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blocks.TraderInterfaceBlock;
import io.github.lightman314.lightmanscurrency.api.traders.FullTradeResult;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeableBlockEntity;
import io.github.lightman314.lightmanscurrency.api.ejection.IDumpable;
import io.github.lightman314.lightmanscurrency.common.menus.providers.EasyMenuProvider;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traderinterface.NetworkTradeReference;
import io.github.lightman314.lightmanscurrency.common.traderinterface.handlers.SidedHandler;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.TradeResult;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.network.message.interfacebe.CPacketInterfaceHandlerMessage;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeable;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.SpeedUpgrade;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TraderInterfaceBlockEntity extends EasyBlockEntity implements IUpgradeable, IDumpable, IServerTicker, IUpgradeableBlockEntity {
	
	public static final int INTERACTION_DELAY = 20;
	
	private boolean allowRemoval = false;
	public boolean allowRemoval() { return this.allowRemoval; }
	public void flagAsRemovable() { this.allowRemoval = true; }
	
	public enum InteractionType {
		RESTOCK_AND_DRAIN(true, true, true, false, 3),
		RESTOCK(true, true, false, false, 1),
		DRAIN(true, false, true, false, 2),
		TRADE(false, false, false, true, 0);
		
		public final boolean requiresPermissions;
		public final boolean restocks;
		public final boolean drains;
		public final boolean trades;
		public final int index;
		public final Component getDisplayText() { return LCText.GUI_INTERFACE_INTERACTION_TYPE.get(this).get(); }

		InteractionType(boolean requiresPermissions, boolean restocks, boolean drains, boolean trades, int index) {
			this.requiresPermissions =  requiresPermissions;
			this.restocks = restocks;
			this.drains = drains;
			this.trades = trades;
			this.index = index;
		}
		
		public static InteractionType fromIndex(int index) {
			for(InteractionType type : InteractionType.values())
			{
				if(type.index == index)
					return type;
			}
			return TRADE;
		}
		
		public static int size() { return 4; }
		
	}
	
	public enum ActiveMode {
		DISABLED(0, be -> false),
		REDSTONE_OFF(1, be -> {
			if(be.level != null)
				return !be.level.hasNeighborSignal(be.getBlockPos());
			return false;
		}),
		REDSTONE_ONLY(2, be ->{
			if(be.level != null)
				return be.level.hasNeighborSignal(be.getBlockPos());
			return false;
		}),
		ALWAYS_ON(3, be -> true);
		
		public final int index;
		public final Component getDisplayText() { return LCText.GUI_INTERFACE_ACTIVE_MODE.get(this).get(); }
		public final ActiveMode getNext() { return fromIndex(this.index + 1); }
		
		private final Function<TraderInterfaceBlockEntity,Boolean> active;
		public boolean isActive(TraderInterfaceBlockEntity blockEntity) { return this.active.apply(blockEntity); }
		
		ActiveMode(int index, Function<TraderInterfaceBlockEntity,Boolean> active) { this.index = index; this.active = active;}
		
		public static ActiveMode fromIndex(int index) {
			for(ActiveMode mode : ActiveMode.values())
			{
				if(mode.index == index)
					return mode;
			}
			return DISABLED;
		}
	}
	
	public final OwnerData owner = new OwnerData(this, this::OnOwnerChanged);
	public void initOwner(Entity owner) { if(!this.owner.hasOwner() && owner instanceof Player player) this.owner.SetOwner(PlayerOwner.of(player)); }
	private void OnOwnerChanged() {
		this.mode = ActiveMode.DISABLED;
		this.cachedContext = null;
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveOwner(this.saveMode(new CompoundTag()),this.level.registryAccess()));
	}
	
	public PlayerReference getReferencedPlayer() { return this.owner.getPlayerForContext(); }
	
	public MutableComponent getOwnerName() { return this.owner.getName(); }

	@Nullable
	public IBankAccount getBankAccount() {
		BankReference reference = this.getAccountReference();
		if(reference != null)
			return reference.get();
		return null;
	}
	@Nullable
	public BankReference getAccountReference() {
		if(QuarantineAPI.IsDimensionQuarantined(this))
			return null;
		return this.owner.getValidOwner().asBankReference();
	}

	public final StatTracker statTracker = new StatTracker(() -> {},this);

	List<SidedHandler<?>> handlers = new ArrayList<>();
	
	private ActiveMode mode = ActiveMode.DISABLED;
	public ActiveMode getMode() { return this.mode; }
	public void setMode(ActiveMode mode) { this.mode = mode; this.setModeDirty(); }
	
	private boolean onlineMode = false;
	public boolean isOnlineMode() { return this.onlineMode; }
	public void setOnlineMode(boolean onlineMode) { this.onlineMode = onlineMode; this.setOnlineModeDirty(); }
	
	private InteractionType interaction = InteractionType.TRADE;
	public InteractionType getInteractionType() { return this.interaction; }
	public void setInteractionType(InteractionType type) {
		if(this.getBlacklistedInteractions().contains(type))
		{
			LightmansCurrency.LogInfo("Attempted to set interaction type to " + type.name() + ", but that type is blacklisted for this interface type (" + this.getClass().getName() + ").");
			return;
		}
		this.interaction = type;
		this.setInteractionDirty();
	}
	public List<InteractionType> getBlacklistedInteractions() { return new ArrayList<>(); }
	
	NetworkTradeReference reference = new NetworkTradeReference(this::isClient, () -> this.level.registryAccess(), this::deserializeTrade);
	public boolean hasTrader() { return this.getTrader() != null; }
	public TraderData getTrader() {
		TraderData trader = this.reference.getTrader();
		if(this.interaction.requiresPermissions && !this.hasTraderPermissions(trader))
			return null;
		return trader;
	}
	public int getTradeIndex() { return this.reference.getTradeIndex(); }
	public TradeData getReferencedTrade() { return this.reference.getLocalTrade(); }
	public TradeData getTrueTrade() { return this.reference.getTrueTrade(); }
	
	private SimpleContainer upgradeSlots = new SimpleContainer(5);
	@Nonnull
	@Override
	public Container getUpgrades() { return this.upgradeSlots; }
	/**
	 * @see #getUpgrades()
	 */
	@Deprecated(since = "2.2.3.5")
	public Container getUpgradeInventory() { return this.upgradeSlots; }
	
	public void setUpgradeSlotsDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveUpgradeSlots(new CompoundTag(), this.level.registryAccess()));
	}
	
	public void setTrader(long traderID) {
		//Trader is the same id. Ignore the change.
		if(this.reference.getTraderID() == traderID)
			return;
		this.reference.setTrader(traderID);
		this.reference.setTrade(-1);
		this.cachedContext = null;
		this.setTradeReferenceDirty();
	}
	
	public void setTradeIndex(int tradeIndex) {
		this.reference.setTrade(tradeIndex);
		this.setTradeReferenceDirty();
	}
	
	public void acceptTradeChanges() {
		this.reference.refreshTrade();
		this.setTradeReferenceDirty();
	}
	
	private TradeResult lastResult = TradeResult.SUCCESS;
	public TradeResult mostRecentTradeResult() { return this.lastResult; }

	protected abstract TradeData deserializeTrade(@Nonnull CompoundTag compound,@Nonnull HolderLookup.Provider lookup);
	
	private int waitTimer = INTERACTION_DELAY;
	
	public boolean canAccess(Player player) { return this.owner.isMember(player); }
	
	/**
	 * Whether the given player has owner-level permissions.
	 * If owned by a team, this will return true for team admins &amp; the team owner.
	 */
	public boolean isOwner(Player player) { return this.owner.isAdmin(player); }
	
	protected TraderInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	
	public void setModeDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveMode(new CompoundTag()));
	}
	
	public void setOnlineModeDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveOnlineMode(new CompoundTag()));
	}
	
	public void setLastResultDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveLastResult(new CompoundTag()));
	}

	public void setStatsDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveStatTracker(new CompoundTag(),this.level.registryAccess()));
	}
	
	protected abstract TradeContext.Builder buildTradeContext(TradeContext.Builder baseContext);

	private TradeContext cachedContext = null;

	//Don't mark final to prevent conflicts with LC Tech not yet updating to the new method
	public TradeContext getTradeContext() {
		if(this.cachedContext == null)
		{
			if(this.interaction.trades)
				this.cachedContext = this.buildTradeContext(TradeContext.create(this.getTrader(), this.getReferencedPlayer()).withBankAccount(this.getAccountReference())).build();
			else
				this.cachedContext = TradeContext.createStorageMode(this.getTrader());
		}
		return this.cachedContext;
	}
	
	protected final <H extends SidedHandler<?>> H addHandler(@Nonnull H handler) {
		handler.setParent(this);
		this.handlers.add(handler);
		return handler;
	}
	
	@Override
	protected void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		this.saveOwner(compound,lookup);
		this.saveMode(compound);
		this.saveOnlineMode(compound);
		this.saveInteraction(compound);
		this.saveLastResult(compound);
		this.saveReference(compound,lookup);
		this.saveUpgradeSlots(compound, lookup);
		this.saveStatTracker(compound,lookup);
	}
	
	protected final CompoundTag saveOwner(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		if(this.owner != null)
			compound.put("Owner", this.owner.save(lookup));
		return compound;
	}
	
	protected final CompoundTag saveMode(CompoundTag compound) {
		compound.putString("Mode", this.mode.name());
		return compound;
	}
	
	protected final CompoundTag saveOnlineMode(CompoundTag compound) {
		compound.putBoolean("OnlineMode", this.onlineMode);
		return compound;
	}
	
	protected final CompoundTag saveInteraction(CompoundTag compound) {
		compound.putString("InteractionType", this.interaction.name());
		return compound;
	}
	
	protected final CompoundTag saveLastResult(CompoundTag compound) {
		compound.putString("LastResult", this.lastResult.name());
		return compound;
	}
	
	protected final CompoundTag saveReference(CompoundTag compound,@Nonnull HolderLookup.Provider lookup) {
		compound.put("Trade", this.reference.save(lookup));
		return compound;
	}
	
	protected final CompoundTag saveUpgradeSlots(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		InventoryUtil.saveAllItems("Upgrades", compound, this.upgradeSlots, lookup);
		return compound;
	}

	protected final CompoundTag saveStatTracker(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		compound.put("Stats", this.statTracker.save(lookup));
		return compound;
	}
	
	protected final CompoundTag saveHandler(CompoundTag compound, SidedHandler<?> handler, @Nonnull HolderLookup.Provider lookup) {
		compound.put(handler.getTag(), handler.save(lookup));
		return compound;
	}
	
	public void setHandlerDirty(SidedHandler<?> handler) {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveHandler(new CompoundTag(), handler, this.level.registryAccess()));
	}
	
	@Override
	public void loadAdditional(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		if(compound.contains("Owner", Tag.TAG_COMPOUND))
		{
			this.owner.load(compound.getCompound("Owner"),lookup);
			this.cachedContext = null;
		}
		if(compound.contains("Mode"))
			this.mode = EnumUtil.enumFromString(compound.getString("Mode"), ActiveMode.values(), ActiveMode.DISABLED);
		if(compound.contains("OnlineMode"))
			this.onlineMode = compound.getBoolean("OnlineMode");
		if(compound.contains("InteractionType", Tag.TAG_STRING))
		{
			this.interaction = EnumUtil.enumFromString(compound.getString("InteractionType"), InteractionType.values(), InteractionType.TRADE);
			this.cachedContext = null;
		}
		if(compound.contains("Trade", Tag.TAG_COMPOUND))
			this.reference.load(compound.getCompound("Trade"),lookup);
		if(compound.contains("Upgrades"))
			this.upgradeSlots = InventoryUtil.loadAllItems("Upgrades", compound, 5, lookup);
		if(compound.contains("Stats"))
			this.statTracker.load(compound.getCompound("Stats"),lookup);
		for(SidedHandler<?> handler : this.handlers) {
			if(compound.contains(handler.getTag(), Tag.TAG_COMPOUND))
				handler.load(compound.getCompound(handler.getTag()), lookup);
		}
	}
	
	public void setInteractionDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveInteraction(new CompoundTag()));
	}
	
	protected final Direction getRelativeSide(Direction side) {
		Direction relativeSide = side;
		if(relativeSide != null & this.getBlockState().getBlock() instanceof IRotatableBlock)
			relativeSide = IRotatableBlock.getRelativeSide(((IRotatableBlock)this.getBlockState().getBlock()).getFacing(this.getBlockState()), side);
		return relativeSide;
	}
	
	public void sendHandlerMessage(ResourceLocation type, CompoundTag message) {
		if(this.isClient())
			new CPacketInterfaceHandlerMessage(this.worldPosition, type, message).send();
	}
	
	public void receiveHandlerMessage(ResourceLocation type, Player player, CompoundTag message) {
		if(!this.canAccess(player))
			return;
		for (SidedHandler<?> handler : this.handlers) {
			if (handler.getType().equals(type))
				handler.receiveMessage(message);
		}
	}
	
	public void setTradeReferenceDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveReference(new CompoundTag(),this.registryAccess()));
	}

	/**
	 * @see #TryExecuteTrade()
	 */
	@Nonnull
	public TradeResult interactWithTrader() { return this.TryExecuteTrade().simpleResult; }

	@Nonnull
	public FullTradeResult TryExecuteTrade()
	{
		TradeContext context = this.getTradeContext();
		TraderData trader = this.getTrader();
		if(trader != null)
		{
			FullTradeResult result = trader.TryExecuteTradeWithResults(context, this.reference.getTradeIndex());
			if(this.lastResult != result.simpleResult)
			{
				this.lastResult = result.simpleResult;
				this.setLastResultDirty();
			}
			//Automatically log money paid/earned & trade executed
			if(result.isSuccess())
			{
				TradeEvent.PostTradeEvent data = result.data;
				TradeData trade = data.getTrade();
				if(trade.getTradeDirection() == TradeDirection.SALE)
					this.statTracker.incrementStat(StatKeys.Generic.MONEY_PAID, data.getPricePaid());
				else if(trade.getTradeDirection() == TradeDirection.PURCHASE)
					this.statTracker.incrementStat(StatKeys.Generic.MONEY_EARNED, data.getPricePaid());
				this.statTracker.incrementStat(StatKeys.Traders.TRADES_EXECUTED,1);
				this.setStatsDirty();
			}
			return result;
		}
		if(this.lastResult != TradeResult.FAIL_NULL)
		{
			this.lastResult = TradeResult.FAIL_NULL;
			this.setLastResultDirty();
		}
		return FullTradeResult.failure(this.lastResult);
	}
	
	public boolean isActive() { return this.mode.isActive(this) && this.onlineCheck(); }
	
	public boolean onlineCheck() {
		//Always return false on the client
		if(this.isClient())
			return false;
		//Always return true if we're not in online mode
		if(!this.onlineMode)
			return true;

		return this.owner.getValidOwner().isOnline();
	}
	
	public final boolean hasTraderPermissions(TraderData trader) {
		if(trader == null)
			return false;
		//If this is owned by a player, check player permissions
		if(this.owner.getValidOwner() instanceof PlayerOwner po)
			return trader.hasPermission(po.player, Permissions.INTERACTION_LINK);
		//Otherwise require exact ownership match
		return trader.getOwner().getValidOwner().matches(this.owner.getValidOwner());
	}
	
	@Override
	public void serverTick() {
		//Disable all functions if dimension is quarantined just to be safe
		if(QuarantineAPI.IsDimensionQuarantined(this))
			return;
		if(this.isActive())
		{
			if(--this.waitTimer <= 0)
			{
				this.waitTimer = this.getInteractionDelay();
				if(this.interaction.requiresPermissions)
				{
					if(!this.validTrader() || !this.hasTraderPermissions(this.getTrader()))
						return;
					if(this.interaction.drains)
						this.drainTick();
					if(this.interaction.restocks)
						this.restockTick();
				}
				else if(this.interaction.trades)
				{
					if(!this.validTrade())
						return;
					this.tradeTick();
				}
				if(this.hasHopperUpgrade())
				{
					this.hopperTick();
				}
			}
		}
	}
	
	
	
	//Returns whether the trader referenced is valid
	public boolean validTrader() {
		TraderData trader = this.getTrader();
		return trader != null && this.validTraderType(trader);
	}
	
	public boolean validTrade() {
		TradeData expectedTrade = this.getReferencedTrade();
		TradeData trueTrade = this.getTrueTrade();
		if(expectedTrade == null || trueTrade == null)
			return false;
		return trueTrade.AcceptableDifferences(trueTrade.compare(expectedTrade));
	}
	
	public abstract boolean validTraderType(TraderData trader);
	
	protected abstract void drainTick();
	
	protected abstract void restockTick();
	
	protected abstract void tradeTick();
	
	protected abstract void hopperTick();
	
	public void openMenu(Player player) {
		if(this.canAccess(player))
		{
			MenuProvider provider = this.getMenuProvider();
			if(provider == null)
				return;
			player.openMenu(provider,this.worldPosition);
		}
	}
	
	protected MenuProvider getMenuProvider() { return new InterfaceMenuProvider(this); }
	
	public static class InterfaceMenuProvider implements EasyMenuProvider {
		private final TraderInterfaceBlockEntity blockEntity;
		public InterfaceMenuProvider(TraderInterfaceBlockEntity blockEntity) { this.blockEntity = blockEntity; }
		@Override
		public AbstractContainerMenu createMenu(int windowID, @Nonnull Inventory inventory, @Nonnull Player player) {
			return new TraderInterfaceMenu(windowID, inventory, this.blockEntity);
		}
	}
	
	protected int getInteractionDelay() {
		int delay = INTERACTION_DELAY;
		for(int i = 0; i < this.upgradeSlots.getContainerSize() && delay > 1; ++i)
		{
			ItemStack stack = this.upgradeSlots.getItem(i);
			if(stack.getItem() instanceof UpgradeItem upgrade)
			{
				if(upgrade.getUpgradeType() instanceof SpeedUpgrade)
					delay -= UpgradeItem.getUpgradeData(stack).getIntValue(SpeedUpgrade.DELAY_AMOUNT);
			}
		}
		return delay;
	}
	
	public abstract void initMenuTabs(TraderInterfaceMenu menu);
	
	public boolean allowUpgrade(@Nonnull UpgradeType type) {
		return type == Upgrades.SPEED || (type == Upgrades.HOPPER && this.allowHopperUpgrade() && !this.hasHopperUpgrade()) || this.allowAdditionalUpgrade(type);
	}
	
	protected boolean allowHopperUpgrade() { return true; }
	
	protected boolean allowAdditionalUpgrade(UpgradeType type) { return false; }
	
	protected final boolean hasHopperUpgrade() { return UpgradeType.hasUpgrade(Upgrades.HOPPER, this.upgradeSlots); }

	@Nonnull
	public final List<ItemStack> getContents(@Nonnull Level level, @Nonnull BlockPos pos, @Nullable BlockState state, boolean dropBlock) {
		List<ItemStack> contents = new ArrayList<>();
		
		//Drop trader block
		if(dropBlock && state != null)
		{
			if(state.getBlock() instanceof TraderInterfaceBlock)
				contents.add(((TraderInterfaceBlock)state.getBlock()).getDropBlockItem(state, this));
			else
				contents.add(new ItemStack(state.getBlock()));
		}
		
		//Drop upgrade slots
		for(int i = 0; i < this.upgradeSlots.getContainerSize(); ++i)
		{
			if(!this.upgradeSlots.getItem(i).isEmpty())
				contents.add(this.upgradeSlots.getItem(i));
		}
		
		//Dump contents
		this.getAdditionalContents(contents);
		return contents;
		
	}
	
	protected abstract void getAdditionalContents(List<ItemStack> contents);

	@Nonnull
	@Override
	public EjectionData buildEjectionData(@Nonnull Level level, @Nonnull BlockPos pos, @Nullable BlockState state) {
		List<ItemStack> contents = this.getContents(level,pos,state,true);
		return new BasicEjectionData(this.owner,contents,state == null ? LCText.BLOCK_ITEM_TRADER_INTERFACE.get() : state.getBlock().getName());
	}
}
