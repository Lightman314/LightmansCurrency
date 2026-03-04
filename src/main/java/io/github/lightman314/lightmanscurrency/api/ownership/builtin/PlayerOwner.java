package io.github.lightman314.lightmanscurrency.api.ownership.builtin;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class PlayerOwner extends Owner {

    public static final OwnerType<PlayerOwner> TYPE = new Type();

    public final PlayerReference player;
    private PlayerOwner(PlayerReference player) { this.player = player; }

    public static PlayerOwner of(Player player) { return of(PlayerReference.of(player)); }
    public static PlayerOwner of(PlayerReference player) { return new PlayerOwner(player); }

    @Override
    public Component getName() { return this.player.getNameComponent(this.isClient()); }
    
    @Override
    public Component getCommandLabel() { return LCText.COMMAND_LCADMIN_DATA_OWNER_PLAYER.get(this.player.getName(false), this.player.id.toString()); }

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
    public boolean isAdmin(PlayerReference player) { return this.player.is(player); }
    @Override
    public boolean isMember(PlayerReference player) { return this.isAdmin(player); }

    
    @Override
    public PlayerReference asPlayerReference() { return this.player; }

    @Nullable
    @Override
    public BankReference asBankReference() { return PlayerBankReference.of(this.player).flagAsClient(this); }

    @Override
    public void pushNotification(Supplier<? extends Notification> notificationSource, int notificationLevel, boolean sendToChat) { NotificationAPI.getApi().PushPlayerNotification(this.player.id, notificationSource.get(), sendToChat); }

    @Override
    public OwnerType<?> getType() { return TYPE; }
    
    @Override
    public Owner copy() { return new PlayerOwner(this.player); }

    @Override
    public boolean matches(Owner other) { return other instanceof PlayerOwner po && po.player.is(this.player); }

    @Override
    public int hash() { return this.player.hashCode(); }

    private static class Type extends OwnerType<PlayerOwner>
    {
        private static final MapCodec<PlayerOwner> MAP_CODEC = PlayerReference.CODEC.fieldOf("player")
                .xmap(PlayerOwner::new,o -> o.player);
        private static final StreamCodec<FriendlyByteBuf,PlayerOwner> STREAM_CODEC = PlayerReference.STREAM_CODEC
                .map(PlayerOwner::new,o -> o.player);

        @Override
        public Owner loadOldData(CompoundTag tag, HolderLookup.Provider lookup) { return of(PlayerReference.load(tag.getCompound("Player"))); }
        @Override
        public MapCodec<PlayerOwner> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<FriendlyByteBuf,PlayerOwner> streamCodec() { return STREAM_CODEC; }
    }

}
