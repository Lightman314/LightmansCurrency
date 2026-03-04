package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;

public interface IPermissionProvider {

    int getPermissionLevel(String permission,PlayerReference player);

}
