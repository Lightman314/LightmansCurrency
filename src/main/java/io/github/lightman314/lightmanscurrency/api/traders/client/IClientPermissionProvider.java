package io.github.lightman314.lightmanscurrency.api.traders.client;

import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.client.PermissionOption;

import java.util.List;

public interface IClientPermissionProvider {

    void addPermissionOptions(TraderData trader,PermissionOptionsBuilder builder);

    static List<PermissionOption> getPermissionOptions(TraderData trader)
    {
        //Create builder
        PermissionOptionsBuilder builder = new PermissionOptionsBuilder(trader);
        //Automatically add the "open storage" permission, as this is the only *truly global* permission
        builder.addSimple(Permissions.OPEN_STORAGE);
        //Add options from client attachments
        TraderClientHooks.forEach(trader,IClientPermissionProvider.class,attachment -> attachment.addPermissionOptions(trader,builder));
        return builder.getResult();
    }

}