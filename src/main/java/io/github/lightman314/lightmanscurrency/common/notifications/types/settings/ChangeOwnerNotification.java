package io.github.lightman314.lightmanscurrency.common.notifications.types.settings;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class ChangeOwnerNotification extends Notification {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "change_ownership");
	
	PlayerReference player;
	OwnershipData newOwner;
	OwnershipData oldOwner;
	
	public ChangeOwnerNotification(CompoundTag compound) { this.load(compound); }
	
	public ChangeOwnerNotification(PlayerReference player, PlayerReference newOwner, PlayerReference oldOwner) { this(player, OwnershipData.of(newOwner), OwnershipData.of(oldOwner)); }
	public ChangeOwnerNotification(PlayerReference player, PlayerReference newOwner, Team oldOwner) { this(player, OwnershipData.of(newOwner), OwnershipData.of(oldOwner)); }
	public ChangeOwnerNotification(PlayerReference player, Team newOwner, PlayerReference oldOwner) { this(player, OwnershipData.of(newOwner), OwnershipData.of(oldOwner)); }
	public ChangeOwnerNotification(PlayerReference player, Team newOwner, Team oldOwner) { this(player, OwnershipData.of(newOwner), OwnershipData.of(oldOwner)); }
	
	private ChangeOwnerNotification(PlayerReference player, OwnershipData newOwner, OwnershipData oldOwner) {
		this.player = player;
		this.newOwner = newOwner;
		this.oldOwner = oldOwner;
	}
	
	@Override
	protected ResourceLocation getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

	@Override
	public MutableComponent getMessage() {
		if(newOwner.is(this.player))
			return Component.translatable("log.settings.newowner.taken", this.player.getName(true), this.oldOwner.getName());
		if(oldOwner.is(this.player))
			return Component.translatable("log.settings.newowner.passed", this.player.getName(true), this.newOwner.getName());
		else
			return Component.translatable("log.settings.newowner.transferred", this.player.getName(true), this.oldOwner.getName(), this.newOwner.getName());
	}

	@Override
	protected void saveAdditional(CompoundTag compound) {
		compound.put("Player", this.player.save());
		compound.put("NewOwner", this.newOwner.save());
		compound.put("OldOwner", this.oldOwner.save());
	}

	@Override
	protected void loadAdditional(CompoundTag compound) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.newOwner = OwnershipData.load(compound.getCompound("NewOwner"));
		this.oldOwner = OwnershipData.load(compound.getCompound("OldOwner"));
	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof ChangeOwnerNotification)
		{
			ChangeOwnerNotification n = (ChangeOwnerNotification)other;
			return n.player.is(this.player) && n.newOwner.is(this.newOwner) && n.oldOwner.is(this.oldOwner);
		}
		return false;
	}
	
	
	
	private static class OwnershipData {
		
		public final PlayerReference player;
		public final long team;
		
		private OwnershipData(PlayerReference player, long team) {
			this.player = player;
			this.team = team;
		}
		
		public String getName() {
			if(this.player != null)
				return this.player.getName(true);
			Team team = TeamSaveData.GetTeam(true, this.team);
			if(team != null)
				return team.getName();
			return "DELETED TEAM";
		}
		
		public CompoundTag save() {
			CompoundTag compound = new CompoundTag();
			if(this.player != null)
				compound.put("Player", this.player.save());
			else
				compound.putLong("Team", this.team);
			return compound;
		}
		
		public boolean is(PlayerReference player) {
			if(this.player != null)
				return this.player.is(player);
			return false;
		}
		
		public boolean is(OwnershipData other) {
			if(this.player != null)
			{
				if(other.player != null)
					return this.player.is(other.player);
				return false;
			}
			else if(other.player != null)
				return false;
			return this.team == other.team;
		}
		
		public static OwnershipData of(PlayerReference player) { return new OwnershipData(player, -1); }
		public static OwnershipData of(Team team) { return new OwnershipData(null, team.getID()); }
		public static OwnershipData of(long teamID) { return new OwnershipData(null, teamID); }
		
		public static OwnershipData load(CompoundTag compound) {
			if(compound.contains("Player"))
				return of(PlayerReference.load(compound.getCompound("Player")));
			else
				return of(compound.getLong("Team"));
		}
		
	}


	
	
	
}
