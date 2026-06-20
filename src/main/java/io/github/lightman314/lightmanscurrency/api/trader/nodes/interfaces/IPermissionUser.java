package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;

import java.util.function.Consumer;

public interface IPermissionUser {

    void addDefaultAllyPermission(Consumer<Permission<?>> handler);

}