package io.github.lightman314.lightmanscurrency.api.trader.permissions;

public final class BuiltInPermissions {
    private BuiltInPermissions() {}

    public static void intialize() {}

    public static final Permission<Boolean> OPEN_STORAGE = BooleanPermissionType.of(true);
    public static final Permission<Boolean> EDIT_DISPLAY = BooleanPermissionType.of(true);
    public static final Permission<Boolean> ADD_REMOVE_ALLIES = BooleanPermissionType.of(false);
    public static final Permission<Boolean> EDIT_ALLY_PERMS = BooleanPermissionType.of(false);
    public static final Permission<Boolean> BREAK_TRADER = BooleanPermissionType.of(false);
    public static final Permission<Boolean> COLLECT_MONEY = BooleanPermissionType.of(false);
    public static final Permission<Boolean> STORE_MONEY = BooleanPermissionType.of(false);

}