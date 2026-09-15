package io.github.lightman314.lightmanscurrency.api.trader.notifications.settings;

import com.mojang.datafixers.Products;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.codecs.partial.SPart2;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.TraderSettingNotification;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public abstract class ChangeSettingNotification extends TraderSettingNotification.SingleLine {

    protected final PlayerReference player;
    protected final String getPlayerName() { return this.player.getName(this); }
    protected final Component setting;

    protected ChangeSettingNotification(PlayerReference player,Component setting) {
        this.player = player;
        this.setting = setting;
    }

    public static ChangeSettingNotification dumb(PlayerReference player,Component setting) { return new Dumb(player,setting); }

    public static ChangeSettingNotification simple(PlayerReference player,Component setting,int newValue) { return simple(player,setting,String.valueOf(newValue)); }
    public static ChangeSettingNotification simple(PlayerReference player,Component setting,boolean newValue) { return simple(player,setting,LCText.GUI_SETTINGS_VALUE_TRUE_FALSE.getComponent(newValue)); }
    public static ChangeSettingNotification simple(PlayerReference player,Component setting,String newValue) { return simple(player,setting,Component.literal(newValue)); }
    public static ChangeSettingNotification simple(PlayerReference player,Component setting,Component newValue) { return new Simple(player,setting,newValue); }

    public static ChangeSettingNotification advanced(PlayerReference player,Component setting,int newValue,int oldValue) { return advanced(player,setting,String.valueOf(newValue),String.valueOf(oldValue)); }
    public static ChangeSettingNotification advanced(PlayerReference player,Component setting,String newValue,String oldValue) { return advanced(player,setting,Component.literal(newValue),Component.literal(oldValue)); }
    public static ChangeSettingNotification advanced(PlayerReference player,Component setting,Component newValue,Component oldValue) { return new Advanced(player,setting,newValue,oldValue); }

    protected final boolean baseEquals(ChangeSettingNotification other) {
        return other.player.equals(this.player) && other.setting.equals(this.setting);
    }

    protected final int baseHash() { return Objects.hash(this.player,this.setting); }

    protected static <T extends ChangeSettingNotification> Products.P2<RecordCodecBuilder.Mu<T>,PlayerReference,Component> settingFields(RecordCodecBuilder.Instance<T> builder) {
        return builder.group(
                PlayerReference.CODEC.fieldOf("player").forGetter(n -> n.player),
                ComponentSerialization.CODEC.fieldOf("setting").forGetter(n -> n.setting)
        );
    }

    protected static <T extends ChangeSettingNotification> SPart2<RegistryFriendlyByteBuf,T,PlayerReference,Component> settingStreamFields(Class<T> clazz) {return settingStreamFields(); }
    protected static <T extends ChangeSettingNotification> SPart2<RegistryFriendlyByteBuf,T,PlayerReference,Component> settingStreamFields() {
        return new SPart2<>(
                PlayerReference.STREAM_CODEC,n -> n.player,
                ComponentSerialization.STREAM_CODEC,n -> n.setting
        );
    }

    public static class Dumb extends ChangeSettingNotification {

        private static final MapCodec<Dumb> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> settingFields(builder)
                .apply(builder,Dumb::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,Dumb> STREAM_CODEC = settingStreamFields(Dumb.class).assemble(Dumb::new);

        public static final NotificationType<Dumb> TYPE = new NotificationType<>(MAP_CODEC,STREAM_CODEC);

        public static final TextEntry TEXT = TextEntry.notification(TYPE);

        private Dumb(PlayerReference player,Component setting) { super(player,setting); }

        @Override
        protected Component getMessage() { return TEXT.get(this.getPlayerName(),this.setting); }

        @Override
        public NotificationType<?> getType() { return TYPE; }

        @Override
        protected boolean equals(Notification other) { return other instanceof Dumb d && this.baseEquals(d); }
        @Override
        protected int hash() { return this.baseHash(); }

    }

    public static class Simple extends ChangeSettingNotification {

        private static final MapCodec<Simple> MAP_CODEC = RecordCodecBuilder.mapCodec(builder ->
                settingFields(builder)
                .and(ComponentSerialization.CODEC.fieldOf("newValue").forGetter(n -> n.newValue))
                .apply(builder,Simple::new));
        private static final StreamCodec<RegistryFriendlyByteBuf,Simple> STREAM_CODEC = StreamHelper.combine(
                settingStreamFields(Simple.class),
                ComponentSerialization.STREAM_CODEC,n -> n.newValue,
                Simple::new);

        public static final NotificationType<Simple> TYPE = new NotificationType<>(MAP_CODEC,STREAM_CODEC);

        public static final TextEntry TEXT = TextEntry.notification(TYPE);

        private final Component newValue;
        private Simple(PlayerReference player,Component setting,Component newValue) {
            super(player,setting);
            this.newValue = newValue;
        }
        @Override
        protected Component getMessage() { return TEXT.get(this.getPlayerName(),this.setting,this.newValue); }
        @Override
        public NotificationType<?> getType() { return TYPE; }

        @Override
        protected boolean equals(Notification other) {
            if(other instanceof Simple s)
                return s.newValue.equals(this.newValue) && this.baseEquals(s);
            return false;
        }
        @Override
        protected int hash() { return Objects.hash(this.baseHash(),this.newValue); }

    }

    public static class Advanced extends ChangeSettingNotification {

        private static final MapCodec<Advanced> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> settingFields(builder)
                .and(builder.group(
                        ComponentSerialization.CODEC.fieldOf("newValue").forGetter(n -> n.newValue),
                        ComponentSerialization.CODEC.fieldOf("oldValue").forGetter(n -> n.oldValue)
                )).apply(builder,Advanced::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,Advanced> STREAM_CODEC = StreamHelper.combine(
                settingStreamFields(Advanced.class),
                ComponentSerialization.STREAM_CODEC,n -> n.newValue,
                ComponentSerialization.STREAM_CODEC,n -> n.oldValue,
                Advanced::new);

        public static final NotificationType<Advanced> TYPE = new NotificationType<>(MAP_CODEC,STREAM_CODEC);

        public static final TextEntry TEXT = TextEntry.notification(TYPE);

        private final Component newValue;
        private final Component oldValue;
        private Advanced(PlayerReference player,Component setting,Component newValue,Component oldValue) {
            super(player,setting);
            this.newValue = newValue;
            this.oldValue = oldValue;
        }

        @Override
        protected Component getMessage() { return TEXT.get(this.getPlayerName(),this.setting,this.oldValue,this.newValue); }
        @Override
        public NotificationType<?> getType() { return TYPE; }
        @Override
        protected boolean equals(Notification other) {
            return other instanceof Advanced a && a.newValue.equals(this.newValue) && a.oldValue.equals(this.oldValue) && this.baseEquals(a);
        }
        @Override
        protected int hash() { return Objects.hash(this.baseHash(),this.newValue,this.oldValue); }
    }

}
