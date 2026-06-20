package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;

public interface IPermissionSource {

    <T> T getPlayerPermission(PlayerReference player, Permission<T> permission);

}