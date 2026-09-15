package io.github.lightman314.lightmanscurrency.api.trader.permissions;

public final class BuiltInPermissions {
    private BuiltInPermissions() {}

    public static final Permission<Boolean> OPEN_STORAGE = BooleanPermissionType.of(true);
    public static final Permission<Boolean> EDIT_DISPLAY = BooleanPermissionType.of(true);
    public static final Permission<Boolean> EDIT_TRADES = BooleanPermissionType.of(true);
    public static final Permission<Boolean> ADD_REMOVE_ALLIES = BooleanPermissionType.of(false);
    public static final Permission<Boolean> EDIT_ALLY_PERMS = BooleanPermissionType.of(false);
    public static final Permission<TriStatePermission> BREAK_TRADER = EnumPermissionType.INSTANCE.create(TriStatePermission.NONE);
    public static final Permission<Boolean> COLLECT_MONEY = BooleanPermissionType.of(false);
    public static final Permission<Boolean> STORE_MONEY = BooleanPermissionType.of(false);
    public static final Permission<Boolean> EDIT_TRADE_RULES = BooleanPermissionType.of(true);
    public static final Permission<Boolean> EDIT_SETTINGS = BooleanPermissionType.of(true);
    public static final Permission<Boolean> TRANSFER_OWNERSHIP = BooleanPermissionType.of(false);
    public static final Permission<TriStatePermission> VIEW_LOGS = EnumPermissionType.INSTANCE.create(TriStatePermission.NONE);
    public static final Permission<Boolean> EXTERNAL_ACCESS_SETTINGS = BooleanPermissionType.of(true);

}