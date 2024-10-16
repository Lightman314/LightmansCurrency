package io.github.lightman314.lightmanscurrency.api.ejection;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCRegistries;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.ISidedObject;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionSaveData;
import io.github.lightman314.lightmanscurrency.common.notifications.types.ejection.OwnableBlockEjectedNotification;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class EjectionData implements ISidedObject {

    private boolean isClient = false;

    @Override
    public boolean isClient() { return this.isClient; }
    @Nonnull
    @Override
    public EjectionData flagAsClient() { return this.flagAsClient(true); }
    @Nonnull
    @Override
    public EjectionData flagAsClient(boolean isClient) { this.isClient = isClient; return this; }
    @Nonnull
    @Override
    public Object flagAsClient(@Nonnull IClientTracker tracker) { return this.flagAsClient(tracker.isClient()); }

    public EjectionData() { }

    @Nonnull
    public abstract OwnerData getOwner();

    public final boolean canAccess(@Nonnull Player player) { return this.getOwner().isMember(player); }

    @Nonnull
    public abstract Component getName();

    @Nonnull
    public abstract EjectionDataType getType();

    @Nonnull
    public abstract Container getContents();

    public boolean isEmpty() { return this.getContents().isEmpty(); }

    public void setChanged() {
        if(this.isServer())
            EjectionSaveData.MarkEjectionDataDirty();
        this.onChanged();
    }

    protected void onChanged() {}

    @Nonnull
    public final CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        tag.putString("type",LCRegistries.EJECTION_DATA.getKey(this.getType()).toString());
        LightmansCurrency.LogDebug("Saved Ejection Data:\n" + tag.getAsString());
        return tag;
    }

    protected abstract void saveAdditional(@Nonnull CompoundTag tag);

    public boolean canSplit() { return false; }

    @Nonnull
    public IconData getSplitButtonIcon() { return IconUtil.ICON_X; }

    @Nonnull
    public List<Component> getSplitButtonTooltip() { return Lists.newArrayList(LCText.TOOLTIP_EJECTION_SPLIT_GENERIC.get()); }

    public void splitContents() { if(this.canSplit()) throw new NotImplementedException("Ejection Data flags itself as being able to be split, but does not override the split method!"); }

    public final void pushNotificationToOwner() {
        Owner owner = this.getOwner().getValidOwner();
        if(owner != null)
            owner.pushNotification(OwnableBlockEjectedNotification.create(this.getName()), 1, true);
    }

}