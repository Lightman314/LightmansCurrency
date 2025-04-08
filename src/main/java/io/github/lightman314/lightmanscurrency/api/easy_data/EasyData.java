package io.github.lightman314.lightmanscurrency.api.easy_data;

import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class EasyData<T> {

    private final List<Consumer<T>> changeListeners = new ArrayList<>();
    public final EasyDataKey key;
    private final IEasyDataHost host;
    public EasyData(EasyDataKey key, IEasyDataHost host) {
        this.key = key;
        this.host = host;
        this.host.registerData(this);
    }

    public final void write(CompoundTag tag) { this.write(ReadWriteContext.of(tag),this.key.tagKey); }
    protected abstract void write(ReadWriteContext context,String tagKey);

    public final void read(CompoundTag tag) {
        if(tag.contains(this.key.tagKey))
            this.read(ReadWriteContext.of(tag),this.key.tagKey);
    }
    protected abstract void read(ReadWriteContext context,String tagKey);

    public abstract T get();
    public final void set(T newValue)
    {
        //Ignore if this is already the current value
        if(this.get().equals(newValue))
            return;
        this.setInternal(newValue);
        this.setChanged();
    }
    protected abstract void setInternal(T newValue);

    /**
     * Variant of {@link #setInternal(Object)} where you can optionally return a notification to be logged about the setting being changed.
     */
    @Nullable
    protected abstract Notification change(PlayerReference player, T newValue);
    public final void trySet(Player player, T newValue)
    {
        if(this.key.category.canEdit(player,this.host) && !this.get().equals(newValue))
        {
            Notification notification = this.change(PlayerReference.of(player),newValue);
            this.setChanged();
            //Push notification
            if(notification != null)
            {
                Consumer<Notification> consumer = this.host.dataChangeNotifier();
                if(consumer != null)
                    consumer.accept(notification);
            }
        }
    }

    private void setChanged() {
        T val = this.get();
        for(Consumer<T> listener : new ArrayList<>(this.changeListeners))
            listener.accept(val);
        this.host.onDataChanged(this);
    }

    public final void addListener(Consumer<T> listener)
    {
        if(this.changeListeners.contains(listener))
            return;
        this.changeListeners.add(listener);
    }

    public final void removeListener(Consumer<T> listener) { this.changeListeners.remove(listener); }

}