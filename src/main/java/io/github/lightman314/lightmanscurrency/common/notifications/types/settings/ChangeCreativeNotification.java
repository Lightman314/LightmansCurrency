package io.github.lightman314.lightmanscurrency.common.notifications.types.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.notifications.*;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class ChangeCreativeNotification extends SingleLineNotification {

	public static final NotificationType<ChangeCreativeNotification> TYPE = new Type();
	
	PlayerReference player = PlayerReference.NULL;
	boolean creative;

	private ChangeCreativeNotification() {}
    private ChangeCreativeNotification(PlayerReference player, boolean creative, CommonData data) {
        super(data);
        this.player = player;
        this.creative = creative;
    }
	public ChangeCreativeNotification(PlayerReference player, boolean creative) { this.player = player; this.creative = creative; }
	
    @Override
	public NotificationType<ChangeCreativeNotification> getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

	@Override
	public Component getMessage() {
		return LCText.NOTIFICATION_SETTINGS_CHANGE_CREATIVE.get(this.player.getName(true), this.creative ? LCText.NOTIFICATION_SETTINGS_CHANGE_CREATIVE_ENABLED.get() : LCText.NOTIFICATION_SETTINGS_CHANGE_CREATIVE_DISABLED.get());
	}
	
	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.creative = compound.getBoolean("Creative");
	}
	
	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof ChangeCreativeNotification n)
		{
			return n.player.is(this.player) && n.creative == this.creative;
		}
		return false;
	}

    private static class Type extends NotificationType<ChangeCreativeNotification>
    {
        private static final MapCodec<ChangeCreativeNotification> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                PlayerReference.CODEC.fieldOf("player").forGetter(n -> n.player),
                Codec.BOOL.fieldOf("creative").forGetter(n -> n.creative),
                baseFields()
        ).apply(builder,ChangeCreativeNotification::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,ChangeCreativeNotification> STREAM_CODEC = StreamHelper.combine(baseStreamFields(),
                PlayerReference.STREAM_CODEC,n -> n.player,
                ByteBufCodecs.BOOL,n -> n.creative,
                ChangeCreativeNotification::new);

        @Override
        protected ChangeCreativeNotification createNew() { return new ChangeCreativeNotification(); }
        @Override
        public MapCodec<ChangeCreativeNotification> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf,ChangeCreativeNotification> streamCodec() { return STREAM_CODEC; }
    }
	
}
