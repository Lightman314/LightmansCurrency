package io.github.lightman314.lightmanscurrency.api.easy_data;

import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
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
    public final EasyDataSettings<T> settings;
    public EasyData(EasyDataSettings<T> builder) {
        this.settings = builder;
        this.settings.host.registerData(this);
    }

    public final void write(CompoundTag tag, HolderLookup.Provider lookup) { this.write(ReadWriteContext.of(tag,lookup),this.settings.tagKey); }
    protected abstract void write(ReadWriteContext context,String tagKey);

    public final void read(CompoundTag tag, HolderLookup.Provider lookup) {
        if(tag.contains(this.settings.tagKey))
            this.read(ReadWriteContext.of(tag,lookup),this.settings.tagKey);
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
        if(this.settings.category.canEdit(player,this.settings.host) && !this.get().equals(newValue))
        {
            T oldValue = this.get();
            PlayerReference pr = PlayerReference.of(player);
            Notification notification = this.change(pr,newValue);
            //Apply third-party notification changes
            notification = this.settings.filterNotification(oldValue,this.get(),pr,notification);
            this.setChanged();
            //Push notification
            if(notification != null)
            {
                Consumer<Notification> consumer = this.settings.host.dataChangeNotifier();
                if(consumer != null)
                    consumer.accept(notification);
            }
        }
    }

    private void setChanged() {
        T val = this.get();
        for(Consumer<T> listener : new ArrayList<>(this.changeListeners))
            listener.accept(val);
        this.settings.host.onDataChanged(this);
    }

    public final void addListener(Consumer<T> listener)
    {
        if(this.changeListeners.contains(listener))
            return;
        this.changeListeners.add(listener);
    }

    public final void removeListener(Consumer<T> listener) { this.changeListeners.remove(listener); }

}
