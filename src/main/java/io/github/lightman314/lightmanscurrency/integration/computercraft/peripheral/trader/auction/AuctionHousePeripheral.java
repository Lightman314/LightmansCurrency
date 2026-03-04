package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.auction;

import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.data.types.TraderDataCache;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.nodes.AuctionTradesNode;
import io.github.lightman314.lightmanscurrency.common.traders.auction.trade.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.integration.computercraft.AccessTrackingPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuctionHousePeripheral extends AccessTrackingPeripheral {

    public static AccessTrackingPeripheral INSTANCE = new AuctionHousePeripheral();

    private AuctionHousePeripheral() {}

    @Override
    public String getType() { return "lc_trader_auction"; }

    @Override
    public boolean equals(@Nullable IPeripheral other) { return other == INSTANCE; }

    private AuctionHouseTrader getTrader() throws LuaException
    {
        if(!LCConfig.SERVER.auctionHouseEnabled.get())
            throw new LuaException("Auction House is disabled!");
        TraderDataCache data = TraderDataCache.TYPE.get(false);
        if(data.getAuctionHouse() instanceof AuctionHouseTrader ah)
            return ah;
        throw new LuaException("Auction House could not be located!");
    }

    private AuctionTradesNode getNode() throws LuaException
    {
        AuctionHouseTrader trader = this.getTrader();
        AuctionTradesNode node = trader.getNode(AuctionTradesNode.TYPE);
        if(node == null)
            throw new LuaException("Auction House is missing a critical node!");
        return node;
    }

    public long getID() throws LuaException { return this.getTrader().getID(); }

    public int getAuctionCount() throws LuaException { return this.getTrader().validTradeCount(); }

    public LCLuaTable getAuctions() throws LuaException {
        AuctionHouseTrader trader = this.getTrader();
        List<LCLuaTable> list = new ArrayList<>();
        AuctionTradesNode node = this.getNode();
        for(int i = 0; i < trader.getTradeCount(); ++i)
        {
            AuctionTradeData trade = node.getTrade(i);
            if(trade.isValid())
            {
                LCLuaTable entry = new LCLuaTable();
                entry.put("LastBid",LCLuaTable.fromMoney(trade.getLastBidAmount()));
                entry.put("MinumumBid",LCLuaTable.fromMoney(trade.getMinNextBid()));
                entry.put("BidDifference",LCLuaTable.fromMoney(trade.getMinBidDifference()));
                PlayerReference lastBidder = trade.getLastBidPlayer();
                entry.put("LastBidder",lastBidder == null ? null : lastBidder.getName(false));
                entry.put("Owner",trade.getOwner().getName(false));
                entry.put("RemainingTime",trade.getRemainingTime(TimeUtil.getCurrentTime()));
                List<Map<String,Object>> items = new ArrayList<>();
                for(ItemStack item : trade.getAuctionItems())
                    items.add(VanillaDetailRegistries.ITEM_STACK.getDetails(item));
                entry.put("Items",LCLuaTable.fromList(items));
                list.add(entry);
            }
        }
        return LCLuaTable.fromList(list);
    }

    @Override
    protected void registerMethods(LCPeripheralMethod.Registration registration) {
        registration.register(LCPeripheralMethod.builder("getID").simple(this::getID));
        registration.register(LCPeripheralMethod.builder("getAuctionCount").simple(this::getAuctionCount));
        registration.register(LCPeripheralMethod.builder("getAuctions").simple(this::getAuctions));
    }

}
