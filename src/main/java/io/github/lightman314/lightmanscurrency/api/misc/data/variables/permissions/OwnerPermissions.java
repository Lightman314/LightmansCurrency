package io.github.lightman314.lightmanscurrency.api.misc.data.variables.permissions;

import io.github.lightman314.lightmanscurrency.api.ownership.IOwnable;

public class OwnerPermissions {

    public static final IVariablePermission MEMBER = (player,host) -> {
        if(host instanceof IOwnable ownable)
            return ownable.getOwner().isMember(player);
        return false;
    };

    public static final IVariablePermission ADMIN = (player,host) -> {
        if(host instanceof IOwnable ownable)
            return ownable.getOwner().isAdmin(player);
        return false;
    };

}