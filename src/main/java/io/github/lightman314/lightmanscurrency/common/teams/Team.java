package io.github.lightman314.lightmanscurrency.common.teams;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;

import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

public class Team {

	public static final int MAX_NAME_LENGTH = 32;
	
	public static final String CATEGORY_MEMBER = "MEMBER";
	public static final String CATEGORY_ADMIN = "ADMIN";
	public static final String CATEGORY_REMOVE = "REMOVE";
	public static final String CATEGORY_OWNER = "OWNER";
	
	final UUID id;
	public UUID getID() { return id; }
	PlayerReference owner = null;
	public PlayerReference getOwner() { return this.owner; }
	String teamName = "Some Team";
	public String getName() { return this.teamName; }
	
	private boolean isClient = false;
	public void flagAsClient() { this.isClient = true; }
	
	List<PlayerReference> admins = Lists.newArrayList();
	public List<PlayerReference> getAdmins() { return this.admins; }
	List<PlayerReference> members = Lists.newArrayList();
	public List<PlayerReference> getMembers() { return this.members; }
	
	/**
	 * Determines if the given player is the owner of this team.
	 * Also returns true if the player is in admin mode.
	 */
	public boolean isOwner(Player player) { return (this.owner != null && this.owner.is(player)) || TradingOffice.isAdminPlayer(player); }
	/**
	 * Determines if the given player is the owner of this team.
	 */
	public boolean isOwner(UUID playerID) { return this.owner != null && this.owner.is(playerID); }
	/**
	 * Determines if the given player is an admin or owner of this team.
	 * Also returns true if the player is in admin mode.
	 */
	public boolean isAdmin(Player player) { return PlayerReference.listContains(this.admins, player.getUUID()) || this.isOwner(player); }
	/**
	 * Determines if the given player is an admin or owner of this team.
	 */
	public boolean isAdmin(UUID playerID) { return PlayerReference.listContains(this.admins, playerID) || this.isOwner(playerID); }
	/**
	 * Determines if the given player is a member, admin, or owner of this team.
	 * Also returns true if the player is in admin mode.
	 */
	public boolean isMember(Player player) { return PlayerReference.listContains(this.members, player.getUUID()) || this.isAdmin(player); }
	/**
	 * Determines if the given player is a member, admin, or owner of this team.
	 */
	public boolean isMember(UUID playerID) { return PlayerReference.listContains(this.members, playerID) || this.isAdmin(playerID); }
	
	public boolean changeAddMember(Player requestor, String name) { return this.changeAny(requestor, name, CATEGORY_MEMBER); }
	public boolean changeAddAdmin(Player requestor, String name) { return this.changeAny(requestor, name, CATEGORY_ADMIN); }
	public boolean changeRemoveMember(Player requestor, String name) { return this.changeAny(requestor, name, CATEGORY_REMOVE); }
	public boolean changeOwner(Player requestor, String name) { return this.changeAny(requestor, name, CATEGORY_OWNER); }
	
	public void changeName(Player requestor, String newName)
	{
		if(this.isAdmin(requestor))
		{
			this.teamName = newName;
			this.markDirty();
		}
	}
	
	public boolean changeAny(Player requestor, String playerName, String category)
	{
		if(category.contentEquals(CATEGORY_MEMBER) && this.isAdmin(requestor))
		{
			//Add or remove the member
			GameProfile profile = this.getProfile(playerName);
			if(profile != null)
			{
				//Confirm that this player isn't already on a list
				if(this.isMember(profile.getId()))
					return false;
				//Add the member
				this.members.add(PlayerReference.of(profile));
				this.markDirty();
			}
			return true;
		}
		else if(category.contentEquals(CATEGORY_ADMIN) && this.isOwner(requestor))
		{
			//Add or remove the admin
			GameProfile profile = this.getProfile(playerName);
			if(profile != null)
			{
				//Confirm that this player isn't already an admin
				if(this.isAdmin(profile.getId()))
					return false;
				//Remove from the member list if making them an admin
				if(this.isMember(profile.getId()))
				{
					boolean notFound = true;
					for(int i = 0; notFound || i < this.members.size(); ++i)
					{
						if(this.members.get(i).is(profile))
						{
							notFound = false;
							this.members.remove(i);
						}
					}
				}
				//Add to the admin list
				this.admins.add(PlayerReference.of(profile));
				this.markDirty();
			}
			return true;
		}
		else if(category.contentEquals(CATEGORY_REMOVE) && this.isAdmin(requestor))
		{
			GameProfile profile = this.getProfile(playerName);
			if(profile != null)
			{
				//Confirm that this player is a member (Can't remove them if they're not in the list in the first place)
				if(!this.isMember(profile.getId()))
					return false;
				
				//Confirm that this player isn't an admin, and if they are, confirm that this is the owner
				if(this.isAdmin(profile.getId()) && !this.isOwner(requestor))
					return false;
				//Cannot remove the owner, can only replace them with the Owner-transfer category.
				if(this.isOwner(profile.getId()))
					return false;
				
				boolean notFound = true;
				if(this.isAdmin(profile.getId()))
				{
					//Remove from admin list if admin
					for(int i = 0; notFound || i < this.admins.size(); ++i)
					{
						if(this.admins.get(i).is(profile))
						{
							notFound = false;
							this.admins.remove(i);
						}
					}
				}
				else
				{
					//Remove from member list if member
					for(int i = 0; notFound || i < this.members.size(); ++i)
					{
						if(this.members.get(i).is(profile))
						{
							notFound = false;
							this.members.remove(i);
						}
					}
				}
				
				if(notFound)
					return false;
				
				this.markDirty();
				
			}
			return true;
		}
		else if(category.contentEquals(CATEGORY_OWNER) && this.isOwner(requestor))
		{
			//Change the owner
			GameProfile profile = this.getProfile(playerName);
			if(profile != null)
			{
				//Cannot set the owner to yourself
				if(this.owner.is(profile))
					return false;
				//Set the previous owner as an admin
				this.admins.add(this.owner);
				//Set the new owner
				this.owner = PlayerReference.of(profile);
				//Check if the new owner is an admin or a member, and if so remove them.
				for(int i = 0; i < this.admins.size(); ++i)
				{
					if(this.admins.get(i).is(this.owner))
						this.admins.remove(i--);
				}
				for(int i = 0; i < this.members.size(); ++i)
				{
					if(this.members.get(i).is(this.owner))
						this.members.remove(i--);
				}
				
				
				this.markDirty();
				
			}
			return true;
		}
		else
			return false;
	}
	
	private GameProfile getProfile(String playerName)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
			return server.getProfileCache().get(playerName).orElse(null);
		return null;
	}
	
	private Team(@Nonnull UUID teamID, @Nonnull PlayerReference owner, @Nonnull String name)
	{
		this.id = teamID;
		this.owner = owner;
		this.teamName = name;
	}
	
	public void markDirty()
	{
		if(!this.isClient)
			TradingOffice.MarkTeamDirty(this.id);
	}
	
	public CompoundTag save()
	{
		CompoundTag compound = new CompoundTag();
		if(this.id != null)
			compound.putUUID("id", this.id);
		if(this.owner != null)
			compound.put("Owner", this.owner.save());
		compound.putString("Name", this.teamName);
		
		ListTag memberList = new ListTag();
		for(int i = 0; i < this.members.size(); ++i)
		{
			CompoundTag thisMember = this.members.get(i).save();
			memberList.add(thisMember);
		}
		compound.put("Members", memberList);
		
		ListTag adminList = new ListTag();
		for(int i = 0; i < this.admins.size(); ++i)
		{
			CompoundTag thisAdmin = this.admins.get(i).save();
			adminList.add(thisAdmin);
		}
		compound.put("Admins", adminList);
		
		return compound;
	}
	
	public static Team load(@Nonnull CompoundTag compound)
	{
		PlayerReference owner = null;
		UUID id = null;
		if(compound.contains("id"))
			id = compound.getUUID("id");
		if(compound.contains("Owner", Tag.TAG_COMPOUND))
			owner = PlayerReference.load(compound.getCompound("Owner"));
		String name = compound.getString("Name");
		
		if(id != null && owner != null)
		{
			Team team = of(id, owner, name);
			
			ListTag adminList = compound.getList("Admins", Tag.TAG_COMPOUND);
			for(int i = 0; i < adminList.size(); ++i)
			{
				PlayerReference admin = PlayerReference.load(adminList.getCompound(i));
				if(admin != null)
					team.admins.add(admin);
			}
			
			ListTag memberList = compound.getList("Members", Tag.TAG_COMPOUND);
			for(int i = 0; i < memberList.size(); ++i)
			{
				PlayerReference member = PlayerReference.load(memberList.getCompound(i));
				if(member != null)
					team.members.add(member);
			}
			
			return team;
			
		}
		return null;
		
	}
	
	//Generate a team id
	public static Team of(@Nonnull UUID id, @Nonnull PlayerReference owner, @Nonnull String name)
	{
		return new Team(id, owner, name);
	}
	
	public static TeamReference referenceOf(@Nullable UUID id)
	{
		if(id == null)
			return null;
		return new TeamReference(id);
	}
	
	public static class TeamReference
	{
		
		UUID teamID;
		
		private TeamReference(UUID teamID)
		{
			this.teamID = teamID;
		}
		
		public UUID getID() { return this.teamID; }
		
		@Nullable
		public Team getTeam(boolean isClient)
		{
			if(isClient)
				return ClientTradingOffice.getTeam(this.teamID);
			else
				return TradingOffice.getTeam(teamID);
		}

	}
	
	public static Comparator<Team> sorterFor(Player player)
	{
		return new TeamSorter(player);
	}
	
	private static class TeamSorter implements Comparator<Team>
	{

		private final Player player;
		
		private TeamSorter(Player player) { this.player = player; }
		
		@Override
		public int compare(Team o1, Team o2) {
			
			if(o1.isOwner(player) && !o2.isOwner(player))
				return -1;
			if(!o1.isOwner(player) && o2.isOwner(player))
				return 1;
			
			if(o1.isAdmin(player) && !o2.isAdmin(player))
				return -1;
			if(!o1.isAdmin(player) && o2.isAdmin(player))
				return 1;
			
			return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
			
		}
		
	}
	
}
