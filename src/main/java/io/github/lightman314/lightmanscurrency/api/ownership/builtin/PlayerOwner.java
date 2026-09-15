package io.github.lightman314.lightmanscurrency.api.ownership.builtin;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.ownership.MemberLevel;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerType;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.Objects;

public class PlayerOwner extends Owner {

    private static final MapCodec<PlayerOwner> MAP_CODEC = PlayerReference.CODEC.fieldOf("player")
            .xmap(PlayerOwner::new,o -> o.player);
    private static final StreamCodec<FriendlyByteBuf,PlayerOwner> STREAM_CODEC = PlayerReference.STREAM_CODEC
            .map(PlayerOwner::new,o -> o.player);

    public static final OwnerType<PlayerOwner> TYPE = new OwnerType<>(MAP_CODEC,STREAM_CODEC);

    public static final TextEntry TOOLTIP_OWNER_PLAYER = TextEntry.tooltip(LCApi.MODID,"ownership.player");
    public static final TextEntry COMMAND_OWNER_LABEL_PLAYER = TextEntry.command(LCApi.MODID,"owner.label.player");

    public final PlayerReference player;
    private PlayerOwner(PlayerReference player) { this.player = player; }

    public static PlayerOwner of(Player player) { return of(PlayerReference.of(player)); }
    public static PlayerOwner of(PlayerReference player) { return new PlayerOwner(Objects.requireNonNull(player)); }

    @Override
    public Component getName() { return this.player.getNameComponent(this); }

    @Override
    public Component getCommandLabel() { return COMMAND_OWNER_LABEL_PLAYER.get(this.player.getName(this),this.player.id.toString()); }

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
    public BankReference asBankReference() { return null;
        //return PlayerBankReference.of(this.player).flagAsClient(this); //TODO re-implement player bank accounts
    }

    @Override
    public void postNotification(Notification notification, MemberLevel targets, boolean sendToChat) { LCApi.getNotificationAPI().pushPlayerNotification(this.player.id,notification,sendToChat); }

    @Override
    public OwnerType<?> getType() { return TYPE; }

    @Override
    public Owner copy() { return new PlayerOwner(this.player); }

    @Override
    public boolean matches(Owner other) { return other instanceof PlayerOwner po && po.player.is(this.player); }

    @Override
    public int hash() { return this.player.hashCode(); }

}