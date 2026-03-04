package io.github.lightman314.lightmanscurrency.common.core.custom;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderType;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.commands.CommandTrader;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.GachaTrader;
import io.github.lightman314.lightmanscurrency.common.traders.item.*;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModTraderTypes {

    public static final DeferredRegister<TraderType<?>> REGISTER;

    static {
        REGISTER = DeferredRegister.create(LCRegistries.TRADER_TYPES, LightmansCurrency.MODID);

        REGISTER.register("auction_house",() -> AuctionHouseTrader.TYPE);
        REGISTER.register("item_trader",() -> ItemTraderData.TYPE);
        REGISTER.register("item_trader_armor",() -> ItemTraderDataArmor.TYPE);
        REGISTER.register("item_trader_book",() -> ItemTraderDataBook.TYPE);
        REGISTER.register("item_trader_ticket",() -> ItemTraderDataTicket.TYPE);
        REGISTER.register("paygate",() -> PaygateTraderData.TYPE);
        REGISTER.register("commands",() -> CommandTrader.TYPE);
        REGISTER.register("slot_machine_trader",() -> SlotMachineTraderData.TYPE);
        REGISTER.register("gacha",() -> GachaTrader.TYPE);

    }

}
