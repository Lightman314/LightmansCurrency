package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.api.events.TraderEvent;
import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;

import io.github.lightman314.lightmanscurrency.api.traders.tracking.PlayerTraderTrackingHolder;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.tracking.TrackingLevel;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menus.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashSet;
import java.util.Set;

public class TerminalMenu extends LazyMessageMenu implements IValidatedMenu {

    private final MenuValidator validator;

    private final PlayerTraderTrackingHolder trackingHolder = new PlayerTraderTrackingHolder(this,TrackingLevel.CUSTOMER);
    private final Set<Long> endTrackingList = new HashSet<>();

    @Override
    public MenuValidator getValidator() { return this.validator; }

    public TerminalMenu(int id, Inventory inventory, MenuValidator validator)
    {
        super(ModMenus.NETWORK_TERMINAL.get(), id, inventory, validator);
        this.validator = validator;
        //Flag the validator has having gone through the network
        this.validator.isThroughNetwork = true;
        this.addValidator(() -> !QuarantineAPI.IsDimensionQuarantined(this));
        NeoForge.EVENT_BUS.register(this);
        for(TraderData trader : TraderAPI.getApi().GetAllNetworkTraders(false))
            this.trackingHolder.requestTracking(trader,this.player);
    }

    //Listen on low priority so that the end tracking happens after the trader is synced with the player
    //So they'll know on the logical client that this is no longer a network trader
    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onServerTick(ServerTickEvent.Post event)
    {
        for(Long trader : this.endTrackingList)
            this.trackingHolder.endTracking(trader,this.player);
        this.endTrackingList.clear();
    }

    //Flag traders that are no longer visible on the network to stop being tracked by this player after the next server tick
    @SubscribeEvent
    private void onNetworkTraderUpdate(TraderEvent.TraderNetworkStatusUpdated event)
    {
        if(!event.isNetworkAccessible())
            this.endTrackingList.add(event.getID());
        else //Request tracking for traders that are now network-accessible
            this.trackingHolder.requestTracking(event.getTrader(),this.player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.trackingHolder.clear(player);
        NeoForge.EVENT_BUS.unregister(this);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }

    public void openAllTraders()
    {
        if(this.isClient())
        {
            this.SendMessage(this.builder().setFlag("OpenAllTraders"));
            return;
        }
        player.openMenu(TraderData.getTraderMenuForAllNetworkTraders(validator),encoder(validator));
    }

    public void openTrader(long traderID)
    {
        if(this.isClient())
        {
            this.SendMessage(this.builder().setLong("OpenTrader",traderID));
            return;
        }
        TraderData trader = TraderAPI.getApi().GetTrader(false,traderID);
        if(trader != null && trader.isNetworkAccessible())
            trader.openTraderMenu(this.player,this.validator);
    }

    @Override
    public void processMessage(LazyPacketData message) {
        if(message.contains("OpenTrader"))
            this.openTrader(message.getLong("OpenTrader"));
        if(message.contains("OpenAllTraders"))
            this.openAllTraders();
    }


}
