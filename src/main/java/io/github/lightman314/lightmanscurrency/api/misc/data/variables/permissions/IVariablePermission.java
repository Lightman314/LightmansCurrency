package io.github.lightman314.lightmanscurrency.api.misc.data.variables.permissions;

import io.github.lightman314.lightmanscurrency.api.misc.data.variables.IVariableHost;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IVariablePermission {

    boolean canEdit(Player player, IVariableHost host);

    default IVariablePermission and(IVariablePermission other) { return CombinedPermission.and(this,other); }
    default IVariablePermission or(IVariablePermission other) { return CombinedPermission.or(this,other); }

    static IVariablePermission noTest() { return (player,host) -> true; }
    static IVariablePermission membersOnly() { return OwnerPermissions.MEMBER; }
    static IVariablePermission adminsOnly() { return OwnerPermissions.ADMIN; }

    static IVariablePermission hasPermission(String permission) { return LeveledPermissions.hasPermission(permission); }
    static IVariablePermission minPermission(String permission,int minLevel) { return LeveledPermissions.minPermission(permission,minLevel); }
    static IVariablePermission exactPermission(String permission,int exactLevel) { return LeveledPermissions.exactPermission(permission,exactLevel); }

}
