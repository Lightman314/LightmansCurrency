package io.github.lightman314.lightmanscurrency.api;

import io.github.lightman314.lightmanscurrency.api.bank_account.BankAPI;
import io.github.lightman314.lightmanscurrency.api.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.config.ConfigAPI;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionAPI;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.api.quarantine.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.api.stats.PlayerStatsAPI;
import io.github.lightman314.lightmanscurrency.api.trader.TraderAPI;
import io.github.lightman314.lightmanscurrency.features.admin.AdminMode;
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
    public static NotificationAPI getNotificationAPI() { return NotificationAPIImpl.INSTANCE; }
    public static PlayerStatsAPI getPlayerStatsAPI() { return PlayerStatsAPIImpl.INSTANCE; }
    public static EjectionAPI getEjectionAPI() { return EjectionAPIImpl.INSTANCE; }
    public static QuarantineAPI getQuarantineAPI() { return QuarantineAPIImpl.INSTANCE; }

    public static boolean isInAdminMode(Player player) { return AdminMode.isAdmin(player); }

}
