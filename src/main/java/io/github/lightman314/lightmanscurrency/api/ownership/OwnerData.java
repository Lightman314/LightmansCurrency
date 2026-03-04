package io.github.lightman314.lightmanscurrency.api.ownership;

import java.util.Objects;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.FakeOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.PlayerOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.TeamOwner;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;


public final class OwnerData implements IClientTracker{

    public static final Codec<OwnerData> CODEC = Codec.withAlternative(RecordCodecBuilder.create(builder -> builder.group(
            Owner.CODEC.fieldOf("backup").forGetter(o -> o.backupOwner),
            Owner.CODEC.fieldOf("owner").forGetter(o -> o.currentOwner)
    ).apply(builder,OwnerData::new)),
            CodecHelper.oldValueLoader(OwnerData::loadOldData,"Owner Data"));

    public static final StreamCodec<RegistryFriendlyByteBuf,OwnerData> STREAM_CODEC = StreamCodec.composite(
            Owner.STREAM_CODEC,o -> o.backupOwner,
            Owner.STREAM_CODEC,o -> o.currentOwner,
            OwnerData::new);

	private Owner backupOwner = Owner.getNull(this);
	private Owner currentOwner = Owner.getNull(this);

	@Override
	public boolean isClient() { return this.parent.isClient(); }

	private IClientTracker parent;
	private final Consumer<OwnerData> onChanged;

    public OwnerData withParent(IClientTracker parent) { this.parent = parent; return this; }

    private OwnerData(Owner backupOwner,Owner currentOwner) {
        this();
        this.backupOwner = backupOwner;
        this.backupOwner.setParent(this);
        this.currentOwner = currentOwner;
        this.currentOwner.setParent(this);
    }
    public OwnerData() { this(IClientTracker.forClient(),() -> {});}
	public OwnerData(IClientTracker parent) { this(parent,o -> {}); }
	public OwnerData(IClientTracker parent, Runnable onChanged) { this(parent,o -> onChanged.run()); }
	public OwnerData(IClientTracker parent, Consumer<OwnerData> onChanged) { this.parent = parent; this.onChanged = onChanged; }

	public Owner getValidOwner() { return this.currentOwner.stillValid() ? this.currentOwner : this.backupOwner; }
	public boolean hasOwner() { return this.currentOwner.stillValid() || this.backupOwner.stillValid(); }
	
	public CompoundTag save(DataContext<Tag> context) { return (CompoundTag)context.write(this,CODEC); }

    private static OwnerData loadOldData(CompoundTag compound,HolderLookup.Provider lookup)
    {
        Owner backupOwner;
        Owner currentOwner = Owner.getNull();
        if(compound.contains("BackupOwner") && compound.contains("Owner"))
        {
            backupOwner = Owner.load(compound.getCompound("BackupOwner"), lookup);
            currentOwner = Owner.load(compound.getCompound("Owner"), lookup);
        }
        else
        {
            backupOwner = FakeOwner.of("NULL");
            //Load deprecated save data
            if(compound.contains("Custom"))
            {
                MutableComponent custom = Component.Serializer.fromJson(compound.getString("Custom"), lookup);
                backupOwner = FakeOwner.of(custom.copy());
                currentOwner = FakeOwner.of(custom);
            }
            if(compound.contains("Player"))
            {
                PlayerReference player = PlayerReference.load(compound.getCompound("Player"));
                backupOwner = PlayerOwner.of(player);
                currentOwner = PlayerOwner.of(player);
            }

            if(compound.contains("Team")) //Don't set a team owner as the backup owner
                currentOwner = TeamOwner.of(compound.getLong("Team"));
        }
        if(backupOwner == null)
            backupOwner = Owner.getNull();
        if(currentOwner == null)
            currentOwner = Owner.getNull();
        return new OwnerData(backupOwner,currentOwner);
    }

	public void load(CompoundTag compound,DataContext<Tag> context)
	{
        OwnerData d = context.readOrDefault(compound,CODEC,null);
        if(d != null)
            this.copyFrom(d);
	}

	public void copyFrom(OwnerData owner) {
        if(owner == null)
            return;
		this.backupOwner = owner.backupOwner.copy();
		this.backupOwner.setParent(this);
		this.currentOwner = owner.currentOwner.copy();
		this.currentOwner.setParent(this);
	}
	
	public PlayerReference getPlayerForContext() { return this.getValidOwner().asPlayerReference(); }
	
	public boolean isAdmin(Player player) { return LCAdminMode.isAdminPlayer(player) || this.isAdmin(PlayerReference.of(player)); }
	
	public boolean isAdmin(PlayerReference player) { return this.getValidOwner().isAdmin(player); }
	
	public boolean isMember(Player player) { return LCAdminMode.isAdminPlayer(player) || this.isMember(PlayerReference.of(player));}
	
	public boolean isMember(PlayerReference player) { return this.getValidOwner().isMember(player); }

	public Component getName() { return this.getValidOwner().getName(); }

	public void SetOwner(Owner newOwner)
	{
        if(this.currentOwner.matches(newOwner))
            return;
		this.currentOwner = newOwner.copy();
		this.currentOwner.setParent(this);
		if(this.currentOwner.alwaysValid())
		{
			this.backupOwner = this.currentOwner.copy();
			this.backupOwner.setParent(this);
		}
		this.setChanged();
	}

	public void setChanged() { this.onChanged.accept(this); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof OwnerData owner)
            return owner.currentOwner.matches(this.currentOwner) && owner.backupOwner.matches(this.backupOwner);
        return false;
    }

    @Override
    public int hashCode() { return Objects.hash(this.currentOwner,this.backupOwner); }

}
