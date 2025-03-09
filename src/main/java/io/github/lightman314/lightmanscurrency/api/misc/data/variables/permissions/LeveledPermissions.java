package io.github.lightman314.lightmanscurrency.api.misc.data.variables.permissions;

import io.github.lightman314.lightmanscurrency.api.misc.IPermissions;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class LeveledPermissions {

    public static IVariablePermission hasPermission(String permission) { return (player,host) -> {
        if(host instanceof IPermissions p)
            return p.hasPermission(player,permission);
        return false;
    };
    }

    public static IVariablePermission minPermission(String permission,int minLevel) { return (player,host) -> {
        if(host instanceof IPermissions p)
            return p.getPermissionLevel(player,permission) >= minLevel;
        return false;
    };
    }

    public static IVariablePermission exactPermission(String permission,int exactLevel) { return (player,host) -> {
        if(host instanceof IPermissions p)
            return p.getPermissionLevel(player,permission) == exactLevel;
        return false;
    };
    }

}