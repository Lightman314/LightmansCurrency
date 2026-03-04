package io.github.lightman314.lightmanscurrency.api.traders.client;

import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.client.BooleanPermission;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.client.PermissionOption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionOptionsBuilder {

    private final TraderData trader;
    private final Map<String,PermissionOption> options = new HashMap<>();
    public PermissionOptionsBuilder(TraderData trader) { this.trader = trader; }

    public void add(PermissionOption option)
    {
        if(this.trader.isPermissionBlocked(option.permission))
            return;
        this.options.put(option.permission,option);
    }

    public void addSimple(String permission) { this.add(BooleanPermission.of(permission)); }

    public List<PermissionOption> getResult() { return new ArrayList<>(this.options.values()); }

}
