package io.github.lightman314.lightmanscurrency.api.traders;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.MoneyHolder;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.MultiMoneyHolder;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxableContext;
import io.github.lightman314.lightmanscurrency.api.ticket.TicketCollectionResult;
import io.github.lightman314.lightmanscurrency.api.traders.discount_codes.CouponSource;
import io.github.lightman314.lightmanscurrency.api.traders.discount_codes.IDiscountCodeSource;
import io.github.lightman314.lightmanscurrency.common.blockentity.handler.ICanCopy;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.menus.slots.InteractionSlot;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.ItemRequirement;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TradeContext {

    private static long nextID = 0;

    public final long id;

    public final boolean isStorageMode;

    //Trader Data (public as it will be needed for trade data context)
    private final TraderData trader;
    public boolean hasTrader() { return this.trader != null; }
    public TraderData getTrader() { return this.trader; }

    private final ITaxableContext taxContext;
    public ITaxableContext getTaxContext() { return this.taxContext; }

    //Player Data
    @Nullable
    private final Player player;
    public boolean hasPlayer() { return this.player != null; }
    @Nullable
    public Player getPlayer() { return this.player; }

    //Public as it will be needed to run trade events to confirm a trades alerts/cost for display purposes
    private final PlayerReference playerReference;
    public boolean hasPlayerReference() { return this.playerReference != null; }
    public final PlayerReference getPlayerReference() { return this.playerReference; }

    //Money/Payment related data
    private final MultiMoneyHolder moneyHolders;

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
    private final IItemHandler itemHandler;
    private boolean hasItemHandler() { return this.itemHandler != null; }

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
        this.moneyHolders = new MultiMoneyHolder(builder.moneyHandlers);
        this.discountCodeSources = builder.discountCodes;
        this.discountCodeSources.sort(Comparator.comparingInt(IDiscountCodeSource::priority).reversed());
        this.playerReference = builder.playerReference;
        this.interactionSlot = builder.interactionSlot;
        this.itemHandler = builder.itemHandler;
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
        if(this.hasItemHandler())
            return InventoryUtil.CanExtractItem(this.itemHandler, item);
        else if(this.hasPlayer())
            return InventoryUtil.GetItemCount(this.player.getInventory(), item) >= item.getCount();
        return false;
    }

    /**
     * Whether the given item stacks are present in the item handler, and can be successfully removed without issue.
     */
    public boolean hasItems(ItemStack... items)
    {
        for(ItemStack item : InventoryUtil.combineQueryItems(items))
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
        for(ItemStack item : InventoryUtil.combineQueryItems(items))
        {
            if(!hasItem(item))
                return false;
        }
        return true;
    }

    /**
     * Whether the given item stacks are present in the item handler, and can be successfully removed without issue.
     */
    public boolean hasItems(ItemRequirement... requirements)
    {
        if(this.hasItemHandler())
            return ItemRequirement.getFirstItemsMatchingRequirements(this.itemHandler, requirements) != null;
        else if(this.hasPlayer())
            return ItemRequirement.getFirstItemsMatchingRequirements(this.player.getInventory(), requirements) != null;
        return false;
    }

    /**
     * Whether a ticket with the given ticket id is present in the item handler, and can be successfully removed without issue.<br>
     * As of v2.2.6.3, also returns true if a pass with said ID is present
     */
    public boolean hasTicket(long ticketID) {
        if(this.hasItemHandler())
        {
            for(int i = 0; i < this.itemHandler.getSlots(); ++i)
            {
                ItemStack stack = this.itemHandler.getStackInSlot(i);
                if(TicketItem.isTicket(stack) || TicketItem.isPass(stack))
                {
                    long id = TicketItem.GetTicketID(stack);
                    if(id == ticketID)
                    {
                        ItemStack copyStack = stack.copy();
                        copyStack.setCount(1);
                        if(InventoryUtil.CanExtractItem(this.itemHandler, copyStack))
                            return true;
                    }
                }
            }
        }
        else if(this.hasPlayer())
        {
            Inventory inventory = this.player.getInventory();
            for(int i = 0; i < inventory.getContainerSize(); ++i)
            {
                ItemStack stack = inventory.getItem(i);
                if(TicketItem.isTicket(stack) || TicketItem.isPass(stack))
                {
                    long id = TicketItem.GetTicketID(stack);
                    if(id == ticketID)
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Whether a ticket with the given ticket id is present in the item handler, and can be successfully removed without issue.
     * @deprecated Use {@link #collectTicket(long)} for all ticket/pass interactions
     */
    @Deprecated(since = "2.2.6.3")
    public boolean hasPass(long ticketID) {
        if(this.hasItemHandler())
        {
            for(int i = 0; i < this.itemHandler.getSlots(); ++i)
            {
                ItemStack stack = this.itemHandler.getStackInSlot(i);
                if(TicketItem.isPass(stack))
                {
                    long id = TicketItem.GetTicketID(stack);
                    if(id == ticketID)
                        return true;
                }
            }
        }
        else if(this.hasPlayer())
        {
            Inventory inventory = this.player.getInventory();
            for(int i = 0; i < inventory.getContainerSize(); ++i)
            {
                ItemStack stack = inventory.getItem(i);
                if(TicketItem.isPass(stack))
                {
                    long id = TicketItem.GetTicketID(stack);
                    if(id == ticketID)
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Whether a pass with infinite uses and the given ticket id is present in the item handler
     */
    public boolean hasInfinitePass(long ticketID)
    {
        if(this.hasItemHandler())
        {
            for(int i = 0; i < this.itemHandler.getSlots(); ++i)
            {
                ItemStack stack = this.itemHandler.getStackInSlot(i);
                if(TicketItem.isInfinitePass(stack))
                {
                    long id = TicketItem.GetTicketID(stack);
                    if(id == ticketID)
                        return true;
                }
            }
        }
        else if(this.hasPlayer())
        {
            Inventory inventory = this.player.getInventory();
            for(int i = 0; i < inventory.getContainerSize(); ++i)
            {
                ItemStack stack = inventory.getItem(i);
                if(TicketItem.isInfinitePass(stack))
                {
                    long id = TicketItem.GetTicketID(stack);
                    if(id == ticketID)
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Removes the given item stack from the item handler.
     * @return Whether the extraction was successful. Will return false if it could not be extracted correctly.
     */
    public boolean collectItem(ItemStack item)
    {
        if(this.hasItem(item))
        {
            if(this.hasItemHandler())
            {
                InventoryUtil.RemoveItemCount(this.itemHandler, item);
                return true;
            }
            else if(this.hasPlayer())
            {
                InventoryUtil.RemoveItemCount(this.player.getInventory(), item);
                return true;
            }
        }
        return false;
    }

    public boolean collectItems(List<ItemStack> items)
    {
        items = InventoryUtil.combineQueryItems(items);
        for(ItemStack item : items)
        {
            if(!this.hasItem(item))
                return false;
        }
        for(ItemStack item : items)
            this.collectItem(item);
        return true;
    }

    public List<ItemStack> getCollectableItems(ItemRequirement...requirements)
    {
        if(this.hasItemHandler())
            return ItemRequirement.getFirstItemsMatchingRequirements(this.itemHandler, requirements);
        else if(this.hasPlayer())
            return ItemRequirement.getFirstItemsMatchingRequirements(this.player.getInventory(), requirements);
        return null;
    }

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
        {
            //Don't consume anything if a pass with infinite durability is present
            if(this.hasInfinitePass(ticketID))
                return TicketCollectionResult.PASS;
            if(this.hasItemHandler())
            {
                for(int i = 0; i < this.itemHandler.getSlots(); ++i) {
                    ItemStack stack = this.itemHandler.getStackInSlot(i);
                    if(TicketItem.isTicket(stack))
                    {
                        long id = TicketItem.GetTicketID(stack);
                        if(id == ticketID)
                        {
                            ItemStack extractStack = stack.copy();
                            extractStack.setCount(1);
                            if(InventoryUtil.RemoveItemCount(this.itemHandler, extractStack))
                                return TicketCollectionResult.PASS_WITH_STUB;
                        }
                    }
                    else if(TicketItem.isPass(stack))
                    {
                        long id = TicketItem.GetTicketID(stack);
                        if(id == ticketID)
                        {
                            ItemStack extra = TicketItem.damageTicket(stack);
                            boolean spawnStub = extra.isEmpty() && stack.isEmpty();
                            if(!extra.isEmpty())
                            {
                                extra = ItemHandlerHelper.insertItem(this.itemHandler,extra,false);
                                if(!extra.isEmpty() && this.player != null)
                                    ItemHandlerHelper.giveItemToPlayer(this.player,extra);
                            }
                            return TicketCollectionResult.pass(spawnStub);
                        }
                    }
                }
            }
            else if(this.hasPlayer())
            {
                Inventory inventory = this.player.getInventory();
                for(int i = 0; i < inventory.getContainerSize(); ++i)
                {
                    ItemStack stack = inventory.getItem(i);
                    if(TicketItem.isTicket(stack))
                    {
                        long id = TicketItem.GetTicketID(stack);
                        if(id == ticketID)
                        {
                            inventory.removeItem(i, 1);
                            inventory.setChanged();
                            return TicketCollectionResult.PASS_WITH_STUB;
                        }
                    }
                    else if(TicketItem.isPass(stack))
                    {
                        long id = TicketItem.GetTicketID(stack);
                        if(id == ticketID)
                        {
                            ItemStack extra = TicketItem.damageTicket(stack);
                            boolean spawnStub = extra.isEmpty() && stack.isEmpty();
                            if(!extra.isEmpty())
                                ItemHandlerHelper.giveItemToPlayer(this.player,extra);
                            return TicketCollectionResult.pass(spawnStub);
                        }
                    }
                }
            }
        }
        return TicketCollectionResult.FAIL;
    }

    public boolean canFitItem(ItemStack item)
    {
        if(item.isEmpty())
            return true;
        if(this.hasItemHandler())
            return ItemHandlerHelper.insertItemStacked(this.itemHandler, item, true).isEmpty();
        return this.hasPlayer();
    }

    public boolean canFitItems(ItemStack... items)
    {
        if(this.hasItemHandler())
        {
            IItemHandler original = this.itemHandler;
            IItemHandler copy;
            if(original instanceof ICanCopy<?>)
            {
                copy = ((ICanCopy<? extends IItemHandler>)original).copy();
            }
            else
            {
                //Assume a default item handler
                NonNullList<ItemStack> inventory = NonNullList.withSize(original.getSlots(), ItemStack.EMPTY);
                for(int i = 0; i < original.getSlots(); ++i)
                    inventory.set(i, original.getStackInSlot(i));
                copy = new ItemStackHandler(inventory);
            }
            for(ItemStack item : items)
            {
                if(!ItemHandlerHelper.insertItemStacked(copy, item, false).isEmpty())
                    return false;
            }
            return true;
        }
        return this.hasPlayer();
    }

    public boolean canFitItems(List<ItemStack> items)
    {
        if(this.hasItemHandler())
        {
            IItemHandler original = this.itemHandler;
            IItemHandler copy = null;
            if(original instanceof ICanCopy<?>)
            {
                try{
                    copy = ((ICanCopy<? extends IItemHandler>)original).copy();
                } catch(Throwable t) { LightmansCurrency.LogDebug("Error copying item handler.",t); }
            }
            if(copy == null)
            {
                //Assume a default item handler
                NonNullList<ItemStack> inventory = NonNullList.withSize(original.getSlots(), ItemStack.EMPTY);
                for(int i = 0; i < original.getSlots(); ++i)
                    inventory.set(i, original.getStackInSlot(i).copy());
                copy = new ItemStackHandler(inventory);
            }
            for(ItemStack item : InventoryUtil.combineQueryItems(items))
            {
                if(!ItemHandlerHelper.insertItemStacked(copy, item, false).isEmpty())
                    return false;
            }
            return true;
        }
        return this.hasPlayer();
    }

    public boolean putItem(ItemStack item)
    {
        if(this.canFitItem(item))
        {
            if(this.hasItemHandler())
            {
                ItemStack leftovers = ItemHandlerHelper.insertItemStacked(this.itemHandler, item, false);
                if(leftovers.isEmpty())
                    return true;
                else
                {
                    //Failed to place the items in the item handler, so take what few were placed back.
                    ItemStack placedStack = item.copy();
                    placedStack.setCount(item.getCount() - leftovers.getCount());
                    if(!item.isEmpty())
                        this.collectItem(placedStack);
                    return false;
                }
            }
            if(this.hasPlayer())
            {
                ItemHandlerHelper.giveItemToPlayer(this.player, item);
                return true;
            }
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
            IEnergyStorage energyHandler = batteryStack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
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
                IEnergyStorage energyHandler = batteryStack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
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
            IEnergyStorage energyHandler = batteryStack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
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
                IEnergyStorage energyHandler = batteryStack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
                if(energyHandler != null)
                    energyHandler.receiveEnergy(amount,false);
                return true;
            }
        }
        return false;
    }

    public boolean hasCustomData(ResourceLocation key) { return this.customData.containsKey(key); }
    @Nullable
    public Object getCustomData(ResourceLocation key) { return this.customData.get(key); }

    public static TradeContext createStorageMode(TraderData trader) { return new Builder(trader).build(); }
    @Deprecated(since = "2.3.0.3")
    public static Builder create(TraderData trader, Player player) { return create(trader,player,false); }
    public static Builder create(TraderData trader, Player player, boolean networkAccess) { return new Builder(trader, player,true,ITaxableContext.simpleContext(trader,networkAccess)); }
    @Deprecated(since = "2.3.0.3")
    public static Builder create(TraderData trader, PlayerReference player) { return create(trader,player,false); }
    public static Builder create(TraderData trader, PlayerReference player, boolean networkAccess) { return new Builder(trader, player, ITaxableContext.simpleContext(trader,networkAccess)); }

    @MethodsReturnNonnullByDefault
    @FieldsAreNonnullByDefault
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
        private IItemHandler itemHandler;
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
                this.withMoneyHolder(MoneyAPI.getApi().GetPlayersMoneyHandler(player));
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
        public Builder withCoinSlots(Container coinSlots) {
            if(this.player == null)
                return this;
            return this.withMoneyHandler(MoneyAPI.getApi().GetContainersMoneyHandler(coinSlots, this.player), LCText.TOOLTIP_MONEY_SOURCE_SLOTS.get(), 100);
        }

        public Builder withMoneyHandler(IMoneyHandler moneyHandler, Component title, int priority) { return this.withMoneyHolder(MoneyHolder.createFromHandler(moneyHandler, title, priority)); }
        public Builder withMoneyHolder(IMoneyHolder moneyHandler) {
            if(!this.moneyHandlers.contains(moneyHandler))
                this.moneyHandlers.add(moneyHandler);
            return this;
        }

        public Builder withInteractionSlot(InteractionSlot interactionSlot) { this.interactionSlot = interactionSlot; return this; }

        public Builder withItemHandler(IItemHandler itemHandler) { this.itemHandler = itemHandler; return this; }
        public Builder withFluidHandler(IFluidHandler fluidHandler) { this.fluidHandler = fluidHandler; return this; }
        public Builder withEnergyHandler(IEnergyStorage energyHandler) { this.energyHandler = energyHandler; return this; }

        public Builder withCustomData(ResourceLocation key, Object customData) { this.customData.put(key,customData); return this; }

        public TradeContext build() { return new TradeContext(this); }

    }

}