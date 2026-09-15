package io.github.lightman314.lightmanscurrency.api.ownership.builtin;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.ownership.MemberLevel;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nullable;

public class FakeOwner extends Owner {

    private static final MapCodec<FakeOwner> MAP_CODEC = ComponentSerialization.CODEC
            .fieldOf("name")
            .xmap(FakeOwner::of,FakeOwner::getName);
    private static final StreamCodec<RegistryFriendlyByteBuf,FakeOwner> STREAM_CODEC = ComponentSerialization.STREAM_CODEC
            .map(FakeOwner::of,FakeOwner::getName);

    public static final OwnerType<FakeOwner> TYPE = new OwnerType<>(MAP_CODEC,STREAM_CODEC);

    private final Component name;
    private FakeOwner(Component name) { this.name = name; }

    public static FakeOwner of(String name) { return of(Component.literal(name)); }
    public static FakeOwner of(Component name) { return new FakeOwner(name); }

    @Override
    public Component getName() { return this.name.copy(); }
    @Override
    public Component getCommandLabel() { return COMMAND_OWNER_LABEL_CUSTOM.get(this.getName()); }

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
    public void postNotification(Notification notificationSource, MemberLevel targets, boolean sendToChat) { }

    @Override
    public OwnerType<?> getType() { return TYPE; }

    @Override
    public Owner copy() { return new FakeOwner(this.name); }

    @Override
    public boolean matches(Owner other) { return other instanceof FakeOwner fo && fo.name.equals(this.name); }
    @Override
    public int hash() { return this.name.hashCode(); }

}