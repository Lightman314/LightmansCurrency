package io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.value.IItemBasedValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.MoneyStorageNode;
import io.github.lightman314.lightmanscurrency.common.traders.item.nodes.ItemStorageNode;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.nodes.SlotMachineNode;
import io.github.lightman314.lightmanscurrency.common.util.TagUtil;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.OldDataHelper;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class SlotMachineEntry {

    public static final int ITEM_LIMIT = 4;

    public static final Codec<SlotMachineEntry> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ItemStack.OPTIONAL_CODEC.listOf(0,ITEM_LIMIT).fieldOf("items").forGetter(e -> e.items),
            Codec.DOUBLE.fieldOf("odds").forGetter(SlotMachineEntry::getOdds),
            Codec.BOOL.fieldOf("use_custom_icons").forGetter(SlotMachineEntry::hasCustomIcons),
            IconData.CODEC.listOf(0,ITEM_LIMIT).fieldOf("custom_icons").forGetter(SlotMachineEntry::getCustomIcons)
    ).apply(builder,SlotMachineEntry::new));

    public static final StreamCodec<RegistryFriendlyByteBuf,SlotMachineEntry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,SlotMachineEntry::getSyncID,
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list(ITEM_LIMIT)),e -> e.items,
            ByteBufCodecs.DOUBLE,SlotMachineEntry::getOdds,
            ByteBufCodecs.BOOL,SlotMachineEntry::hasCustomIcons,
            IconData.STREAM_CODEC.apply(ByteBufCodecs.list(ITEM_LIMIT)),SlotMachineEntry::getCustomIcons,
            SlotMachineEntry::new);

    public static final DecimalFormat ODDS_FORMATTER;

    static {
        ODDS_FORMATTER = new DecimalFormat();
        ODDS_FORMATTER.setMaximumFractionDigits(2);
        ODDS_FORMATTER.setMinimumFractionDigits(2);
    }

    public static IconData DEFAULT_ICON = IconUtil.ICON_X;
    public static NonNullList<IconData> createDefaultIcons() { return NonNullList.withSize(ITEM_LIMIT,DEFAULT_ICON); }

    private SlotMachineNode node;
    private int syncID;
    public int getSyncID() { return this.syncID; }
    public void initialize(SlotMachineNode node)
    {
        if(this.syncID < 0)
            this.syncID = node.getNextSyncID();
    }

    //Items
    public final List<ItemStack> items;
    public void TryAddItem(ItemStack item) {
        if(this.items.size() >= ITEM_LIMIT || item.isEmpty())
            return;
        this.items.add(item);
        this.validateItems();
        this.setChanged();
    }

    //Odds
    private double odds = 0.01d;
    public double getOdds() { return this.odds; }
    public String getOddsString() { return ODDS_FORMATTER.format(this.odds); }
    public void setOdds(double newOdds) {
        newOdds = MathUtil.clamp(newOdds,0.01d,99.9d);
        if(this.odds != newOdds)
        {
            this.odds = newOdds;
            this.setChanged();
        }
    }

    //Custom Icons
    private boolean useCustomIcons = false;
    public boolean hasCustomIcons() { return this.useCustomIcons; }
    public void setHasCustomIcons(boolean newState) {
        if(this.useCustomIcons != newState)
        {
            this.useCustomIcons = newState;
            this.setChanged();
        }
    }

    private final NonNullList<IconData> customIcons = createDefaultIcons();
    public List<IconData> getCustomIcons() { return ImmutableList.copyOf(this.customIcons); }
    public void setCustomIcon(int index,@Nullable IconData icon) {
        if(index < 0 || index >= ITEM_LIMIT)
            return;
        this.customIcons.set(index,Objects.requireNonNullElse(icon,DEFAULT_ICON));
        this.setChanged();
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

    public void setChanged()
    {
        if(this.node != null)
            this.node.setEntryChanged(this);
    }

    private SlotMachineEntry(List<ItemStack> items, double odds, boolean useCustomIcons, List<IconData> icons) { this(-1,items,odds,useCustomIcons,icons); }
    private SlotMachineEntry(int syncID,List<ItemStack> items, double odds, boolean useCustomIcons, List<IconData> icons) {
        this.syncID = syncID;
        this.items = ItemHandlerUtil.copyList(items);
        while(this.items.size() >= ITEM_LIMIT)
            this.items.removeLast();
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
        return ItemHandlerUtil.copyList(this.items);
    }
    
    public static List<ItemStack> splitDisplayItems(List<ItemStack> displayItems)
    {
        if(displayItems.size() >= ITEM_LIMIT)
            return displayItems;
        int totalCount = 0;
        for(ItemStack s : displayItems)
            totalCount+= s.getCount();
        List<ItemStack> result = ItemHandlerUtil.copyList(displayItems);
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
    public boolean GiveToCustomer(TraderData trader, TradeContext context)
    {
        if(this.hasStock(trader))
        {
            if(this.isMoney())
            {
                MoneyValue reward = this.getMoneyValue();
                if(!context.givePayment(reward))
                    return false;
                if(!trader.hasInfiniteStock())
                {
                    MoneyStorageNode node = trader.getNode(MoneyStorageNode.TYPE);
                    if(node != null)
                        node.removeStoredMoney(reward,null);
                }
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
                if(!trader.hasInfiniteStock())
                {
                    ItemStorageNode node = trader.getNode(ItemStorageNode.TYPE);
                    if(node != null)
                    {
                        for(ItemStack i : this.items)
                            node.getStorage().removeItemLimited(i.copy());
                    }
                }
            }
            return true;
        }
        else
            return false;
    }

    public int getStock(TraderData trader)
    {
        if(!this.isValid())
            return 0;
        if(trader.hasInfiniteStock())
            return Integer.MAX_VALUE;
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
            ItemStorageNode node = trader.getNode(ItemStorageNode.TYPE);
            if(node == null)
                return 0;
            int minStock = Integer.MAX_VALUE;
            for(ItemStack item : ItemHandlerUtil.combineStacks(this.items))
            {
                int count = node.getStorage().getItemCount(item);
                int stock = count / item.getCount();
                if(stock < minStock)
                    minStock = stock;
            }
            return minStock;
        }
    }

    public boolean hasStock(TraderData trader) { return this.getStock(trader) > 0; }

    public boolean isItemRelevant(ItemStack item)
    {
        if(this.isMoney())
            return false;
        return this.items.stream().anyMatch(i -> ItemStack.isSameItemSameComponents(i, item));
    }

    public static SlotMachineEntry create() { return new SlotMachineEntry(new ArrayList<>(), 1,false,new ArrayList<>()); }
    public static SlotMachineEntry create(SlotMachineNode node) {
        SlotMachineEntry entry = create();
        entry.initialize(node);
        return entry;
    }

    @Deprecated
    public static SlotMachineEntry loadOldData(CompoundTag compound, HolderLookup.Provider lookup)
    {
        List<ItemStack> items = new ArrayList<>();
        if(compound.contains("Items"))
        {
            ListTag itemList = compound.getList("Items", Tag.TAG_COMPOUND);
            for(int i = 0; i < itemList.size(); ++i)
            {
                ItemStack stack = OldDataHelper.loadItem(itemList.getCompound(i),lookup);
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
            TagUtil.readIconList(temp,compound.getList("Icons",Tag.TAG_COMPOUND),lookup,DEFAULT_ICON);
            icons = temp;
        }
        return new SlotMachineEntry(items,odds,useCustom,icons);
    }

    
    public static SlotMachineEntry parse(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException
    {
        List<ItemStack> items = new ArrayList<>();
        JsonArray itemList = GsonHelper.getAsJsonArray(json, "Items");
        for(int i = 0; i < itemList.size(); ++i)
        {
            try{
                ItemStack stack = ItemStack.CODEC.decode(context.ops(),itemList.get(i)).getOrThrow(JsonSyntaxException::new).getFirst();
                items.add(stack);
            } catch (JsonSyntaxException | ResourceLocationException t) { LightmansCurrency.LogError("Error parsing Slot Machine Entry item #" + (i + 1), t); }
        }
        if(items.isEmpty())
            throw new JsonSyntaxException("Slot Machine Entry has no valid items!");
        if(items.size() > ITEM_LIMIT)
            throw new JsonSyntaxException("Slot Machine Entry cannot have more than " + ITEM_LIMIT + " items! (Has " + items.size() + ")");
        int weight = GsonHelper.getAsInt(json, "Weight", 1);
        List<IconData> icons = new ArrayList<>();
        if(json.has("Icons"))
        {
            JsonArray iconList = GsonHelper.getAsJsonArray(json,"Icons");
            NonNullList<IconData> temp = NonNullList.withSize(ITEM_LIMIT,IconUtil.ICON_X);
            for(int i = 0; i < iconList.size() && i < temp.size(); ++i)
                temp.set(i,IconData.CODEC.decode(context.ops(),iconList.get(i)).getOrThrow(JsonSyntaxException::new).getFirst());
            icons = temp;
        }
        return new SlotMachineEntry(items,weight,!icons.isEmpty(),icons);
    }

}