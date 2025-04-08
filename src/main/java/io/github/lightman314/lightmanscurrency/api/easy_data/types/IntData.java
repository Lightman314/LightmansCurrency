package io.github.lightman314.lightmanscurrency.api.easy_data.types;

import io.github.lightman314.lightmanscurrency.api.easy_data.EasyData;
import io.github.lightman314.lightmanscurrency.api.easy_data.EasyDataKey;
import io.github.lightman314.lightmanscurrency.api.easy_data.IEasyDataHost;
import io.github.lightman314.lightmanscurrency.api.easy_data.ReadWriteContext;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class IntData extends EasyData<Integer> {

    private int value;
    private final int minValue;
    private final int maxValue;
    private final boolean simpleNotification;
    public IntData(EasyDataKey key, IEasyDataHost host, int defaultValue, int minValue, int maxValue, boolean simpleNotification)
    {
        super(key, host);
        this.value = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.simpleNotification = simpleNotification;
    }

    public static IntData of(EasyDataKey key, IEasyDataHost host) { return of(key,host,0); }
    public static IntData of(EasyDataKey key, IEasyDataHost host, int defaultValue) { return of(key,host,defaultValue,Integer.MIN_VALUE,Integer.MAX_VALUE); }
    public static IntData of(EasyDataKey key, IEasyDataHost host, int defaultValue, int minValue, int maxValue) { return of(key,host,defaultValue,minValue,maxValue,false); }
    public static IntData of(EasyDataKey key, IEasyDataHost host, int defaultValue, int minValue, int maxValue, boolean simpleNotification) { return new IntData(key,host,defaultValue,minValue,maxValue,simpleNotification); }

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
        if(this.simpleNotification)
            return ChangeSettingNotification.simple(player,this.key.dataName,this.value);
        else
            return ChangeSettingNotification.advanced(player,this.key.dataName,this.value,oldValue);
    }

}