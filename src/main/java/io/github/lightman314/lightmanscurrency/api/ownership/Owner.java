package io.github.lightman314.lightmanscurrency.api.ownership;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.stats.StatKey;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class Owner implements IClientTracker {

    public static final Codec<Owner> CODEC = Codec.withAlternative(
            //Desired Codec
            OwnerType.CODEC.dispatch(Owner::getType,OwnerType::codec),
            CodecHelper.oldValueLoader(Owner::loadOld,"Owner Data"));
    public static final StreamCodec<RegistryFriendlyByteBuf,Owner> STREAM_CODEC = OwnerType.STREAM_CODEC
            .dispatch(Owner::getType,OwnerType::streamCodec);

    public static Owner getNull() { return new NullOwner(); }
    public static Owner getNull(IClientTracker parent) {
        Owner owner = getNull();
        owner.setParent(parent);
        return owner;
    }
    public static final OwnerType<Owner> NULL_TYPE = new NullType();


    private IClientTracker parent = null;
    @Override
    public final boolean isClient() { return this.parent == null || this.parent.isClient(); }
    public final void setParent(IClientTracker parent) { this.parent = parent; }

    public abstract Component getName();
    public abstract Component getCommandLabel();

    /**
     * Whether this owner is still valid/exists.
     * @return If this owner still exists.<br>
     * <code>false</code> if this owner has somehow been deleted (such as a team that's been disbanded, etc.)<br>
     * Should alway return <code>true</code> if {@link #alwaysValid()} returns <code>true</code>
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
    public abstract boolean isAdmin(PlayerReference player);
    public abstract boolean isMember(PlayerReference player);

    
    public abstract PlayerReference asPlayerReference();
    @Nullable
    public abstract BankReference asBankReference();

    public boolean hasNotificationLevels() { return false; }

    
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
    public abstract void pushNotification(Supplier<? extends Notification> notificationSource, int notificationLevel, boolean sendToChat);

    public <T> void incrementStat(StatKey<?,T> key, T addValue) {}

    public abstract OwnerType<?> getType();

    public final CompoundTag save(HolderLookup.Provider lookup) { return (CompoundTag)CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE,lookup),this).getOrThrow(); }

    public static Owner load(CompoundTag tag, HolderLookup.Provider lookup) { return CODEC.decode(RegistryOps.create(NbtOps.INSTANCE,lookup),tag).getOrThrow().getFirst(); }

    @Nullable
    private static Owner loadOld(CompoundTag tag, HolderLookup.Provider lookup)
    {
        ResourceLocation id = ResourceLocation.parse(tag.getString("Type"));
        OwnerType<?> type = LCRegistries.OWNER_TYPES.get(id);
        if(type != null)
            return type.loadOldData(tag,lookup);
        LightmansCurrency.LogError("No owner type " + id + " is registered!\nCould not load the owner!");
        return null;
    }
    
    public abstract Owner copy();

    public final Owner copyWithParent(IClientTracker newParent)
    {
        Owner newOwner = this.copy();
        newOwner.setParent(newParent);
        return newOwner;
    }

    @Override
    public final boolean equals(Object obj) {
        if(obj instanceof Owner o)
            return this.matches(o);
        return false;
    }

    public abstract boolean matches(Owner other);

    public abstract int hash();

    @Override
    public final int hashCode() { return Objects.hash(LCRegistries.OWNER_TYPES.getKey(this.getType()),this.hash()); }

    private static class NullOwner extends Owner {
        
        @Override
        public Component getName() { return LCText.GUI_OWNER_NULL.get(); }
        @Override
        public Component getCommandLabel() { return LCText.COMMAND_LCADMIN_DATA_OWNER_CUSTOM.get(this.getName()); }
        @Override
        public boolean stillValid() { return false; }
        @Override
        public boolean isOnline() { return false; }
        @Override
        public boolean isAdmin(PlayerReference player) { return false; }
        @Override
        public boolean isMember(PlayerReference player) { return false; }
        @Override
        public PlayerReference asPlayerReference() { return PlayerReference.NULL; }
        @Nullable
        @Override
        public BankReference asBankReference() { return null; }
        @Override
        public void pushNotification(Supplier<? extends Notification> notificationSource, int notificationLevel, boolean sendToChat) { }
        @Override
        public OwnerType<?> getType() { return NULL_TYPE; }
        @Override
        public Owner copy() { return getNull(); }
        @Override
        public boolean matches(Owner other) { return other.isNull(); }
        @Override
        public int hash() { return 0; }
    }

    private static class NullType extends OwnerType<Owner>
    {
        private static final MapCodec<Owner> MAP_CODEC = MapCodec.unit(Owner::getNull);
        private static final StreamCodec<ByteBuf,Owner> STREAM_CODEC = StreamHelper.unit(Owner::getNull);

        @Override
        public Owner loadOldData(CompoundTag tag, HolderLookup.Provider lookup) { return getNull(); }
        @Override
        public MapCodec<Owner> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, Owner> streamCodec() { return STREAM_CODEC; }
    }


}
