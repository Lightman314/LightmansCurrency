package io.github.lightman314.lightmanscurrency.api.misc.player;

import java.util.function.Consumer;

import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.FakeOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.PlayerOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.TeamOwner;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;


public final class OwnerData {

	private Owner backupOwner = Owner.NULL;
	private Owner currentOwner = Owner.NULL;
	
	private final IClientTracker parent;
	private final Consumer<OwnerData> onChanged;
	
	public OwnerData(IClientTracker parent, Consumer<OwnerData> onChanged) { this.parent = parent; this.onChanged = onChanged; }

	@Nonnull
	public Owner getValidOwner() { return this.currentOwner.stillValid() ? this.currentOwner : this.backupOwner; }

	public boolean hasOwner() { return this.currentOwner.stillValid() || this.backupOwner.stillValid(); }

	public void flagAsClient() {
		this.backupOwner.flagAsClient();
		this.currentOwner.flagAsClient();
	}

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
			if(this.parent.isClient())
			{
				this.backupOwner.flagAsClient();
				this.currentOwner.flagAsClient();
			}
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

	}
	
	public void copyFrom(OwnerData owner) {
		this.backupOwner = owner.backupOwner;
		this.currentOwner = owner.currentOwner;
	}

	@Deprecated(since = "2.2.1.4")
	public boolean hasPlayer() { return this.getPlayer() != null; }
	@Deprecated(since = "2.2.1.4")
	public PlayerReference getPlayer() {
		if(this.currentOwner instanceof PlayerOwner po)
			return po.player;
		if(this.backupOwner instanceof PlayerOwner po)
			return po.player;
		return null;
	}

	@Deprecated(since = "2.2.1.4")
	public boolean hasTeam() { return this.getTeam() != null; }
	@Deprecated(since = "2.2.1.4")
	public Team getTeam()
	{
		if(this.currentOwner instanceof TeamOwner to)
			return TeamSaveData.GetTeam(this.parent.isClient(), to.teamID);
		return null;
	}
	@Nonnull
	public PlayerReference getPlayerForContext() { return this.getValidOwner().asPlayerReference(); }
	
	public boolean isAdmin(@Nonnull Player player) { return LCAdminMode.isAdminPlayer(player) || this.isAdmin(PlayerReference.of(player)); }
	
	public boolean isAdmin(@Nonnull PlayerReference player) { return this.getValidOwner().isAdmin(player); }
	
	public boolean isMember(@Nonnull Player player) { return LCAdminMode.isAdminPlayer(player) || this.isMember(PlayerReference.of(player));}
	
	public boolean isMember(@Nonnull PlayerReference player) { return this.getValidOwner().isMember(player); }

	/**
	 * @deprecated Use {@link #getName()} instead.
	 */
	@Deprecated(since = "2.2.1.4")
	public String getOwnerName() { return this.getName().getString(); }
	/**
	 * @deprecated Use {@link #getName()} instead.
	 */
	@Deprecated(since = "2.2.1.4")
	public String getOwnerName(boolean ignored) { return this.getName().getString(); }

	@Nonnull
	public MutableComponent getName() { return this.getValidOwner().getName(); }

	/**
	 * @deprecated Use {@link #SetOwner(Owner)} instead.
	 */
	@Deprecated(since = "2.2.1.4")
	public void SetCustomOwner(String customOwner) { this.SetOwner(FakeOwner.of(customOwner)); }
	/**
	 * @deprecated Use {@link #SetOwner(Owner)} instead.
	 */
	@Deprecated(since = "2.2.1.4")
	public void SetCustomOwner(MutableComponent customOwner) { this.SetOwner(FakeOwner.of(customOwner)); }

	/**
	 * @deprecated Use {@link #SetOwner(Owner)} instead.
	 */
	@Deprecated(since = "2.2.1.4")
	public void SetOwner(PlayerReference player) { this.SetOwner(PlayerOwner.of(player)); }

	/**
	 * @deprecated Use {@link #SetOwner(Owner)} instead.
	 */
	@Deprecated(since = "2.2.1.4")
	public void SetOwner(Player player) { this.SetOwner(PlayerReference.of(player)); }

	/**
	 * @deprecated Use {@link #SetOwner(Owner)} instead.
	 */
	@Deprecated(since = "2.2.1.4")
	public void SetOwner(Team team) {
		if(team == null)
			return;
		this.SetOwner(TeamOwner.of(team));
	}

	public void SetOwner(@Nonnull Owner newOwner)
	{
		this.currentOwner = newOwner;
		if(newOwner.alwaysValid())
			this.backupOwner = newOwner;
		this.onChanged.accept(this);
	}
	
}
