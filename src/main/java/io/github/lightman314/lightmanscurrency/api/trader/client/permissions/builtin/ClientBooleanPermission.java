package io.github.lightman314.lightmanscurrency.api.trader.client.permissions.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.SpriteButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IPositionalWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.trader.client.permissions.ClientPermission;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.PermissionValue;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClientBooleanPermission extends ClientPermission {

    private ClientBooleanPermission() {}

    public static final ClientBooleanPermission INSTANCE = new ClientBooleanPermission();

    @Override
    public int addWidget(Supplier<PermissionValue<?>> currentValue,Consumer<Object> requestChange,Permission<?> permission,IPositionalWidgetHolder holder,int width,int startY) {
        BooleanSupplier getter = getValue(currentValue);
        holder.addChild(ScreenPosition.of(0,startY), SpriteButton.builder()
                .withSprite(LCSprites.CHECKBOX.buildSprite(getter))
                .onPress(() -> requestChange.accept(!getter.getAsBoolean()))
                .build());
        this.addLabel(holder,permission,12,startY + 1,width);
        return 10;
    }

    private BooleanSupplier getValue(Supplier<PermissionValue<?>> currentValue) {
        return () -> {
            PermissionValue<?> v = currentValue.get();
            if(v == null)
                return false;
            return v.get().equals(true);
        };
    }

}