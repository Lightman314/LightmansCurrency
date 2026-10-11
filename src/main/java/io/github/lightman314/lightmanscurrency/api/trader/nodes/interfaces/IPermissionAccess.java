package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import net.minecraft.world.entity.player.Player;

public interface IPermissionAccess {

    /**
     * Whether the player has full admin-mode permissions to perform ultimate actions
     * @see io.github.lightman314.lightmanscurrency.api.LCApi#isInAdminMode(Player) LCApi.isInAdminMode(Player)
     */
    default boolean isAdminPlayer() { return false; }
    <T> T getPermission(Permission<T> permission);

}