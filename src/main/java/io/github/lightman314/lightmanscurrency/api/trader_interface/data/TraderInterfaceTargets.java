package io.github.lightman314.lightmanscurrency.api.trader_interface.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TraderInterfaceTargets {

    private final TraderInterfaceBlockEntity parent;

    private final List<Long> traders = new ArrayList<>();
    private final List<TradeReference> trades = new ArrayList<>();

    public TraderInterfaceTargets(TraderInterfaceBlockEntity parent) { this.parent = parent; }

    @Nullable
    public TradeData loadTrade(CompoundTag tag,HolderLookup.Provider lookup) { return this.parent.deserializeTrade(tag,lookup); }

    public CompoundTag save(HolderLookup.Provider lookup)
    {
        CompoundTag tag = new CompoundTag();
        tag.putLongArray("Traders",this.traders);
        ListTag tradeList = new ListTag();
        for(TradeReference trade : this.trades)
            tradeList.add(trade.save(lookup));
        tag.put("Trades",tradeList);
        return tag;
    }

    public void load(CompoundTag tag, HolderLookup.Provider lookup)
    {
        //Check for old NetworkTradeReference data
        if(tag.contains("Traders") && tag.contains("Trades"))
        {
            this.traders.clear();
            this.trades.clear();
            for(long id : tag.getLongArray("Traders"))
                this.traders.add(id);
            ListTag tradeList = tag.getList("Trades",Tag.TAG_COMPOUND);
            for(int i = 0; i < tradeList.size(); ++i)
            {
                TradeReference trade = TradeReference.load(this,tradeList.getCompound(i),lookup);
                if(trade != null)
                    this.trades.add(trade);
                else
                    LightmansCurrency.LogDebug("Failed to load a Trade Reference\n" + tradeList.getCompound(i).getAsString());
            }
        }
    }

    public void loadFromOldData(CompoundTag tag, HolderLookup.Provider lookup)
    {
        //Load from old NetworkTradeReference Data
        this.traders.clear();
        this.trades.clear();
        this.traders.add(tag.getLong("TraderID"));
        int tradeIndex = -1;
        TradeData trade = null;
        if(tag.contains("TradeIndex"))
            tradeIndex = tag.getInt("TradeIndex");
        else if(tag.contains("tradeIndex"))
            tradeIndex = tag.getInt("tradeIndex");
        if(tag.contains("Trade"))
            trade = this.loadTrade(tag.getCompound("Trade"),lookup);
        else if(tag.contains("trade"))
            trade = this.loadTrade(tag.getCompound("trade"),lookup);
        if(tradeIndex >= 0 && trade != null)
            this.trades.add(TradeReference.of(this,tradeIndex,trade));
    }

    @Nullable
    public TraderData getTrader()
    {
        if(!this.traders.isEmpty())
            return TraderAPI.API.GetTrader(this.parent.isClient(),this.traders.getFirst());
        return null;
    }

    public int getTraderCount() { return this.traders.size(); }

    public List<TraderData> getTraders() {
        if(this.parent.getInteractionType().targetsTraders())
        {
            List<TraderData> list = new ArrayList<>();
            for(long traderID : this.traders)
            {
                TraderData trader = TraderAPI.API.GetTrader(this.parent.isClient(),traderID);
                if(trader != null)
                    list.add(trader);
            }
            return list;
        }
        //If in trade target mode, only target a single trader
        else
            return Lists.newArrayList(this.getTrader());
    }

    public TradeData copyTrade(TradeData trade) {
        if(trade == null)
            return null;
        return this.loadTrade(trade.getAsNBT(this.parent.registryAccess()),this.parent.registryAccess());
    }

    public List<TradeReference> getTradeReferences() { return ImmutableList.copyOf(this.trades); }

    public boolean toggleTrader(long traderID)
    {
        //If it's already selected, we can always deselect it
        if(this.traders.contains(traderID))
        {
            this.traders.remove(traderID);
            //Clear selected trades if no trader is selected
            //Since you must always deselect the trader before selecting another one in trade mode
            //This will force the selected trades to always be cleared when selecting a different trader
            if(this.traders.isEmpty())
                this.trades.clear();
            return true;
        }
        //Otherwise confirm we have room for the new selection
        if(this.parent.getInteractionType().trades() && !this.traders.isEmpty())
            return false;
        if(this.traders.size() >= this.parent.getSelectableCount())
            return false;
        this.traders.add(traderID);
        return true;
    }

    public boolean tick(Predicate<TraderData> allowedTraders)
    {
        boolean changed = false;
        int selectableCount = Math.max(1,this.parent.getSelectableCount());
        //Remove invalid traders first so that if too many are selected a valid one will always be the default remainder
        for(int i = 0; i < this.traders.size(); ++i)
        {
            TraderData trader = TraderAPI.API.GetTrader(this.parent.isClient(),this.traders.get(i));
            if(trader == null || !allowedTraders.test(trader))
                this.traders.remove(i--);
        }
        //Limit Traders to 1 and trades to selection count if in trade mode
        if(this.parent.getInteractionType().trades())
        {
            if(this.traders.size() > 1)
            {
                long first = this.traders.getFirst();
                this.traders.clear();
                this.traders.add(first);
                changed = true;
            }
            while(this.trades.size() > selectableCount)
            {
                this.trades.removeLast();
                changed = true;
            }
        }
        //Limit Trades to 0 and traders to selection count if in interaction mode
        else if(this.parent.getInteractionType().targetsTraders())
        {
            if(!this.trades.isEmpty())
            {
                this.trades.clear();
                changed = true;
            }
            while(this.traders.size() > selectableCount)
            {
                this.traders.removeLast();
                changed = true;
            }
        }
        return changed;
    }

    public boolean toggleTrade(int tradeIndex)
    {
        if(this.parent.getInteractionType().targetsTraders())
            return false;
        //Remove Trade if already on the list
        for(int i = 0; i < this.trades.size(); ++i)
        {
            TradeReference trade = this.trades.get(i);
            if(trade.getTradeIndex() == tradeIndex)
            {
                this.trades.remove(i);
                return true;
            }
        }
        if(this.trades.size() >= this.parent.getSelectableCount())
            return false;
        TradeReference trade = TradeReference.of(this,tradeIndex);
        if(trade != null)
        {
            this.trades.add(trade);
            return true;
        }
        return false;
    }

}
