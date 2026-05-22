package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxCollector;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxableContext;
import io.github.lightman314.lightmanscurrency.api.taxes.TaxAPI;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.SyncedTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.ITradeListener;

import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TaxesNode extends SyncedTraderNode implements ITradeListener {

    private static final MapCodec<TaxesNode> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.intRange(0,99).fieldOf("maxTaxRate").forGetter(n -> n.acceptableTaxRate),
            Codec.LONG.listOf().fieldOf("ignoredTaxCollectors").forGetter(n -> n.ignoredTaxCollectors),
            Codec.BOOL.fieldOf("ignoreAllTaxes").forGetter(n -> n.ignoreAllTaxes)
    ).apply(builder,TaxesNode::new));

    public static final TraderNodeType<TaxesNode> TYPE = TraderNodeType.simple(TaxesNode::new,MAP_CODEC);

    private int acceptableTaxRate = 99;
    public int getAcceptableTaxRate() { return this.acceptableTaxRate; }
    public boolean setAcceptableTaxRate(@Nullable PlayerReference admin,int newRate)
    {
        newRate = Math.clamp(newRate,0,99);
        if(newRate != this.acceptableTaxRate)
        {
            this.acceptableTaxRate = newRate;
            if(admin != null)
                this.pushLocalNotification(ChangeSettingNotification.advanced(admin, LCText.DATA_ENTRY_TRADER_TAXES_RATE.get(), newRate, this.acceptableTaxRate));
            this.setChanged(builder -> builder.setInt("acceptableTaxRate",this.acceptableTaxRate));
            return true;
        }
        return false;
    }

    private final List<Long> ignoredTaxCollectors;
    public List<Long> getIgnoredTaxCollectors() { return this.ignoredTaxCollectors; }
    public void setIgnoredTaxCollectors(List<Long> newList) {
        this.ignoredTaxCollectors.clear();
        this.ignoredTaxCollectors.addAll(newList);
        this.setIgnoredTaxCollectorsChanged();
    }
    private void setIgnoredTaxCollectorsChanged()
    {
        this.setChanged(builder -> builder.setList("ignoredList",this.ignoredTaxCollectors,LazyPacketData.LONG_FACTORY));
    }

    private boolean ignoreAllTaxes = false;
    public boolean IgnoresAllTaxes() { return this.ignoreAllTaxes; }
    public void setIgnoreAllTaxes(@Nullable PlayerReference admin, boolean newValue)
    {
        if(newValue != this.ignoreAllTaxes)
        {
            this.ignoreAllTaxes = newValue;
            if(admin != null)
                this.pushLocalNotification(ChangeSettingNotification.simple(admin, LCText.DATA_ENTRY_TRADER_TAXES_IGNORE_ALL.get(), this.ignoreAllTaxes));
            this.setChanged(builder -> builder.setBoolean("ignoreAllTaxes",this.ignoreAllTaxes));
        }
    }

    private TaxesNode() { this.ignoredTaxCollectors = new ArrayList<>(); }
    private TaxesNode(int acceptableTaxRate,List<Long> ignoredTaxCollectors,boolean ignoreAllTaxes)
    {
        this.acceptableTaxRate = acceptableTaxRate;
        this.ignoredTaxCollectors = new ArrayList<>(ignoredTaxCollectors);
        this.ignoreAllTaxes = ignoreAllTaxes;
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,ISyncingContext context) {
        builder.setInt("acceptableTaxRate",this.acceptableTaxRate)
                .setBoolean("ignoreAllTaxes",this.ignoreAllTaxes)
                .setList("ignoredList",this.ignoredTaxCollectors,LazyPacketData.LONG_FACTORY);
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        if(data.contains("acceptableTaxRate"))
            this.acceptableTaxRate = data.getInt("acceptableTaxRate");
        if(data.contains("ignoreAllTaxes"))
            this.ignoreAllTaxes = data.getBoolean("ignoreAllTaxes");
        if(data.contains("ignoredList"))
        {
            this.ignoredTaxCollectors.clear();
            this.ignoredTaxCollectors.addAll(data.getList("ignoredList",Long.class));
        }
    }

    public boolean IsEntryDirectlyIgnored(ITaxCollector collector) { return this.ignoredTaxCollectors.contains(collector.getID()); }
    public boolean ShouldIgnoreTaxEntry(ITaxCollector collector) { return this.ignoreAllTaxes || this.ignoredTaxCollectors.contains(collector.getID()); }
    public boolean AllowTaxEntry(ITaxCollector collector)  { return !this.ShouldIgnoreTaxEntry(collector); }

    public List<ITaxCollector> getApplicableTaxes(ITaxableContext context) { return TaxAPI.getApi().GetTaxCollectorsFor(context).stream().filter(this::AllowTaxEntry).toList(); }
    public final List<ITaxCollector> getPossibleTaxes() { return TaxAPI.getApi().GetPotentialTaxCollectorsFor(this.trader); }

    public final int getTotalTaxPercentage() { return this.getTotalTaxPercentage(ITaxableContext.defaultContext(this.trader)); }
    public final int getTotalTaxPercentage(ITaxableContext context)
    {
        List<ITaxCollector> entries = this.getApplicableTaxes(context);
        int taxPercentage = 0;
        for(ITaxCollector entry : entries)
            taxPercentage += entry.getTaxRate();
        return taxPercentage;
    }
    public final Pair<Integer,Integer> getTotalTaxPercentageRange()
    {
        int min = Integer.MAX_VALUE;
        int max = 0;
        Set<ITaxableContext> set = this.trader.getPossibleContexts();
        if(set.isEmpty())
            set = ITaxableContext.defaultSet(this.trader);
        for(ITaxableContext c : set)
        {
            int result = this.getTotalTaxPercentage(c);
            min = Math.min(result,min);
            max = Math.max(result,max);
        }
        return Pair.of(min,max);
    }
    public final boolean exceedsAcceptableTaxRate(ITaxableContext context) { return this.getTotalTaxPercentage(context) > this.acceptableTaxRate; }

    public MoneyValue payTaxesOn(MoneyValue amount, ITaxableContext context)
    {
        MoneyValue paidCache = MoneyValue.empty();
        for(ITaxCollector tax : this.getApplicableTaxes(context))
        {
            //Obey ignored tax settings
            if(!this.ShouldIgnoreTaxEntry(tax))
            {
                MoneyValue paid = tax.CalculateAndPayTaxes(this.trader,amount);
                MoneyValue temp = paidCache.addValue(paid);
                if(!temp.isEmpty())
                    paidCache = temp;
            }
        }
        return paidCache;
    }

    @Override
    public void beforeTrade(TradeEvent.PreTradeEvent event) {
        if(this.getTotalTaxPercentage(event.getContext().getTaxContext()) > this.acceptableTaxRate)
            event.addDenial(LCText.TOOLTIP_TAX_LIMIT.get());
    }

    @Override
    public void handleSettingsChange(Player player, LazyPacketData message) {
        if(message.contains("AcceptableTaxRate"))
        {
            if(this.hasPermission(player, Permissions.EDIT_SETTINGS))
            {
                int newRate = MathUtil.clamp(message.getInt("AcceptableTaxRate"), 0, 99);
                this.setAcceptableTaxRate(PlayerReference.of(player),newRate);
            }
        }
        if(message.contains("ForceIgnoreAllTaxCollectors"))
        {
            boolean newState = message.getBoolean("ForceIgnoreAllTaxCollectors");
            if((!newState || LCAdminMode.isAdminPlayer(player)))
                this.setIgnoreAllTaxes(PlayerReference.of(player),newState);
        }
        if(message.contains("ForceIgnoreTaxCollector"))
        {
            if(LCAdminMode.isAdminPlayer(player))
            {
                ITaxCollector entry = TaxAPI.getApi().GetTaxCollector(this,message.getLong("ForceIgnoreTaxCollector"));
                if(entry != null && entry.IsInArea(this.trader))
                {
                    if(this.ignoredTaxCollectors.contains(entry.getID()))
                        return;
                    this.ignoredTaxCollectors.add(entry.getID());
                    this.setIgnoredTaxCollectorsChanged();
                }
            }
        }
        if(message.contains("PardonTaxCollector"))
        {
            if(this.hasPermission(player,Permissions.EDIT_SETTINGS))
            {
                ITaxCollector entry = TaxAPI.getApi().GetTaxCollector(this,message.getLong("PardonTaxCollector"));
                if(entry != null && this.ignoredTaxCollectors.contains(entry.getID()))
                {
                    this.ignoredTaxCollectors.remove(entry.getID());
                    this.setIgnoredTaxCollectorsChanged();
                }
            }
        }
        if(message.contains("AcceptTaxCollector"))
        {
            if(this.hasPermission(player,Permissions.EDIT_SETTINGS))
            {
                ITaxCollector entry = TaxAPI.getApi().GetTaxCollector(this,message.getLong("AcceptTaxCollector"));
                if(entry != null && entry.IsInArea(this.trader))
                    entry.AcceptTaxable(this.trader);
            }
        }
    }

    @Override
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        //Tax Settings
        if(tag.contains("AcceptableTaxRate"))
            this.acceptableTaxRate = tag.getInt("AcceptableTaxRate");
        if(tag.contains("IgnoreAllTaxCollectors"))
            this.ignoreAllTaxes = tag.getBoolean("IgnoreAllTaxCollectors");
        if(tag.contains("IgnoreTaxCollectors"))
        {
            this.ignoredTaxCollectors.clear();
            for(long val : tag.getLongArray("IgnoreTaxCollectors"))
                this.ignoredTaxCollectors.add(val);
        }
    }

}
