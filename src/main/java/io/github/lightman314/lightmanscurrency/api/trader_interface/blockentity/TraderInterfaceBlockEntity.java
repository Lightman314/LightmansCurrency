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
import io.github.lightman314.lightmanscurrency.api.trader_interface.data.TradeReference;
import io.github.lightman314.lightmanscurrency.api.trader_interface.data.TraderInterfaceTargets;
import io.github.lightman314.lightmanscurrency.api.traders.FullTradeResult;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeableBlockEntity;
import io.github.lightman314.lightmanscurrency.api.ejection.IDumpable;
import io.github.lightman314.lightmanscurrency.common.menus.providers.EasyMenuProvider;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traderinterface.handlers.SidedHandler;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.TradeResult;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.InteractionUpgrade;
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
		RESTOCK_AND_DRAIN(3),
		RESTOCK(1),
		DRAIN(2),
		TRADE(0);

		public final int index;
		public final Component getDisplayText() { return LCText.GUI_INTERFACE_INTERACTION_TYPE.get(this).get(); }

		InteractionType(int index) {
			this.index = index;
		}

		public boolean targetsTraders() { return this != TRADE; }
		public boolean restocks() { return this == RESTOCK || this == RESTOCK_AND_DRAIN; }
		public boolean drains() { return this == DRAIN || this == RESTOCK_AND_DRAIN; }
		public boolean trades() { return this == TRADE; }
		
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
	
	public final TraderInterfaceTargets targets = new TraderInterfaceTargets(this);
	
	private SimpleContainer upgradeSlots = new SimpleContainer(5);
	@Nonnull
	@Override
	public Container getUpgrades() { return this.upgradeSlots; }

	public int getSelectableCount()
	{
		int count = 1;
		for(int i = 0; i < this.upgradeSlots.getContainerSize(); ++i)
		{
			ItemStack stack = this.upgradeSlots.getItem(i);
			if(stack.getItem() instanceof UpgradeItem upgradeItem)
			{
				if(upgradeItem.getUpgradeType() == Upgrades.INTERACTION)
				{
					count += UpgradeItem.getUpgradeData(stack).getIntValue(InteractionUpgrade.INTERACTIONS);
				}
			}
		}
		return count;
	}

	public void setUpgradeSlotsDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveUpgradeSlots(new CompoundTag(), this.level.registryAccess()));
	}

	public void toggleTrader(long traderID)
	{
		if(this.targets.toggleTrader(traderID))
			this.setTargetsDirty();
	}

	public void toggleTradeIndex(int tradeIndex) {
		if(this.targets.toggleTrade(tradeIndex))
			this.setTargetsDirty();
	}

	public void acceptTradeChanges(int entry)
	{
		List<TradeReference> references = this.targets.getTradeReferences();
		if(entry < 0 || entry >= references.size())
			return;
		references.get(entry).refreshTrade();
		this.setTargetsDirty();
	}

	@Nullable
	public abstract TradeData deserializeTrade(@Nonnull CompoundTag compound,@Nonnull HolderLookup.Provider lookup);
	
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

	public void setStatsDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveStatTracker(new CompoundTag(),this.level.registryAccess()));
	}
	
	protected abstract TradeContext.Builder buildTradeContext(TradeContext.Builder baseContext);

	//Don't mark final to prevent conflicts with LC Tech not yet updating to the new method
	public TradeContext getTradeContext(@Nonnull TraderData trader) {
		if(this.interaction.trades())
			return this.buildTradeContext(TradeContext.create(trader,this.getReferencedPlayer()).withBankAccount(this.getAccountReference())).build();
		else
			return TradeContext.createStorageMode(trader);
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
		this.saveTargets(compound,lookup);
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
	
	protected final CompoundTag saveTargets(CompoundTag compound,@Nonnull HolderLookup.Provider lookup) {
		compound.put("Targets", this.targets.save(lookup));
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
		if(this.isServer())
			BlockEntityUtil.sendUpdatePacket(this, this.saveHandler(new CompoundTag(), handler, this.level.registryAccess()));
	}
	
	@Override
	public void loadAdditional(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		if(compound.contains("Owner", Tag.TAG_COMPOUND))
			this.owner.load(compound.getCompound("Owner"),lookup);
		if(compound.contains("Mode"))
			this.mode = EnumUtil.enumFromString(compound.getString("Mode"), ActiveMode.values(), ActiveMode.DISABLED);
		if(compound.contains("OnlineMode"))
			this.onlineMode = compound.getBoolean("OnlineMode");
		if(compound.contains("InteractionType", Tag.TAG_STRING))
			this.interaction = EnumUtil.enumFromString(compound.getString("InteractionType"), InteractionType.values(), InteractionType.TRADE);
		if(compound.contains("Trade", Tag.TAG_COMPOUND))
			this.targets.loadFromOldData(compound.getCompound("Trade"),lookup);
		if(compound.contains("Targets",Tag.TAG_COMPOUND))
			this.targets.load(compound.getCompound("Targets"),lookup);
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
	
	public void setTargetsDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveTargets(new CompoundTag(),this.registryAccess()));
	}

	@Nonnull
	public FullTradeResult TryExecuteTrade(@Nonnull TradeReference target)
	{
		TraderData trader = this.targets.getTrader();
		if(trader != null)
		{
			TradeContext context = this.getTradeContext(trader);
			FullTradeResult result = trader.TryExecuteTradeWithResults(context, target.getTradeIndex());
			target.setLastResult(result.simpleResult);
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
		else
		{
			target.setLastResult(TradeResult.FAIL_NULL);
			return FullTradeResult.failure(TradeResult.FAIL_NULL);
		}
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
		if(this.targets.tick(this::validTrader))
			this.setTargetsDirty();
		if(this.isActive())
		{
			if(--this.waitTimer <= 0)
			{
				this.waitTimer = this.getInteractionDelay();
				if(this.interaction.targetsTraders())
				{
					for(TraderData trader : this.targets.getTraders())
					{
						if(!this.validTrader(trader) && !this.hasTraderPermissions(trader))
							continue;
						if(this.interaction.drains())
							this.drainTick(trader);
						if(this.interaction.restocks())
							this.restockTick(trader);
					}
				}
				else if(this.interaction.trades())
				{
					for(TradeReference trade : this.targets.getTradeReferences())
					{
						if(!this.validTrade(trade))
							continue;
						this.tradeTick(trade);
					}
				}
				if(this.hasHopperUpgrade())
				{
					this.hopperTick();
				}
			}
		}
	}
	
	
	
	//Returns whether the trader referenced is valid
	public boolean validTrader(@Nonnull TraderData trader) {
		return trader != null && this.validTraderType(trader);
	}
	
	public boolean validTrade(@Nonnull TradeReference trade) {
		TradeData expectedTrade = trade.getLocalTrade();
		TradeData trueTrade = trade.getTrueTrade();
		if(expectedTrade == null || trueTrade == null)
			return false;
		return trueTrade.AcceptableDifferences(trueTrade.compare(expectedTrade));
	}
	
	public abstract boolean validTraderType(TraderData trader);
	
	protected abstract void drainTick(@Nonnull TraderData trader);
	
	protected abstract void restockTick(@Nonnull TraderData trader);
	
	protected abstract void tradeTick(@Nonnull TradeReference trade);
	
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
		return type == Upgrades.SPEED || (type == Upgrades.HOPPER && this.allowHopperUpgrade()) || type == Upgrades.INTERACTION || this.allowAdditionalUpgrade(type);
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
