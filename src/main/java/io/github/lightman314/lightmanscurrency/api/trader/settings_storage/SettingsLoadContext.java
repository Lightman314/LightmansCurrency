package io.github.lightman314.lightmanscurrency.api.trader.settings_storage;

import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionAccess;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.PermissionValue;

import java.util.List;
import java.util.Map;

public interface SettingsLoadContext extends IPermissionAccess {

    /**
     * Call after performing an action that may result in more sub-settings being added to the list of available settings<br>
     * Only results in said sub-settings being loaded correctly if they are added to the list <b>after</b> this one
     */
    void reloadPotentialSettings();

    interface Mutable extends SettingsLoadContext {
        void definePreviousOwner(Owner owner);
        void definePreviousPermissionMapMembers(List<PlayerReference> players);
        void definePreviousPermissionMap(Map<Permission<?>, PermissionValue<?>> permissionMap);
    }

}
