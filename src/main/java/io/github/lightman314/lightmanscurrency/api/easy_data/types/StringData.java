package io.github.lightman314.lightmanscurrency.api.easy_data.types;

import io.github.lightman314.lightmanscurrency.api.easy_data.EasyData;
import io.github.lightman314.lightmanscurrency.api.easy_data.EasyDataSettings;
import io.github.lightman314.lightmanscurrency.api.easy_data.ReadWriteContext;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeSettingNotification;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class StringData extends EasyData<String> {

    private String value;
    private final int maxLength;

    private StringData(EasyDataSettings<String> settings, String defaultValue, int maxLength) {
        super(settings);
        this.value = defaultValue;
        this.maxLength = maxLength;
    }

    public static EasyDataSettings.Builder<String,StringData> builder() { return builder(""); }
    public static EasyDataSettings.Builder<String,StringData> builder(String defaultValue) { return builder("",-1); }
    public static EasyDataSettings.Builder<String,StringData> builder(String defaultValue, int maxLength) { return EasyDataSettings.builder(b -> new StringData(b,defaultValue,maxLength)); }

    @Override
    protected void write(ReadWriteContext context, String tagKey) { context.tag.putString(tagKey,this.value); }

    @Override
    protected void read(ReadWriteContext context, String tagKey) { this.value = context.tag.getString(tagKey); }

    @Override
    public String get() { return this.value; }

    @Override
    protected void setInternal(String newValue) {
        if(this.maxLength > 0 && newValue.length() > this.maxLength)
            this.value = newValue.substring(0,this.maxLength);
        else
            this.value = newValue;
    }

    @Nullable
    @Override
    protected Notification change(PlayerReference player, String newValue) {
        this.set(newValue);
        return ChangeSettingNotification.simple(player,this.settings.dataName,this.value);
    }

}