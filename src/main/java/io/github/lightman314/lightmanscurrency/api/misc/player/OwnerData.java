package io.github.lightman314.lightmanscurrency.api.misc.player;

import java.util.function.Consumer;

import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.FakeOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.PlayerOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.TeamOwner;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;


public final class OwnerData implements IClientTracker{

	private Owner backupOwner = Owner.getNull(this);
	private Owner currentOwner = Owner.getNull(this);

	@Override
	public boolean isClient() { return this.parent.isClient(); }

	private final IClientTracker parent;
	private final Consumer<OwnerData> onChanged;
	
	public OwnerData(@Nonnull IClientTracker parent) { this(parent,o -> {}); }
	public OwnerData(@Nonnull IClientTracker parent, @Nonnull Runnable onChanged) { this(parent,o -> onChanged.run()); }
	public OwnerData(@Nonnull IClientTracker parent, @Nonnull Consumer<OwnerData> onChanged) { this.parent = parent; this.onChanged = onChanged; }

	@Nonnull
	public Owner getValidOwner() { return this.currentOwner.stillValid() ? this.currentOwner : this.backupOwner; }

	public boolean hasOwner() { return this.currentOwner.stillValid() || this.backupOwner.stillValid(); }

	@Nonnull
	public CompoundTag save(@Nonnull HolderLookup.Provider lookup)
	{
		CompoundTag compound = new CompoundTag();
		compound.put("BackupOwner", this.backupOwner.save(lookup));
		compound.put("Owner", this.currentOwner.save(lookup));
		return compound;
	}
	
	public void load(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup)
	{
		if(compound.contains("BackupOwner") && compound.contains("Owner"))
		{
			this.backupOwner = Owner.load(compound.getCompound("BackupOwner"), lookup);
			this.currentOwner = Owner.load(compound.getCompound("Owner"), lookup);
		}
		else
		{
			this.backupOwner = FakeOwner.of("NULL");
			//Load deprecated save data
			if(compound.contains("Custom"))
			{
				MutableComponent custom = Component.Serializer.fromJson(compound.getString("Custom"), lookup);
				this.backupOwner = FakeOwner.of(custom.copy());
				this.currentOwner = FakeOwner.of(custom);
			}
			if(compound.contains("Player"))
			{
				PlayerReference player = PlayerReference.load(compound.getCompound("Player"));
				this.backupOwner = PlayerOwner.of(player);
				this.currentOwner = PlayerOwner.of(player);
			}

			if(compound.contains("Team")) //Don't set a team owner as the backup owner
				this.currentOwner = TeamOwner.of(compound.getLong("Team"));
		}
		if(this.backupOwner == null)
			this.backupOwner = Owner.getNull();
		this.backupOwner.setParent(this);
		if(this.currentOwner == null)
			this.currentOwner = Owner.getNull();
		this.currentOwner.setParent(this);
	}

	public void copyFrom(@Nonnull OwnerData owner) {
		this.backupOwner = owner.backupOwner.copy();
		this.currentOwner = owner.currentOwner.copy();
	}

	@Nonnull
	public PlayerReference getPlayerForContext() { return this.getValidOwner().asPlayerReference(); }
	
	public boolean isAdmin(@Nonnull Player player) { return LCAdminMode.isAdminPlayer(player) || this.isAdmin(PlayerReference.of(player)); }
	
	public boolean isAdmin(@Nonnull PlayerReference player) { return this.getValidOwner().isAdmin(player); }
	
	public boolean isMember(@Nonnull Player player) { return LCAdminMode.isAdminPlayer(player) || this.isMember(PlayerReference.of(player));}
	
	public boolean isMember(@Nonnull PlayerReference player) { return this.getValidOwner().isMember(player); }

	@Nonnull
	public MutableComponent getName() { return this.getValidOwner().getName(); }

	public void SetOwner(@Nonnull Owner newOwner)
	{
		this.currentOwner = newOwner;
		if(newOwner.alwaysValid())
			this.backupOwner = newOwner;
		this.onChanged.accept(this);
	}
	
}
