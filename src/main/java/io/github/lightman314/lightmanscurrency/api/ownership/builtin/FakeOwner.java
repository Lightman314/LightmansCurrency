package io.github.lightman314.lightmanscurrency.api.ownership.builtin;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class FakeOwner extends Owner {

    public static final OwnerType<FakeOwner> TYPE = new Type();

    private final Component name;
    private FakeOwner(Component name) { this.name = name; }
    
    public static FakeOwner of(String name) { return of(EasyText.literal(name)); }
    public static FakeOwner of(Component name) { return new FakeOwner(name); }

    @Override
    public Component getName() { return this.name.copy(); }
    @Override
    public Component getCommandLabel() { return LCText.COMMAND_LCADMIN_DATA_OWNER_CUSTOM.get(this.getName()); }

    @Override
    public boolean stillValid() { return true; }
    @Override
    public boolean alwaysValid() { return true; }

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
    public OwnerType<?> getType() { return TYPE; }
    
    @Override
    public Owner copy() { return new FakeOwner(this.name); }

    @Override
    public boolean matches(Owner other) { return other instanceof FakeOwner fo && fo.name.equals(this.name); }

    @Override
    public int hash() { return this.name.hashCode(); }

    private static class Type extends OwnerType<FakeOwner>
    {
        private static final MapCodec<FakeOwner> MAP_CODEC = ComponentSerialization.CODEC
                .fieldOf("name")
                .xmap(FakeOwner::of,FakeOwner::getName);
        private static final StreamCodec<RegistryFriendlyByteBuf,FakeOwner> STREAM_CODEC = ComponentSerialization.STREAM_CODEC
                .map(FakeOwner::of,FakeOwner::getName);

        @Override
        public Owner loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
            return new FakeOwner(Component.Serializer.fromJson(tag.getString("Name"),lookup));
        }
        @Override
        public MapCodec<FakeOwner> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, FakeOwner> streamCodec() { return STREAM_CODEC; }

    }



}
