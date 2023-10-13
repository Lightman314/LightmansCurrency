package io.github.lightman314.lightmanscurrency.common.teams;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.common.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.common.bank.reference.types.TeamBankReference;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationSaveData;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.NonNullSupplier;

public class Team {

	public static final int MAX_NAME_LENGTH = 32;
	
	private final long id;
	public long getID() { return this.id; }
	PlayerReference owner;
	public PlayerReference getOwner() { return this.owner; }
	String teamName;
	public String getName() { return this.teamName; }
	
	private boolean isClient = false;
	public Team flagAsClient() { this.isClient = true; return this; }
	
	List<PlayerReference> admins = new ArrayList<>();
	/**
	 * List of the teams admins.
	 * Does not included the owner.
	 */
	public List<PlayerReference> getAdmins() { return this.admins; }
	List<PlayerReference> members = new ArrayList<>();
	/**
	 * List of the teams members.
	 * Does not include admins or the owner.
	 */
	public List<PlayerReference> getMembers() { return this.members; }
	
	/**
	 * List of the teams members, admins, and the owner.
	 */
	public List<PlayerReference> getAllMembers() {
		List<PlayerReference> result = new ArrayList<>();
		result.addAll(this.members);
		result.addAll(this.admins);
		result.add(this.owner);
		return result;
	}
	
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
	public BankReference getBankReference() { if(this.hasBankAccount()) return TeamBankReference.of(this.id).flagAsClient(this.isClient); return null; }
	
	/**
	 * Determines if the given player is the owner of this team.
	 * Also returns true if the player is in admin mode.
	 */
	public boolean isOwner(Player player) { return (this.owner != null && this.owner.is(player)) || LCAdminMode.isAdminPlayer(player); }
	/**
	 * Determines if the given player is the owner of this team.
	 */
	public boolean isOwner(UUID playerID) { return this.owner != null && this.owner.is(playerID); }
	/**
	 * Determines if the given player is an admin or owner of this team.
	 * Also returns true if the player is in admin mode.
	 */
	public boolean isAdmin(Player player) { return PlayerReference.isInList(this.admins, player) || this.isOwner(player); }
	/**
	 * Determines if the given player is an admin or owner of this team.
	 */
	public boolean isAdmin(UUID playerID) { return PlayerReference.isInList(this.admins, playerID) || this.isOwner(playerID); }
	/**
	 * Determines if the given player is a member, admin, or owner of this team.
	 * Also returns true if the player is in admin mode.
	 */
	public boolean isMember(Player player) { return PlayerReference.isInList(this.members, player) || this.isAdmin(player); }
	/**
	 * Determines if the given player is a member, admin, or owner of this team.
	 */
	public boolean isMember(UUID playerID) { return PlayerReference.isInList(this.members, playerID) || this.isAdmin(playerID); }
	
	public void changeAddMember(Player requestor, String name) {
		if(!this.isAdmin(requestor))
			return;
		PlayerReference player = PlayerReference.of(false, name);
		if(player == null)
			return;
		//Add or remove the member
		//Confirm that this player isn't already on a list
		if(this.isMember(player.id))
			return;
		//Add the member
		this.members.add(player);
		this.markDirty();
	}
	public void changeAddAdmin(Player requestor, String name) {
		if(!this.isAdmin(requestor))
			return;
		PlayerReference player = PlayerReference.of(false, name);
		if(player == null)
			return;
		//Add or remove the admin
		//Check if the player is an admin. If they are demote them
		if(this.isAdmin(player.id))
		{
			//If the player is the owner, cannot do anything. Requires the owner transfer command
			if(this.isOwner(player.id))
				return;
			//Can only demote admins if owner or self
			if(player.is(requestor) || this.isOwner(requestor))
			{
				//Remove them from the admin list
				PlayerReference.removeFromList(this.admins, player);
				//Add them as a member
				this.members.add(player);
				this.markDirty();
			}
		}
		//If the player is not already an admin, confirm that this is the owner promoting them and promote them to admin
		else if(this.isOwner(requestor))
		{
			//Remove from the member list if making them an admin
			if(this.isMember(player.id))
				PlayerReference.removeFromList(this.members, player);
			//Add to the admin list
			this.admins.add(player);
			this.markDirty();
		}
	}
	public void changeRemoveMember(Player requestor, String name) {
		PlayerReference player = PlayerReference.of(false, name);
		if(player == null)
			return;
		if(!this.isAdmin(requestor) && !player.is(requestor))
			return;
		//Confirm that this player is a member (Can't remove them if they're not in the list in the first place)
		if(!this.isMember(player.id))
			return;

		//Confirm that this player isn't an admin, and if they are, confirm that this is the owner or self
		if(this.isAdmin(player.id) && !(this.isOwner(requestor) || PlayerReference.of(requestor).is(player)))
			return;
		//Cannot remove the owner, can only replace them with the Owner-transfer category.
		if(this.isOwner(player.id))
			return;

		boolean notFound = true;
		if(this.isAdmin(player.id))
			PlayerReference.removeFromList(this.admins, player);
		else
			PlayerReference.removeFromList(this.members, player);

		if(notFound)
			return;

		this.markDirty();
	}
	public void changeOwner(Player requestor, String name) {
		if(!this.isOwner(requestor))
			return;
		PlayerReference player = PlayerReference.of(false, name);
		if(player == null)
			return;
		//Cannot set the owner to the already present owner
		if(this.owner.is(player))
			return;
		//Set the previous owner as an admin
		this.admins.add(this.owner);
		//Set the new owner
		this.owner = player;
		//Check if the new owner is an admin or a member, and if so remove them.
		PlayerReference.removeFromList(this.admins, player);
		PlayerReference.removeFromList(this.members, player);
		this.markDirty();
	}
	
	public void changeName(Player requestor, String newName)
	{
		if(this.isAdmin(requestor))
		{
			this.teamName = newName;
			if(this.bankAccount != null)
				this.bankAccount.updateOwnersName(this.teamName);
			this.markDirty();
		}
	}
	
	public void createBankAccount(Player requestor)
	{
		if(this.hasBankAccount() || !isOwner(requestor))
			return;
		this.bankAccount = new BankAccount(this::markDirty);
		this.bankAccount.updateOwnersName(this.teamName);
		this.bankAccount.setNotificationConsumer(this::notificationSender);
		this.markDirty();
	}
	
	private void notificationSender(NonNullSupplier<Notification> notification) {
		List<PlayerReference> sendTo = new ArrayList<>();
		if(this.bankAccountLimit < 1)
			sendTo.addAll(this.members);
		if(this.bankAccountLimit < 2)
			sendTo.addAll(this.admins);
		sendTo.add(this.owner);
		for(PlayerReference player : sendTo)
		{
			if(player != null && player.id != null)
			{
				NotificationSaveData.PushNotification(player.id, notification.get());
			}
		}
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

	public final void HandleEditRequest(@Nonnull ServerPlayer requestor, @Nonnull LazyPacketData request)
	{
		if(request.contains("Disband") && this.isOwner(requestor))
			TeamSaveData.RemoveTeam(this.getID());
		if(request.contains("ChangeOwner", LazyPacketData.TYPE_STRING))
			this.changeOwner(requestor, request.getString("ChangeOwner"));
		if(request.contains("AddMember", LazyPacketData.TYPE_STRING))
			this.changeAddMember(requestor, request.getString("AddMember"));
		if(request.contains("AddAdmin", LazyPacketData.TYPE_STRING))
			this.changeAddAdmin(requestor, request.getString("AddAdmin"));
		if(request.contains("RemoveMember", LazyPacketData.TYPE_STRING))
			this.changeRemoveMember(requestor, request.getString("RemoveMember"));
		if(request.contains("ChangeName", LazyPacketData.TYPE_STRING))
			this.changeName(requestor, request.getString("ChangeName"));
		if(request.contains("CreateBankAccount"))
			this.createBankAccount(requestor);
		if(request.contains("ChangeBankLimit", LazyPacketData.TYPE_INT))
			this.changeBankLimit(requestor, request.getInt("ChangeBankLimit"));
	}
	
	private Team(long teamID, @Nonnull PlayerReference owner, @Nonnull String name)
	{
		this.id = teamID;
		this.owner = owner;
		this.teamName = name;
	}
	
	public void markDirty()
	{
		if(!this.isClient)
			TeamSaveData.MarkTeamDirty(this.id);
	}
	
	public CompoundTag save()
	{
		CompoundTag compound = new CompoundTag();
		compound.putLong("ID", this.id);
		if(this.owner != null)
			compound.put("Owner", this.owner.save());
		compound.putString("Name", this.teamName);

		PlayerReference.saveList(compound, this.members, "Members");

		PlayerReference.saveList(compound, this.admins, "Admins");
		
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
		long id = -1;
		if(compound.contains("ID"))
			id = compound.getLong("ID");
		if(compound.contains("Owner", Tag.TAG_COMPOUND))
			owner = PlayerReference.load(compound.getCompound("Owner"));
		String name = compound.getString("Name");
		
		if(owner != null)
		{
			Team team = of(id, owner, name);

			team.admins = PlayerReference.loadList(compound, "Admins");

			team.members = PlayerReference.loadList(compound, "Members");
			
			if(compound.contains("BankAccount", Tag.TAG_COMPOUND))
			{
				team.bankAccount = new BankAccount(team::markDirty, compound.getCompound("BankAccount"));
				if(compound.contains("BankLimit", Tag.TAG_INT))
					team.bankAccountLimit = compound.getInt("BankLimit");
				team.bankAccount.updateOwnersName(team.teamName);
				team.bankAccount.setNotificationConsumer(team::notificationSender);
			}
			
			return team;
			
		}
		return null;
	}
	
	public static Team of(long id, @Nonnull PlayerReference owner, @Nonnull String name) { return new Team(id, owner, name); }
	
	public static Comparator<Team> sorterFor(Player player) { return new TeamSorter(player); }

	private record TeamSorter(Player player) implements Comparator<Team>
	{

		@Override
		public int compare(Team o1, Team o2)
		{

			if (o1.isOwner(player) && !o2.isOwner(player))
				return -1;
			if (!o1.isOwner(player) && o2.isOwner(player))
				return 1;

			if (o1.isAdmin(player) && !o2.isAdmin(player))
				return -1;
			if (!o1.isAdmin(player) && o2.isAdmin(player))
				return 1;

			return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());

		}

	}
	
}
