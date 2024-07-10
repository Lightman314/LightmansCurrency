package io.github.lightman314.lightmanscurrency.api.ownership.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class FakeOwner extends Owner {

    public static final OwnerType TYPE = OwnerType.create(ResourceLocation.fromNamespaceAndPath(MoneyAPI.MODID,"fake"),
            (tag,lookup) -> of(Component.Serializer.fromJson(tag.getString("Name"),lookup)));

    private final MutableComponent name;
    private FakeOwner(@Nonnull MutableComponent name) { this.name = name; }
    @Nonnull
    public static FakeOwner of(@Nonnull String name) { return of(EasyText.literal(name)); }
    @Nonnull
    public static FakeOwner of(@Nonnull MutableComponent name) { return new FakeOwner(name); }

    @Nonnull
    @Override
    public MutableComponent getName() { return this.name.copy(); }

    @Nonnull
    @Override
    public MutableComponent getCommandLabel() { return LCText.COMMAND_LCADMIN_DATA_OWNER_CUSTOM.get(this.getName()); }

    @Override
    public boolean stillValid() { return true; }
    @Override
    public boolean alwaysValid() { return true; }

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
    public void pushNotification(@Nonnull Supplier<? extends Notification> notificationSource, int notificationLevel, boolean sendToChat) { }

    @Nonnull
    @Override
    public OwnerType getType() { return TYPE; }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup) { tag.putString("Name", Component.Serializer.toJson(this.name,lookup)); }

    @Nonnull
    @Override
    public Owner copy() { return new FakeOwner(this.name); }

    @Override
    public boolean matches(@Nonnull Owner other) { return other instanceof FakeOwner fo && fo.name.equals(this.name); }

}
