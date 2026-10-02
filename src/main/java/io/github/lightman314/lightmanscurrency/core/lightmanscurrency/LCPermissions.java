package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.*;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCPermissions {
    private LCPermissions() {}

    public static final DeferredRegister<Permission<?>> REGISTER = DeferredRegister.create(LCRegistries.Trader.PERMISSION,LCApi.MODID);

    public static final Permission<Boolean> OPEN_STORAGE = registerBoolean("open_storage",true);
    public static final Permission<Boolean> EDIT_DISPLAY = registerBoolean("edit_display",true);
    public static final Permission<Boolean> EDIT_TRADES = registerBoolean("edit_trades",true);
    public static final Permission<Boolean> ADD_REMOVE_ALLIES = registerBoolean("add_remove_allies",false);
    public static final Permission<Boolean> EDIT_ALLY_PERMS = registerBoolean("edit_ally_perms",false);
    public static final Permission<TriStatePermission> BREAK_TRADER = registerTriState("break_trader",TriStatePermission.NONE);
    public static final Permission<Boolean> COLLECT_MONEY = registerBoolean("collect_money",false);
    public static final Permission<Boolean> STORE_MONEY = registerBoolean("store_money",false);
    public static final Permission<Boolean> EDIT_TRADE_RULES = registerBoolean("eidt_trade_rules",true);
    public static final Permission<Boolean> EDIT_SETTINGS = registerBoolean("edit_settings",true);
    public static final Permission<Boolean> TRANSFER_OWNERSHIP = registerBoolean("transfer_ownership",false);
    public static final Permission<TriStatePermission> VIEW_LOGS = registerTriState("view_logs",TriStatePermission.NONE);
    public static final Permission<Boolean> EXTERNAL_ACCESS_SETTINGS = registerBoolean("external_access_settings",true);

    public static Permission<Boolean> registerBoolean(String name,boolean defaultValue) { return register(name,BooleanPermissionType.of(defaultValue)); }
    public static Permission<TriStatePermission> registerTriState(String name,TriStatePermission defaultValue) { return register(name,EnumPermissionType.INSTANCE.create(defaultValue)); }

    public static <T> Permission<T> register(String name,Permission<T> permission) {
        REGISTER.register(name,() -> permission);
        return permission;
    }

}
