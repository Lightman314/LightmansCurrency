package io.github.lightman314.lightmanscurrency.api.ownership;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.stats.StatKey;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Owner implements IClientTracker {

    public static Owner getNull() { return new NullOwner(); }
    public static Owner getNull(@Nonnull IClientTracker parent) {
        Owner owner = getNull();
        owner.setParent(parent);
        return owner;
    }
    public static final OwnerType NULL_TYPE = OwnerType.create(new ResourceLocation(LightmansCurrency.MODID,"null"), (t) -> getNull());

    private IClientTracker parent = null;
    @Override
    public final boolean isClient() { return this.parent == null || this.parent.isClient(); }
    public final void setParent(@Nonnull IClientTracker parent) { this.parent = parent; }


    @Nonnull
    public abstract MutableComponent getName();
    @Nonnull
    public abstract MutableComponent getCommandLabel();

    /**
     * Whether this owner is still valid/exists.
     * @return  if this owner still exists.<br>
     * <code>false</code> if this owner has somehow been deleted (such as a team that's been disbanded, etc.)
     */
    public abstract boolean stillValid();

    /**
     * Whether this owner will always be valid/exist.
     * @return <code>true</code> if this owner cannot be deleted in any way, shape, and/or form (i.e. a direct player reference).<br>
     * <code></code>
     */
    public boolean alwaysValid() { return false; }

    public final boolean isNull() { return this instanceof NullOwner; }

    public abstract boolean isOnline();
    public abstract boolean isAdmin(@Nonnull PlayerReference player);
    public abstract boolean isMember(@Nonnull PlayerReference player);

    @Nonnull
    public abstract PlayerReference asPlayerReference();
    @Nullable
    public abstract BankReference asBankReference();

    public boolean hasNotificationLevels() { return false; }

    @Nonnull
    public static MutableComponent getOwnerLevelBlurb(int notificationLevel) {
        return switch (notificationLevel) {
            case 0 -> LCText.BLURB_OWNERSHIP_MEMBERS.get();
            case 1 -> LCText.BLURB_OWNERSHIP_ADMINS.get();
            default -> LCText.BLURB_OWNERSHIP_OWNER.get();
        };
    }

    public static int validateNotificationLevel(int level) { return level % 3; }

    /**
     * Pushes notifications to all players relevant to this owner.
     * @param notificationSource A notification generator, so that each player receives a unique instance of the notification.
     * @param notificationLevel The notification level. Determines who should receive the notification.<br>
     *                          0: All Members should receive the notification.
     *                          1: Only Admins should receive the notification.
     *                          2: Only the owner should receive the notification.
     */
    public abstract void pushNotification(@Nonnull NonNullSupplier<? extends Notification> notificationSource, int notificationLevel, boolean sendToChat);

    public <T> void incrementStat(@Nonnull StatKey<?,T> key, @Nonnull T addValue) {}

    @Nonnull
    public abstract OwnerType getType();

    @Nonnull
    public final CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", this.getType().getID().toString());
        this.saveAdditional(tag);
        return tag;
    }

    protected abstract void saveAdditional(@Nonnull CompoundTag tag);

    @Nullable
    public static Owner load(@Nonnull CompoundTag tag)
    {
        ResourceLocation id = new ResourceLocation(tag.getString("Type"));
        OwnerType type = OwnershipAPI.API.getOwnerType(id);
        if(type != null)
            return type.load(tag);
        LightmansCurrency.LogError("No owner type " + id + " is registered!\nCould not load the owner!");
        return null;
    }

    public abstract Owner copy();

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Owner o)
            return this.matches(o);
        return false;
    }

    public abstract boolean matches(@Nonnull Owner other);

    private static class NullOwner extends Owner {
        @Nonnull
        @Override
        public MutableComponent getName() { return LCText.GUI_OWNER_NULL.get(); }
        @Nonnull
        @Override
        public MutableComponent getCommandLabel() { return LCText.COMMAND_LCADMIN_DATA_OWNER_CUSTOM.get(this.getName()); }
        @Override
        public boolean stillValid() { return false; }
        @Override
        public boolean isOnline() { return false; }
        @Override
        public boolean isAdmin(@Nonnull PlayerReference player) { return false; }
        @Override
        public boolean isMember(@Nonnull PlayerReference player) { return false; }
        @Nonnull
        @Override
        public PlayerReference asPlayerReference() { return PlayerReference.NULL; }
        @Nullable
        @Override
        public BankReference asBankReference() { return null; }
        @Override
        public void pushNotification(@Nonnull NonNullSupplier<? extends Notification> notificationSource, int notificationLevel, boolean sendToChat) { }
        @Nonnull
        @Override
        public OwnerType getType() { return NULL_TYPE; }
        @Override
        protected void saveAdditional(@Nonnull CompoundTag tag) { }
        @Nonnull
        @Override
        public Owner copy() { return getNull(); }
        @Override
        public boolean matches(@Nonnull Owner other) { return other.isNull(); }
    }

}
