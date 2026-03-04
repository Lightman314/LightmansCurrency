package io.github.lightman314.lightmanscurrency.common.notifications.types;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.notifications.*;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;

import java.util.function.Supplier;

public class TextNotification extends SingleLineNotification {

	public static final NotificationType<TextNotification> TYPE = new Type();
	
	private Component text = EasyText.empty();
	private NotificationCategory category = NullCategory.INSTANCE;

	private TextNotification() {}

    private TextNotification(Component text, NotificationCategory category,CommonData data) { super(data); this.text = text; this.category = category; }
	public TextNotification(Component text){ this(text, NullCategory.INSTANCE); }
	public TextNotification(Component text, NotificationCategory category) { this.text = text; this.category = category; }

	public static Supplier<Notification> create(Component text) { return () -> new TextNotification(text); }
	public static Supplier<Notification> create(Component text, NotificationCategory category) { return () -> new TextNotification(text, category); }

    @Override
	public NotificationType<TextNotification> getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return this.category; }

	@Override
	public Component getMessage() { return this.text; }

	@Override
	protected void loadAdditional(CompoundTag compound,HolderLookup.Provider lookup) {
		if(compound.contains("Text", Tag.TAG_STRING))
			this.text = Component.Serializer.fromJson(compound.getString("Text"),lookup);
		if(compound.contains("Category", Tag.TAG_COMPOUND))
			this.category = NotificationCategory.CODEC.decode(RegistryOps.create(NbtOps.INSTANCE,lookup),compound.get("Category")).getOrThrow().getFirst();
	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof TextNotification otherText)
			return otherText.text.equals(this.text) && otherText.category.matches(this.category);
		return false;
	}

    private static class Type extends NotificationType<TextNotification>
    {

        private static final MapCodec<TextNotification> CODEC = RecordCodecBuilder.mapCodec(builder ->
                builder.group(
                        ComponentSerialization.CODEC.fieldOf("text").forGetter(n -> n.text),
                        NotificationCategory.CODEC.fieldOf("category").forGetter(n -> n.category),
                        baseFields())
                        .apply(builder,TextNotification::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,TextNotification> STREAM_CODEC = StreamHelper.combine(baseStreamFields(),
                ComponentSerialization.STREAM_CODEC,n -> n.text,
                NotificationCategory.STREAM_CODEC,n -> n.category,
                TextNotification::new);

        @Override
        protected TextNotification createNew() { return new TextNotification(); }

        @Override
        public MapCodec<TextNotification> codec() { return CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TextNotification> streamCodec() { return STREAM_CODEC; }

    }
	
}
