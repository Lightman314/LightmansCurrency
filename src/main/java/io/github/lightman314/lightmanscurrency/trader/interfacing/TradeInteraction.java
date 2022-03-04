package io.github.lightman314.lightmanscurrency.trader.interfacing;

import io.github.lightman314.lightmanscurrency.blockentity.UniversalTraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.universal_traders.RemoteTradeData.RemoteTradeResult;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData.TradeComparisonResult;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public abstract class TradeInteraction<I extends TradeInteraction<I,T>, T extends TradeData> {

	public enum InteractionMode { RESTOCK(true, true, false, false), DRAIN(true, false, true, false), RESTOCK_AND_DRAIN(true, true, true, false), TRADE(false, false, false, true); 
		public final boolean requiresPermissions;
		public final boolean restock;
		public final boolean drain;
		public final boolean trade;
		InteractionMode(boolean requiresPermissions, boolean restock, boolean drain, boolean trade) {
			this.requiresPermissions = requiresPermissions;
			this.restock = restock;
			this.drain = drain;
			this.trade = trade;
		}
	}
	
	private InteractionMode mode = InteractionMode.TRADE;
	public InteractionMode currentMode() { return this.mode; }
	
	protected final UniversalTraderInterfaceBlockEntity<I,T> parent;
	public final UniversalTradeReference<T> tradeReference;
	public abstract boolean isValid();
	
	private boolean active;
	public boolean isActive() { return this.active; }
	public void setActive(boolean active) { this.active = active; }
	
	public TradeInteraction(UniversalTraderInterfaceBlockEntity<I,T> parent) {
		this.parent = parent;
		this.tradeReference = new UniversalTradeReference<T>(this.parent::isClient, this.parent.tradeDeserializer);
	}
	
	public CompoundTag save() {
		CompoundTag compound = new CompoundTag();
		compound.put("TradeReference", this.tradeReference.save());
		compound.putString("Mode", this.mode.name());
		this.saveAdditionalData(compound);
		return compound;
	}
	protected abstract void saveAdditionalData(CompoundTag compound);
	
	public void load(CompoundTag compound) {
		if(compound.contains("TradeReference", Tag.TAG_COMPOUND))
			this.tradeReference.load(compound.getCompound("TradeReference"));
		if(compound.contains("Mode", Tag.TAG_STRING))
			this.mode = EnumUtil.enumFromString(compound.getString("Mode"), InteractionMode.values(), InteractionMode.TRADE);
	}
	
	protected abstract void loadAdditionalData(CompoundTag compound);
	
	/**
	 * Gets the differences between the local copy of the trade, and the traders actual trade.
	 */
	protected TradeComparisonResult getTradeDifferences() {
		T localTrade = this.tradeReference.getLocalTrade();
		T trueTrade = this.tradeReference.getTrueTrade();
		if(localTrade != null)
			return localTrade.compare(trueTrade);
		return new TradeComparisonResult();
	}
	
	/**
	 * Compares the local copy of the trade to the current trade on that trader, and determines whether any changes to the trade are acceptable.
	 * Examples of acceptable changes, are a decrease in price, or an increase in the quantity sold.
	 * Will return false if this interaction is not in TRADE mode.
	 */
	protected boolean acceptableTrade() {
		if(this.mode == InteractionMode.TRADE)
		{
			TradeComparisonResult result = this.getTradeDifferences();
			if(!result.isCompatible() || !this.tradeReference.hasTrade())
				return false;
			T localTrade = this.tradeReference.getLocalTrade();
			return localTrade.AcceptableDifferences(result);
		}
		else
			return false;
	}
	
	/**
	 * Confirms that this interface's owner still has permission to RESTOCK or DRAIN the trader.
	 * Will return false if this interaction is not in RESTOCK, DRAIN, or RESTOCK_AND_DRAIN mode.
	 */
	protected boolean acceptableTrader() {
		if(this.mode.requiresPermissions)
		{
			UniversalTraderData trader = this.tradeReference.getTrader();
			if(trader == null)
				return false;
			return this.validTrader(trader) && trader.hasPermission(this.parent.getOwner(), Permissions.INTERACTION_LINK);
		}
		return false;
	}
	
	/**
	 * Used to determine whether the trader is a valid type for this interaction;
	 */
	protected abstract boolean validTrader(UniversalTraderData trader);
	
	/**
	 * Interacts with the universal trade that this trader is linked to.
	 * Should only be run after confirming that the trade & traders are both valid to interact with.
	 */
	protected RemoteTradeResult interact() {
		return this.parent.interactWithTrader(this.tradeReference.getTrader(), this.tradeReference.getTradeIndex());
	}
	
	public final void interactionTick() {
		this.pretick();
		if(this.mode.restock)
			this.restockTick();
		if(this.mode.drain)
			this.drainTick();
		if(this.mode.trade)
			this.tradeTick();
		this.tick();
	}
	
	/**
	 * Run before the restockTick, drainTick, & tradeTick every tick
	 */
	protected void pretick() { }
	
	/**
	 * Run after the restockTick, drainTick & tradeTick every tick
	 */
	protected void tick() { }
	
	/**
	 * Run each tick if the interaction is in RESTOCK or RESTOCK_AND_DRAIN mode.
	 */
	protected abstract void restockTick();
	
	/**
	 * Run each tick if the interaction is in DRAIN or RESTOCK_AND_DRAIN mode.
	 */
	protected abstract void drainTick();
	
	/**
	 * Run each tick if the interaction is in TRADE mode.
	 */
	protected abstract void tradeTick();
}
