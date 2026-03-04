package io.github.lightman314.lightmanscurrency.common.traders.item.nodes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketType;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRule;

import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.UpgradesNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IPersistentNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.ITerminalDisplay;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.TradeOfferSourceNode;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.traders.item.tabs.ItemTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.IItemExtractionFilter;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.IItemInsertionFilter;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.IItemTradeMode;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.ItemTradeType;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.restrictions.ITradeRestrictionSource;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.TradeOfferUpgrade;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ItemTradeNode extends TradeOfferSourceNode<ItemTradeData> implements IPersistentNode, IItemInsertionFilter, IItemExtractionFilter, ITerminalDisplay {

    private static final MapCodec<ItemTradeNode> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.fieldOf("baseCount").forGetter(n -> n.baseTradeCount),
            ItemTradeData.CODEC.listOf(1,Integer.MAX_VALUE).fieldOf("trades").forGetter(n -> n.trades)
    ).apply(builder,ItemTradeNode::new));

    public static final TraderNodeType<ItemTradeNode> TYPE = TraderNodeType.advanced(ItemTradeNode::factory,MAP_CODEC);

    private ItemTradeType<?> tradeType = ItemTradeData.DEFAULT_TYPE;
    private ITradeRestrictionSource restrictionSource = i -> ItemTradeRestriction.NONE;

    private int baseTradeCount;
    protected final List<ItemTradeData> trades;
    private ItemTradeNode(int count) {
        this.baseTradeCount = Math.max(0,count);
        this.trades = new ArrayList<>();
    }
    private ItemTradeNode(int baseCount,List<ItemTradeData> trades)
    {
        this.baseTradeCount = baseCount;
        this.trades = new ArrayList<>(trades);
        TradeData.afterLoad(this.trades,this);
    }

    @Override
    public void onAttach() {
        super.onAttach();
        for(TraderNode node : this.trader.getNodeIterable())
        {
            if(node instanceof IItemTradeMode c)
                this.tradeType = c.expectedTradeType();
            if(node instanceof ITradeRestrictionSource s)
                this.restrictionSource = s;
        }
        if(this.baseTradeCount <= 0)
            this.baseTradeCount = 0;
        if(this.trades.isEmpty())
            this.trades.addAll(ItemTradeData.listOfSize(this.baseTradeCount,this.tradeType));
        this.trades.replaceAll(trade -> this.tradeType.validateType(trade));
        TradeData.afterLoad(this.trades,this);
    }

    @Override
    public void refactorTrades() {
        int expectedCount = this.baseTradeCount;
        UpgradesNode node = this.trader.getNode(UpgradesNode.TYPE);
        if(node != null)
            expectedCount += TradeOfferUpgrade.getBonusTrades(node.getContainer());
        this.forceTradeCount(expectedCount);
    }

    public void forceTradeCount(int newCount) { this.forceTradeCount(newCount,this.tradeType::create); }

    @Override
    protected void forceTradeCount(int newCount, Supplier<ItemTradeData> factory) {
        super.forceTradeCount(newCount, factory);
        this.validateRestrictions();
    }

    @Override
    protected void onTradeAdded(ItemTradeData trade, int index) {
        this.validateRestriction(index);
    }

    protected void validateRestrictions()
    {
        for(int i = 0; i < this.trades.size(); ++i)
            this.validateRestriction(i);
    }
    protected void validateRestriction(int index)
    {
        ItemTradeData trade = this.getTrade(index);
        if(trade != null)
            trade.setRestriction(this.restrictionSource.getTradeRestriction(index));
    }

    @Override
    protected List<ItemTradeData> getEditableList() { return this.trades; }

    @Override
    protected Supplier<LazyPacketType<ItemTradeData>> getPacketType() { return ModLazyPackets.ITEM_TRADE; }

    @Override
    public boolean addTrade(Player player) {
        if(LCAdminMode.isAdminPlayer(player) && this.baseTradeCount < TraderData.GLOBAL_TRADE_LIMIT)
        {
            //Increment the trade count
            this.baseTradeCount++;
            this.setChangedNoPacket();
            this.refactorTrades();
            return true;
        }
        return false;
    }

    @Override
    public boolean removeTrade(Player player) {
        if(LCAdminMode.isAdminPlayer(player) && this.baseTradeCount > 1)
        {
            //Decrement the base trade count
            this.baseTradeCount--;
            this.setChangedNoPacket();
            this.refactorTrades();
            return true;
        }
        return false;
    }

    @Override
    public boolean supportsTradeRules() { return true; }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        if(tag.contains(TradeData.DEFAULT_KEY))
        {
            this.trades.clear();
            this.trades.addAll(ItemTradeData.loadAllData(tag,this.tradeType,lookup));
            TradeData.afterLoad(this.trades,this);
            this.validateRestrictions();
        }
        if(tag.contains("BaseTradeCount"))
            this.baseTradeCount = tag.getInt("BaseTradeCount");
        if(this.baseTradeCount <= 0) //Reset base trade count to current trade count if the current value is invalid
            this.baseTradeCount = this.trades.size();
    }

    @Override
    public void writePersistentData(JsonObject json, DataContext<JsonElement> context, String id, String ownerName) {
        JsonArray tradeList = new JsonArray();
        for(ItemTradeData trade : this.trades)
        {
            if(trade.isValid())
            {
                JsonObject tradeData = new JsonObject();
                JsonArray ignoreNBTData = new JsonArray();
                tradeData.addProperty("TradeType", trade.getTradeDirection().name());
                if(trade.getSellItem(0).isEmpty())
                {
                    tradeData.add("SellItem", ItemStack.CODEC.encodeStart(context.ops(),trade.getActualItem(1)).getOrThrow());
                    if(trade.hasCustomName(1))
                        tradeData.addProperty("DisplayName", trade.getCustomName(1));
                    //Manually assign to the 0th index, as this is what the loaded trade will acknowledge
                    if(!trade.getEnforceNBT(1))
                        ignoreNBTData.add(0);
                }
                else
                {
                    tradeData.add("SellItem",ItemStack.CODEC.encodeStart(context.ops(),trade.getSellItem(0)).getOrThrow());
                    if(trade.hasCustomName(0))
                        tradeData.addProperty("DisplayName",trade.getCustomName(0));
                    if(!trade.getEnforceNBT(0))
                        ignoreNBTData.add(0);
                    if(!trade.getSellItem(1).isEmpty())
                    {
                        tradeData.add("SellItem2",ItemStack.CODEC.encodeStart(context.ops(),trade.getSellItem(1)).getOrThrow());
                        if(trade.hasCustomName(1))
                            tradeData.addProperty("DisplayName2", trade.getCustomName(1));
                        if(!trade.getEnforceNBT(1))
                            ignoreNBTData.add(1);
                    }
                }

                if(trade.isSale() || trade.isPurchase())
                    tradeData.add("Price", trade.getCost().toJson());

                if(trade.isBarter())
                {
                    if(trade.getBarterItem(0).isEmpty())
                    {
                        tradeData.add("BarterItem", ItemStack.CODEC.encodeStart(context.ops(),trade.getBarterItem(1)).getOrThrow());
                        //Manually assign to the 2nd index, as this is what the loaded trade will acknowledge
                        if(!trade.getEnforceNBT(3))
                            ignoreNBTData.add(2);
                    }
                    else
                    {
                        tradeData.add("BarterItem", ItemStack.CODEC.encodeStart(context.ops(),trade.getBarterItem(0)).getOrThrow());
                        if(!trade.getEnforceNBT(2))
                            ignoreNBTData.add(2);
                        if(!trade.getBarterItem(1).isEmpty())
                        {
                            tradeData.add("BarterItem2",ItemStack.CODEC.encodeStart(context.ops(),trade.getBarterItem(1)).getOrThrow());
                            if(!trade.getEnforceNBT(3))
                                ignoreNBTData.add(3);
                        }
                    }
                }

                //Save ignored NBT slots (if relevant)
                if(!ignoreNBTData.isEmpty())
                    tradeData.add("IgnoreNBT", ignoreNBTData);

                JsonObject ruleData = TradeRule.savePersistentRules(trade.getRuleMap(),context);
                if(!ruleData.isEmpty())
                    tradeData.add("Rules", ruleData);

                trade.saveAdditionalJsonData(tradeData,context);

                tradeList.add(tradeData);
            }
        }
        json.add("Trades", tradeList);

    }

    @Override
    public void loadPersistentData(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException {
        JsonArray trades = GsonHelper.getAsJsonArray(json, "Trades");
        this.trades.clear();
        for(int i = 0; i < trades.size() && this.trades.size() < TraderData.GLOBAL_TRADE_LIMIT; ++i)
        {
            try {
                JsonObject tradeData = trades.get(i).getAsJsonObject();

                ItemTradeData newTrade = this.tradeType.create(false);
                //Sell Item
                newTrade.setItem(ItemStack.CODEC.decode(context.ops(),GsonHelper.getAsJsonObject(tradeData,"SellItem")).getOrThrow(JsonSyntaxException::new).getFirst(), 0);
                if(tradeData.has("SellItem2"))
                    newTrade.setItem(ItemStack.CODEC.decode(context.ops(),GsonHelper.getAsJsonObject(tradeData, "SellItem2")).getOrThrow(JsonSyntaxException::new).getFirst(), 1);
                //Trade Type
                if(tradeData.has("TradeType"))
                    newTrade.setTradeType(ItemTradeData.loadTradeType(GsonHelper.getAsString(tradeData, "TradeType")));
                //Trade Price
                if(tradeData.has("Price"))
                {
                    if(newTrade.isBarter())
                        LightmansCurrency.LogWarning("Price is being defined for a barter trade. Price will be ignored.");
                    else
                        newTrade.setCost(MoneyValue.loadFromJson(tradeData.get("Price")));
                }
                else if(!newTrade.isBarter())
                {
                    LightmansCurrency.LogWarning("Price is not defined on a non-barter trade. Price will be assumed to be free.");
                    newTrade.setCost(MoneyValue.free());
                }
                if(tradeData.has("BarterItem"))
                {
                    if(newTrade.isBarter())
                    {
                        newTrade.setItem(ItemStack.CODEC.decode(context.ops(),GsonHelper.getAsJsonObject(tradeData,"BarterItem")).getOrThrow(JsonSyntaxException::new).getFirst(), 2);
                        if(tradeData.has("BarterItem2"))
                            newTrade.setItem(ItemStack.CODEC.decode(context.ops(),GsonHelper.getAsJsonObject(tradeData,"BarterItem2")).getOrThrow(JsonSyntaxException::new).getFirst(), 3);
                    }
                    else
                    {
                        LightmansCurrency.LogWarning("BarterItem is being defined for a non-barter trade. Barter item will be ignored.");
                    }
                }
                if(tradeData.has("DisplayName"))
                    newTrade.setCustomName(0, GsonHelper.getAsString(tradeData, "DisplayName"));
                if(tradeData.has("DisplayName2"))
                    newTrade.setCustomName(1, GsonHelper.getAsString(tradeData, "DisplayName2"));
                if(tradeData.has("Rules"))
                    newTrade.setRules(TradeRule.loadPersistentRules(tradeData,"Rules",context));
                if(tradeData.has("IgnoreNBT"))
                {
                    JsonArray ignoreNBTData = GsonHelper.getAsJsonArray(tradeData,"IgnoreNBT");
                    for(int j = 0; j < ignoreNBTData.size(); ++j)
                    {
                        int slot = ignoreNBTData.get(j).getAsInt();
                        newTrade.setEnforceNBT(slot, false);
                    }
                }
                newTrade.loadAdditionalJsonData(tradeData,context);
                this.trades.add(newTrade);

            } catch(Exception e) { LightmansCurrency.LogError("Error parsing item trade at index " + i, e); }
        }

        if(this.trades.isEmpty())
            throw new JsonSyntaxException("Trader has no valid trades!");
    }

    @Nullable
    @Override
    public CompoundTag writePersistentTag(DataContext<Tag> context) { return this.writePersistentRuleTag(context); }

    @Override
    public void readPersistentTag(CompoundTag tag, DataContext<Tag> context) { this.readPersistentRuleTag(tag,context); }

    @Override
    public boolean isItemRelevant(ItemStack stack) {
        for(ItemTradeData trade : this.trades)
        {
            if(trade.allowItemInStorage(stack))
                return true;
        }
        return false;
    }

    @Override
    public boolean allowExtraction(ItemStack stack) {
        for(ItemTradeData trade : this.trades)
        {
            if(trade.isValid() && (trade.isSale() || trade.isBarter()))
            {
                for(int i = 0; i < 2; ++i)
                {
                    if(trade.getItemRequirement(i).test(stack))
                        return false;
                }
            }
        }
        return true;
    }

    @Override
    public void addTerminalInfo(List<Component> tooltip, @Nullable Player player) {
        if(this.trader instanceof ItemTraderData t)
        {
            int tradeCount = 0;
            int outOfStock = 0;
            for(ItemTradeData trade : this.trades)
            {
                if(trade.isValid())
                {
                    tradeCount++;
                    if(!this.trader.hasInfiniteStock() && !trade.hasStock(t))
                        outOfStock++;
                }
            }
            tooltip.add(LCText.TOOLTIP_NETWORK_TERMINAL_TRADE_COUNT.get(tradeCount));
            if(outOfStock > 0)
                tooltip.add(LCText.TOOLTIP_NETWORK_TERMINAL_OUT_OF_STOCK_COUNT.get(outOfStock));
        }
    }

    @Override
    public void applyStorageTabs(ITraderStorageMenu menu) {
        menu.addTab(new ItemTradeEditTab(menu));
    }

    private static ItemTradeNode factory(@Nullable Object argument)
    {
        if(argument instanceof Number num)
            return new ItemTradeNode(num.intValue());
        return new ItemTradeNode(0);
    }

}
