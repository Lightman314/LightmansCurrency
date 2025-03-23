package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs.DailyTradesTab;
import io.github.lightman314.lightmanscurrency.common.traders.rules.ITradeRuleHost;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DailyTrades extends TradeRule {

    public static final TradeRuleType<DailyTrades> TYPE = new TradeRuleType<>(VersionUtil.lcResource("daily_trades"),DailyTrades::new);

    private final Map<UUID,Data> data = new HashMap<>();
    public int dataSize() { return this.data.size(); }
    private long interactionDelay = TimeUtil.DURATION_DAY;
    public long getInteractionDelay() { return this.interactionDelay; }

    private DailyTrades() { super(TYPE); }

    @Override
    protected boolean onlyAllowOnTraders() { return true; }

    @Override
    protected boolean canActivate(@Nullable ITradeRuleHost host) { return super.canActivate(host) && host instanceof TraderData trader && (trader.getTradeCount() > 1 || trader.getMaxTradeCount() > 1); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconUtil.ICON_DAILY_TRADE; }

    //Check if trade index is the one flagged as the next trade index for the given player
    @Override
    public void beforeTrade(@Nonnull TradeEvent.PreTradeEvent event) {
        if(event.getPlayerReference() == null)
            return;
        Data data = this.data.getOrDefault(event.getPlayerReference().id,new Data());
        int index = event.getTradeIndex();
        if(data.nextIndex == index)
        {
            if(TimeUtil.compareTime(this.interactionDelay,data.lastTimeStamp))
            {
                //Time has not yet passed, give info about the remaining time to wait
                long timeUntil = data.lastTimeStamp + this.interactionDelay - TimeUtil.getCurrentTime();
                event.addDenial(LCText.TRADE_RULE_DAILY_TRADES_LOCKED_WAITING.get(new TimeUtil.TimeData(timeUntil).getShortString()));
            }
            else
            {
                //Enough time has passed, allow the interaction
                event.addHelpful(LCText.TRADE_RULE_DAILY_TRADES_ALLOWED.get());
            }
        }
        else if(index < data.nextIndex)
        {
            //Already completed, so locked permanently
            event.addDenial(LCText.TRADE_RULE_DAILY_TRADES_LOCKED_COMPLETE.get());
        }
        else
        {
            //Trade is not the next one in the list, so locked until previous trade is complete
            event.addDenial(LCText.TRADE_RULE_DAILY_TRADES_LOCKED_NOT_NEXT.get());
        }
    }

    //Update data
    @Override
    public void afterTrade(@Nonnull TradeEvent.PostTradeEvent event) {
        PlayerReference player = event.getPlayerReference();
        if(player == null)
            return;
        Data dat = this.data.getOrDefault(player.id,new Data());
        if(event.getTradeIndex() == dat.nextIndex)
        {
            dat.nextIndex++;
            dat.lastTimeStamp = TimeUtil.getCurrentTime();
            this.data.put(player.id,dat);
            event.markDirty();
        }
        else
            LightmansCurrency.LogWarning("A Daily Trade managed to go through for a trade that is not flagged as the next in the progression lineup.");
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
        compound.putLong("Delay",this.interactionDelay);
        this.saveData(compound);
    }

    private void saveData(@Nonnull CompoundTag compound)
    {
        ListTag dataList = new ListTag();
        this.data.forEach((id,dat) -> {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("Player",id);
            tag.putInt("Index",dat.nextIndex);
            tag.putLong("Time",dat.lastTimeStamp);
            dataList.add(tag);
        });
        compound.put("Data",dataList);
    }

    @Override
    protected void loadAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
        this.interactionDelay = compound.getLong("Delay");
        this.loadData(compound);
    }

    private void loadData(@Nonnull CompoundTag compound)
    {
        this.data.clear();
        ListTag dataList = compound.getList("Data", Tag.TAG_COMPOUND);
        for(int i = 0; i < dataList.size(); ++i)
        {
            CompoundTag tag = dataList.getCompound(i);
            Data dat = new Data();
            UUID id = tag.getUUID("Player");
            dat.nextIndex = tag.getInt("Index");
            dat.lastTimeStamp = tag.getLong("Time");
            this.data.put(id,dat);
        }
    }

    @Override
    public JsonObject saveToJson(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup) {
        json.addProperty("Delay",this.interactionDelay);
        return json;
    }

    @Override
    public void loadFromJson(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException {
        this.interactionDelay = GsonHelper.getAsLong(json,"Delay");
    }

    @Override
    public CompoundTag savePersistentData(@Nonnull HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        this.saveData(tag);
        return tag;
    }

    @Override
    public void loadPersistentData(@Nonnull CompoundTag data, @Nonnull HolderLookup.Provider lookup) {
        this.loadData(data);
    }

    @Override
    protected void handleUpdateMessage(@Nonnull LazyPacketData updateInfo) {
        if(updateInfo.contains("ClearData"))
            this.data.clear();
        if(updateInfo.contains("SetDelay"))
            this.interactionDelay = updateInfo.getLong("SetDelay");
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public TradeRulesClientSubTab createTab(TradeRulesClientTab<?> parent) { return new DailyTradesTab(parent); }

    private static class Data
    {
        public int nextIndex = 0;
        public long lastTimeStamp = Long.MIN_VALUE;
    }

}
