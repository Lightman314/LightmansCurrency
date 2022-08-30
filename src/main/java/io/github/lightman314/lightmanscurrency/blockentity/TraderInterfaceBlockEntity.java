package io.github.lightman314.lightmanscurrency.blockentity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.ItemInterfaceBlockEntity.IItemHandlerBlock;
import io.github.lightman314.lightmanscurrency.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.blocks.tradeinterface.templates.TraderInterfaceBlock;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.Team.TeamReference;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.interfacebe.MessageHandlerMessage;
import io.github.lightman314.lightmanscurrency.trader.IDumpable;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext.TradeResult;
import io.github.lightman314.lightmanscurrency.trader.interfacing.UniversalTradeReference;
import io.github.lightman314.lightmanscurrency.trader.interfacing.handlers.SidedHandler;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.upgrades.SpeedUpgrade;
import io.github.lightman314.lightmanscurrency.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.upgrades.UpgradeType.IUpgradeable;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.server.ServerLifecycleHooks;

@SuppressWarnings("removal")
public abstract class TraderInterfaceBlockEntity extends TickableBlockEntity implements IUpgradeable, IDumpable {
	
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
		public final Component getDisplayText() { return Component.translatable("gui.lightmanscurrency.interface.type." + this.name().toLowerCase()); }
		
		public final InteractionType getNext() { return fromIndex(this.index + 1); }
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
		public final Component getDisplayText() { return Component.translatable("gui.lightmanscurrency.interface.mode." + this.name().toLowerCase()); }
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
	
	PlayerReference owner = null;
	public void initOwner(Entity owner) { if(this.owner == null) this.owner = PlayerReference.of(owner); }
	public void setOwner(String name) {
		PlayerReference newOwner = PlayerReference.of(name);
		if(newOwner != null)
		{
			this.owner = newOwner;
			this.team = null;
			this.mode = ActiveMode.DISABLED;
			this.setChanged();
			if(!this.isClient())
			{
				BlockEntityUtil.sendUpdatePacket(this, this.saveOwner(this.saveMode(new CompoundTag())));
			}
		}
	}
	
	TeamReference team = null;
	public boolean hasTeam() { return this.getTeam() != null; }
	public Team getTeam() { 
		if(this.team != null)
			return this.team.getTeam(this.isClient());
		return null;
	}
	public void setTeam(UUID teamID) {
		this.team = Team.referenceOf(teamID);
		this.setChanged();
		if(!this.isClient())
		{
			BlockEntityUtil.sendUpdatePacket(this, this.saveOwner(new CompoundTag()));
		}
	}
	
	public PlayerReference getReferencedPlayer() {
		if(this.hasTeam())
		{
			Team team = this.getTeam();
			if(team.getOwner() != null)
				return team.getOwner().copyWithName(team.getName());
		}
		if(this.owner != null)
			return this.owner;
		return null;
	}
	
	public String getOwnerName() {
		if(this.hasTeam())
			return this.getTeam().getName();
		else if(this.owner != null)
			return this.owner.lastKnownName();
		else
			return "ERROR";
	}
	
	public BankAccount getBankAccount() { 
		AccountReference reference = this.getAccountReference();
		if(reference != null)
			return reference.get();
		return null;
	}
	public AccountReference getAccountReference() {
		if(this.hasTeam())
			return BankAccount.GenerateReference(this.isClient(), this.getTeam());
		if(this.owner != null)
			return BankAccount.GenerateReference(this.isClient(), this.owner);
		return null;
	}
	
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
	
	UniversalTradeReference reference = new UniversalTradeReference(this::isClient, this::deserializeTrade);
	public boolean hasTrader() { return this.reference.hasTrader(); }
	public UniversalTraderData getTrader() { return this.reference.getTrader(); }
	public int getTradeIndex() { return this.reference.getTradeIndex(); }
	public TradeData getReferencedTrade() { return this.reference.getLocalTrade(); }
	public TradeData getTrueTrade() { return this.reference.getTrueTrade(); }
	
	private SimpleContainer upgradeSlots = new SimpleContainer(5);
	public Container getUpgradeInventory() { return this.upgradeSlots; }
	
	public void setUpgradeSlotsDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveUpgradeSlots(new CompoundTag()));
	}
	
	public void setTrader(UUID traderID) {
		//Trader is the same id. Ignore the change.
		if(this.reference.getTraderID() != null && this.reference.getTraderID().equals(traderID))
			return;
		this.reference.setTrader(traderID);
		this.reference.setTrade(-1);
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
	
	protected abstract TradeData deserializeTrade(CompoundTag compound);
	
	private int waitTimer = INTERACTION_DELAY;
	
	public boolean canAccess(Player player) {
		if(this.hasTeam())
		{
			Team team = this.getTeam();
			return team.isMember(player);
		}
		else if(this.owner != null)
			return this.owner.is(player) || TradingOffice.isAdminPlayer(player);
		return true;
	}
	
	/**
	 * Whether the given player has owner-level permissions.
	 * If owned by a team, this will return true for team admins & the team owner.
	 */
	public boolean isOwner(Entity player) {
		if(this.hasTeam())
		{
			Team team = this.getTeam();
			return team.isAdmin((Player)player);
		}
		else if(this.owner != null)
			return this.owner.is(player) || TradingOffice.isAdminPlayer((Player)player);
		return true;
	}
	
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
	
	protected abstract TradeContext.Builder buildTradeContext(TradeContext.Builder baseContext);
	
	//Don't mark final to prevent conflicts with LC Tech not yet updating to the new method
	public TradeContext getTradeContext() {
		if(this.interaction.trades)
			return this.buildTradeContext(TradeContext.create(this.getTrader(), this.getReferencedPlayer()).withBankAccount(this.getAccountReference()).withMoneyListener(this::trackMoneyInteraction)).build();
		return TradeContext.createStorageMode(this.getTrader());
	}
	
	public boolean isClient() { return this.level != null ? this.level.isClientSide : true; }
	
	protected final <H extends SidedHandler<?>> H addHandler(@Nonnull H handler) {
		handler.setParent(this);
		this.handlers.add(handler);
		return handler;
	}
	
	@Override
	public CompoundTag getUpdateTag() { return this.saveWithoutMetadata(); }
	
	@Override
	protected void saveAdditional(CompoundTag compound) {
		this.saveOwner(compound);
		this.saveMode(compound);
		this.saveOnlineMode(compound);
		this.saveInteraction(compound);
		this.saveLastResult(compound);
		this.saveReference(compound);
		this.saveUpgradeSlots(compound);
		for(SidedHandler<?> handler : this.handlers) this.saveHandler(compound, handler);
	}
	
	protected final CompoundTag saveOwner(CompoundTag compound) {
		if(this.owner != null)
			compound.put("Owner", this.owner.save());
		if(this.team != null)
			compound.putUUID("Team", this.team.getID());
		else
			compound.putBoolean("Team", false);
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
	
	protected final CompoundTag saveReference(CompoundTag compound) {
		compound.put("Trade", this.reference.save());
		return compound;
	}
	
	protected final CompoundTag saveUpgradeSlots(CompoundTag compound) {
		InventoryUtil.saveAllItems("Upgrades", compound, this.upgradeSlots);
		return compound;
	}
	
	protected final CompoundTag saveHandler(CompoundTag compound, SidedHandler<?> handler) {
		compound.put(handler.getTag(), handler.save());
		return compound;
	}
	
	public void setHandlerDirty(SidedHandler<?> handler) {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveHandler(new CompoundTag(), handler));
	}
	
	@Override
	public void load(CompoundTag compound) {
		if(compound.contains("Owner", Tag.TAG_COMPOUND))
			this.owner = PlayerReference.load(compound.getCompound("Owner"));
		if(compound.contains("Team", Tag.TAG_INT_ARRAY))
			this.team = Team.referenceOf(compound.getUUID("Team"));
		else if(compound.contains("Team"))
			this.team = null;
		if(compound.contains("Mode"))
			this.mode = EnumUtil.enumFromString(compound.getString("Mode"), ActiveMode.values(), ActiveMode.DISABLED);
		if(compound.contains("OnlineMode"))
			this.onlineMode = compound.getBoolean("OnlineMode");
		if(compound.contains("InteractionType", Tag.TAG_STRING))
			this.interaction = EnumUtil.enumFromString(compound.getString("InteractionType"), InteractionType.values(), InteractionType.TRADE);
		if(compound.contains("Trade", Tag.TAG_COMPOUND))
			this.reference.load(compound.getCompound("Trade"));
		if(compound.contains("Upgrades"))
			this.upgradeSlots = InventoryUtil.loadAllItems("Upgrades", compound, 5);
		for(SidedHandler<?> handler : this.handlers) {
			if(compound.contains(handler.getTag(), Tag.TAG_COMPOUND))
				handler.load(compound.getCompound(handler.getTag()));
		}
	}
	
	public void setInteractionDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveInteraction(new CompoundTag()));
	}
	
	@Override
	public <C> LazyOptional<C> getCapability(@Nonnull Capability<C> cap, @Nullable Direction side) {
		Direction relativeSide = this.getRelativeSide(side);
		for(int i = 0; i < this.handlers.size(); ++i) {
			Object handler = this.handlers.get(i).getHandler(relativeSide);
			if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && handler instanceof IItemHandler)
				return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> (IItemHandler)handler));
			if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && handler instanceof IFluidHandler)
				return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> (IFluidHandler)handler));
			else if(cap == CapabilityEnergy.ENERGY && handler instanceof IEnergyStorage)
				return CapabilityEnergy.ENERGY.orEmpty(cap, LazyOptional.of(() -> (IEnergyStorage)handler));
		}
		return super.getCapability(cap, side);
	}
	
	protected final Direction getRelativeSide(Direction side) {
		Direction relativeSide = side;
		if(relativeSide != null & this.getBlockState().getBlock() instanceof IRotatableBlock)
			relativeSide = IItemHandlerBlock.getRelativeSide(((IRotatableBlock)this.getBlockState().getBlock()).getFacing(this.getBlockState()), side);
		return relativeSide;
	}
	
	public void sendHandlerMessage(ResourceLocation type, CompoundTag message) {
		if(this.isClient())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageHandlerMessage(this.worldPosition, type, message));
	}
	
	public void receiveHandlerMessage(ResourceLocation type, Player player, CompoundTag message) {
		if(!this.canAccess(player))
			return;
		for(int i = 0; i < this.handlers.size(); ++i) {
			if(this.handlers.get(i).getType().equals(type))
				this.handlers.get(i).receiveMessage(message);
		}
	}
	
	public void setTradeReferenceDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveReference(new CompoundTag()));
	}
	
	public TradeResult interactWithTrader() {
		TradeContext tradeContext = this.getTradeContext();
		UniversalTraderData trader = this.getTrader();
		if(trader != null)
			this.lastResult = trader.ExecuteTrade(tradeContext, this.reference.getTradeIndex());
		else
			this.lastResult = TradeResult.FAIL_NULL;
		this.setLastResultDirty();
		return this.lastResult;
	}
	
	protected void trackMoneyInteraction(CoinValue price, boolean isDeposit) {
		
	}
	
	public boolean isActive() {
		return this.mode.isActive(this) && this.onlineCheck();
	}
	
	public boolean onlineCheck() {
		//Always return false on the client
		
		if(this.isClient())
			return false;
		if(!this.onlineMode)
			return true;
			
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server == null)
			return false;
		if(this.hasTeam())
		{
			Team team = this.getTeam();
			for(PlayerReference member : team.getAllMembers())
			{
				if(member != null && server.getPlayerList().getPlayer(member.id) != null)
					return true;
			}
		}
		else if(this.owner != null)
			return server.getPlayerList().getPlayer(this.owner.id) != null;
		return false;
	}
	
	@Override
	public void serverTick() {
		if(this.isActive())
		{
			this.waitTimer -= 1;
			if(this.waitTimer <= 0)
			{
				this.waitTimer = this.getInteractionDelay();
				if(this.interaction.requiresPermissions)
				{
					if(!this.validTrader() || !this.getTrader().hasPermission(this.getReferencedPlayer(), Permissions.INTERACTION_LINK))
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
			}
		}
			
	}
	
	//Returns whether the trader referenced is valid
	public boolean validTrader() {
		UniversalTraderData trader = this.reference.getTrader();
		return trader != null && this.validTraderType(trader);
	}
	
	public boolean hasTraderPermissions() {
		UniversalTraderData trader = this.reference.getTrader();
		if(trader != null && this.owner != null)
			return trader.hasPermission(this.owner, Permissions.INTERACTION_LINK);
		return false;
	}
	
	public boolean validTrade() {
		TradeData expectedTrade = this.getReferencedTrade();
		TradeData trueTrade = this.getTrueTrade();
		if(expectedTrade == null || trueTrade == null)
			return false;
		return expectedTrade.AcceptableDifferences(expectedTrade.compare(trueTrade));
	}
	
	public abstract boolean validTraderType(UniversalTraderData trader);
	
	protected abstract void drainTick();
	
	protected abstract void restockTick();
	
	protected abstract void tradeTick();
	
	public void openMenu(Player player) {
		if(this.canAccess(player))
		{
			MenuProvider provider = this.getMenuProvider();
			if(provider == null)
				return;
			NetworkHooks.openScreen((ServerPlayer)player, provider, this.worldPosition);
		}
	}
	
	protected MenuProvider getMenuProvider() { return new InterfaceMenuProvider(this); }
	
	public static class InterfaceMenuProvider implements MenuProvider {
		private final TraderInterfaceBlockEntity blockEntity;
		public InterfaceMenuProvider(TraderInterfaceBlockEntity blockEntity) { this.blockEntity = blockEntity; }
		@Override
		public AbstractContainerMenu createMenu(int windowID, Inventory inventory, Player player) {
			return new TraderInterfaceMenu(windowID, inventory, this.blockEntity);
		}
		@Override
		public Component getDisplayName() { return Component.empty(); }
	}
	
	protected int getInteractionDelay() {
		int delay = INTERACTION_DELAY;
		for(int i = 0; i < this.upgradeSlots.getContainerSize() && delay > 1; ++i)
		{
			ItemStack stack = this.upgradeSlots.getItem(i);
			if(stack.getItem() instanceof UpgradeItem)
			{
				UpgradeItem upgrade = (UpgradeItem)stack.getItem();
				if(upgrade.getUpgradeType() instanceof SpeedUpgrade)
					delay -= UpgradeItem.getUpgradeData(stack).getIntValue(SpeedUpgrade.DELAY_AMOUNT);
			}
		}
		return delay;
	}
	
	public abstract void initMenuTabs(TraderInterfaceMenu menu);
	
	public boolean allowUpgrade(UpgradeType type) {
		return type == UpgradeType.SPEED || this.allowAdditionalUpgrade(type);
	}
	
	protected boolean allowAdditionalUpgrade(UpgradeType type) { return false; }
	
	public final List<ItemStack> dumpContents(BlockState state, boolean dropBlock) { 
		List<ItemStack> contents = new ArrayList<>();
		
		//Drop trader block
		if(dropBlock)
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
		this.dumpContents(contents);
		return contents;
		
	}
	
	@Override
	public PlayerReference getOwner() {
		return this.owner;
	}
	
}
