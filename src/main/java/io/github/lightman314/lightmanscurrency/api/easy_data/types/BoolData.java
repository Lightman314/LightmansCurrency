package io.github.lightman314.lightmanscurrency.api.easy_data.types;

import io.github.lightman314.lightmanscurrency.api.easy_data.EasyData;
import io.github.lightman314.lightmanscurrency.api.easy_data.EasyDataKey;
import io.github.lightman314.lightmanscurrency.api.easy_data.IEasyDataHost;
import io.github.lightman314.lightmanscurrency.api.easy_data.ReadWriteContext;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeSettingNotification;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BoolData extends EasyData<Boolean> {

    private boolean value;
    public BoolData(EasyDataKey key, IEasyDataHost host, boolean defaultValue) {
        super(key, host);
        this.value = defaultValue;
    }

    public static BoolData of(EasyDataKey key, IEasyDataHost host) { return of(key,host,false); }
    public static BoolData of(EasyDataKey key, IEasyDataHost host, boolean defaultValue) { return new BoolData(key,host,defaultValue); }

    @Override
    protected void write(ReadWriteContext context, String tagKey) { context.tag.putBoolean(tagKey,this.value); }

    @Override
    protected void read(ReadWriteContext context, String tagKey) { this.value = context.tag.getBoolean(tagKey); }

    @Override
    public Boolean get() { return this.value; }

    @Override
    protected void setInternal(Boolean newValue) { this.value = newValue; }

    @Nullable
    @Override
    protected Notification change(PlayerReference player, Boolean newValue) {
        if(this.value == newValue)
            return null;
        this.value = newValue;
        if(player == null)
            return null;
        return ChangeSettingNotification.simple(player,this.key.dataName,newValue);
    }

}
