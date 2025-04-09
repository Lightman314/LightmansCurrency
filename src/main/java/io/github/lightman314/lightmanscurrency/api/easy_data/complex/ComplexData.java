package io.github.lightman314.lightmanscurrency.api.easy_data.complex;

import io.github.lightman314.lightmanscurrency.api.easy_data.EasyData;
import io.github.lightman314.lightmanscurrency.api.easy_data.EasyDataSettings;
import io.github.lightman314.lightmanscurrency.api.easy_data.ReadWriteContext;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeSettingNotification;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ComplexData<T> extends EasyData<T> {

    private final Function<T,CompoundTag> writer;
    private final Function<CompoundTag,T> reader;
    public ComplexData(EasyDataSettings<T> settings, Function<T,CompoundTag> writer, Function<CompoundTag,T> reader) {
        super(settings);
        this.writer = writer;
        this.reader = reader;
    }

    @Override
    protected final void write(ReadWriteContext context, String tagKey) { context.tag.put(tagKey,this.writer.apply(this.get())); }

    @Override
    protected final void read(ReadWriteContext context, String tagKey) { this.setInternal(this.reader.apply(context.tag.getCompound(tagKey))); }

    @Nullable
    protected Notification getChangeNotification(PlayerReference player,T oldValue,T newValue) { return ChangeSettingNotification.dumb(player,this.settings.dataName); }

    @Nullable
    @Override
    protected Notification change(PlayerReference player, T newValue) {
        T oldValue = this.get();
        this.setInternal(newValue);
        return this.getChangeNotification(player, oldValue,newValue);
    }

}