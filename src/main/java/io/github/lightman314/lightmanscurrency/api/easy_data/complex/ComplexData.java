package io.github.lightman314.lightmanscurrency.api.easy_data.complex;

import io.github.lightman314.lightmanscurrency.api.easy_data.EasyData;
import io.github.lightman314.lightmanscurrency.api.easy_data.EasyDataSettings;
import io.github.lightman314.lightmanscurrency.api.easy_data.ReadWriteContext;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeSettingNotification;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiFunction;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ComplexData<T> extends EasyData<T> {

    private final BiFunction<T,HolderLookup.Provider,CompoundTag> writer;
    private final BiFunction<CompoundTag,HolderLookup.Provider,T> reader;
    public ComplexData(EasyDataSettings<T> settings, BiFunction<T,HolderLookup.Provider,CompoundTag> writer, BiFunction<CompoundTag,HolderLookup.Provider,T> reader) {
        super(settings);
        this.writer = writer;
        this.reader = reader;
    }

    @Override
    protected final void write(ReadWriteContext context, String tagKey) { context.tag.put(tagKey,this.writer.apply(this.get(),context.lookup)); }

    @Override
    protected final void read(ReadWriteContext context, String tagKey) { this.set(this.reader.apply(context.tag.getCompound(tagKey),context.lookup)); }

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
