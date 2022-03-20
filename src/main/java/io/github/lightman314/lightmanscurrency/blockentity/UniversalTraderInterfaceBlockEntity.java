package io.github.lightman314.lightmanscurrency.blockentity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.blockentity.ItemInterfaceBlockEntity.IItemHandlerBlock;
import io.github.lightman314.lightmanscurrency.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.universal_traders.RemoteTradeData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.RemoteTradeData.RemoteTradeResult;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.interfacebe.MessageHandlerMessage;
import io.github.lightman314.lightmanscurrency.trader.interfacing.UniversalTradeReference;
import io.github.lightman314.lightmanscurrency.trader.interfacing.handlers.SidedHandler;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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

public abstract class UniversalTraderInterfaceBlockEntity<T extends TradeData> extends TickableBlockEntity {
	
	public static final int INTERACTION_DELAY = 20;
	
	public enum InteractionType {
		RESTOCK_AND_DRAIN(true, true, true, false, 1),
		RESTOCK(true, true, false, false, 2),
		DRAIN(true, false, true, false, 3),
		TRADE(false, false, false, true, 0);
		
		public final boolean requiresPermissions;
		public final boolean restocks;
		public final boolean drains;
		public final boolean trades;
		public final int index;
		public final Component getDisplayText() { return new TranslatableComponent("gui.lightmanscurrency.interface.type." + this.name().toLowerCase()); }
		
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
			return fromIndex(0);
		}
		
		public static int size() {
			return 4;
		}
		
	}
	
	PlayerReference owner = null;
	public PlayerReference getOwner() { return this.owner; }
	public void initOwner(Entity owner) { if(this.owner == null) this.owner = PlayerReference.of(owner); }
	boolean linkToAccount = false;
	public void setLinkToAccount(boolean linkToAccount) { this.linkToAccount = true; }
	
	List<SidedHandler<?>> handlers = new ArrayList<>();
	
	private boolean isActive = false;
	public boolean interactionActive() { return this.isActive; }
	public void toggleActive() { this.isActive = !this.isActive; this.setActiveDirty(); }
	
	private InteractionType interaction = InteractionType.TRADE;
	public InteractionType getInteractionType() { return this.interaction; }
	public void setInteractionType(InteractionType type) { this.interaction = type; this.setInteractionDirty(); }
	
	UniversalTradeReference<T> reference = new UniversalTradeReference<T>(this::isClient, this::deserializeTrade);
	public UniversalTraderData getTrader() { return this.reference.getTrader(); }
	public int getTradeIndex() { return this.reference.getTradeIndex(); }
	public T getReferencedTrade() { return this.reference.getLocalTrade(); }
	public T getTrueTrade() { return this.reference.getTrueTrade(); }
	
	public void setTrader(UUID traderID) {
		//Trader is the same id. Ignore the change.
		if(this.reference.getTraderID() != null && this.reference.getTraderID().equals(traderID))
			return;
		this.reference.setTrader(traderID);
		this.reference.setTrade(-1);
		this.setTradeReferenceDirty();
	}
	public void setTradeIndex(int tradeIndex) { this.reference.setTrade(tradeIndex); this.setTradeReferenceDirty(); }
	
	private RemoteTradeResult lastResult = RemoteTradeResult.SUCCESS;
	public RemoteTradeResult mostRecentTradeResult() { return this.lastResult; }
	
	protected abstract T deserializeTrade(CompoundTag compound);
	
	private int waitTimer = INTERACTION_DELAY;
	
	public AccountReference getBankAccount() {
		if(this.owner != null && this.linkToAccount)
			return BankAccount.GenerateReference(this.isClient(), this.owner);
		return null;
	}
	
	public boolean isOwner(Entity player) {
		if(this.owner != null && this.owner.is(player))
			return true;
		if(player instanceof Player)
			return TradingOffice.isAdminPlayer((Player)player);
		return false;
	}
	
	public final Function<CompoundTag,T> tradeDeserializer;
	
	protected UniversalTraderInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Function<CompoundTag,T> tradeDeserializer) {
		super(type, pos, state);
		this.tradeDeserializer = tradeDeserializer;
	}
	
	public void setActiveDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveActivated(new CompoundTag()));
	}
	
	public void setLastResultDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveLastResult(new CompoundTag()));
	}
	
	public abstract RemoteTradeData getRemoteTradeData();
	
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
		this.saveActivated(compound);
		this.saveInteraction(compound);
		this.saveLastResult(compound);
		this.saveReference(compound);
		for(SidedHandler<?> handler : this.handlers) this.saveHandler(compound, handler);
	}
	
	protected final CompoundTag saveOwner(CompoundTag compound) {
		if(this.owner != null)
			compound.put("Owner", this.owner.save());
		return compound;
	}
	
	protected final CompoundTag saveActivated(CompoundTag compound) {
		compound.putBoolean("Activated", this.isActive);
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
		if(compound.contains("Activated"))
			this.isActive = compound.getBoolean("Activated");
		if(compound.contains("InteractionType", Tag.TAG_STRING))
			this.interaction = EnumUtil.enumFromString(compound.getString("InteractionType"), InteractionType.values(), InteractionType.TRADE);
		if(compound.contains("Trade", Tag.TAG_COMPOUND))
			this.reference.load(compound.getCompound("Trade"));
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
		if(!this.isOwner(player))
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
	
	public RemoteTradeResult interactWithTrader() {
		RemoteTradeData remoteTradeData = this.getRemoteTradeData();
		UniversalTraderData trader = this.getTrader();
		if(trader != null)
			this.lastResult = trader.handleRemotePurchase(this.reference.getTradeIndex(), remoteTradeData);
		else
			this.lastResult = RemoteTradeResult.FAIL_NULL;
		this.setLastResultDirty();
		return this.lastResult;
	}
	
	@Override
	public void serverTick() {
		if(this.isActive)
		{
			this.waitTimer -= 1;
			if(this.waitTimer <= 0)
			{
				this.waitTimer = INTERACTION_DELAY;
				if(this.interaction.requiresPermissions)
				{
					if(!this.validTrader())
					{
						this.isActive = false;
						this.setActiveDirty();
						return;
					}
					if(this.interaction.drains)
						this.drainTick();
					if(this.interaction.restocks)
						this.restockTick();
				}
				else if(this.interaction.trades)
				{
					if(!this.validTrade())
					{
						this.isActive = false;
						this.setActiveDirty();
						return;
					}
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
		T expectedTrade = this.getReferencedTrade();
		T trueTrade = this.getTrueTrade();
		if(expectedTrade == null || trueTrade == null)
			return false;
		return expectedTrade.AcceptableDifferences(expectedTrade.compare(trueTrade));
	}
	
	protected abstract boolean validTraderType(UniversalTraderData trader);
	
	protected abstract void drainTick();
	
	protected abstract void restockTick();
	
	protected abstract void tradeTick();
	
	public void openMenu(Player player) {
		if(this.isOwner(player))
		{
			MenuProvider provider = this.getMenuProvider();
			if(provider == null)
				return;
			NetworkHooks.openGui((ServerPlayer)player, provider, this.worldPosition);
		}
	}
	
	protected abstract MenuProvider getMenuProvider();
	
	public abstract void dumpContents(Level level, BlockPos pos);
	
}
