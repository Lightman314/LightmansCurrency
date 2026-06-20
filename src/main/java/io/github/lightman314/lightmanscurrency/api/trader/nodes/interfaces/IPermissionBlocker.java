package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;

public interface IPermissionBlocker {

    boolean blockPermission(Permission<?> permission);

}