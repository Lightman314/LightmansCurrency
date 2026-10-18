package io.github.lightman314.lightmanscurrency.api.trader.client.permissions.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IPositionalWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.trader.client.permissions.ClientPermission;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.PermissionValue;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClientIntegerPermission extends ClientPermission {

    private ClientIntegerPermission() {}

    public static final ClientIntegerPermission INSTANCE = new ClientIntegerPermission();

    @Override
    public int addWidget(Supplier<PermissionValue<?>> currentValue, Consumer<Object> requestChange, Permission<?> permission, IPositionalWidgetHolder holder, int width, int startY) {
        Supplier<Integer> getter = this.valueGetter(currentValue,permission);
        holder.addChild(ScreenPosition.of(0,startY), TextBoxWrapper.intBuilder()
                .withMaxLength(String.valueOf(this.castValue(permission.getMaxValue())).length())
                .withHandler(requestChange::accept)
                .ofWidth(60)
                .withStartingValue(getter.get())
                .build());
        this.addLabel(holder,permission,62,startY + 6,width);
        return 20;
    }

    private int castValue(Object value) {
        if(value instanceof Number n)
            return n.intValue();
        return 0;
    }

    private Supplier<Integer> valueGetter(Supplier<PermissionValue<?>> currentValue,Permission<?> permission) {
        return () -> {
            PermissionValue<?> value = currentValue.get();
            Object v = value == null ? null : value.get();
            if(v instanceof Number n)
                return n.intValue();
            return castValue(permission.createNew().get());
        };
    }

}