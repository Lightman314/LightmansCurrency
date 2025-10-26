package io.github.lightman314.lightmanscurrency.common.teams;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.misc.ISidedObject;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.TeamBankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.salary.SalaryData;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.api.stats.StatTracker;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.data.types.TeamDataCache;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.common.util.TagUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Team implements ITeam, ISidedObject {

	public static final int MAX_NAME_LENGTH = 32;
	
	private final long id;
	@Override
	public long getID() { return this.id; }
	PlayerReference owner;
	@Override
	public PlayerReference getOwner() { return this.owner; }
	String teamName;
	@Override
	public String getName() { return this.teamName; }
	
	private boolean isClient = false;
	@Override
	public boolean isClient() { return this.isClient; }

	@Override
	
	public Team flagAsClient() { this.isClient = true; return this; }
	@Override
	
	public Team flagAsClient(boolean isClient) { this.isClient = isClient; return this; }
	@Override
	
	public Team flagAsClient(IClientTracker context) { this.isClient = context.isClient(); return this; }

	List<PlayerReference> admins = new ArrayList<>();
	@Override
	public List<PlayerReference> getAdmins() { return ImmutableList.copyOf(this.admins); }
	List<PlayerReference> members = new ArrayList<>();
	@Override
	public List<PlayerReference> getMembers() { return ImmutableList.copyOf(this.members); }
	
	//0 for members, 1 for admins, 2 for owners only
	int bankAccountLimit = 2;
	@Override
	public int getBankLimit() { return this.bankAccountLimit; }
    //0 for members, 1 for admins, 2 for owners only
    int bankSalaryEdit = 2;
    @Override
    public int getBankSalaryEdit() { return this.bankSalaryEdit; }

	TeamBankAccount bankAccount = null;
	@Override
	public boolean hasBankAccount() { return this.bankAccount != null; }
	@Override
	public boolean canAccessBankAccount(PlayerReference player) {
		if(this.bankAccountLimit < 1)
			return this.isMember(player);
		else if(this.bankAccountLimit < 2)
			return this.isAdmin(player);
		else
			return this.isOwner(player);
	}

	@Override
	public boolean canAccessBankAccount(Player player) {
        if(LCAdminMode.isAdminPlayer(player))
            return true;
        return this.canAccessBankAccount(PlayerReference.of(player));
	}
    @Override
    public int getSalaryLevel(PlayerReference player) {
        if(this.isOwner(player))
            return Integer.MAX_VALUE;
        if(this.isAdmin(player))
        {
            if(this.bankSalaryEdit > 1)
                return SalaryData.PERM_VIEW;
            return SalaryData.PERM_EDIT;
        }
        if(this.isMember(player))
        {
            if(this.bankSalaryEdit <= 0)
                return SalaryData.PERM_EDIT;
            return SalaryData.PERM_VIEW;
        }
        return 0;
    }

    @Override
	@Nullable
	public IBankAccount getBankAccount() { return this.bankAccount; }
	@Override
	@Nullable
	public BankReference getBankReference() { if(this.hasBankAccount()) return TeamBankReference.of(this.id).flagAsClient(this.isClient); return null; }
	private final StatTracker statTracker = new StatTracker(this::markDirty,this);
	
	@Override
	public StatTracker getStats() { return this.statTracker; }

	@Override
	public boolean isOwner(Player player) { return this.isOwner(player.getUUID()) || LCAdminMode.isAdminPlayer(player); }
	@Override
	public boolean isOwner(UUID playerID) { return this.owner != null && this.owner.is(playerID); }
	@Override
	public boolean isAdmin(Player player) { return PlayerReference.isInList(this.admins, player) || this.isOwner(player); }
	@Override
	public boolean isAdmin(UUID playerID) { return PlayerReference.isInList(this.admins, playerID) || this.isOwner(playerID); }
	@Override
	public boolean isMember(Player player) { return PlayerReference.isInList(this.members, player) || this.isAdmin(player); }
	@Override
	public boolean isMember(UUID playerID) { return PlayerReference.isInList(this.members, playerID) || this.isAdmin(playerID); }

	public void changePromoteMember(Player requestor, PlayerReference player)
	{
		if(!this.isAdmin(requestor))
			return;
		//Cannot promote the admins as they're already at the highest level (including the owner as well)
		if(this.isAdmin(player))
			return;
		if(this.isMember(player))
		{
			//Only the owner can promote members into admins
			if(!this.isOwner(requestor))
				return;
			PlayerReference.removeFromList(this.members,player);
			this.admins.add(player);
		}
		else
		{
			this.members.add(player);
            //Check for online players only when adding the member for the first time
            //Promoting them is redundant as they're already being checked for
            if(this.bankAccount != null)
                this.bankAccount.checkForOnlinePlayers();
		}
		this.markDirty();
	}

	public void changeDemoteMember(Player requestor, PlayerReference player)
	{
		boolean isSelf = player.is(requestor);
		//Only admins can demote unless you're demoting yourself
		if(!this.isAdmin(requestor) && !isSelf)
			return;
		if(this.isAdmin(player))
		{
			//Cannot demote the owner
			if(this.isOwner(player))
				return;
			//Only the owner can demote admins
			if(!this.isOwner(requestor) && !isSelf)
				return;
			PlayerReference.removeFromList(this.admins,player);
			//Add to the top of the member list if demoted from admin
			this.members.addFirst(player);
			this.markDirty();
		}
		else if(this.isMember(player))
		{
			//We've already checked if this is an admin or self requesting, so simply remove from the member list
			PlayerReference.removeFromList(this.members,player);
			this.markDirty();
		}
	}

	public void changeOwner(Player requestor, PlayerReference player) {
		if(!this.isOwner(requestor))
			return;
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
		this.bankAccount = new TeamBankAccount(this,this::markDirty);
		this.bankAccount.updateOwnersName(this.teamName);
		this.bankAccount.setNotificationConsumer(this::notificationSender);
		this.markDirty();
	}
	
	private void notificationSender(Supplier<Notification> notification) {
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
				NotificationAPI.getApi().PushPlayerNotification(player.id, notification.get());
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

    public void changeSalaryLimit(Player requestor, int newLimit)
    {
        if(isOwner(requestor) && this.bankSalaryEdit != newLimit)
        {
            this.bankSalaryEdit = newLimit;
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

	public void clearStats(Player requestor, boolean fullClear) {
		if(this.isAdmin(requestor))
			this.statTracker.clear(fullClear);
	}
	
	private Team(long teamID, PlayerReference owner, String name)
	{
		this.id = teamID;
		this.owner = owner;
		this.teamName = name;
	}
	
	public void markDirty()
	{
		if(!this.isClient)
			TeamDataCache.TYPE.get(this).markTeamDirty(this.id);
	}

	
	public CompoundTag save(HolderLookup.Provider lookup)
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
			compound.put("BankAccount", this.bankAccount.save(lookup));
			compound.putInt("BankLimit", this.bankAccountLimit);
            compound.putInt("SalaryLimit",this.bankSalaryEdit);
		}

		compound.put("Stats", this.statTracker.save(lookup));
		
		return compound;
	}
	
	public static Team load(CompoundTag compound, HolderLookup.Provider lookup)
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
				team.bankAccount = new TeamBankAccount(team,team::markDirty,compound.getCompound("BankAccount"),lookup);
				if(compound.contains("BankLimit", Tag.TAG_INT))
					team.bankAccountLimit = compound.getInt("BankLimit");
                if(compound.contains("SalaryLimit",Tag.TAG_INT))
                    team.bankSalaryEdit = compound.getInt("SalaryLimit");
				team.bankAccount.updateOwnersName(team.teamName);
				team.bankAccount.setNotificationConsumer(team::notificationSender);
			}

			if(compound.contains("Stats"))
				team.statTracker.load(compound.getCompound("Stats"), lookup);

            //Copy Salary Data to the new salary format
            if(compound.contains("LastSalaryTime"))
            {
                long lastSalaryTime = compound.getLong("LastSalaryTime");
                boolean salaryNotification = compound.getBoolean("SalaryNotification");
                long salaryDelay = compound.getLong("SalaryDelay");
                boolean creativeSalary = compound.getBoolean("CreativeSalaryMode");
                boolean extraAdminSalary = compound.getBoolean("ExtraAdminSalary");
                MoneyValue memberSalary = MoneyValue.safeLoad(compound,"MemberSalary");
                MoneyValue adminSalary = MoneyValue.safeLoad(compound,"AdminSalary");
                boolean failedLastSalary = compound.getBoolean("FailedLastSalary");
                boolean loginRequired = compound.getBoolean("SalaryLoginCheck");
                List<UUID> logins = new ArrayList<>();
                if(compound.contains("SalaryLogins"))
                    logins.addAll(TagUtil.readUUIDList(compound.getList("SalaryLogins",Tag.TAG_INT_ARRAY)));
                if(team.bankAccount != null)
                {
                    if(!memberSalary.isEmpty())
                    {
                        SalaryData salary = team.bankAccount.createNewSalary();
                        if(salary != null)
                        {
                            salary.forceLastSalaryTime(lastSalaryTime);
                            salary.setSalaryNotification(salaryNotification);
                            salary.setSalaryDelay(salaryDelay);
                            salary.setSalaryCreative(null,creativeSalary);
                            salary.setSalary(memberSalary);
                            salary.forceFailedLastSalary(failedLastSalary);
                            salary.setLoginRequiredForSalary(loginRequired);
                            salary.forceOnlinePlayerList(logins);
                            //Set custom target
                            salary.addCustomTarget(TeamBankAccount.TARGET_MEMBERS);
                            if(!extraAdminSalary)
                                salary.addCustomTarget(TeamBankAccount.TARGET_ADMINS);
                            salary.setName("Team Member Salary");
                        }
                    }
                    if(extraAdminSalary && !adminSalary.isEmpty())
                    {
                        //Create admin salary
                        SalaryData salary = team.bankAccount.createNewSalary();
                        if(salary != null)
                        {
                            salary.forceLastSalaryTime(lastSalaryTime);
                            salary.setSalaryNotification(salaryNotification);
                            salary.setSalaryDelay(salaryDelay);
                            salary.setSalaryCreative(null,creativeSalary);
                            salary.setSalary(memberSalary);
                            salary.forceFailedLastSalary(failedLastSalary);
                            salary.setLoginRequiredForSalary(loginRequired);
                            salary.forceOnlinePlayerList(logins);
                            //Set custom target
                            salary.addCustomTarget(TeamBankAccount.TARGET_ADMINS);
                            salary.setName("Team Admin Salary");
                        }
                    }
                }
            }

			return team;
			
		}
		return null;
	}
	
	public static Team of(long id, PlayerReference owner, String name) { return new Team(id, owner, name); }
	
	public static Comparator<ITeam> sorterFor(Player player) { return new TeamSorter(player); }

	private record TeamSorter(Player player) implements Comparator<ITeam>
	{

		@Override
		public int compare(ITeam o1, ITeam o2)
		{

			if (o1.isOwner(this.player) && !o2.isOwner(this.player))
				return -1;
			if (!o1.isOwner(this.player) && o2.isOwner(this.player))
				return 1;

			if (o1.isAdmin(this.player) && !o2.isAdmin(this.player))
				return -1;
			if (!o1.isAdmin(this.player) && o2.isAdmin(this.player))
				return 1;

			return o1.getName().compareToIgnoreCase(o2.getName());

		}

	}

}
