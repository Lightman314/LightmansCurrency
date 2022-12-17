package io.github.lightman314.lightmanscurrency.common.playertrading;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
public class PlayerTradeManager {

    private static final Map<Integer,PlayerTrade> trades = new HashMap<>();

    public static boolean TradeStillValid(int tradeID) { return trades.containsKey(tradeID); }

    @Nullable
    public static PlayerTrade GetTrade(int tradeID) { return trades.get(tradeID); }

    public static List<PlayerTrade> GetAllTrades() { return trades.values().stream().toList(); }

    public static int CreateNewTrade(ServerPlayer host, ServerPlayer guest) {
        int newTradeID = getAvailableTradeID();
        trades.put(newTradeID, new PlayerTrade(host, guest, newTradeID));
        return newTradeID;
    }

    private static int getAvailableTradeID() {
        for(int i = 1; i < Integer.MAX_VALUE - 1; ++i)
        {
            if(!trades.containsKey(i))
                return i;
        }
        throw new RuntimeException("Could not find an available Trade ID between 1 and " + (Integer.MAX_VALUE - 1) + "!");
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if(event.phase == TickEvent.Phase.START)
        {
            List<Integer> removeTrades = new ArrayList<>();
            trades.forEach((id,trade) -> {
                if(trade.shouldCancel())
                    removeTrades.add(id);
            });
            for(int tradeID : removeTrades)
            {
                PlayerTrade trade = trades.get(tradeID);
                trades.remove(tradeID);
                trade.onCancel();
            }
        }
    }

    @SubscribeEvent
    public static void onServerClose(ServerStoppingEvent event) {
        trades.forEach((id,trade) -> trade.onCancel());
        trades.clear();
    }

}
