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

public class ChangeAllyPermissionNotification extends SingleLineNotification {

	public static final NotificationType<ChangeAllyPermissionNotification> TYPE = new Type();
	
	PlayerReference player = PlayerReference.NULL;
	String permission = "";
	int newValue;
	int oldValue;

	private ChangeAllyPermissionNotification() {}
    private ChangeAllyPermissionNotification(PlayerReference player, String permission, int newValue, int oldValue, CommonData data) {
        super(data);
        this.player = player;
        this.permission = permission;
        this.newValue = newValue;
        this.oldValue = oldValue;
    }
	public ChangeAllyPermissionNotification(PlayerReference player, String permission, int newValue, int oldValue) {
		this.player = player;
		this.permission = permission;
		this.newValue = newValue;
		this.oldValue = oldValue;
	}

    @Override
	public NotificationType<ChangeAllyPermissionNotification> getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

	@Override
	public Component getMessage() {
		if(this.oldValue == 0)
			return LCText.NOTIFICATION_SETTINGS_CHANGE_ALLY_PERMISSIONS_SIMPLE.get(this.player.getName(true), this.permission, this.newValue);
		else
			return LCText.NOTIFICATION_SETTINGS_CHANGE_ALLY_PERMISSIONS.get(this.player.getName(true), this.permission, this.oldValue, this.newValue);
	}

	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.permission = compound.getString("Permission");
		this.newValue = compound.getInt("NewValue");
		this.oldValue = compound.getInt("OldValue");
	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof ChangeAllyPermissionNotification n)
		{
			return n.player.is(this.player) && n.permission.equals(this.permission) && n.newValue == this.newValue && n.oldValue == this.oldValue;
		}
		return false;
	}

    private static class Type extends NotificationType<ChangeAllyPermissionNotification>
    {
        private static final MapCodec<ChangeAllyPermissionNotification> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                PlayerReference.CODEC.fieldOf("player").forGetter(n -> n.player),
                Codec.STRING.fieldOf("permission").forGetter(n -> n.permission),
                Codec.INT.fieldOf("old").forGetter(n -> n.oldValue),
                Codec.INT.fieldOf("new").forGetter(n -> n.newValue),
                baseFields()
        ).apply(builder,ChangeAllyPermissionNotification::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,ChangeAllyPermissionNotification> STREAM_CODEC = StreamHelper.combine(baseStreamFields(),
                PlayerReference.STREAM_CODEC,n -> n.player,
                ByteBufCodecs.STRING_UTF8,n -> n.permission,
                ByteBufCodecs.INT,n -> n.oldValue,
                ByteBufCodecs.INT,n -> n.newValue,
                ChangeAllyPermissionNotification::new);

        @Override
        protected ChangeAllyPermissionNotification createNew() { return new ChangeAllyPermissionNotification(); }
        @Override
        public MapCodec<ChangeAllyPermissionNotification> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf,ChangeAllyPermissionNotification> streamCodec() { return STREAM_CODEC; }
    }

}
