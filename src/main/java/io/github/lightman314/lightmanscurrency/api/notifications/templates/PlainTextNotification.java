package io.github.lightman314.lightmanscurrency.api.notifications.templates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.ListHelper;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.category.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.category.builtin.GeneralCategory;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PlainTextNotification extends Notification {

    private static final MapCodec<PlainTextNotification> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ComponentSerialization.CODEC.listOf().fieldOf("text").forGetter(n -> n.text),
            NotificationCategory.CODEC.optionalFieldOf("category").forGetter(PlainTextNotification::getCodecCategory)
    ).apply(builder,PlainTextNotification::parseCodec));
    private static final StreamCodec<RegistryFriendlyByteBuf,PlainTextNotification> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list()),n -> n.text,
            NotificationCategory.STREAM_CODEC,n -> n.category,
            PlainTextNotification::new);
    public static final NotificationType<PlainTextNotification> TYPE = new NotificationType<>(MAP_CODEC,STREAM_CODEC);

    private final List<Component> text;
    private final NotificationCategory category;
    private Optional<NotificationCategory> getCodecCategory() { return this.category.notGeneral() ? Optional.of(this.category) : Optional.empty(); }
    public PlainTextNotification(Component text) { this(text,GeneralCategory.INSTANCE); }
    public PlainTextNotification(Component text,NotificationCategory category) { this(List.of(text),category); }
    public PlainTextNotification(List<Component> text) { this(text,GeneralCategory.INSTANCE); }
    public PlainTextNotification(List<Component> text,NotificationCategory category) {
        this.text = List.copyOf(ListHelper.copyList(text,Component::copy));
        this.category = category.setSidedContext(this);
    }

    private static PlainTextNotification parseCodec(List<Component> text,Optional<NotificationCategory> category) { return new PlainTextNotification(text,category.orElse(GeneralCategory.INSTANCE)); }

    @Override
    public NotificationType<?> getType() { return TYPE; }
    @Override
    public NotificationCategory getCategory() { return this.category; }
    @Override
    public List<Component> getMessageLines() { return this.text; }

    @Override
    protected boolean equals(Notification other) {
        if(other instanceof PlainTextNotification o) {
            if(!o.category.equals(this.category))
                return false;
            return ListHelper.listEquals(this.text,o.text);
        }
        return false;
    }

    @Override
    protected int hash() { return Objects.hash(this.category,this.text); }

}
