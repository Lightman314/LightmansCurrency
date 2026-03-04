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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class ChangeNameNotification extends SingleLineNotification {

	public static final NotificationType<ChangeNameNotification> TYPE = new Type();

	private PlayerReference player = PlayerReference.NULL;
	private String oldName = "";
	private String newName = "";

	private ChangeNameNotification() {}
	private ChangeNameNotification(PlayerReference player, String newName, String oldName, CommonData data) {
        super(data);
        this.player = player;
        this.newName = newName;
        this.oldName = oldName;
    }
	public ChangeNameNotification(PlayerReference player, String newName, String oldName) { this.player = player; this.newName = newName; this.oldName = oldName; }

    @Override
	public NotificationType<ChangeNameNotification> getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

	@Override
	public MutableComponent getMessage() {
		if(this.oldName.isBlank())
			return LCText.NOTIFICATION_SETTINGS_CHANGE_NAME_SET.get(this.player.getName(true), this.newName);
		else if(this.newName.isBlank())
			return LCText.NOTIFICATION_SETTINGS_CHANGE_NAME_RESET.get(this.player.getName(true), this.oldName);
		else
			return LCText.NOTIFICATION_SETTINGS_CHANGE_NAME.get(this.player.getName(true), this.oldName, this.newName);
	}

	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.oldName = compound.getString("OldName");
		this.newName = compound.getString("NewName");
	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof ChangeNameNotification n)
		{
			return n.player.is(this.player) && n.newName.equals(this.newName) && n.oldName.equals(this.oldName);
		}
		return false;
	}

    private static class Type extends NotificationType<ChangeNameNotification>
    {
        private static final MapCodec<ChangeNameNotification> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                PlayerReference.CODEC.fieldOf("player").forGetter(n -> n.player),
                Codec.STRING.fieldOf("new").forGetter(n -> n.newName),
                Codec.STRING.fieldOf("old").forGetter(n -> n.oldName),
                baseFields()
        ).apply(builder,ChangeNameNotification::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,ChangeNameNotification> STREAM_CODEC = StreamHelper.combine(baseStreamFields(),
                PlayerReference.STREAM_CODEC,n -> n.player,
                ByteBufCodecs.STRING_UTF8,n -> n.newName,
                ByteBufCodecs.STRING_UTF8,n -> n.oldName,
                ChangeNameNotification::new);

        @Override
        protected ChangeNameNotification createNew() { return new ChangeNameNotification(); }
        @Override
        public MapCodec<ChangeNameNotification> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ChangeNameNotification> streamCodec() { return STREAM_CODEC; }
    }
	
}
