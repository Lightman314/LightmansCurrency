package io.github.lightman314.lightmanscurrency.common.blockentity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.tradeinterface.templates.TraderInterfaceBlock;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.data_updating.DataConverter;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.IDumpable;
import io.github.lightman314.lightmanscurrency.common.menus.providers.NamelessMenuProvider;
import io.github.lightman314.lightmanscurrency.common.ownership.OwnerData;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.common.traderinterface.NetworkTradeReference;
import io.github.lightman314.lightmanscurrency.common.traderinterface.handlers.SidedHandler;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext.TradeResult;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.interfacebe.MessageHandlerMessage;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType.IUpgradeable;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.SpeedUpgrade;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public abstract class TraderInterfaceBlockEntity extends EasyBlockEntity implements IUpgradeable, IDumpable, ITickableTileEntity {
	
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
		public final ITextComponent getDisplayText() { return EasyText.translatable("gui.lightmanscurrency.interface.type." + this.name().toLowerCase()); }

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
		public final ITextComponent getDisplayText() { return EasyText.translatable("gui.lightmanscurrency.interface.mode." + this.name().toLowerCase()); }
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
	
	public final OwnerData owner = new OwnerData(this, o -> this.setOwnerDirty());
	public void initOwner(Entity owner) { if(!this.owner.hasOwner()) this.owner.SetOwner(PlayerReference.of(owner)); }
	public void setOwner(String name) {
		PlayerReference newOwner = PlayerReference.of(this.isClient(), name);
		if(newOwner != null)
		{
			this.mode = ActiveMode.DISABLED;
			this.owner.SetOwner(newOwner);
		}
	}
	public void setTeam(long teamID) {
		Team team = TeamSaveData.GetTeam(this.isClient(), teamID);
		if(team != null)
		{
			this.mode = ActiveMode.DISABLED;
			this.owner.SetOwner(team);
		}
	}

	public void setOwnerDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveOwner(this.saveMode(new CompoundNBT())));
	}
	
	public PlayerReference getReferencedPlayer() { return this.owner.getPlayerForContext(); }
	
	public String getOwnerName() { return this.owner.getOwnerName(this.isClient()); }
	
	public BankAccount getBankAccount() { 
		AccountReference reference = this.getAccountReference();
		if(reference != null)
			return reference.get();
		return null;
	}
	public AccountReference getAccountReference() {
		if(this.owner != null)
		{
			if(this.owner.hasTeam())
				return BankAccount.GenerateReference(this.isClient(), this.owner.getTeam());
			if(this.owner.hasPlayer())
				return BankAccount.GenerateReference(this.isClient(), this.owner.getPlayer());
		}
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
	
	NetworkTradeReference reference = new NetworkTradeReference(this::isClient, this::deserializeTrade);
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
	
	private Inventory upgradeSlots = new Inventory(5);
	public IInventory getUpgradeInventory() { return this.upgradeSlots; }
	
	public void setUpgradeSlotsDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveUpgradeSlots(new CompoundNBT()));
	}
	
	public void setTrader(long traderID) {
		//Trader is the same id. Ignore the change.
		if(this.reference.getTraderID() == traderID)
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
	
	protected abstract TradeData deserializeTrade(CompoundNBT compound);
	
	private int waitTimer = INTERACTION_DELAY;
	
	public boolean canAccess(PlayerEntity player) { return this.owner.isMember(player); }
	
	/**
	 * Whether the given player has owner-level permissions.
	 * If owned by a team, this will return true for team admins & the team owner.
	 */
	public boolean isOwner(PlayerEntity player) { return this.owner.isAdmin(player); }
	
	protected TraderInterfaceBlockEntity(TileEntityType<?> type) {
		super(type);
	}
	
	public void setModeDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveMode(new CompoundNBT()));
	}
	
	public void setOnlineModeDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveOnlineMode(new CompoundNBT()));
	}
	
	public void setLastResultDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveLastResult(new CompoundNBT()));
	}
	
	protected abstract TradeContext.Builder buildTradeContext(TradeContext.Builder baseContext);
	
	//Don't mark final to prevent conflicts with LC Tech not yet updating to the new method
	public TradeContext getTradeContext() {
		if(this.interaction.trades)
			return this.buildTradeContext(TradeContext.create(this.getTrader(), this.getReferencedPlayer()).withBankAccount(this.getAccountReference()).withMoneyListener(this::trackMoneyInteraction)).build();
		return TradeContext.createStorageMode(this.getTrader());
	}
	
	protected final <H extends SidedHandler<?>> H addHandler(@Nonnull H handler) {
		handler.setParent(this);
		this.handlers.add(handler);
		return handler;
	}

	@Override
	protected void saveAdditional(@Nonnull CompoundNBT compound) {
		this.saveOwner(compound);
		this.saveMode(compound);
		this.saveOnlineMode(compound);
		this.saveInteraction(compound);
		this.saveLastResult(compound);
		this.saveReference(compound);
		this.saveUpgradeSlots(compound);
		for(SidedHandler<?> handler : this.handlers) this.saveHandler(compound, handler);
	}
	
	protected final CompoundNBT saveOwner(CompoundNBT compound) {
		if(this.owner != null)
			compound.put("Owner", this.owner.save());
		return compound;
	}
	
	protected final CompoundNBT saveMode(CompoundNBT compound) {
		compound.putString("Mode", this.mode.name());
		return compound;
	}
	
	protected final CompoundNBT saveOnlineMode(CompoundNBT compound) {
		compound.putBoolean("OnlineMode", this.onlineMode);
		return compound;
	}
	
	protected final CompoundNBT saveInteraction(CompoundNBT compound) {
		compound.putString("InteractionType", this.interaction.name());
		return compound;
	}
	
	protected final CompoundNBT saveLastResult(CompoundNBT compound) {
		compound.putString("LastResult", this.lastResult.name());
		return compound;
	}
	
	protected final CompoundNBT saveReference(CompoundNBT compound) {
		compound.put("Trade", this.reference.save());
		return compound;
	}
	
	protected final CompoundNBT saveUpgradeSlots(CompoundNBT compound) {
		InventoryUtil.saveAllItems("Upgrades", compound, this.upgradeSlots);
		return compound;
	}
	
	protected final CompoundNBT saveHandler(CompoundNBT compound, SidedHandler<?> handler) {
		compound.put(handler.getTag(), handler.save());
		return compound;
	}
	
	public void setHandlerDirty(SidedHandler<?> handler) {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveHandler(new CompoundNBT(), handler));
	}
	
	@Override
	protected void loadAdditional(@Nonnull CompoundNBT compound) {
		if (compound.contains("Owner", Constants.NBT.TAG_COMPOUND)) {
			CompoundNBT ownerTag = compound.getCompound("Owner");
			if (ownerTag.contains("id"))
				this.owner.SetOwner(PlayerReference.load(ownerTag));
			else
				this.owner.load(ownerTag);
		}
		if (compound.contains("Team")) {
			Team team = TeamSaveData.GetTeam(false, DataConverter.getNewTeamID(compound.getUUID("Team")));
			if (team != null)
				this.owner.SetOwner(team);
		}
		if (compound.contains("Mode"))
			this.mode = EnumUtil.enumFromString(compound.getString("Mode"), ActiveMode.values(), ActiveMode.DISABLED);
		if (compound.contains("OnlineMode"))
			this.onlineMode = compound.getBoolean("OnlineMode");
		if (compound.contains("InteractionType", Constants.NBT.TAG_STRING))
			this.interaction = EnumUtil.enumFromString(compound.getString("InteractionType"), InteractionType.values(), InteractionType.TRADE);
		if (compound.contains("Trade", Constants.NBT.TAG_COMPOUND))
			this.reference.load(compound.getCompound("Trade"));
		if (compound.contains("Upgrades"))
			this.upgradeSlots = InventoryUtil.loadAllItems("Upgrades", compound, 5);
		for (SidedHandler<?> handler : this.handlers) {
			if (compound.contains(handler.getTag(), Constants.NBT.TAG_COMPOUND))
				handler.load(compound.getCompound(handler.getTag()));
		}
	}
	
	public void setInteractionDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveInteraction(new CompoundNBT()));
	}
	
	@Override
	public <C> @Nonnull LazyOptional<C> getCapability(@Nonnull Capability<C> cap, @Nullable Direction side) {
		Direction relativeSide = this.getRelativeSide(side);
		for (SidedHandler<?> sidedHandler : this.handlers) {
			Object handler = sidedHandler.getHandler(relativeSide);
			if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && handler instanceof IItemHandler)
				return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> (IItemHandler) handler));
			if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && handler instanceof IFluidHandler)
				return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> (IFluidHandler) handler));
			else if (cap == CapabilityEnergy.ENERGY && handler instanceof IEnergyStorage)
				return CapabilityEnergy.ENERGY.orEmpty(cap, LazyOptional.of(() -> (IEnergyStorage) handler));
		}
		return super.getCapability(cap, side);
	}
	
	protected final Direction getRelativeSide(Direction side) {
		Direction relativeSide = side;
		if(relativeSide != null & this.getBlockState().getBlock() instanceof IRotatableBlock)
			relativeSide = IRotatableBlock.getRelativeSide(((IRotatableBlock)this.getBlockState().getBlock()).getFacing(this.getBlockState()), side);
		return relativeSide;
	}
	
	public void sendHandlerMessage(ResourceLocation type, CompoundNBT message) {
		if(this.isClient())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageHandlerMessage(this.worldPosition, type, message));
	}
	
	public void receiveHandlerMessage(ResourceLocation type, PlayerEntity player, CompoundNBT message) {
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
			BlockEntityUtil.sendUpdatePacket(this, this.saveReference(new CompoundNBT()));
	}
	
	public TradeResult interactWithTrader() {
		TradeContext tradeContext = this.getTradeContext();
		TraderData trader = this.getTrader();
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
		if(this.owner.hasTeam())
		{
			Team team = this.owner.getTeam();
			for(PlayerReference member : team.getAllMembers())
			{
				if(member != null && server.getPlayerList().getPlayer(member.id) != null)
					return true;
			}
		}
		else if(this.owner.hasPlayer())
			return server.getPlayerList().getPlayer(this.owner.getPlayer().id) != null;
		return false;
	}
	
	public final boolean hasTraderPermissions(TraderData trader) {
		if(trader == null)
			return false;
		Team team = this.owner.getTeam();
		if(team != null)
			return trader.getOwner().getTeam() == team;
		return trader.hasPermission(this.owner.getPlayer(), Permissions.INTERACTION_LINK);
	}
	
	@Override
	public void tick() {
		if(this.isClient())
			return;
		if(this.isActive())
		{
			this.waitTimer -= 1;
			if(this.waitTimer <= 0)
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
		return expectedTrade.AcceptableDifferences(expectedTrade.compare(trueTrade));
	}
	
	public abstract boolean validTraderType(TraderData trader);
	
	protected abstract void drainTick();
	
	protected abstract void restockTick();
	
	protected abstract void tradeTick();
	
	protected abstract void hopperTick();
	
	public void openMenu(PlayerEntity player) {
		if(this.canAccess(player))
		{
			INamedContainerProvider provider = this.getMenuProvider();
			if(provider == null)
				return;
			NetworkHooks.openGui((ServerPlayerEntity)player, provider, this.worldPosition);
		}
	}
	
	protected INamedContainerProvider getMenuProvider() { return new InterfaceMenuProvider(this); }
	
	public static class InterfaceMenuProvider extends NamelessMenuProvider {
		private final TraderInterfaceBlockEntity blockEntity;
		public InterfaceMenuProvider(TraderInterfaceBlockEntity blockEntity) { this.blockEntity = blockEntity; }
		@Override
		public Container createMenu(int windowID, @Nonnull PlayerInventory inventory, @Nonnull PlayerEntity player) {
			return new TraderInterfaceMenu(windowID, inventory, this.blockEntity);
		}
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
		return type == UpgradeType.SPEED || (type == UpgradeType.HOPPER && this.allowHopperUpgrade() && !this.hasHopperUpgrade()) || this.allowAdditionalUpgrade(type);
	}
	
	protected boolean allowHopperUpgrade() { return true; }
	
	protected boolean allowAdditionalUpgrade(UpgradeType type) { return false; }
	
	protected final boolean hasHopperUpgrade() { return UpgradeType.hasUpgrade(UpgradeType.HOPPER, this.upgradeSlots); }
	
	public final List<ItemStack> getContents(World level, BlockPos pos, BlockState state, boolean dropBlock) {
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
		this.getAdditionalContents(contents);
		return contents;
		
	}
	
	protected abstract void getAdditionalContents(List<ItemStack> contents);
	
	@Override
	public OwnerData getOwner() { return this.owner; }
	
}