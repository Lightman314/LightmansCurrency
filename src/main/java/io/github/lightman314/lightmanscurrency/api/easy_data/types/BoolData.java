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
public class BoolData extends EasyData<Boolean> {

    private boolean value;
    private BoolData(EasyDataSettings<Boolean> builder, boolean defaultValue) {
        super(builder);
        this.value = defaultValue;
    }

    public static EasyDataSettings.Builder<Boolean,BoolData> builder() { return builder(false); }
    public static EasyDataSettings.Builder<Boolean,BoolData> builder(boolean defaultValue) { return EasyDataSettings.builder(b -> new BoolData(b,defaultValue)); }

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
        return ChangeSettingNotification.simple(player,this.settings.dataName,newValue);
    }

}
