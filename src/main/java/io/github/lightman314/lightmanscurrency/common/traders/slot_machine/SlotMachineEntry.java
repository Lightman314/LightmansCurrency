package io.github.lightman314.lightmanscurrency.common.traders.slot_machine;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.value.IItemBasedValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.util.TagUtil;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class SlotMachineEntry {

    public static final int ITEM_LIMIT = 4;

    public static final DecimalFormat ODDS_FORMATTER;

    static {
        ODDS_FORMATTER = new DecimalFormat();
        ODDS_FORMATTER.setMaximumFractionDigits(2);
        ODDS_FORMATTER.setMinimumFractionDigits(2);
    }

    public static IconData DEFAULT_ICON = IconUtil.ICON_X;
    public static NonNullList<IconData> createDefaultIcons() { return NonNullList.withSize(ITEM_LIMIT,DEFAULT_ICON); }

    //Items
    public final List<ItemStack> items;
    public void TryAddItem(ItemStack item) { if(this.items.size() >= ITEM_LIMIT || item.isEmpty()) return; this.items.add(item); }

    //Odds
    private double odds = 0.01d;
    public double getOdds() { return this.odds; }
    public String getOddsString() { return ODDS_FORMATTER.format(this.odds); }
    public void setOdds(double newOdds) { this.odds = MathUtil.clamp(newOdds,0.01d,99.9d); }

    //Custom Icons
    private boolean useCustomIcons = false;
    public boolean hasCustomIcons() { return this.useCustomIcons; }
    public void setHasCustomIcons(boolean newState) { this.useCustomIcons = newState; }

    private final NonNullList<IconData> customIcons = createDefaultIcons();
    public List<IconData> getCustomIcons() { return ImmutableList.copyOf(this.customIcons); }
    public void setCustomIcon(int index,@Nullable IconData icon) {
        if(index < 0 || index >= ITEM_LIMIT)
            return;
        this.customIcons.set(index, Objects.requireNonNullElse(icon,DEFAULT_ICON));
    }

    public List<IconData> getIconsToDisplay() {
        if(this.useCustomIcons)
            return ImmutableList.copyOf(this.customIcons);
        else
        {
            List<ItemStack> items = splitDisplayItems(this.getDisplayItems());
            List<IconData> result = new ArrayList<>();
            for(int i = 0; i < ITEM_LIMIT; ++i)
            {
                if(i < items.size())
                    result.add(ItemIcon.ofItem(items.get(i).copyWithCount(1)));
                else
                    result.add(IconUtil.ICON_X);
            }
            return ImmutableList.copyOf(result);
        }
    }

    private SlotMachineEntry(List<ItemStack> items, double odds, boolean useCustomIcons, List<IconData> icons) {
        this.items = InventoryUtil.copyList(items);
        this.setOdds(odds);
        this.useCustomIcons = useCustomIcons;
        for(int i = 0; i < this.customIcons.size() && i < icons.size(); ++i)
            this.customIcons.set(i,icons.get(i));
    }

    public boolean isValid() { return !this.items.isEmpty() && this.odds > 0d && this.odds < 100d; }

    public boolean isMoney() {
        if(this.items.isEmpty())
            return false;
        ChainData chain = null;
        for(ItemStack item : this.items)
        {
            if(CoinAPI.getApi().IsCoin(item, false))
            {
                if(chain == null)
                    chain = CoinAPI.getApi().ChainDataOfCoin(item);
                else if(chain != CoinAPI.getApi().ChainDataOfCoin(item)) //Reject if coins are from different chains
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
            if(CoinAPI.getApi().IsCoin(item, false))
            {
                if(chain == null)
                    chain = CoinAPI.getApi().ChainDataOfCoin(item);
                else if(chain != CoinAPI.getApi().ChainDataOfCoin(item)) //Reject if coins are from different chains
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

    public static List<ItemStack> splitDisplayItems(List<ItemStack> displayItems)
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
                result.add(s.split(splitCount));
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
                    trader.removeStoredMoney(reward, null);
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

    public CompoundTag save()
    {
        CompoundTag compound = new CompoundTag();
        ListTag itemList = new ListTag();
        for(ItemStack item : this.items)
            itemList.add(InventoryUtil.saveItemNoLimits(item));
        compound.put("Items",itemList);
        compound.putDouble("Odds",this.odds);

        compound.putBoolean("CustomIcons",this.useCustomIcons);
        compound.put("Icons",TagUtil.writeIconList(this.customIcons));
        return compound;
    }

    public JsonObject toJson()
    {
        JsonObject json = new JsonObject();
        JsonArray itemList = new JsonArray();
        for(ItemStack item : this.items)
            itemList.add(FileUtil.convertItemStack(item));
        json.add("Items", itemList);
        json.addProperty("Odds",this.odds);
        if(this.useCustomIcons)
        {
            JsonArray iconList = new JsonArray();
            for(IconData icon : this.customIcons)
                iconList.add(icon.write());
            json.add("Icons",iconList);
        }
        return json;
    }

    public static SlotMachineEntry create() { return new SlotMachineEntry(new ArrayList<>(), 1,false,new ArrayList<>()); }

    public static SlotMachineEntry load(CompoundTag compound)
    {
        List<ItemStack> items = new ArrayList<>();
        if(compound.contains("Items"))
        {
            ListTag itemList = compound.getList("Items", Tag.TAG_COMPOUND);
            for(int i = 0; i < itemList.size(); ++i)
            {
                ItemStack stack = InventoryUtil.loadItemNoLimits(itemList.getCompound(i));
                if(!stack.isEmpty())
                    items.add(stack);
            }
        }
        double odds = 0.01d;
        if(compound.contains("Odds"))
            odds = compound.getDouble("Odds");
        boolean useCustom = compound.getBoolean("CustomIcons");
        List<IconData> icons = new ArrayList<>();
        if(compound.contains("Icons"))
        {
            NonNullList<IconData> temp  = NonNullList.withSize(SlotMachineEntry.ITEM_LIMIT,DEFAULT_ICON);
            TagUtil.readIconList(temp,compound.getList("Icons",Tag.TAG_COMPOUND),DEFAULT_ICON);
            icons = temp;
        }
        return new SlotMachineEntry(items,odds,useCustom,icons);
    }


    public static SlotMachineEntry parse(JsonObject json) throws JsonSyntaxException, ResourceLocationException
    {
        List<ItemStack> items = new ArrayList<>();
        JsonArray itemList = GsonHelper.getAsJsonArray(json, "Items");
        for(int i = 0; i < itemList.size(); ++i)
        {
            try{
                ItemStack stack = FileUtil.parseItemStack(itemList.get(i).getAsJsonObject());
                if(stack.isEmpty())
                    throw new JsonSyntaxException("Cannot add an empty item to a Slot Machine Entry!");
                items.add(stack);
            } catch (JsonSyntaxException | ResourceLocationException t) { LightmansCurrency.LogError("Error parsing Slot Machine Entry item #" + (i + 1), t); }
        }
        if(items.isEmpty())
            throw new JsonSyntaxException("Slot Machine Entry has no valid items!");
        int weight = GsonHelper.getAsInt(json, "Weight", 1);
        List<IconData> icons = new ArrayList<>();
        if(json.has("Icons"))
        {
            JsonArray iconList = GsonHelper.getAsJsonArray(json,"Icons");
            NonNullList<IconData> temp = NonNullList.withSize(ITEM_LIMIT,IconUtil.ICON_X);
            for(int i = 0; i < iconList.size() && i < temp.size(); ++i)
                temp.set(i,IconData.parse(GsonHelper.convertToJsonObject(iconList.get(i),"Icons[" + i + "]")));
            icons = temp;
        }
        return new SlotMachineEntry(items,weight,!icons.isEmpty(),icons);
    }

}