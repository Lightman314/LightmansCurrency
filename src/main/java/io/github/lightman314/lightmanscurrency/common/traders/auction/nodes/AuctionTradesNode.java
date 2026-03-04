package io.github.lightman314.lightmanscurrency.common.traders.auction.nodes;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.events.AuctionHouseEvent;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketType;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxCollector;
import io.github.lightman314.lightmanscurrency.api.taxes.TaxAPI;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;

import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.INetworkController;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IPermissionProvider;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.ITerminalDisplay;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.TradeOfferSourceNode;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tabs.AuctionCreateTab;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tabs.AuctionTradeCancelTab;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.trade.AuctionTradeData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AuctionTradesNode extends TradeOfferSourceNode<AuctionTradeData> implements ITerminalDisplay, INetworkController, IPermissionProvider {

    private static final MapCodec<AuctionTradesNode> MAP_CODEC = AuctionTradeData.CODEC.listOf().fieldOf("auctions")
            .xmap(AuctionTradesNode::new,AuctionTradesNode::getAllTrades);

    public static final TraderNodeType<AuctionTradesNode> TYPE = TraderNodeType.simple(AuctionTradesNode::new,MAP_CODEC);

    private final List<AuctionTradeData> trades = new ArrayList<>();

    private AuctionTradesNode() {}
    private AuctionTradesNode(List<AuctionTradeData> trades) {
        this.trades.addAll(trades);
        TradeData.afterLoad(this.trades,this);
    }

    public void setTradesChanged()
    {
        this.setChanged(builder -> builder.clear() //Clear call to reset any and all "edit_trade" data that was written
                .setList("trades",this.trades,ModLazyPackets.AUCTION_TRADE));
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    protected List<AuctionTradeData> getEditableList() { return this.trades; }
    @Override
    protected Supplier<LazyPacketType<AuctionTradeData>> getPacketType() { return ModLazyPackets.AUCTION_TRADE; }

    @Override
    @SuppressWarnings("deprecation")
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        //Load trades
        if(tag.contains("Trades"))
        {
            this.trades.clear();
            ListTag tradeList = tag.getList("Trades", Tag.TAG_COMPOUND);
            for(int i = 0; i < tradeList.size(); ++i)
                this.trades.add(AuctionTradeData.loadOldData(tradeList.getCompound(i),lookup));
        }
    }

    public int getPlayerTradeCount(Player player) { return (int)this.trades.stream().filter(AuctionTradeData::isValid).filter(trade -> trade.isOwner(player)).count(); }

    @Override
    public int getTradeStock(int index) {
        AuctionTradeData trade = this.getTrade(index);
        return trade != null && trade.isValid() ? 1 : 0;
    }

    public int getTradeIndex(AuctionTradeData trade) {
        return this.trades.indexOf(trade);
    }

    public boolean hasPersistentAuction(String id) {
        for(AuctionTradeData trade : this.trades)
        {
            if(trade.isPersistentID(id) && trade.isValid())
                return true;
        }
        return false;
    }

    @Override
    public List<AuctionTradeData> getAllTrades() { return this.trades; }

    @Override
    public Predicate<TradeData> getTradeStorageFilter(ITraderStorageMenu menu) { return trade -> trade instanceof AuctionTradeData at && at.isOwner(menu.getPlayer()) && at.isValid(); }

    @Override
    public void applyStorageTabs(ITraderStorageMenu menu) {
        super.applyStorageTabs(menu);
        //Cancel Trade tab
        menu.addTab(new AuctionTradeCancelTab(menu));
        //Create Trade tab
        menu.addTab(new AuctionCreateTab(menu));
    }

    @Override
    public boolean addTrade(Player player) { return false; }
    @Override
    public boolean removeTrade(Player player) { return false; }

    @Override
    public boolean supportsTradeRules() { return false; }

    public boolean addTrade(AuctionTradeData trade, @Nullable Player player, boolean persistent) {

        if(!(this.trader instanceof AuctionHouseTrader ah))
            return false;
        AuctionHouseEvent.AuctionEvent.CreateAuctionEvent.Pre e1 = new AuctionHouseEvent.AuctionEvent.CreateAuctionEvent.Pre(ah, trade, player, persistent);
        if(NeoForge.EVENT_BUS.post(e1).isCanceled())
            return false;
        trade = e1.getAuction();

        trade.startTimer();
        if(trade.isValid())
        {
            //Validate the player trade limits
            if(player != null && !LCAdminMode.isAdminPlayer(player))
            {
                int tradeCount = this.getPlayerTradeCount(player);
                int limit = LCConfig.SERVER.auctionHousePlayerLimit.get();
                if(tradeCount >= limit)
                {
                    LightmansCurrency.LogInfo("Player has exceeded their auction limit. Limit: " + limit + " Count: " + tradeCount);
                    return false;
                }
                //Otherwise attempt to collect the submission fee
                MoneyValue price = LCConfig.SERVER.auctionHouseSubmitPrice.get();
                if(!price.isEmpty())
                {
                    IMoneyHandler handler = MoneyAPI.getApi().GetPlayersMoneyHandler(player);
                    if(handler.extractMoney(price,true).isEmpty())
                    {
                        handler.extractMoney(price,false);
                        //Store the submission fee into the server tax
                        if(LCConfig.SERVER.auctionHouseStoreFeeInServerTax.get())
                        {
                            ITaxCollector serverTax = TaxAPI.getApi().GetServerTaxCollector(this);
                            serverTax.PayTaxesDirectly(this.trader,price);
                        }
                    }
                    else
                    {
                        LightmansCurrency.LogInfo("Player does not have enough money to pay the submission fee");
                        return false;
                    }
                }
            }

            this.trades.add(trade);
            this.setTradeChanged(trade);

            AuctionHouseEvent.AuctionEvent.CreateAuctionEvent.Post e2 = new AuctionHouseEvent.AuctionEvent.CreateAuctionEvent.Post(ah, trade, player, persistent);
            NeoForge.EVENT_BUS.post(e2);
            return true;
        }
        else
            LightmansCurrency.LogError("Auction Trade is not fully valid. Unable to add it to the list.");
        return false;
    }

    public void tickTrades(AuctionHouseTrader trader,AuctionStorageNode storageNode, boolean canDelete)
    {
        //Check if any trades have expired
        long currentTime = System.currentTimeMillis();
        for(int i = 0; i < this.trades.size(); ++i)
        {
            AuctionTradeData trade = this.trades.get(i);
            //Check if the auction has timed out and should be executed
            if(trade.hasExpired(currentTime))
            {
                //Execute the trade if the time has run out
                //Includes sending notifications and payment to the relevant players storage
                trade.ExecuteTrade(storageNode,trader);
            }
            //Check if the trade should be deleted
            if(canDelete && !trade.isValid())
            {
                //Delete the trade if it's no longer valid
                this.trades.remove(i);
                //Flag it as changed to send a removal packet to the client
                this.setTradeChanged(i);
                i--;
            }
        }
    }

    @Override
    public void applyTerminalTextColor(AtomicInteger color) {
        int auctionCount = 0;
        for(AuctionTradeData auction : new ArrayList<>(this.trades))
        {
            if(auction.isValid() && auction.isActive())
            {
                //Green if there's an auction available, normal color if not.
                color.set(0x00FF00);
                return;
            }
        }
    }

    @Override
    public void addTerminalInfo(List<Component> tooltip, @Nullable Player player) {
        int auctionCount = 0;
        for(AuctionTradeData auction : this.trades)
        {
            if(auction.isValid() && auction.isActive())
                auctionCount++;
        }
        tooltip.add(LCText.TOOLTIP_NETWORK_TERMINAL_AUCTION_HOUSE.get(auctionCount));
    }

    @Override
    public boolean visibleToNetwork() { return AuctionHouseTrader.shouldShowOnTerminal(); }

    @Override
    public int getPermissionLevel(String permission, PlayerReference player) {
        if(permission.equals(Permissions.EDIT_TRADES))
            return 1;
        return 0;
    }

    private static class Type extends TraderNodeType<AuctionTradesNode>
    {
        @Override
        public AuctionTradesNode create(@Nullable Object argument) { return new AuctionTradesNode(); }
        @Override
        public MapCodec<AuctionTradesNode> codec() { return MAP_CODEC; }
    }

}
