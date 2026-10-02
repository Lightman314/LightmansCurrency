package io.github.lightman314.lightmanscurrency.api.trader.client.permissions;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.client.ClientPairedRegistry;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IPositionalWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.PermissionType;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.PermissionValue;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ClientPermission {

    public static final ClientPairedRegistry<PermissionType<?>,ClientPermission> REGISTRY = ClientPairedRegistry.builder(LCRegistries.Trader.PERMISSION_TYPE,ClientPermission.class)
            .throwIfUndefined().build();

    public abstract int addWidget(Supplier<PermissionValue<?>> currentValue,Consumer<FancyPacketMap.Mutable> requestChange,IPositionalWidgetHolder holder,int width,int startY);

}