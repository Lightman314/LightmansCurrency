package io.github.lightman314.lightmanscurrency.api.easy_data.types;

import io.github.lightman314.lightmanscurrency.api.easy_data.EasyData;
import io.github.lightman314.lightmanscurrency.api.easy_data.EasyDataSettings;
import io.github.lightman314.lightmanscurrency.api.easy_data.ReadWriteContext;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EnumData<T extends Enum<T>> extends EasyData<T> {

    private T value;
    private final T defaultValue;
    private final T[] allValues;
    private final Function<T,Component> nameGetter;
    private EnumData(EasyDataSettings<T> builder, T defaultValue, @Nullable Function<T,Component> nameGetter) {
        super(builder);
        this.value = this.defaultValue = defaultValue;
        this.allValues = this.defaultValue.getDeclaringClass().getEnumConstants();
        this.nameGetter = nameGetter;
    }

    public static <A extends Enum<A>> EasyDataSettings.Builder<A,EnumData<A>> builder(A defaultValue) { return builder(defaultValue,null); }
    public static <A extends Enum<A>> EasyDataSettings.Builder<A,EnumData<A>> builder(A defaultValue, @Nullable Function<A,Component> nameGetter) { return EasyDataSettings.builder(b -> new EnumData<>(b,defaultValue,nameGetter)); }

    @Override
    protected void write(ReadWriteContext context, String tagKey) {
        context.tag.putString(tagKey,this.value.toString());
    }

    @Override
    protected void read(ReadWriteContext context, String tagKey) {
        this.value = EnumUtil.enumFromString(context.tag.getString(tagKey),this.allValues,this.defaultValue);
    }

    @Override
    public T get() { return this.value; }
    @Override
    protected void setInternal(T newValue) { this.value = newValue; }

    @Nullable
    @Override
    protected Notification change(PlayerReference player, T newValue) {
        if(this.value == newValue)
            return null;
        T oldValue = this.value;
        this.value = newValue;
        if(player == null)
            return null;
        return ChangeSettingNotification.advanced(player,this.settings.dataName,this.nameGetter.apply(this.value),this.nameGetter.apply(oldValue));
    }
}