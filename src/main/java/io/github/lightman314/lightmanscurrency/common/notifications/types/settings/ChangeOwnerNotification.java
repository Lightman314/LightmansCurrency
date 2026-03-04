package io.github.lightman314.lightmanscurrency.common.notifications.types.settings;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.notifications.*;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.PlayerOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.TeamOwner;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;

public class ChangeOwnerNotification extends SingleLineNotification {

	public static final NotificationType<ChangeOwnerNotification> TYPE = new Type();
	
	PlayerReference player = PlayerReference.NULL;
	Owner newOwner = Owner.getNull(this);
	Owner oldOwner = Owner.getNull(this);
	
	private ChangeOwnerNotification() { }
    private ChangeOwnerNotification(PlayerReference player,Owner newOwner,Owner oldOwner,CommonData data)
    {
        super(data);
        this.player = player;
        this.newOwner = newOwner.copyWithParent(this);
        this.oldOwner = oldOwner.copyWithParent(this);
    }
	public ChangeOwnerNotification(PlayerReference player, Owner newOwner, Owner oldOwner) {
		this.player = player;
		this.newOwner = newOwner.copyWithParent(this);
		this.oldOwner = oldOwner.copyWithParent(this);
	}

    @Override
	public NotificationType<ChangeOwnerNotification> getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

	@Override
	public MutableComponent getMessage() {
		if(this.newOwner.asPlayerReference().isExact(this.player))
			return LCText.NOTIFICATION_SETTINGS_CHANGE_OWNER_TAKEN.get(this.newOwner.getName(), this.oldOwner.getName());
		if(this.oldOwner.asPlayerReference().isExact(this.player))
			return LCText.NOTIFICATION_SETTINGS_CHANGE_OWNER_PASSED.get(this.oldOwner.getName(), this.newOwner.getName());
		else
			return LCText.NOTIFICATION_SETTINGS_CHANGE_OWNER_TRANSFERRED.get(this.player.getName(true), this.oldOwner.getName(), this.newOwner.getName());
	}

	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.newOwner = safeLoad(compound.getCompound("NewOwner"),lookup);
		this.newOwner.setParent(this);
		this.oldOwner = safeLoad(compound.getCompound("OldOwner"),lookup);
		this.oldOwner.setParent(this);
	}

	
	private static Owner safeLoad(CompoundTag tag, HolderLookup.Provider lookup)
	{
		if(tag.contains("Type"))
		{
			Owner o = Owner.load(tag,lookup);
			return o != null ? o : Owner.getNull();
		}
		if(tag.contains("Player"))
		{
			PlayerReference pr = PlayerReference.load(tag.getCompound("Player"));
			if(pr != null)
				return PlayerOwner.of(pr);
		}
		if(tag.contains("Team"))
		{
			long teamID = tag.getLong("Team");
			return TeamOwner.of(teamID);
		}
		return Owner.getNull();
	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof ChangeOwnerNotification n)
		{
			return n.player.is(this.player) && n.newOwner.matches(this.newOwner) && n.oldOwner.matches(this.oldOwner);
		}
		return false;
	}

    private static class Type extends NotificationType<ChangeOwnerNotification>
    {
        private static final MapCodec<ChangeOwnerNotification> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                PlayerReference.CODEC.fieldOf("player").forGetter(n -> n.player),
                Owner.CODEC.fieldOf("new").forGetter(n -> n.newOwner),
                Owner.CODEC.fieldOf("old").forGetter(n -> n.oldOwner),
                baseFields()
        ).apply(builder,ChangeOwnerNotification::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,ChangeOwnerNotification> STREAM_CODEC = StreamHelper.combine(baseStreamFields(),
                PlayerReference.STREAM_CODEC,n -> n.player,
                Owner.STREAM_CODEC,n -> n.newOwner,
                Owner.STREAM_CODEC,n -> n.oldOwner,
                ChangeOwnerNotification::new);

        @Override
        protected ChangeOwnerNotification createNew() { return new ChangeOwnerNotification(); }
        @Override
        public MapCodec<ChangeOwnerNotification> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ChangeOwnerNotification> streamCodec() { return STREAM_CODEC; }
    }

}
