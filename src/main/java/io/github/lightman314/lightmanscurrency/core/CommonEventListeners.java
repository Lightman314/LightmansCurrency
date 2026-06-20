package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerClient;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerCommon;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerServer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;

@EventBusSubscriber
public class CommonEventListeners {

    @SubscribeEvent
    private static void addRegistries(NewRegistryEvent event)
    {
        event.register(LCRegistries.Money.VALUE_TYPE);
        event.register(LCRegistries.Money.VALUE_HELPER);
        event.register(LCRegistries.Coins.VALUE_DISPLAY_SERIALIZER);
        event.register(LCRegistries.Coins.ATM_ICON_TYPE);
        event.register(LCRegistries.Coins.ATM_COMMAND_TYPE);
        event.register(LCRegistries.Bank.REFERENCE_TYPE);
        event.register(LCRegistries.Ownership.OWNER_TYPE);
        event.register(LCRegistries.Ownership.POTENTIAL_OWNER);
        event.register(LCRegistries.Trader.TRADER_TYPES);
        event.register(LCRegistries.Trader.TRADER_NODE_TYPE);
        event.register(LCRegistries.Trader.TRADE_DATA_TYPE);
        event.register(LCRegistries.Trader.TRADE_PRICE_TYPE);
        event.register(LCRegistries.Trader.PERMISSION_TYPE);
        event.register(LCRegistries.Trader.PERMISSION);
        event.register(LCRegistries.Upgrades.UPGRADES);
        event.register(LCRegistries.Upgrades.NUMBER_SOURCE);
        event.register(LCRegistries.Misc.MENU_VALIDATOR);
        event.register(LCRegistries.Misc.ICON_TYPE);
        event.register(LCRegistries.Data.FANCY_DATA);
        event.register(LCRegistries.Network.PACKET_TYPE);
    }

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Pre event)
    {
        Player player = event.getEntity();
        ISidedContext context = ISidedContext.wrap(player);
        if(player.containerMenu instanceof ITickerClient t && context.isClient())
            t.clientTick();
        if(player.containerMenu instanceof ITickerCommon t)
            t.tick();
        if(player.containerMenu instanceof ITickerServer t)
            t.serverTick();
    }

}
