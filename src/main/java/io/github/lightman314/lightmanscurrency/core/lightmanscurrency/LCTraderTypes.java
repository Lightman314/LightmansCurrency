package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderType;
import io.github.lightman314.lightmanscurrency.features.trader.item.ItemTraderType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCTraderTypes {

    private LCTraderTypes() {}

    public static final DeferredRegister<TraderType> REGISTER = DeferredRegister.create(LCRegistries.Trader.TRADER_TYPES,LCApi.MODID);

    public static final DeferredHolder<TraderType,ItemTraderType> ITEM_TRADER = register("item_trader",new ItemTraderType());

    private static <T extends TraderType> DeferredHolder<TraderType,T> register(String name,T type) {
        return REGISTER.register(name,() -> type);
    }

}