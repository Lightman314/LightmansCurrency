package io.github.lightman314.lightmanscurrency.api;

import io.github.lightman314.lightmanscurrency.api.bank_account.BankAPI;
import io.github.lightman314.lightmanscurrency.api.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.config.ConfigAPI;
import io.github.lightman314.lightmanscurrency.api.trader.TraderAPI;
import io.github.lightman314.lightmanscurrency.features.api_impl.*;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public final class LCApi {

    private LCApi() {}

    public static final String MODID = "lightmanscurrency";
    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MODID,path); }

    public static MoneyAPI getMoneyAPI() { return MoneyAPIImpl.INSTANCE; }
    public static CoinAPI getCoinAPI() { return CoinAPIImpl.INSTANCE; }
    public static ConfigAPI getConfigAPI() { return ConfigAPIImpl.INSTANCE; }
    public static BankAPI getBankAPI() { return BankAPIImpl.INSTANCE; }
    public static TraderAPI getTraderAPI() { return TraderAPIImpl.INSTANCE; }

    //TODO implement admin mode hook
    public static boolean isInAdminMode(Player player) { return false; }

}
