package io.github.lightman314.lightmanscurrency.common.traders.slot_machine;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.CoinValueHolder;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class SlotMachineEntry {

    public static final int ITEM_LIMIT = 4;

    public final List<ItemStack> items;
    public void TryAddItem(ItemStack item) { if(this.items.size() >= ITEM_LIMIT || item.isEmpty()) return; this.items.add(item); }
    private int weight;
    public int getWeight() { return this.weight; }
    public void setWeight(int newWeight) { this.weight = Math.max(1, newWeight); }

    private SlotMachineEntry(List<ItemStack> items, int weight) { this.items = items; this.setWeight(weight); }

    public boolean isValid() { return this.items.size() > 0 && this.weight > 0; }

    public boolean isMoney() { return this.items.size() > 0 && this.items.stream().allMatch(i -> MoneyUtil.isCoin(i, false)); }
    public CoinValue getMoneyValue() { return this.isMoney() ? MoneyUtil.getCoinValue(this.items) : CoinValue.EMPTY; }

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
            return MoneyUtil.getCoinsOfValue(this.getMoneyValue());
        else
            return InventoryUtil.copyList(this.items);
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
                CoinValue reward = this.getMoneyValue();
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
                            context.collectItem(this.items.get(i).copy());
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

    public boolean hasStock(SlotMachineTraderData trader)
    {
        if(trader.isCreative())
            return true;
        if(this.isMoney())
            return trader.getStoredMoney().getValueNumber() >= this.getMoneyValue().getValueNumber();
        for(ItemStack item : InventoryUtil.combineQueryItems(this.items))
        {
            if(!trader.getStorage().hasItem(item))
                return false;
        }
        return true;
    }

    public boolean isItemRelevant(ItemStack item)
    {
        if(this.isMoney())
            return false;
        return this.items.stream().anyMatch(i -> InventoryUtil.ItemMatches(i, item));
    }

    public CompoundTag save()
    {
        CompoundTag compound = new CompoundTag();
        ListTag itemList = new ListTag();
        for(ItemStack item : this.items)
            itemList.add(item.save(new CompoundTag()));
        compound.put("Items",itemList);
        compound.putInt("Weight", this.weight);
        return compound;
    }

    public JsonObject toJson()
    {
        JsonObject json = new JsonObject();
        JsonArray itemList = new JsonArray();
        for(ItemStack item : this.items)
            itemList.add(FileUtil.convertItemStack(item));
        json.add("Items", itemList);
        json.addProperty("Weight", this.weight);
        return json;
    }

    public static SlotMachineEntry create() { return new SlotMachineEntry(new ArrayList<>(), 1); }

    public static SlotMachineEntry load(CompoundTag compound)
    {
        List<ItemStack> items = new ArrayList<>();
        if(compound.contains("Items"))
        {
            ListTag itemList = compound.getList("Items", Tag.TAG_COMPOUND);
            for(int i = 0; i < itemList.size(); ++i)
            {
                ItemStack stack = ItemStack.of(itemList.getCompound(i));
                if(!stack.isEmpty())
                    items.add(stack);
            }
        }
        if(compound.contains("Weight"))
            return new SlotMachineEntry(items, compound.getInt("Weight"));
        else
            return new SlotMachineEntry(items, 1);
    }

    public static SlotMachineEntry parse(JsonObject json) throws Exception
    {
        List<ItemStack> items = new ArrayList<>();
        if(json.has("Items"))
        {
            JsonArray itemList = json.getAsJsonArray("Items");
            for(int i = 0; i < itemList.size(); ++i)
            {
                try{
                    ItemStack stack = FileUtil.parseItemStack(itemList.get(i).getAsJsonObject());
                    if(stack.isEmpty())
                        throw new RuntimeException("Cannot add an empty item to a Slot Machine Entry!");
                    items.add(stack);
                } catch (Throwable t) { LightmansCurrency.LogError("Error parsing Slot Machine Entry item #" + (i + 1), t); }
            }
            if(items.size() == 0)
                throw new RuntimeException("Slot Machie Entry has no valid items!");
        }
        else
            throw new RuntimeException("Slot Machine Entry has no 'Items' entry!");
        int weight = 1;
        if(json.has("Weight"))
            weight = json.get("Weight").getAsInt();
        else
            LightmansCurrency.LogWarning("Slot Machine Entry has no 'Weight' entry! Will default to a weight of 1.");
        return new SlotMachineEntry(items, weight);
    }
}