package io.github.lightman314.lightmanscurrency.common.traders.paygate.nodes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketType;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;

import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.TradeOfferSourceNode;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tabs.PaygateTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.OutputConflictHandling;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.trade.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Supplier;

public class PaygateTradeNode extends TradeOfferSourceNode<PaygateTradeData> {

    private static final MapCodec<PaygateTradeNode> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            PaygateTradeData.CODEC.listOf().fieldOf("trades").forGetter(PaygateTradeNode::getAllTrades),
            OutputConflictHandling.CODEC.fieldOf("conflict_handling").forGetter(PaygateTradeNode::getConflictHandling)
    ).apply(builder,PaygateTradeNode::new));

    public static final TraderNodeType<PaygateTradeNode> TYPE = TraderNodeType.simple(PaygateTradeNode::new,MAP_CODEC);

    private final List<PaygateTradeData> trades = PaygateTradeData.listOfSize(1);

    private OutputConflictHandling conflictHandling = OutputConflictHandling.DENY_SIDE_CONFLICT;
    public OutputConflictHandling getConflictHandling() { return this.conflictHandling; }
    public void setConflictHandling(OutputConflictHandling conflictHandling)
    {
        if(this.conflictHandling == conflictHandling)
            return;
        this.conflictHandling = conflictHandling;
        this.setChanged(builder -> builder.setInt("conflict_handling",this.conflictHandling.ordinal()));
    }

    private PaygateTradeNode() {}
    private PaygateTradeNode(List<PaygateTradeData> trades, OutputConflictHandling conflictHandling)
    {
        this.trades.clear();
        this.trades.addAll(trades);
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void onAttach() {
        PaygateTradeData.setupParents(this.trades,this.trader);
        TradeData.afterLoad(this.trades,this);
    }

    @Override
    public int getTradeStock(int index) {
        PaygateTradeData trade = this.getTrade(index);
        if(trade == null || !trade.isValid())
            return 0;
        if(this.conflictHandling.allowsConflicts)
            return 1;
        if(this.trader instanceof PaygateTraderData paygate)
        {
            if(paygate.isActive(trade.getOutputSides()))
                return 0;
            return 1;
        }
        return 1;
    }

    @Override
    public boolean canEasilyChangeQuantity() { return true; }

    @Override
    public int getMaxTradeCount() { return 16; }

    @Override
    public boolean addTrade(Player player) {
        if(this.trades.size() >= this.getMaxTradeCount() && !LCAdminMode.isAdminPlayer(player))
            return false;
        if(this.trades.size() >= TraderData.GLOBAL_TRADE_LIMIT)
            return false;
        if(!this.hasPermission(player, Permissions.EDIT_TRADES))
            return false;
        PaygateTradeData newTrade = new PaygateTradeData();
        this.trades.add(newTrade);
        newTrade.setParent(this.trader);
        TradeData.afterLoad(newTrade,this);
        this.setTradeChanged(this.trades.size() - 1);
        return true;
    }

    @Override
    public boolean removeTrade(Player player) {
        if(this.trades.size() <= 1)
            return false;
        this.trades.removeLast();
        this.setTradeChanged(this.trades.size());
        return false;
    }

    @Override
    public boolean supportsTradeRules() { return true; }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,Player player) {
        super.createSyncPacket(builder,player);
        builder.setInt("conflict_handling",this.conflictHandling.ordinal());
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        super.onDataSync(data);
        if(data.contains("conflict_handling"))
            this.conflictHandling = EnumUtil.enumFromOrdinal(data.getInt("conflict_handling"),OutputConflictHandling.values(),OutputConflictHandling.DENY_ANY);
    }

    @Override
    public void handleSettingsChange(Player player, LazyPacketData message) {
        super.handleSettingsChange(player, message);
        if(message.contains("ChangeConflictMode"))
        {
            OutputConflictHandling newMode = EnumUtil.enumFromOrdinal(message.getInt("ChangeConflictMode"),OutputConflictHandling.values(),null);
            if(newMode != null && this.hasPermission(player,Permissions.EDIT_SETTINGS))
                this.setConflictHandling(newMode);
        }
    }

    @Override
    protected List<PaygateTradeData> getEditableList() { return this.trades; }

    @Override
    protected Supplier<LazyPacketType<PaygateTradeData>> getPacketType() { return ModLazyPackets.PAYGATE_TRADE; }

    @Override
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {

        if(tag.contains(TradeData.DEFAULT_KEY))
        {
            this.trades.clear();
            this.trades.addAll(PaygateTradeData.loadAllData(TradeData.DEFAULT_KEY,tag,lookup));
        }
        if(tag.contains("ConflictMode"))
            this.conflictHandling = EnumUtil.enumFromString(tag.getString("ConflictMode"),OutputConflictHandling.values(),OutputConflictHandling.DENY_SIDE_CONFLICT);
    }

    @Override
    public void applyStorageTabs(ITraderStorageMenu menu) {
        menu.addTab(new PaygateTradeEditTab(menu));
    }
}
