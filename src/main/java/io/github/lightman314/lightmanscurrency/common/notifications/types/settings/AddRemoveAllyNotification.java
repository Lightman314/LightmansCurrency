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

public class AddRemoveAllyNotification extends SingleLineNotification {

	public static final NotificationType<AddRemoveAllyNotification> TYPE = new Type();

    private PlayerReference player = PlayerReference.NULL;
    private boolean isAdd = false;
    private PlayerReference ally = PlayerReference.NULL;

	private AddRemoveAllyNotification() {}
	private AddRemoveAllyNotification(PlayerReference player, boolean isAdd, PlayerReference ally, CommonData data) {
        super(data);
        this.player = player;
        this.isAdd = isAdd;
        this.ally = ally;
    }

	public AddRemoveAllyNotification(PlayerReference player, boolean isAdd, PlayerReference ally) {
		this.player = player;
		this.isAdd = isAdd;
		this.ally = ally;
	}

    @Override
	public NotificationType<AddRemoveAllyNotification> getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

	@Override
	public Component getMessage() {
		return LCText.NOTIFICATION_SETTINGS_ADD_REMOVE_ALLY.get(this.player.getName(true), this.isAdd ? LCText.GUI_ADDED.get() : LCText.GUI_REMOVED.get(), this.ally.getName(true), this.isAdd ? LCText.GUI_TO.get() : LCText.GUI_FROM.get());
	}

	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.isAdd = compound.getBoolean("Add");
		this.ally = PlayerReference.load(compound.getCompound("Ally"));
	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof AddRemoveAllyNotification n)
		{
			return n.player.is(this.player) && n.isAdd == this.isAdd && n.ally.is(this.ally);
		}
		return false;
	}

    private static class Type extends NotificationType<AddRemoveAllyNotification>
    {
        private static final MapCodec<AddRemoveAllyNotification> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                PlayerReference.CODEC.fieldOf("player").forGetter(n -> n.player),
                Codec.BOOL.fieldOf("isAdd").forGetter(n -> n.isAdd),
                PlayerReference.CODEC.fieldOf("ally").forGetter(n -> n.ally),
                baseFields()
        ).apply(builder,AddRemoveAllyNotification::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,AddRemoveAllyNotification> STREAM_CODEC = StreamHelper.combine(baseStreamFields(),
                PlayerReference.STREAM_CODEC,n -> n.player,
                ByteBufCodecs.BOOL,n -> n.isAdd,
                PlayerReference.STREAM_CODEC,n -> n.ally,
                AddRemoveAllyNotification::new);

        @Override
        protected AddRemoveAllyNotification createNew() { return new AddRemoveAllyNotification(); }
        @Override
        public MapCodec<AddRemoveAllyNotification> codec() { return CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AddRemoveAllyNotification> streamCodec() { return STREAM_CODEC; }
    }

}
