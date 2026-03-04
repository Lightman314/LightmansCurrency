package io.github.lightman314.lightmanscurrency.common.notifications.types.ejection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.notifications.*;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

public class OwnableBlockEjectedNotification extends SingleLineNotification {

    public static final NotificationType<OwnableBlockEjectedNotification> TYPE = new Type();

    private Component name = EasyText.empty();
    private boolean ejected = false;
    private boolean anarchy = false;

    private OwnableBlockEjectedNotification() {}
    private OwnableBlockEjectedNotification(Component name, boolean ejected, boolean anarchy, CommonData data) {
        super(data);
        this.name = name;
        this.ejected = ejected;
        this.anarchy = anarchy;
    }
    public OwnableBlockEjectedNotification(Component name) {
        this.name = name.copy();
        this.ejected = LCConfig.SERVER.safelyEjectMachineContents.get();
        this.anarchy = LCConfig.SERVER.anarchyMode.get();
    }

    public static Supplier<Notification> create(Component name) { return () -> new OwnableBlockEjectedNotification(name); }

    @Override
    public NotificationType<?> getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return NullCategory.INSTANCE; }
    
    @Override
    public Component getMessage() { return this.getText().get(this.name); }

    private TextEntry getText() {
        if(this.anarchy)
            return LCText.NOTIFICATION_EJECTION_ANARCHY;
        if(this.ejected)
            return LCText.NOTIFICATION_EJECTION_EJECTED;
        return LCText.NOTIFICATION_EJECTION_DROPPED;
    }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
        this.name = Component.Serializer.fromJson(compound.getString("Name"),lookup);
        if(compound.contains("Ejected"))
            this.ejected = compound.getBoolean("Ejected");
        if(compound.contains("Anarchy"))
            this.anarchy = compound.getBoolean("Anarchy");
    }

    @Override
    protected boolean canMerge(Notification other) { return false; }

    private static class Type extends NotificationType<OwnableBlockEjectedNotification>
    {
        private static final MapCodec<OwnableBlockEjectedNotification> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(n -> n.name),
                Codec.BOOL.fieldOf("ejected").forGetter(n -> n.ejected),
                Codec.BOOL.fieldOf("anarchy").forGetter(n -> n.anarchy),
                baseFields()
                ).apply(builder,OwnableBlockEjectedNotification::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,OwnableBlockEjectedNotification> STREAM_CODEC = StreamHelper.combine(baseStreamFields(),
                ComponentSerialization.STREAM_CODEC,n -> n.name,
                ByteBufCodecs.BOOL,n -> n.ejected,
                ByteBufCodecs.BOOL,n -> n.anarchy,
                OwnableBlockEjectedNotification::new);

        @Override
        protected OwnableBlockEjectedNotification createNew() { return new OwnableBlockEjectedNotification(); }
        @Override
        public MapCodec<OwnableBlockEjectedNotification> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf,OwnableBlockEjectedNotification> streamCodec() { return STREAM_CODEC; }
    }

}
