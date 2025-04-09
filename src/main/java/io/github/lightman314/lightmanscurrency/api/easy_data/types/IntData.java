package io.github.lightman314.lightmanscurrency.api.easy_data.types;

import io.github.lightman314.lightmanscurrency.api.easy_data.EasyData;
import io.github.lightman314.lightmanscurrency.api.easy_data.EasyDataSettings;
import io.github.lightman314.lightmanscurrency.api.easy_data.ReadWriteContext;
import io.github.lightman314.lightmanscurrency.api.easy_data.util.NotificationReplacer;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class IntData extends EasyData<Integer> {

    private int value;
    private final int minValue;
    private final int maxValue;
    private IntData(EasyDataSettings<Integer> builder, int defaultValue, int minValue, int maxValue)
    {
        super(builder);
        this.value = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public static EasyDataSettings.Builder<Integer,IntData> builder() { return builder(0); }
    public static EasyDataSettings.Builder<Integer,IntData> builder(int defaultValue) { return builder(0, Integer.MIN_VALUE, Integer.MAX_VALUE); }
    public static EasyDataSettings.Builder<Integer,IntData> builder(int defaultValue, int minValue, int maxValue) { return EasyDataSettings.builder(b -> new IntData(b,defaultValue,minValue,maxValue)); }

    @Override
    protected void write(ReadWriteContext context, String tagKey) {
        context.tag.putInt(tagKey,this.value);
    }

    @Override
    protected void read(ReadWriteContext context, String tagKey) {
        this.value = MathUtil.clamp(context.tag.getInt(tagKey),this.minValue,this.maxValue);
    }

    @Override
    public Integer get() { return this.value; }

    @Override
    protected void setInternal(Integer newValue) { this.value = MathUtil.clamp(newValue,this.minValue,this.maxValue); }

    @Nullable
    @Override
    protected Notification change(PlayerReference player, Integer newValue) {
        newValue = MathUtil.clamp(newValue,this.minValue,this.maxValue);
        if(newValue == this.value)
            return null;
        int oldValue = this.value;
        this.value = newValue;
        return ChangeSettingNotification.advanced(player,this.settings.dataName,this.value,oldValue);
    }

    public static final NotificationReplacer<Integer> SIMPLE_NOTIFICATION = new NotificationReplacer<>() {
        @Override
        public Notification replaceNotification(@Nonnull Integer oldValue, @Nonnull Integer newValue, @Nonnull PlayerReference player, @Nonnull EasyDataSettings<Integer> settings, @Nullable Notification originalNotification) {
            return ChangeSettingNotification.simple(player,settings.dataName,newValue);
        }
    };

}