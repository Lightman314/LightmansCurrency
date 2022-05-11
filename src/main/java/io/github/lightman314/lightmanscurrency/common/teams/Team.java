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
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
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
	/**
	 * List of the teams admins.
	 * Does not included the owner.
	 */
	public List<PlayerReference> getAdmins() { return this.admins; }
	List<PlayerReference> members = Lists.newArrayList();
	/**
	 * List of the teams members.
	 * Does not include admins or the owner.
	 */
	public List<PlayerReference> getMembers() { return this.members; }
	
	//0 for members, 1 for admins, 2 for owners only
	int bankAccountLimit = 2;
	public int getBankLimit() { return this.bankAccountLimit; }
	BankAccount bankAccount = null;
	public boolean hasBankAccount() { return this.bankAccount != null; }
	public boolean canAccessBankAccount(Player player) {
		if(this.bankAccountLimit < 1)
			return this.isMember(player);
		else if(this.bankAccountLimit < 2)
			return this.isAdmin(player);
		else
			return this.isOwner(player);
	}
	public BankAccount getBankAccount() { return this.bankAccount; }
	
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
	
	public void changeAddMember(Player requestor, String name) { this.changeAny(requestor, name, CATEGORY_MEMBER); }
	public void changeAddAdmin(Player requestor, String name) { this.changeAny(requestor, name, CATEGORY_ADMIN); }
	public void changeRemoveMember(Player requestor, String name) { this.changeAny(requestor, name, CATEGORY_REMOVE); }
	public void changeOwner(Player requestor, String name) { this.changeAny(requestor, name, CATEGORY_OWNER); }
	
	public void changeName(Player requestor, String newName)
	{
		if(this.isAdmin(requestor))
		{
			this.teamName = newName;
			this.bankAccount.updateOwnersName(this.teamName);
			this.markDirty();
		}
	}
	
	public void changeAny(Player requestor, String playerName, String category)
	{
		if(category.contentEquals(CATEGORY_MEMBER) && this.isAdmin(requestor))
		{
			//Add or remove the member
			GameProfile profile = this.getProfile(playerName);
			if(profile != null)
			{
				//Confirm that this player isn't already on a list
				if(this.isMember(profile.getId()))
					return;
				//Add the member
				this.members.add(PlayerReference.of(profile));
				this.markDirty();
			}
		}
		else if(category.contentEquals(CATEGORY_ADMIN) && this.isAdmin(requestor))
		{
			//Add or remove the admin
			GameProfile profile = this.getProfile(playerName);
			if(profile != null)
			{
				//Check if the player is an admin. If they are demote them
				if(this.isAdmin(profile.getId()))
				{
					//If the player is the owner, cannot do anything. Requires the owner transfer command
					if(this.isOwner(profile.getId()))
						return;
					//Can only demote admins if owner or self
					if(PlayerReference.of(requestor).is(profile) || this.isOwner(requestor))
					{
						//Remove them from the admin list
						boolean notFound = true;
						for(int i = 0; notFound && i < this.admins.size(); ++i)
						{
							if(this.admins.get(i).is(profile))
							{
								notFound = false;
								this.admins.remove(i);
							}
						}
						//Add them as a member
						this.members.add(PlayerReference.of(profile));
						this.markDirty();
					}
				}
				//If the player is not already an admin, confirm that this is the owner promoting them and promote them to admin
				else if(this.isOwner(requestor))
				{
					//Remove from the member list if making them an admin
					if(this.isMember(profile.getId()))
					{
						boolean notFound = true;
						for(int i = 0; notFound && i < this.members.size(); ++i)
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
			}
		}
		else if(category.contentEquals(CATEGORY_REMOVE) && (this.isAdmin(requestor) || PlayerReference.of(requestor).is(playerName)))
		{
			GameProfile profile = this.getProfile(playerName);
			if(profile != null)
			{
				//Confirm that this player is a member (Can't remove them if they're not in the list in the first place)
				if(!this.isMember(profile.getId()))
					return;
				
				//Confirm that this player isn't an admin, and if they are, confirm that this is the owner or self
				if(this.isAdmin(profile.getId()) && !(this.isOwner(requestor) || PlayerReference.of(requestor).is(profile)))
					return;
				//Cannot remove the owner, can only replace them with the Owner-transfer category.
				if(this.isOwner(profile.getId()))
					return;
				
				boolean notFound = true;
				if(this.isAdmin(profile.getId()))
				{
					//Remove from admin list if admin
					for(int i = 0; notFound && i < this.admins.size(); ++i)
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
					for(int i = 0; notFound && i < this.members.size(); ++i)
					{
						if(this.members.get(i).is(profile))
						{
							notFound = false;
							this.members.remove(i);
						}
					}
				}
				
				if(notFound)
					return;
				
				this.markDirty();
				
			}
		}
		else if(category.contentEquals(CATEGORY_OWNER) && this.isOwner(requestor))
		{
			//Change the owner
			GameProfile profile = this.getProfile(playerName);
			if(profile != null)
			{
				//Cannot set the owner to yourself
				if(this.owner.is(profile))
					return;
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
		}
	}
	
	public void createBankAccount(Player requestor)
	{
		if(this.hasBankAccount() || !isOwner(requestor))
			return;
		this.bankAccount = new BankAccount(() -> this.markDirty());
		this.bankAccount.updateOwnersName(this.teamName);
		this.markDirty();
	}
	
	public void changeBankLimit(Player requestor, int newLimit)
	{
		if(isOwner(requestor) && this.bankAccountLimit != newLimit)
		{
			this.bankAccountLimit = newLimit;
			this.markDirty();
		}
	}
	
	public static int NextBankLimit(int currentLimit)
	{
		int result = currentLimit - 1;
		if(result < 0)
			result = 2;
		return result;
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
		
		//Bank Account
		if(this.bankAccount != null)
		{
			compound.put("BankAccount", this.bankAccount.save());
			compound.putInt("BankLimit", this.bankAccountLimit);
		}
		
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
			
			if(compound.contains("BankAccount", Tag.TAG_COMPOUND))
			{
				team.bankAccount = new BankAccount(team::markDirty, compound.getCompound("BankAccount"));
				if(compound.contains("BankLimit", Tag.TAG_INT))
					team.bankAccountLimit = compound.getInt("BankLimit");
				team.bankAccount.updateOwnersName(team.teamName);
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
