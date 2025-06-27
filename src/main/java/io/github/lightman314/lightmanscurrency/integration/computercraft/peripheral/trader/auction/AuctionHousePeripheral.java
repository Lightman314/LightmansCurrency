package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.auction;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.data.types.TraderDataCache;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AuctionHousePeripheral extends LCPeripheral {

    public static IPeripheral INSTANCE = new AuctionHousePeripheral();

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

    @LuaFunction(mainThread = true)
    public long getID() throws LuaException { return this.getTrader().getID(); }

    @LuaFunction(mainThread = true)
    public int getAuctionCount() throws LuaException { return this.getTrader().validTradeCount(); }

    @LuaFunction(mainThread = true)
    public LCLuaTable[] getAuctions() throws LuaException {
        AuctionHouseTrader trader = this.getTrader();
        List<LCLuaTable> list = new ArrayList<>();
        for(int i = 0; i < trader.getTradeCount(); ++i)
        {
            AuctionTradeData trade = trader.getTrade(i);
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
                List<LCLuaTable> items = new ArrayList<>();
                for(ItemStack item : trade.getAuctionItems())
                    items.add(LCLuaTable.fromTag(InventoryUtil.saveItemNoLimits(item)));
                entry.put("Items",items.toArray(LCLuaTable[]::new));
                list.add(entry);
            }
        }
        return list.toArray(LCLuaTable[]::new);
    }



}