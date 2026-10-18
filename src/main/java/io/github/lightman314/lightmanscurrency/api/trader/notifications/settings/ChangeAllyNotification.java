package io.github.lightman314.lightmanscurrency.api.trader.notifications.settings;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.TraderSettingNotification;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;
import java.util.function.BiFunction;

public abstract sealed class ChangeAllyNotification extends TraderSettingNotification.SingleLine {

    private static <T extends ChangeAllyNotification> MapCodec<T> createCodec(BiFunction<PlayerReference,PlayerReference,T> factory) {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                PlayerReference.CODEC.fieldOf("player").forGetter(n -> n.player),
                PlayerReference.CODEC.fieldOf("ally").forGetter(n -> n.ally)
        ).apply(builder,factory));
    }
    private static <T extends ChangeAllyNotification> StreamCodec<FriendlyByteBuf,T> createStreamCodec(BiFunction<PlayerReference,PlayerReference,T> factory) {
        return StreamCodec.composite(
                PlayerReference.STREAM_CODEC,n -> n.player,
                PlayerReference.STREAM_CODEC,n -> n.ally,
                factory);
    }
    protected static <T extends ChangeAllyNotification> NotificationType<T> createType(BiFunction<PlayerReference,PlayerReference,T> factory) { return new NotificationType<>(createCodec(factory),createStreamCodec(factory)); }

    protected final PlayerReference player;
    protected final PlayerReference ally;
    protected ChangeAllyNotification(PlayerReference player,PlayerReference ally) {
        this.player = player;
        this.ally = ally;
    }

    public static ChangeAllyNotification added(PlayerReference player,PlayerReference ally) { return new AddAlly(player,ally); }
    public static ChangeAllyNotification removed(PlayerReference player,PlayerReference ally) { return new RemoveAlly(player,ally); }


    protected abstract TextEntry getText();

    @Override
    protected final Component getMessage() { return this.getText().get(this.player.getName(this),this.ally.getName(this)); }

    @Override
    protected boolean equals(Notification other) { return other instanceof ChangeAllyNotification can && other.getClass() == this.getClass() && can.player.equals(this.player) && can.ally.equals(this.ally); }

    @Override
    protected int hash() { return Objects.hash(this.player,this.ally); }

    public static final class AddAlly extends ChangeAllyNotification {

        public static final NotificationType<AddAlly> TYPE = createType(AddAlly::new);

        public static final TextEntry TEXT = TextEntry.notification(TYPE);

        private AddAlly(PlayerReference player, PlayerReference ally) { super(player, ally); }

        @Override
        protected TextEntry getText() { return TEXT; }
        @Override
        public NotificationType<?> getType() { return TYPE; }
    }

    public static final class RemoveAlly extends ChangeAllyNotification {

        public static final NotificationType<RemoveAlly> TYPE = createType(RemoveAlly::new);

        public static final TextEntry TEXT = TextEntry.notification(TYPE);

        private RemoveAlly(PlayerReference player, PlayerReference ally) { super(player, ally); }

        @Override
        protected TextEntry getText() { return TEXT; }
        @Override
        public NotificationType<?> getType() { return TYPE; }
    }

}
