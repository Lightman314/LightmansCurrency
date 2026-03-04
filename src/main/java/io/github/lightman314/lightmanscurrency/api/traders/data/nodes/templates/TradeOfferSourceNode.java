package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketType;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin.TradeRulesTab;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRule;

import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.trade.RuleSupportingTradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IUpgradeHandler;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeStackHandler;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin.MultiPriceTab;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.util.NumberUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.*;

public abstract class TradeOfferSourceNode<T extends TradeData> extends PlayerSyncedTraderNode implements IUpgradeHandler {

    public final void setTradeChanged(TradeData trade)
    {
        try {
            int index = this.getEditableList().indexOf((T)trade);
            if(index >= 0)
                this.setTradeChanged(index);
        } catch (ClassCastException ignored) {}
    }
    public void setTradeChanged(int tradeIndex)
    {
        List<T> list = this.getEditableList();
        if(tradeIndex >= 0 && tradeIndex < list.size())
        {
            this.setTradeChanged(tradeIndex,builder ->
                builder.remove("delete")
                        .setCustom("edit",list.get(tradeIndex),this.getPacketType()));
        }
        else
        {
            this.setTradeChanged(tradeIndex,builder -> builder
                    .remove("edit")
                    .setFlag("delete"));
        }
    }

    private void setTradeRulesChanged(int tradeIndex)
    {
        if(!this.supportsTradeRules())
            return;
        List<T> list = this.getEditableList();
        if(tradeIndex >= 0 && tradeIndex < this.getTradeCount())
        {
            this.setTradeChanged(tradeIndex,(player,builder) -> {
                if(this.getTrade(tradeIndex) instanceof RuleSupportingTradeData rt)
                    builder.setMap("rules",TradeRule.encodeRules(this::builder,rt,player));
            });
        }
    }

    public void setTradeRuleChanged(TradeData trade,TradeRuleType<?> type)
    {
        if(!this.supportsTradeRules())
            return;
        try {
            int index = this.getEditableList().indexOf((T)trade);
            if(index >= 0)
                this.setTradeRuleChanged(index,type);
        } catch (ClassCastException ignored) {}
    }
    public void setTradeRuleChanged(int tradeIndex,TradeRuleType<?> type)
    {
        if(!this.supportsTradeRules())
            return;
        this.setTradeChanged(tradeIndex,(player,builder) ->
            builder.modifyMap("rules",map -> {
                TradeData trade = this.getTrade(tradeIndex);
                if(trade instanceof RuleSupportingTradeData rt)
                    TradeRule.encodeRule(map,this::builder,rt,type,player);
            }));
    }

    protected final void setTradeChanged(int tradeIndex,Consumer<LazyPacketData.Builder> consumer)
    {
        this.setChanged(builder -> builder.modifyMap(tradeKey(tradeIndex),consumer));
    }
    protected final void setTradeChanged(int tradeIndex,BiConsumer<Player,LazyPacketData.Builder> consumer)
    {
        this.setChanged((player,builder) -> builder.modifyMap(tradeKey(tradeIndex),entry -> consumer.accept(player,entry)));
    }

    private static String tradeKey(int index) { return "trade_" + index; }
    private static int parseTradeKey(String key)
    {
        if(key.startsWith("trade_"))
            return NumberUtil.GetIntegerValue(key.substring("trade_".length()),-1);
        return -1;
    }

    @Override
    public boolean hasNoConflicts(TraderData trader, Map<TraderNodeType<?>, TraderNode> partialMap) {
        for(TraderNode node : partialMap.values())
        {
            if(node instanceof TradeOfferSourceNode<?>)
            {
                LightmansCurrency.LogError("Multiple Trade Offer Source Nodes present on " + trader.getType() + "\n" + this.getType() + " and " + node.getType());
                return false;
            }
        }
        return this.noConflicts(trader,partialMap);
    }

    protected boolean noConflicts(TraderData trader, Map<TraderNodeType<?>, TraderNode> partialMap) { return true;}

    /**
     * @return The current number of trade offers this node has
     */
    public int getTradeCount() { return this.getEditableList().size(); }

    /**
     * @param index The index of the trade you want to obtain
     * @return The trade at the given index, or <code>null</code> if the index is out of bounds
     */
    @Nullable
    public final T getTrade(int index)
    {
        List<T> list = this.getEditableList();
        if(index >= 0 && index < list.size())
            return list.get(index);
        return null;
    }

    public int getTradeStock(int index)
    {
        T trade = this.getTrade(index);
        if(trade != null && this.trader != null)
            return trade.getStock(TradeContext.createStorageMode(this.trader));
        return 0;
    }

    public int indexOfTrade(T trade) { return this.getEditableList().indexOf(trade); }

    /**
     * @return A list of all trades
     */
    public List<T> getAllTrades() { return ImmutableList.copyOf(this.getEditableList()); }

    /**
     * Whether the trade count can be increased or decreased by any means<br>
     * When <code>true</code>, Creative Mode settings will include the +/- buttons to forcibly add/remove trades
     */
    public boolean canChangeQuantity() { return true; }

    /**
     * @return Whether Trade Offer Upgrades can add additional trade offers<br>
     * Defaults to <code>true</code> if {@link #canChangeQuantity()} is <code>true</code> & {@link #canEasilyChangeQuantity()} is <code>false</code>
     */
    public boolean canUpgradeChangeQuantity() { return this.canChangeQuantity() && !this.canEasilyChangeQuantity(); }

    /**
     * Whether any player with edit trade permissions can add or remove trades from this node
     * @return When <code>true</code>, the +/- buttons will appear in the default trade edit tab
     */
    public boolean canEasilyChangeQuantity() { return false; }

    /**
     * @return The maximum number of trades that can be added by a non-admin when {@link #canEasilyChangeQuantity()} is <code>true</code><br>
     * Ignored if {@link #canEasilyChangeQuantity()} is <code>false</code>
     */
    public int getMaxTradeCount() { return 1; }

    /**
     * Called after a Trade Offer Upgrade is added or removed from the upgrades
     */
    public void refactorTrades() {}

    /**
     * Called after a player hits the + button in either the trader storage tab, or the creative settings tab
     * @param player The player attempting the interaction
     * @return Whether a trade was successfully added or not
     */
    public abstract boolean addTrade(Player player);

    /**
     * Called after a player hits the - button in either the trader storage tab, or the creative settings tab
     * @param player The player attempting the interaction
     * @return Whether a trade was successfully removed or not
     */
    public abstract boolean removeTrade(Player player);

    @Override
    public void initializeAllyPermissions(BiConsumer<String, Integer> defaultConsumer) {
        defaultConsumer.accept(Permissions.EDIT_TRADES,1);
    }

    public abstract boolean supportsTradeRules();

    public boolean supportsMultiPriceEditing() { return true; }

    @Override
    public boolean allowUpgrade(UpgradeType upgrade) { return upgrade == Upgrades.TRADE_OFFERS && this.canUpgradeChangeQuantity(); }

    @Override
    public void afterUpgradesChanged(UpgradeStackHandler container) {
        this.refactorTrades();
    }

    @Override
    public void applyStorageTabs(ITraderStorageMenu menu) {
        //Basic Trade Edit Tab
        menu.addTab(new BasicTradeEditTab(menu));
        //Multi Price Tab
        if(this.supportsMultiPriceEditing())
            menu.addTab(new MultiPriceTab(menu));
        //Trade Rule tab for trades if the first trade supports trade rules
        if(this.supportsTradeRules())
            menu.addTab(new TradeRulesTab.Trade(menu));
    }

    public Predicate<TradeData> getTradeStorageFilter(ITraderStorageMenu menu) { return Predicates.alwaysTrue(); }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,Player player) {
        List<T> trades = this.getEditableList();
        for(int i = 0; i < trades.size(); ++i)
        {
            LazyPacketData.Builder entry = this.builder();
            T trade = trades.get(i);
            entry.setCustom("edit",trades.get(i),this.getPacketType());
            if(this.supportsTradeRules() && trade instanceof RuleSupportingTradeData rt)
                entry.setMap("rules",TradeRule.encodeRules(this::builder,rt,player));
            builder.setMap(tradeKey(i),entry);
        }
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        Set<Integer> pendingRemovals = new HashSet<>();
        Map<Integer,LazyPacketData> pendingEdits = new HashMap<>();
        List<T> list = this.getEditableList();
        for(String key : data.keySet())
        {
            int index = parseTradeKey(key);
            if(index >= 0)
            {
                LazyPacketData entry = data.getMap(key);
                if(entry.contains("delete"))
                    pendingRemovals.add(index);
                else
                {
                    Pair<Boolean,Integer> result = this.tryParseEditData(entry,list,index);
                    if(result.getFirst() && result.getSecond() >= 0)
                    {
                        for(int i = result.getSecond(); pendingEdits.containsKey(i); ++i)
                        {
                            if(this.tryParseEditData(pendingEdits.get(i),list,i).getFirst())
                                pendingEdits.remove(i);
                        }
                    }
                    if(!result.getFirst() && result.getSecond() >= 0)
                        pendingEdits.put(index,entry);
                }
            }
        }
        List<Integer> sortedRemovals = new ArrayList<>(pendingRemovals);
        sortedRemovals.sort((a,b) -> Integer.compare(b,a));
        for(int i : sortedRemovals)
            list.remove(i);
    }

    //Return rules
    //Boolean == true
    //Trade was added. If integer returned is a positive value, please loop through all ints larger than it for previous failures
    //Boolean == false
    //Trade was NOT added. If integer returned is a positve value, please store the key for later if another addition succeeds to see if this trade can be added properly.
    private Pair<Boolean,Integer> tryParseEditData(LazyPacketData data, List<T> trades, int index)
    {
        if(index >= 0 && index < trades.size())
        {
            if(data.contains("edit"))
            {
                //Replace existing trade
                T trade = data.getCustom("edit",this.getPacketType());
                T oldTrade = trades.get(index);
                trades.set(index,trade);
                //Copy trade rules to the new trade
                if(trade instanceof RuleSupportingTradeData rt1 && oldTrade instanceof RuleSupportingTradeData old)
                    rt1.copyRules(old);
                TradeData.afterLoad(trade,this);
            }
            if(this.supportsTradeRules() && data.contains("rules"))
            {
                T trade = trades.get(index);
                if(trade instanceof RuleSupportingTradeData rt)
                    TradeRule.decodeRules(data.getMap("rules"),rt);
            }
            return Pair.of(true,-1);
        }
        else if(index == trades.size())
        {
            if(data.contains("edit"))
            {
                //Add new trade
                T trade = data.getCustom("edit",this.getPacketType());
                trades.add(trade);
                TradeData.afterLoad(trade,this);
            }
            if(this.supportsTradeRules() && data.contains("rules"))
            {
                T trade = trades.get(index);
                if(trade instanceof RuleSupportingTradeData rt)
                    TradeRule.decodeRules(data.getMap("rules"),rt);
            }
            return Pair.of(true,trades.size());
        }
        return Pair.of(false,index);
    }

    @Override
    public void handleSettingsChange(Player player, LazyPacketData message) {
        if(message.contains("AddTrade"))
            this.addTrade(player);
        if(message.contains("RemoveTrade"))
            this.removeTrade(player);
    }

    protected final void forceTradeCount(int newCount, Function<Boolean,T> factory) { this.forceTradeCount(newCount,() -> factory.apply(true)); }
    protected void forceTradeCount(int newCount, Supplier<T> factory)
    {
        newCount = Math.clamp(newCount,this.minTradeCount(),TraderData.GLOBAL_TRADE_LIMIT);
        List<T> list = this.getEditableList();
        while(list.size() > newCount)
        {
            list.removeLast();
            this.setTradeChanged(list.size());
        }
        while(list.size() < newCount)
        {
            T trade = factory.get();
            list.add(trade);
            TradeData.afterLoad(trade,this);
            this.onTradeAdded(trade,list.size() - 1);
            this.setTradeChanged(list.size() - 1);
            this.setTradeRulesChanged(list.size() - 1);
        }
    }

    protected int minTradeCount() { return 1; }

    protected void onTradeAdded(T trade,int index) {}
    protected abstract List<T> getEditableList();
    protected abstract Supplier<LazyPacketType<T>> getPacketType();

    @Nullable
    protected final CompoundTag writePersistentRuleTag(DataContext<Tag> context) {
        CompoundTag tag = new CompoundTag();
        ListTag tradePersistentData = new ListTag();
        boolean tradesAreRelevant = false;
        for (T trade : this.getEditableList()) {
            if(trade instanceof RuleSupportingTradeData ruleTrade)
            {
                ListTag entry = TradeRule.savePersistentData(ruleTrade.getRuleMap(), context);
                if(!entry.isEmpty())
                    tradesAreRelevant = true;
                tradePersistentData.add(entry);
            }
        }
        if(tradesAreRelevant)
        {
            tag.put("PersistentTradeData", tradePersistentData);
            return tag;
        }
        return null;
    }

    protected void readPersistentRuleTag(CompoundTag tag, DataContext<Tag> context) {
        if(tag.contains("PersistentTradeData"))
        {
            ListTag oldList = tag.getList("PersistentTradeData", Tag.TAG_COMPOUND);
            ListTag newList = tag.getList("PersistentTradeData",Tag.TAG_LIST);
            ListTag list;
            //See if we're loading the old way (list of compound tags), or the new way (list of lists)
            BiFunction<ListTag,Integer,ListTag> getter = ListTag::getList;
            if(newList.isEmpty() && !oldList.isEmpty())
            {
                getter = (l,i) -> {
                    CompoundTag e = l.getCompound(i);
                    return e.getList("RuleData",Tag.TAG_COMPOUND);
                };
                list = oldList;
            }
            else
                list = newList;
            List<T> trades = this.getEditableList();
            for(int i = 0; i < list.size() && i < trades.size(); ++i)
            {
                T trade = trades.get(i);
                if(trade instanceof RuleSupportingTradeData ruleTrade)
                {
                    ListTag entry = getter.apply(list,i);
                    TradeRule.loadPersistentData(entry, ruleTrade.getRuleMap(),context);
                }
            }
        }
    }

}
