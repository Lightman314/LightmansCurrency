package io.github.lightman314.lightmanscurrency.api.easy_data;

import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
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
    public ComplexData(EasyDataKey key, IEasyDataHost host, Function<T,CompoundTag> writer, Function<CompoundTag,T> reader) {
        super(key, host);
        this.writer = writer;
        this.reader = reader;
    }

    @Override
    protected final void write(ReadWriteContext context, String tagKey) { context.tag.put(tagKey,this.writer.apply(this.get())); }

    @Override
    protected final void read(ReadWriteContext context, String tagKey) { this.set(this.reader.apply(context.tag.getCompound(tagKey))); }

    @Nullable
    protected Notification getChangeNotification(T oldValue,T newValue) { return null; }

    @Nullable
    @Override
    protected Notification change(PlayerReference player, T newValue) {
        T oldValue = this.get();
        this.setInternal(newValue);
        return this.getChangeNotification(oldValue,newValue);
    }

}