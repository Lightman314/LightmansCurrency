package io.github.lightman314.lightmanscurrency.api.ownership;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;

import javax.annotation.Nullable;
import java.util.Objects;

public abstract class Owner implements ISidedContext.Mutable<Owner> {

    public static final Codec<Owner> CODEC = LCRegistries.Ownership.OWNER_TYPE.byNameCodec()
            .dispatch(Owner::getType,OwnerType::codec);
    public static final StreamCodec<RegistryFriendlyByteBuf,Owner> STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.Ownership.OWNER_TYPE_KEY)
            .dispatch(Owner::getType,OwnerType::streamCodec);

    public static Owner getNull() { return new NullOwner(); }
    public static Owner getNull(ISidedContext parent) {
        Owner owner = getNull();
        owner.setSidedContext(parent);
        return owner;
    }
    public static final OwnerType<Owner> NULL_TYPE = new NullType();


    private ISidedContext parent = null;
    @Override
    public final boolean isClient() { return this.parent == null || this.parent.isClient(); }
    @Override
    public final Owner setSidedContext(ISidedContext context) { this.parent = context; return this; }

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

    /*/**
     * Pushes notifications to all players relevant to this owner.
     * @param notificationSource A notification generator, so that each player receives a unique instance of the notification.
     * @param notificationLevel The notification level. Determines who should receive the notification.<br>
     *                          0: All Members should receive the notification.
     *                          1: Only Admins should receive the notification.
     *                          2: Only the owner should receive the notification.
     */
    /*public abstract void pushNotification(Supplier<? extends Notification> notificationSource,MemberLevel targets, boolean sendToChat);

    public <T> void incrementStat(StatKey<?,T> key, T addValue) {}*/

    public abstract OwnerType<?> getType();

    public final CompoundTag save(HolderLookup.Provider lookup) { return (CompoundTag)CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE,lookup),this).getOrThrow(); }

    public static Owner load(CompoundTag tag, HolderLookup.Provider lookup) { return CODEC.decode(RegistryOps.create(NbtOps.INSTANCE,lookup),tag).getOrThrow().getFirst(); }

    public abstract Owner copy();

    public final Owner copyWithContext(ISidedContext context) { return this.copy().setSidedContext(context); }

    @Override
    public final boolean equals(Object obj) {
        if(obj instanceof Owner o)
            return this.matches(o);
        return false;
    }

    public abstract boolean matches(Owner other);

    public abstract int hash();

    @Override
    public final int hashCode() { return Objects.hash(LCRegistries.Ownership.OWNER_TYPE.getKey(this.getType()),this.hash()); }

    private static class NullOwner extends Owner {

        @Override
        public Component getName() { return LCText.Ownership.GUI_OWNER_NULL.get(); }
        @Override
        public Component getCommandLabel() { return LCText.Ownership.COMMAND_OWNER_LABEL_CUSTOM.get(this.getName()); }
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
        //@Override
        //public void pushNotification(Supplier<? extends Notification> notificationSource, int notificationLevel, boolean sendToChat) { }
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
        private static final StreamCodec<ByteBuf,Owner> STREAM_CODEC = StreamHelper.uncheckedUnit(Owner::getNull);

        @Override
        public MapCodec<Owner> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, Owner> streamCodec() { return STREAM_CODEC; }
    }


}