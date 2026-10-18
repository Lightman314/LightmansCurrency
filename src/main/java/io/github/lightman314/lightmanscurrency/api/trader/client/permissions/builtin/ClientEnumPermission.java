package io.github.lightman314.lightmanscurrency.api.trader.client.permissions.builtin;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.TextButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IPositionalWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.trader.client.permissions.ClientPermission;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.PermissionValue;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.TriStatePermission;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.builtin.EnumPermissionType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ClientEnumPermission<T extends Enum<T> & StringRepresentable> extends ClientPermission {

    public static final ClientEnumPermission<TriStatePermission> TRI_STATE = new ClientEnumPermission<>(TriStatePermission.class);

    private final Class<T> clazz;
    public ClientEnumPermission(Class<T> clazz) { this.clazz = clazz; }

    @Override
    public int addWidget(Supplier<PermissionValue<?>> currentValue,Consumer<Object> requestChange,Permission<?> permission, IPositionalWidgetHolder holder, int width, int startY) {
        Supplier<T> getter = this.getter(currentValue);
        holder.addChild(ScreenPosition.of(0,startY), TextButton.builder()
                .withText(this.nameGetter(permission,getter))
                .ofWidth(80)
                .onPress(() -> {
                    T current = getter.get();
                    T next = EnumHelper.nextEnum(current);
                    LightmansCurrency.LogInfo("Cycling value from " + current + " to " + next);
                    requestChange.accept(next);
                })
                .onAltPress(() -> {
                    T current = getter.get();
                    T previous = EnumHelper.nextEnum(current);
                    LightmansCurrency.LogInfo("Cycling value from " + current + " to " + previous);
                    requestChange.accept(previous);
                })
                .build());
        this.addLabel(holder,permission,82,startY + 6,width);
        return 20;
    }

    private Supplier<T> getter(Supplier<PermissionValue<?>> currentValue) {
        return () -> {
            PermissionValue<?> value = currentValue.get();
            if(value == null)
                return this.clazz.getEnumConstants()[0];
            Object v = value.get();
            if(this.clazz.isInstance(v))
                return this.clazz.cast(v);
            return this.clazz.getEnumConstants()[0];
        };
    }

    private Supplier<Component> nameGetter(Permission<?> permission,Supplier<T> getter) {
        return () -> Component.translatable(EnumPermissionType.getEntryKey(permission,getter.get()));
    }

}