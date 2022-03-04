package io.github.lightman314.lightmanscurrency.blockentity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.blockentity.ItemInterfaceBlockEntity.IItemHandlerBlock;
import io.github.lightman314.lightmanscurrency.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.universal_traders.RemoteTradeData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.RemoteTradeData.RemoteTradeResult;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.interfacebe.MessageHandlerMessage;
import io.github.lightman314.lightmanscurrency.trader.interfacing.TradeInteraction;
import io.github.lightman314.lightmanscurrency.trader.interfacing.handlers.SidedHandler;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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

public abstract class UniversalTraderInterfaceBlockEntity<I extends TradeInteraction<I,T>,T extends TradeData> extends TickableBlockEntity {

	public static final int INTERACTION_LIMIT = 3;
	
	PlayerReference owner = null;
	public PlayerReference getOwner() { return this.owner; }
	public void initOwner(Player owner) { if(this.owner == null) this.owner = PlayerReference.of(owner); }
	boolean linkToAccount = false;
	public void setLinkToAccount(boolean linkToAccount) { this.linkToAccount = true; }
	
	List<SidedHandler<I,?>> handlers = new ArrayList<>();
	
	public AccountReference getBankAccount() {
		if(this.owner != null && this.linkToAccount)
			return BankAccount.GenerateReference(this.isClient(), this.owner);
		return null;
	}
	
	public boolean isOwner(Entity player) {
		if(this.owner != null && this.owner.is(player))
			return true;
		return false;
	}
	
	public final Function<CompoundTag,T> tradeDeserializer;
	
	protected UniversalTraderInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Function<CompoundTag,T> tradeDeserializer) {
		super(type, pos, state);
		this.tradeDeserializer = tradeDeserializer;
	}
	
	public abstract List<I> getInteractions();
	
	public abstract void markTradeInteractionsDirty();
	
	public abstract RemoteTradeData getRemoteTradeData();
	
	public boolean isClient() { return this.level.isClientSide; }
	
	protected final void addHandler(@Nonnull SidedHandler<I,?> handler) {
		handler.setParent(this);
		this.handlers.add(handler);
	}
	
	@Override
	protected void saveAdditional(CompoundTag compound) {
		if(this.owner != null)
			compound.put("Owner", this.owner.save());
		for(int i = 0; i < this.handlers.size(); ++i) {
			SidedHandler<I,?> handler = this.handlers.get(i);
			compound.put(handler.getTag(), handler.save());
		}
	}
	
	@Override
	public void load(CompoundTag compound) {
		if(compound.contains("Owner", Tag.TAG_COMPOUND))
			this.owner = PlayerReference.load(compound.getCompound("Owner"));
		for(int i = 0; i < this.handlers.size(); ++i) {
			SidedHandler<I,?> handler = this.handlers.get(i);
			if(compound.contains(handler.getTag(), Tag.TAG_COMPOUND))
				handler.load(compound.getCompound(handler.getTag()));
		}
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
	
	public RemoteTradeResult interactWithTrader(UniversalTraderData trader, int tradeIndex) {
		RemoteTradeData remoteTradeData = this.getRemoteTradeData();
		if(trader != null)
			return trader.handleRemotePurchase(tradeIndex, remoteTradeData);
		return RemoteTradeResult.FAIL_NULL;
	}
	
}
