package io.github.lightman314.lightmanscurrency.api.trader.notifications.settings;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.TraderSettingNotification;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.PermissionValue;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public class ChangeAllyPermissionNotification extends TraderSettingNotification.SingleLine {

    private static final MapCodec<ChangeAllyPermissionNotification> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            PlayerReference.CODEC.fieldOf("player").forGetter(n -> n.player),
            PermissionValue.CODEC.fieldOf("value").forGetter(n -> n.newValue)
    ).apply(builder,ChangeAllyPermissionNotification::new));
    private static final StreamCodec<RegistryFriendlyByteBuf,ChangeAllyPermissionNotification> STREAM_CODEC = StreamCodec.composite(
            PlayerReference.STREAM_CODEC,n -> n.player,
            PermissionValue.STREAM_CODEC,n -> n.newValue,
            ChangeAllyPermissionNotification::new);
    public static final NotificationType<ChangeAllyPermissionNotification> TYPE = new NotificationType<>(MAP_CODEC,STREAM_CODEC);

    public static final TextEntry TEXT = TextEntry.notification(TYPE);

    private final PlayerReference player;
    private final PermissionValue<?> newValue;

    private ChangeAllyPermissionNotification(PlayerReference player,PermissionValue<?> newValue) {
        this.player = player;
        this.newValue = newValue;
    }

    public static ChangeAllyPermissionNotification of(PlayerReference player,PermissionValue<?> value) { return new ChangeAllyPermissionNotification(player,value.copy()); }

    @Override
    protected Component getMessage() {
        return TEXT.get(this.player.getName(this),this.newValue.getName(),this.newValue.getValueText());
    }

    @Override
    public NotificationType<?> getType() { return TYPE; }

    @Override
    protected boolean equals(Notification other) {
        return other instanceof ChangeAllyPermissionNotification capn && capn.player.equals(this.player) && capn.newValue.equals(this.newValue);
    }

    @Override
    protected int hash() { return Objects.hash(this.player,this.newValue); }
}
