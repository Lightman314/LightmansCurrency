package io.github.lightman314.lightmanscurrency.api.ownership.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class PlayerOwner extends Owner {

    public static final OwnerType TYPE = OwnerType.create(ResourceLocation.fromNamespaceAndPath(MoneyAPI.MODID,"player"),
            (tag,lookup) -> of(PlayerReference.load(tag.getCompound("Player"))));

    public final PlayerReference player;
    private PlayerOwner(@Nonnull PlayerReference player) { this.player = player; }

    @Nonnull
    public static PlayerOwner of(@Nonnull Player player) { return of(PlayerReference.of(player)); }
    @Nonnull
    public static PlayerOwner of(@Nonnull PlayerReference player) { return new PlayerOwner(player); }

    @Nonnull
    @Override
    public MutableComponent getName() { return this.player.getNameComponent(this.isClient()); }

    @Nonnull
    @Override
    public MutableComponent getCommandLabel() { return LCText.COMMAND_LCADMIN_DATA_OWNER_PLAYER.get(this.player.getName(false), this.player.id.toString()); }

    @Override
    public boolean stillValid() { return true; }
    @Override
    public boolean alwaysValid() { return true; }

    @Override
    public boolean isOnline() {
        if(this.isClient())
            return false;
        return this.player.isOnline();
    }

    @Override
    public boolean isAdmin(@Nonnull PlayerReference player) { return this.player.is(player); }
    @Override
    public boolean isMember(@Nonnull PlayerReference player) { return this.isAdmin(player); }

    @Nonnull
    @Override
    public PlayerReference asPlayerReference() { return this.player; }

    @Nullable
    @Override
    public BankReference asBankReference() { return PlayerBankReference.of(this.player).flagAsClient(this.isClient()); }

    @Override
    public void pushNotification(@Nonnull Supplier<? extends Notification> notificationSource, int notificationLevel, boolean sendToChat) { NotificationAPI.PushPlayerNotification(this.player.id, notificationSource.get(), sendToChat); }

    @Nonnull
    @Override
    public OwnerType getType() { return TYPE; }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup) { tag.put("Player", this.player.save()); }

    @Override
    public boolean matches(@Nonnull Owner other) { return other instanceof PlayerOwner po && po.player.is(this.player); }

}
