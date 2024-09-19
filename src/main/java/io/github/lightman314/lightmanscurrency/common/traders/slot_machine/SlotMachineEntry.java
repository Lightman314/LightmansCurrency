package io.github.lightman314.lightmanscurrency.common.traders.slot_machine;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.value.IItemBasedValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class SlotMachineEntry {

    public static final int ITEM_LIMIT = 4;

    public final List<ItemStack> items;
    public void TryAddItem(ItemStack item) { if(this.items.size() >= ITEM_LIMIT || item.isEmpty()) return; this.items.add(item); }
    private int weight;
    public int getWeight() { return this.weight; }
    public void setWeight(int newWeight) { this.weight = Math.max(1, newWeight); }

    private SlotMachineEntry(List<ItemStack> items, int weight) { this.items = InventoryUtil.copyList(items); this.setWeight(weight); }

    public boolean isValid() { return !this.items.isEmpty() && this.weight > 0; }

    public boolean isMoney() {
        if(this.items.isEmpty())
            return false;
        ChainData chain = null;
        for(ItemStack item : this.items)
        {
            if(CoinAPI.API.IsCoin(item, false))
            {
                if(chain == null)
                    chain = CoinAPI.API.ChainDataOfCoin(item);
                else if(chain != CoinAPI.API.ChainDataOfCoin(item)) //Reject if coins are from different chains
                    return false;
            }
            else
                return false;
        }
        return true;
    }
    public MoneyValue getMoneyValue() {
        if(!this.isMoney())
            return MoneyValue.empty();
        ChainData chain = null;
        long value = 0;
        for(ItemStack item : this.items)
        {
            if(CoinAPI.API.IsCoin(item, false))
            {
                if(chain == null)
                    chain = CoinAPI.API.ChainDataOfCoin(item);
                else if(chain != CoinAPI.API.ChainDataOfCoin(item)) //Reject if coins are from different chains
                    return MoneyValue.empty();
                value += chain.getCoreValue(item) * item.getCount();
            }
            else if(!item.isEmpty())
                return MoneyValue.empty();
        }
        if(chain == null)
            return MoneyValue.empty();
        return CoinValue.fromNumber(chain.chain, value);
    }

    public void validateItems()
    {
        for(int i = 0; i < this.items.size(); ++i)
        {
            if(this.items.get(i).isEmpty())
                this.items.remove(i--);
        }
    }

    public List<ItemStack> getDisplayItems()
    {
        if(this.isMoney())
        {
            MoneyValue value = this.getMoneyValue();
            if(value instanceof IItemBasedValue itemValue)
                return itemValue.getAsSeperatedItemList();
        }
        return InventoryUtil.copyList(this.items);
    }

    @Nonnull
    public static List<ItemStack> splitDisplayItems(@Nonnull List<ItemStack> displayItems)
    {
        if(displayItems.size() >= ITEM_LIMIT)
            return displayItems;
        int totalCount = 0;
        for(ItemStack s : displayItems)
            totalCount+= s.getCount();
        List<ItemStack> result = InventoryUtil.copyList(displayItems);
        Random random = new Random();
        while(result.size() < ITEM_LIMIT && result.size() < totalCount)
        {
            int splitIndex = random.nextInt(result.size());
            ItemStack s = result.get(splitIndex);
            if(s.getCount() > 1)
            {
                int splitCount = s.getCount() / 2;
                ItemStack split = s.split(splitCount);
                result.add(split);
            }
        }
        return result;
    }

    public boolean CanGiveToCustomer(TradeContext context)
    {
        if(this.isMoney())
            return context.hasPaymentMethod();
        else
            return context.canFitItems(this.items);
    }

    /**
     * Gives the entry items to the customer (via the trade context), and removes the items/money from storage
     */
    public boolean GiveToCustomer(SlotMachineTraderData trader, TradeContext context)
    {
        if(this.hasStock(trader))
        {
            if(this.isMoney())
            {
                MoneyValue reward = this.getMoneyValue();
                if(!context.givePayment(reward))
                    return false;
                if(!trader.isCreative())
                    trader.removeStoredMoney(reward, false);
            }
            else
            {
                for(int i = 0; i < this.items.size(); ++i)
                {
                    if(!context.putItem(this.items.get(i).copy()))
                    {
                        for(int x = 0; x < i; ++x)
                            context.collectItem(this.items.get(x).copy());
                        return false;
                    }
                }
                if(!trader.isCreative())
                {
                    for(ItemStack i : this.items)
                        trader.getStorage().removeItem(i);
                    trader.markStorageDirty();
                }
            }
            return true;
        }
        else
            return false;
    }

    public int getStock(SlotMachineTraderData trader)
    {
        if(!this.isValid())
            return 0;
        if(trader.isCreative())
            return 1;
        if(this.isMoney())
        {
            MoneyValue payout = this.getMoneyValue();
            if(payout.isEmpty() || payout.getCoreValue() <= 0)
                return 0;
            IMoneyHolder storedMoney = trader.getStoredMoney();
            MoneyValue totalMoney = storedMoney.getStoredMoney().valueOf(payout.getUniqueName());
            return (int)(totalMoney.getCoreValue() / payout.getCoreValue());
        }
        else
        {
            int minStock = Integer.MAX_VALUE;
            for(ItemStack item : InventoryUtil.combineQueryItems(this.items))
            {
                int count = trader.getStorage().getItemCount(item);
                int stock = count / item.getCount();
                if(stock < minStock)
                    minStock = stock;
            }
            return minStock;
        }
    }

    public boolean hasStock(SlotMachineTraderData trader) { return this.getStock(trader) > 0; }

    public boolean isItemRelevant(ItemStack item)
    {
        if(this.isMoney())
            return false;
        return this.items.stream().anyMatch(i -> InventoryUtil.ItemMatches(i, item));
    }

    public CompoundTag save(@Nonnull HolderLookup.Provider lookup)
    {
        CompoundTag compound = new CompoundTag();
        ListTag itemList = new ListTag();
        for(ItemStack item : this.items)
            itemList.add(InventoryUtil.saveItemNoLimits(item,lookup));
        compound.put("Items",itemList);
        compound.putInt("Weight", this.weight);
        return compound;
    }

    @Nonnull
    public JsonObject toJson(@Nonnull HolderLookup.Provider lookup)
    {
        JsonObject json = new JsonObject();
        JsonArray itemList = new JsonArray();
        for(ItemStack item : this.items)
            itemList.add(FileUtil.convertItemStack(item,lookup));
        json.add("Items", itemList);
        json.addProperty("Weight", this.weight);
        return json;
    }

    public static SlotMachineEntry create() { return new SlotMachineEntry(new ArrayList<>(), 1); }

    public static SlotMachineEntry load(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup)
    {
        List<ItemStack> items = new ArrayList<>();
        if(compound.contains("Items"))
        {
            ListTag itemList = compound.getList("Items", Tag.TAG_COMPOUND);
            for(int i = 0; i < itemList.size(); ++i)
            {
                ItemStack stack = InventoryUtil.loadItemNoLimits(itemList.getCompound(i),lookup);
                if(!stack.isEmpty())
                    items.add(stack);
            }
        }
        if(compound.contains("Weight"))
            return new SlotMachineEntry(items, compound.getInt("Weight"));
        else
            return new SlotMachineEntry(items, 1);
    }

    @Nonnull
    public static SlotMachineEntry parse(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException
    {
        List<ItemStack> items = new ArrayList<>();
        JsonArray itemList = GsonHelper.getAsJsonArray(json, "Items");
        for(int i = 0; i < itemList.size(); ++i)
        {
            try{
                ItemStack stack = FileUtil.parseItemStack(itemList.get(i).getAsJsonObject(),lookup);
                if(stack.isEmpty())
                    throw new JsonSyntaxException("Cannot add an empty item to a Slot Machine Entry!");
                items.add(stack);
            } catch (JsonSyntaxException | ResourceLocationException t) { LightmansCurrency.LogError("Error parsing Slot Machine Entry item #" + (i + 1), t); }
        }
        if(items.isEmpty())
            throw new JsonSyntaxException("Slot Machine Entry has no valid items!");
        int weight = GsonHelper.getAsInt(json, "Weight", 1);
        return new SlotMachineEntry(items, weight);
    }
}