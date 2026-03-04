package io.github.lightman314.lightmanscurrency.common.core.custom;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.item.ticket.TicketItemTrade;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.ItemTradeType;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItemTradeTypes {

    public static final DeferredRegister<ItemTradeType<?>> REGISTER = DeferredRegister.create(LCRegistries.ITEM_TRADE, LightmansCurrency.MODID);

    static {
        REGISTER.register("item",() -> ItemTradeData.DEFAULT_TYPE);
        REGISTER.register("ticket_kiosk",() -> TicketItemTrade.TYPE);
    }

}
