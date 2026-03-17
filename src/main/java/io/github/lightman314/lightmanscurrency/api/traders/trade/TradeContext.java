package io.github.lightman314.lightmanscurrency.api.traders.trade;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.LCItemStackHandler;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.MoneyInventory;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.other.ContainerMoneyHandlerWrapper;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.api.money.capability.MoneyHolder;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxableContext;
import io.github.lightman314.lightmanscurrency.api.ticket.TicketCollectionResult;
import io.github.lightman314.lightmanscurrency.api.ticket.TicketUtil;
import io.github.lightman314.lightmanscurrency.api.traders.InteractionSlotData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.discount_codes.CouponSource;
import io.github.lightman314.lightmanscurrency.api.traders.discount_codes.IDiscountCodeSource;
import io.github.lightman314.lightmanscurrency.api.traders.misc.PlayerInventoryFailsafe;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.common.blockentity.handler.ICanCopy;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.misc.menus.slots.InteractionSlot;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
import io.github.lightman314.lightmanscurrency.util.ItemRequirement;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;

import javax.annotation.Nullable;

public class TradeContext {

	private static long nextID = 0;

	public final long id;

	public final boolean isStorageMode;
	
	//Trader Data (public as it will be needed for trade data context)
	private final TraderData trader;
	public TraderData getTrader() { return this.trader; }
    @Nullable
    public <N extends TraderNode> N getTraderNode(TraderNodeType<N> type) { return this.trader.getNode(type); }
    public boolean hasTraderNode(TraderNodeType<?> type) { return this.trader.hasNode(type); }

	//Player Data
	@Nullable
	private final Player player;
	public boolean hasPlayer() { return this.player != null; }
	@Nullable
	public Player getPlayer() { return this.player; }

    private final ITaxableContext taxContext;
    public ITaxableContext getTaxContext() { return this.taxContext; }
	
	//Public as it will be needed to run trade events to confirm a trades alerts/cost for display purposes
	private final PlayerReference playerReference;
	public boolean hasPlayerReference() { return this.playerReference != null; }
	public final PlayerReference getPlayerReference() { return this.playerReference; }

	//Money/Payment related data
	private final IMoneyHolder moneyHolders;

	//Discount Codes
	private final List<IDiscountCodeSource> discountCodeSources;
	public Set<Integer> getDiscountCodes() {
		Set<Integer> set = new HashSet<>();
		for(var source : this.discountCodeSources)
			set.addAll(source.getDiscountCodes());
		return set;
	}
	public boolean hasDiscountCode(String code) {
        for(IDiscountCodeSource source : this.discountCodeSources)
        {
            if(source.containsCode(code))
                return true;
        }
        return false;
    }
    public void consumeDiscountCode(String code)
    {
        for(IDiscountCodeSource source : this.discountCodeSources)
        {
            if(source.consumeCode(code))
                return;
        }
    }
	
	//Interaction Slots (bucket/battery slot, etc.)
	private final InteractionSlot interactionSlot;
	private boolean hasInteractionSlot(String type) { return this.getInteractionSlot(type) != null; }
	private InteractionSlot getInteractionSlot(String type) { if(this.interactionSlot == null) return null; if(this.interactionSlot.isType(type)) return this.interactionSlot; return null; }

	//Item related data
	private final List<IItemHandler> itemHandlers;
	private boolean hasItemHandler() { return !this.itemHandlers.isEmpty(); }
	
	//Fluid related data
	private final IFluidHandler fluidTank;
	private boolean hasFluidTank() { return this.fluidTank != null; }
	
	//Energy related data
	private final IEnergyStorage energyTank;
	private boolean hasEnergyTank() { return this.energyTank != null; }

    private final Map<ResourceLocation,Object> customData;

	private TradeContext(Builder builder) {
		this.id = nextID++;
		this.isStorageMode = builder.storageMode;
		this.trader = builder.trader;
		this.player = builder.player;
        this.taxContext = builder.taxableContext;
		this.moneyHolders = IMoneyHolder.combine(builder.moneyHandlers);
		this.discountCodeSources = builder.discountCodes;
        this.discountCodeSources.sort(Comparator.comparingInt(IDiscountCodeSource::priority).reversed());
		this.playerReference = builder.playerReference;
		this.interactionSlot = builder.interactionSlot;
        this.itemHandlers = builder.fullItemHandlers();
		this.fluidTank = builder.fluidHandler;
		this.energyTank = builder.energyHandler;
        this.customData = ImmutableMap.copyOf(builder.customData);
	}
	
	public boolean hasPaymentMethod() { return this.hasPlayer(); }
	
	public boolean hasFunds(MoneyValue price)
	{
		if(price.isFree() || price.isEmpty())
			return true;
		return this.getAvailableFunds().containsValue(price);
	}

	public MoneyView getAvailableFunds() { return this.moneyHolders.getStoredMoney(); }

	public List<Component> getAvailableFundsDescription() {
		List<Component> text = new ArrayList<>();
		this.moneyHolders.formatTooltip(text);
		return text;
	}
	
	public boolean getPayment(MoneyValue price)
	{
		if(price == null)
			return false;
		if(price.isFree() || price.isEmpty())
			return true;
		if(this.moneyHolders.extractMoney(price,true).isEmpty())
		{
			this.moneyHolders.extractMoney(price,false);
			return true;
		}
		return false;
	}
	
	public boolean givePayment(MoneyValue price)
	{
		if(price == null)
			return false;
		if(price.isFree())
			return true;
		if(this.moneyHolders.insertMoney(price,true).isEmpty())
		{
			this.moneyHolders.insertMoney(price,false);
			return true;
		}
		return false;
	}
	
	/**
	 * Whether the given item stack is present in the item handler, and can be successfully removed without issue.
	 */
	public boolean hasItem(ItemStack item)
	{
        ItemStack extracted = ItemHandlerUtil.extractItem(this.itemHandlers,item,true);
		return !extracted.isEmpty() && extracted.getCount() == item.getCount();
	}
	
	/**
	 * Whether the given item stacks are present in the item handler, and can be successfully removed without issue.
	 */
	public boolean hasItems(ItemStack... items)
	{
		for(ItemStack item : ItemHandlerUtil.combineStacks(items))
		{
			if(!hasItem(item))
				return false;
		}
		return true;
	}

	/**
	 * Whether the given item stacks are present in the item handler, and can be successfully removed without issue.
	 */
	public boolean hasItems(List<ItemStack> items)
	{
		if(items == null)
			return false;
		for(ItemStack item : ItemHandlerUtil.combineStacks(items))
		{
			if(!hasItem(item))
				return false;
		}
		return true;
	}

	/**
	 * Whether the given item stacks are present in the item handler, and can be successfully removed without issue.
	 */
	public boolean hasItems(ItemRequirement... requirements) {
        return ItemRequirement.getFirstItemsMatchingRequirements(this.itemHandlers,requirements) != null;
    }
	
	/**
	 * Whether a ticket with the given ticket id is present in the item handler, and can be successfully removed without issue.<br>
     * As of v2.2.6.3, also returns true if a pass with said ID is present
	 */
	public boolean hasTicket(long ticketID) { return TicketUtil.hasTicket(this.itemHandlers,ticketID).isPresent(); }

    /**
     * Whether a pass with infinite uses and the given ticket id is present in the item handler
     */
    public boolean hasInfinitePass(long ticketID) { return TicketUtil.hasTicket(this.itemHandlers,ticketID).isUnlimited(); }
	
	/**
	 * Removes the given item stack from the item handler.
	 * @return Whether the extraction was successful. Will return false if it could not be extracted correctly.
	 */
	public boolean collectItem(ItemStack item)
	{
        ItemStack taken = ItemHandlerUtil.extractItem(this.itemHandlers,item,true);
        if(ItemHandlerUtil.isExactMatch(taken,item))
        {
            taken = ItemHandlerUtil.extractItem(this.itemHandlers,item,false);
            if(!ItemHandlerUtil.isExactMatch(taken,item))
            {
                //If we somehow didn't take all of them when **not** simulating, force the items back in
                ItemStack overflow = ItemHandlerUtil.insertItem(this.itemHandlers,item,false);
                if(!overflow.isEmpty() && this.player != null)
                    ItemHandlerHelper.giveItemToPlayer(this.player,overflow);
                return false;
            }
            return true;
        }
        return false;
	}

	public boolean collectItems(List<ItemStack> items)
	{
		items = ItemHandlerUtil.combineStacks(items);
		for(ItemStack item : items)
		{
			if(!this.hasItem(item))
				return false;
		}
		for(ItemStack item : items)
			this.collectItem(item);
		return true;
	}

	public List<ItemStack> getCollectableItems(ItemRequirement...requirements) { return ItemRequirement.getFirstItemsMatchingRequirements(this.itemHandlers,requirements); }

	public void hightlightItems(List<ItemRequirement> requirements, List<Slot> slots, List<Integer> results) {
		if(this.hasPlayer())
		{
			Map<Integer,Integer> inventoryConsumedCounts = new HashMap<>();
			Container inventory = this.player.getInventory();
			for(ItemRequirement requirement : requirements)
			{
				int amountToConsume = requirement.getCount();
				for(int i = 0; i < inventory.getContainerSize() && amountToConsume > 0; ++i)
				{
					ItemStack stack = inventory.getItem(i);
					if(requirement.test(stack) && !stack.isEmpty())
					{
						int alreadyConsumed = inventoryConsumedCounts.getOrDefault(i, 0);
						int consumeCount = Math.min(amountToConsume, stack.getCount() - alreadyConsumed);
						amountToConsume -= consumeCount;
						alreadyConsumed += consumeCount;
						if(alreadyConsumed > 0)
							inventoryConsumedCounts.put(i, alreadyConsumed);
					}
				}
			}
			for(int relevantSlot : inventoryConsumedCounts.keySet())
			{
				for(int i = 0; i < slots.size(); ++i)
				{
					Slot slot = slots.get(i);
					if(slot.container == inventory && slot.getContainerSlot() == relevantSlot)
						results.add(i);
				}
			}
		}
	}
	
	/**
	 * Removes the given ticket from the item handler.
	 * @return Whether the extraction was successful. Will return false if it could not be extracted correctly.
	 */
	public TicketCollectionResult collectTicket(long ticketID) {
        if(this.hasTicket(ticketID))
            return TicketUtil.takeTicket(this.itemHandlers,ticketID,s -> {
                if(this.player != null)
                {
                    ItemHandlerHelper.giveItemToPlayer(this.player,s);
                    return ItemStack.EMPTY;
                }
                return s;
            });
		return TicketCollectionResult.FAIL;
	}
	
	public boolean canFitItem(ItemStack item)
	{
		if(item.isEmpty())
			return true;
        if(this.player != null)
            return true;
        return ItemHandlerUtil.insertItem(this.itemHandlers,item,true).isEmpty();
	}

	public boolean canFitItems(ItemStack... items)
	{
        return this.canFitItems(ImmutableList.copyOf(items));
	}

	public boolean canFitItems(List<ItemStack> items)
	{
        if(this.player != null)
            return true;
        List<IItemHandler> handlers = new ArrayList<>();
        for(IItemHandler handler : this.itemHandlers)
        {
            IItemHandler copy = null;
            if(handler instanceof ICanCopy<?>)
            {
                try {
                    copy = ((ICanCopy<? extends IItemHandler>)handler).copy();
                } catch (ClassCastException ignored) {}
            }
            if(copy == null)
            {
                //Assume a default item handler
                NonNullList<ItemStack> inventory = NonNullList.withSize(handler.getSlots(),ItemStack.EMPTY);
                for(int i = 0; i < handler.getSlots(); ++i)
                    inventory.set(i, handler.getStackInSlot(i));
                copy = new LCItemStackHandler(inventory);
            }
            handlers.add(copy);
        }
        if(this.hasItemHandler())
        {
            for(ItemStack item : items)
            {
                if(!ItemHandlerUtil.insertItem(handlers,item,false).isEmpty())
                    return false;
            }
            return true;
        }
        return false;
	}
	
	public boolean putItem(ItemStack item)
	{
        if(this.canFitItem(item))
        {
            ItemStack leftovers = ItemHandlerUtil.insertItem(this.itemHandlers,item,false);
            if(!leftovers.isEmpty())
            {
                if(this.player != null)
                    ItemHandlerHelper.giveItemToPlayer(this.player,leftovers);
                else
                {
                    //Take the given items and abort
                    int givenCount = item.getCount() - leftovers.getCount();
                    if(givenCount > 0)
                    {
                        ItemStack take = item.copyWithCount(givenCount);
                        ItemHandlerUtil.extractItem(this.itemHandlers,take,false);
                    }
                    return false;
                }
            }
            return true;
        }
        return false;
	}
	
	public boolean hasFluid(FluidStack fluid)
	{
		if(this.hasFluidTank())
		{
			FluidStack result = this.fluidTank.drain(fluid, FluidAction.SIMULATE);
			return !result.isEmpty() && result.getAmount() >= fluid.getAmount();
		}
		if(this.hasInteractionSlot(InteractionSlotData.FLUID_TYPE))
		{
			ItemStack bucketStack = this.getInteractionSlot(InteractionSlotData.FLUID_TYPE).getItem();
			AtomicBoolean hasFluid = new AtomicBoolean(false);
			FluidUtil.getFluidHandler(bucketStack).ifPresent(fluidHandler ->{
				FluidStack result = fluidHandler.drain(fluid, FluidAction.SIMULATE);
				hasFluid.set(!result.isEmpty() && result.getAmount() == fluid.getAmount());
			});
			return hasFluid.get();
		}
		return false;
	}
	
	public boolean drainFluid(FluidStack fluid)
	{
		if(this.hasFluid(fluid))
		{
			if(this.hasFluidTank())
			{
				this.fluidTank.drain(fluid, FluidAction.EXECUTE);
				return true;
			}
			if(this.hasInteractionSlot(InteractionSlotData.FLUID_TYPE))
			{
				InteractionSlot slot = this.getInteractionSlot(InteractionSlotData.FLUID_TYPE);
				ItemStack bucketStack = slot.getItem();
				FluidUtil.getFluidHandler(bucketStack).ifPresent(fluidHandler ->{
					fluidHandler.drain(fluid, FluidAction.EXECUTE);
					slot.set(fluidHandler.getContainer());
				});
				return true;
			}
		}
		return false;
	}
	
	public boolean canFitFluid(FluidStack fluid)
	{
		if(this.hasFluidTank())
			return this.fluidTank.fill(fluid, FluidAction.SIMULATE) == fluid.getAmount();
		if(this.hasInteractionSlot(InteractionSlotData.FLUID_TYPE))
		{
			ItemStack bucketStack = this.getInteractionSlot(InteractionSlotData.FLUID_TYPE).getItem();
			AtomicBoolean fitFluid = new AtomicBoolean(false);
			FluidUtil.getFluidHandler(bucketStack).ifPresent(fluidHandler -> fitFluid.set(fluidHandler.fill(fluid, FluidAction.SIMULATE) == fluid.getAmount()));
			return fitFluid.get();
		}
		return false;
	}
	
	public boolean fillFluid(FluidStack fluid)
	{
		if(this.canFitFluid(fluid))
		{
			if(this.hasFluidTank())
			{
				this.fluidTank.fill(fluid, FluidAction.EXECUTE);
				return true;
			}
			if(this.hasInteractionSlot(InteractionSlotData.FLUID_TYPE))
			{
				InteractionSlot slot = this.getInteractionSlot(InteractionSlotData.FLUID_TYPE);
				ItemStack bucketStack = slot.getItem();
				FluidUtil.getFluidHandler(bucketStack).ifPresent(fluidHandler ->{
					fluidHandler.fill(fluid, FluidAction.EXECUTE);
					//Put the modified item back into the slot
					slot.set(fluidHandler.getContainer());
				});
			}
		}
		return false;
	}
	
	public boolean hasEnergy(int amount)
	{
		if(this.hasEnergyTank())
			return this.energyTank.extractEnergy(amount, true) == amount;
		else if(this.hasInteractionSlot(InteractionSlotData.ENERGY_TYPE))
		{
			ItemStack batteryStack = this.getInteractionSlot(InteractionSlotData.ENERGY_TYPE).getItem();
			boolean hasEnergy = false;
			IEnergyStorage energyHandler = batteryStack.getCapability(Capabilities.EnergyStorage.ITEM);
			if(energyHandler != null)
				return energyHandler.extractEnergy(amount, true) == amount;
			return false;
		}
		return false;
	}
	
	public boolean drainEnergy(int amount)
	{
		if(this.hasEnergy(amount))
		{
			if(this.hasEnergyTank())
			{
				this.energyTank.extractEnergy(amount, false);
				return true;
			}
			if(this.hasInteractionSlot(InteractionSlotData.ENERGY_TYPE))
			{
				ItemStack batteryStack = this.getInteractionSlot(InteractionSlotData.ENERGY_TYPE).getItem();
				IEnergyStorage energyHandler = batteryStack.getCapability(Capabilities.EnergyStorage.ITEM);
				if(energyHandler != null)
					energyHandler.extractEnergy(amount,false);
				return true;
			}
		}
		return false;
	}
	
	public boolean canFitEnergy(int amount)
	{
		if(this.hasEnergyTank())
			return this.energyTank.receiveEnergy(amount, true) == amount;
		else if(this.hasInteractionSlot(InteractionSlotData.ENERGY_TYPE))
		{
			ItemStack batteryStack = this.getInteractionSlot(InteractionSlotData.ENERGY_TYPE).getItem();
			IEnergyStorage energyHandler = batteryStack.getCapability(Capabilities.EnergyStorage.ITEM);
			if(energyHandler != null)
				return energyHandler.receiveEnergy(amount,true) == amount;
			return false;
		}
		return false;
	}
	
	public boolean fillEnergy(int amount)
	{
		if(this.canFitEnergy(amount))
		{
			if(this.hasEnergyTank())
			{
				this.energyTank.receiveEnergy(amount, false);
				return true;
			}
			else if(this.hasInteractionSlot(InteractionSlotData.ENERGY_TYPE))
			{
				ItemStack batteryStack = this.getInteractionSlot(InteractionSlotData.ENERGY_TYPE).getItem();
				IEnergyStorage energyHandler = batteryStack.getCapability(Capabilities.EnergyStorage.ITEM);
				if(energyHandler != null)
					energyHandler.receiveEnergy(amount,false);
				return true;
			}
		}
		return false;
	}

    public boolean hasCustomData(ResourceLocation key) { return this.customData.containsKey(key); }
    public Object getCustomData(ResourceLocation key) { return this.customData.get(key); }
	
	public static TradeContext createStorageMode(TraderData trader) { return new Builder(trader).build(); }
	public static Builder create(TraderData trader, Player player, boolean networkAccess) { return new Builder(trader, player,true,ITaxableContext.simpleContext(trader,networkAccess)); }
	public static Builder create(TraderData trader, PlayerReference player, boolean networkAccess) { return new Builder(trader, player, ITaxableContext.simpleContext(trader,networkAccess)); }

	public static class Builder
	{
		
		//Core
		private final boolean storageMode;
		private final TraderData trader;
        private final ITaxableContext taxableContext;
		@Nullable
		private final Player player;
		@Nullable
		private final PlayerReference playerReference;
		//Money
		private final List<IMoneyHolder> moneyHandlers = new ArrayList<>();
		//Discount Codes
		private final List<IDiscountCodeSource> discountCodes = new ArrayList<>();
		//Interaction Slots
		@Nullable
		private InteractionSlot interactionSlot;
		
		//Item
		@Nullable
		private final List<IItemHandler> itemHandlers = new ArrayList<>();
        private List<IItemHandler> fullItemHandlers()
        {
            List<IItemHandler> result = new ArrayList<>(this.itemHandlers);
            if(this.player != null)
                result.add(new PlayerMainInvWrapper(this.player.getInventory()));
            return result;
        }
		//Fluid
		@Nullable
		private IFluidHandler fluidHandler;
		//Energy
		@Nullable
		private IEnergyStorage energyHandler;

        private final Map<ResourceLocation,Object> customData = new HashMap<>();

		private Builder(TraderData trader) { this.storageMode = true; this.trader = trader; this.player = null; this.playerReference = null; this.taxableContext = ITaxableContext.defaultContext(this.trader); }
		private Builder(TraderData trader, @Nullable Player player, boolean playerInteractable, ITaxableContext taxableContext) {
			this.trader = trader;
			this.player = player;
            this.taxableContext = taxableContext;
			if(this.player != null)
				this.withDiscountCodes(this.player.getInventory(),s -> ItemHandlerHelper.giveItemToPlayer(this.player,s));
			this.playerReference = PlayerReference.of(player);
			this.storageMode = false;
			if(playerInteractable)
            {
                this.withMoneyHolder(MoneyAPI.getApi().GetPlayersMoneyHandlerUnsafe(player));
                this.withMoneyHolder(new PlayerInventoryFailsafe(player));
            }
		}
		private Builder(TraderData trader, @Nullable PlayerReference player, ITaxableContext taxableContext) { this.trader = trader; this.playerReference = player; this.player = null; this.storageMode = false; this.taxableContext = taxableContext; }

		public Builder withDiscountCodes(Container container, Consumer<ItemStack> overflowHandler) { return this.withDiscountCodes(new CouponSource(container,overflowHandler)); }

		public Builder withDiscountCodes(IDiscountCodeSource codeSource)
		{
			this.discountCodes.add(codeSource);
			return this;
		}

		public Builder withBankAccount(@Nullable BankReference bankAccount) {
			if(bankAccount == null)
				return this;
			return this.withMoneyHolder(bankAccount);
		}
		public Builder withCoinSlots(MoneyInventory coinSlots) {
			if(this.player == null)
				return this;
            IClientTracker tracker = this.trader != null ? this.trader : IClientTracker.entityWrapper(this.player);
            return this.withMoneyHandler(new ContainerMoneyHandlerWrapper(coinSlots,tracker),LCText.TOOLTIP_MONEY_SOURCE_SLOTS.get(),-200,-200)
                    .withMoneyHandler(MoneyAPI.getApi().GetContainersMoneyHandler(coinSlots, this.player), LCText.TOOLTIP_MONEY_SOURCE_SLOTS.get(), 100);
		}

		public Builder withMoneyHandler(IMoneyHandler moneyHandler, Component title, int priority) { return this.withMoneyHolder(MoneyHolder.createFromHandler(moneyHandler, title, priority)); }
		public Builder withMoneyHandler(IMoneyHandler moneyHandler, Component title, int priority, int inversePriority) { return this.withMoneyHolder(MoneyHolder.createFromHandler(moneyHandler, title, priority, inversePriority)); }
		public Builder withMoneyHolder(IMoneyHolder moneyHandler) {
			if(!this.moneyHandlers.contains(moneyHandler))
				this.moneyHandlers.add(moneyHandler);
			return this;
		}

		public Builder withInteractionSlot(InteractionSlot interactionSlot) { this.interactionSlot = interactionSlot; return this; }
		
		public Builder withItemHandler(IItemHandler itemHandler) { this.itemHandlers.add(Objects.requireNonNull(itemHandler)); return this; }
		public Builder withFluidHandler(IFluidHandler fluidHandler) { this.fluidHandler = fluidHandler; return this; }
		public Builder withEnergyHandler(IEnergyStorage energyHandler) { this.energyHandler = energyHandler; return this; }

        public Builder withCustomData(ResourceLocation key,Object customData) { this.customData.put(key,customData); return this; }

		public TradeContext build() { return new TradeContext(this); }
		
	}
	
}
