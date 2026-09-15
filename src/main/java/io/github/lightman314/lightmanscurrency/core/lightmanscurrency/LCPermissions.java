package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.BuiltInPermissions;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCPermissions {
    private LCPermissions() {}

    public static final DeferredRegister<Permission<?>> REGISTER = DeferredRegister.create(LCRegistries.Trader.PERMISSION,LCApi.MODID);

    static {
        register("open_storage",BuiltInPermissions.OPEN_STORAGE);
        register("edit_display",BuiltInPermissions.EDIT_DISPLAY);
        register("edit_trades",BuiltInPermissions.EDIT_TRADES);
        register("add_remove_allies",BuiltInPermissions.ADD_REMOVE_ALLIES);
        register("edit_ally_perms",BuiltInPermissions.EDIT_ALLY_PERMS);
        register("break_trader",BuiltInPermissions.BREAK_TRADER);
        register("collect_money",BuiltInPermissions.COLLECT_MONEY);
        register("store_money",BuiltInPermissions.STORE_MONEY);
        register("edit_trade_rules",BuiltInPermissions.EDIT_TRADE_RULES);
        register("edit_settings",BuiltInPermissions.EDIT_SETTINGS);
        register("transfer_ownership",BuiltInPermissions.TRANSFER_OWNERSHIP);
        register("view_logs",BuiltInPermissions.VIEW_LOGS);
        register("external_access_setttings",BuiltInPermissions.EXTERNAL_ACCESS_SETTINGS);
    }

    public static void register(String name,Permission<?> permission) {
        REGISTER.register(name,() -> permission);
    }

}
