package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral;

import dan200.computercraft.api.peripheral.IPeripheral;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TraderEvent;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.integration.computercraft.AccessTrackingPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCComputerHelper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class TerminalPeripheral extends MultiTraderPeripheral {

    public static final TerminalPeripheral INSTANCE = new TerminalPeripheral();
    public static final LazyOptional<IPeripheral> LAZY = LazyOptional.of(() -> INSTANCE);

    private final Consumer<TradeEvent.PreTradeEvent> preTradeEventListener = this::preTradeEvent;
    private final Consumer<TradeEvent.PostTradeEvent> postTradeEventListener = this::postTradeEvent;
    private final Consumer<TraderEvent.CreateNetworkTraderEvent> newTraderEventListener = this::newTraderEvent;
    private final Consumer<TraderEvent.RemoveNetworkTraderEvent> removedTraderEventListener = this::removeTraderEvent;
    private TerminalPeripheral() {}

    @Override
    public String getType() { return "lc_terminal"; }

    @Override
    protected void onFirstAttachment() {
        super.onFirstAttachment();
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST,this.preTradeEventListener);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST,this.postTradeEventListener);
    }

    @Override
    protected void onLastDetachment() {
        MinecraftForge.EVENT_BUS.unregister(this.preTradeEventListener);
        MinecraftForge.EVENT_BUS.unregister(this.postTradeEventListener);
    }

    @Nonnull
    @Override
    protected List<TraderData> getAccessibleTraders() { return TraderAPI.getApi().GetAllNetworkTraders(false); }

    @Override
    protected boolean stillAccessible(TraderData trader) { return trader.showOnTerminal(); }

    @Override
    public boolean equals(@Nullable IPeripheral peripheral) { return peripheral == INSTANCE; }

    private void preTradeEvent(TradeEvent.PreTradeEvent event)
    {
        TraderData t = event.getTrader();
        //Only push event if trader is visible on the network
        if(t.canShowOnTerminal() && LCComputerHelper.getPeripheral(t) instanceof TraderPeripheral<?,?> trader)
        {
            AccessTrackingPeripheral tradeWrapper = trader.safeWrapTrade(event.getTrade());
            if(tradeWrapper == null)
                return;
            LCLuaTable player = LCLuaTable.fromPlayer(event.getPlayerReference());
            boolean canceled = event.isCanceled();
            this.queueEvent("lc_trade_pre",computer -> new Object[]{ trader.asTable(computer),event.getTradeIndex(),tradeWrapper.asTable(computer),player,canceled});
        }
    }

    private void postTradeEvent(TradeEvent.PostTradeEvent event)
    {
        TraderData t = event.getTrader();
        //Only push event if trader is visible on the network
        if(t.canShowOnTerminal() && LCComputerHelper.getPeripheral(t) instanceof TraderPeripheral<?,?> trader)
        {
            AccessTrackingPeripheral tradeWrapper = trader.safeWrapTrade(event.getTrade());
            if(tradeWrapper == null)
                return;
            LCLuaTable player = LCLuaTable.fromPlayer(event.getPlayerReference());
            LCLuaTable finalPrice = LCLuaTable.fromMoney(event.getPricePaid());
            LCLuaTable taxesPaid = LCLuaTable.fromMoney(event.getTaxesPaid());
            this.queueEvent("lc_trade",computer -> new Object[] { trader.asTable(computer),event.getTradeIndex(),tradeWrapper.asTable(computer),player,finalPrice,taxesPaid });
        }
    }

    private void newTraderEvent(TraderEvent.CreateNetworkTraderEvent event)
    {
        AccessTrackingPeripheral trader = LCComputerHelper.getPeripheral(event.getTrader());
        LCLuaTable player = LCLuaTable.fromPlayer(event.getPlayer());
        this.queueEvent("lc_trader_created",computer -> new Object[] { trader.asTable(computer),player,event.getID()});
    }

    private void removeTraderEvent(TraderEvent.RemoveNetworkTraderEvent event)
    {
        AccessTrackingPeripheral trader = LCComputerHelper.getPeripheral(event.getTrader());
        this.queueEvent("lc_trader_removed",computer -> new Object[] { trader.asTable(computer),event.getID()});
    }

}