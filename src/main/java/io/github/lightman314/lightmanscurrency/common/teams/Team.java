package io.github.lightman314.lightmanscurrency.common.teams;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.TeamBankReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.stats.StatKeys;
import io.github.lightman314.lightmanscurrency.api.stats.StatTracker;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationSaveData;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.DepositWithdrawNotification;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

public class Team implements ITeam {

	public static final int MAX_NAME_LENGTH = 32;
	
	private final long id;
	@Override
	public long getID() { return this.id; }
	PlayerReference owner;
	@Override
	@Nonnull
	public PlayerReference getOwner() { return this.owner; }
	String teamName;
	@Override
	@Nonnull
	public String getName() { return this.teamName; }
	
	private boolean isClient = false;
	@Override
	public boolean isClient() { return this.isClient; }

	public Team flagAsClient() { this.isClient = true; return this; }
	
	List<PlayerReference> admins = new ArrayList<>();
	@Override
	@Nonnull
	public List<PlayerReference> getAdmins() { return ImmutableList.copyOf(this.admins); }
	List<PlayerReference> members = new ArrayList<>();
	@Override
	@Nonnull
	public List<PlayerReference> getMembers() { return ImmutableList.copyOf(this.members); }
	
	//0 for members, 1 for admins, 2 for owners only
	int bankAccountLimit = 2;
	@Override
	public int getBankLimit() { return this.bankAccountLimit; }
	BankAccount bankAccount = null;
	@Override
	public boolean hasBankAccount() { return this.bankAccount != null; }

	@Override
	public boolean canAccessBankAccount(@Nonnull PlayerReference player) {
		if(this.bankAccountLimit < 1)
			return this.isMember(player);
		else if(this.bankAccountLimit < 2)
			return this.isAdmin(player);
		else
			return this.isOwner(player);
	}

	@Override
	public boolean canAccessBankAccount(@Nonnull Player player) {
		if(this.bankAccountLimit < 1)
			return this.isMember(player);
		else if(this.bankAccountLimit < 2)
			return this.isAdmin(player);
		else
			return this.isOwner(player);
	}
	@Override
	@Nullable
	public IBankAccount getBankAccount() { return this.bankAccount; }
	public BankReference getBankReference() { if(this.hasBankAccount()) return TeamBankReference.of(this.id).flagAsClient(this.isClient); return null; }

	private final StatTracker statTracker = new StatTracker(this::markDirty,this);
	@Nonnull
	@Override
	public StatTracker getStats() { return this.statTracker; }

	//Salary Settings
	long lastSalaryTime = 0;
	@Override
	public long getLastSalaryTime() { return this.lastSalaryTime; }
	public void setAutoSalaryEnabled(@Nonnull Player player, boolean enabled)
	{
		if(!this.isAdmin(player))
			return;
		if(enabled)
			this.lastSalaryTime = TimeUtil.getCurrentTime();
		else
			this.lastSalaryTime = 0;
		this.markDirty();
	}
	boolean salaryNotification = true;
	@Override
	public boolean getSalaryNotification() { return this.salaryNotification; }
	public void setSalaryNotification(@Nonnull Player player, boolean salaryNotification) { if(!this.isAdmin(player)) return; this.salaryNotification = salaryNotification; this.markDirty(); }
	long salaryDelay = 0;
	@Override
	public long getSalaryDelay() { return this.salaryDelay; }
	public void setSalaryDelay(@Nonnull Player player, long salaryDelay) { if(!this.isAdmin(player)) return; this.salaryDelay = salaryDelay; this.markDirty(); }
	boolean creativeSalaryMode = false;
	@Override
	public boolean isSalaryCreative() { return this.creativeSalaryMode; }
	public void setSalaryMoneyCreative(@Nonnull Player player, boolean creative)  { if(creative && !LCAdminMode.isAdminPlayer(player)) return; this.creativeSalaryMode = creative; this.markDirty(); }
	boolean seperateAdminSalary = false;
	@Override
	public boolean isAdminSalarySeperate() { return this.seperateAdminSalary; }
	public void setAdminSalarySeperate(@Nonnull Player player, boolean seperateAdminSalary) { if(!this.isOwner(player)) return; this.seperateAdminSalary = seperateAdminSalary; this.markDirty(); }
	MoneyValue memberSalary = MoneyValue.empty();
	@Nonnull
	@Override
	public MoneyValue getMemberSalary() { return this.memberSalary; }
	public void setMemberSalary(@Nonnull Player player, @Nonnull MoneyValue memberSalary) { if(!this.isAdmin(player)) return; this.memberSalary = memberSalary; this.markDirty(); }
	MoneyValue adminSalary = MoneyValue.empty();
	@Nonnull
	@Override
	public MoneyValue getAdminSalary() { return this.adminSalary; }
	public void setAdminSalary(@Nonnull Player player, @Nonnull MoneyValue adminSalary) { if(!this.isOwner(player)) return; this.adminSalary = adminSalary; this.markDirty(); }
	boolean failedLastSalary = false;
	@Override
	public boolean failedLastSalaryAttempt() { return this.failedLastSalary; }

	@Nonnull
	@Override
	public List<MoneyValue> getTotalSalaryCost() {
		if(this.seperateAdminSalary)
		{
			List<MoneyValue> result = new ArrayList<>();
			MoneyValue memberCost = this.memberSalary.fromCoreValue(this.memberSalary.getCoreValue() * this.members.size());
			MoneyValue adminCost = this.adminSalary.fromCoreValue(this.adminSalary.getCoreValue() * this.getAdminsAndOwner().size());
			if(memberCost.isEmpty())
			{
				if(adminCost.isEmpty())
					return ImmutableList.of();
				else
					return ImmutableList.of(adminCost);
			}
			else if(adminCost.isEmpty())
				return ImmutableList.of(memberCost);
			if(memberCost.sameType(adminCost))
				return ImmutableList.of(memberCost.addValue(adminCost));
			return ImmutableList.of(memberCost,adminCost);
		}
		else
			return ImmutableList.of(this.memberSalary.fromCoreValue(this.memberSalary.getCoreValue() * this.getMemberCount()));
	}

	@Override
	public boolean canAffordNextSalary() {
		if(this.creativeSalaryMode)
			return true;
		IBankAccount account = this.getBankAccount();
		if(account == null)
			return false;
		for(MoneyValue cost : this.getTotalSalaryCost())
		{
			if(!account.getMoneyStorage().containsValue(cost))
				return false;
		}
		return true;
	}

	@Override
	public boolean isOwner(@Nonnull Player player) { return (this.owner != null && this.owner.is(player)) || LCAdminMode.isAdminPlayer(player); }
	@Override
	public boolean isOwner(@Nonnull UUID playerID) { return this.owner != null && this.owner.is(playerID); }
	@Override
	public boolean isAdmin(@Nonnull Player player) { return PlayerReference.isInList(this.admins, player) || this.isOwner(player); }
	@Override
	public boolean isAdmin(@Nonnull UUID playerID) { return PlayerReference.isInList(this.admins, playerID) || this.isOwner(playerID); }

	@Override
	public boolean isMember(@Nonnull Player player) { return PlayerReference.isInList(this.members, player) || this.isAdmin(player); }
	@Override
	public boolean isMember(@Nonnull UUID playerID) { return PlayerReference.isInList(this.members, playerID) || this.isAdmin(playerID); }
	
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

		if(this.isAdmin(player.id))
			PlayerReference.removeFromList(this.admins, player);
		else
			PlayerReference.removeFromList(this.members, player);

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

	public void clearStats(Player requestor, boolean fullClear) {
		if(this.isAdmin(requestor))
			this.statTracker.clear(fullClear);
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

	@Nonnull
	public CompoundTag save(@Nonnull HolderLookup.Provider lookup)
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
		}

		compound.put("Stats", this.statTracker.save(lookup));

		compound.putLong("LastSalaryTime", this.lastSalaryTime);
		compound.putBoolean("SalaryNotification", this.salaryNotification);
		compound.putLong("SalaryDelay",this.salaryDelay);
		compound.putBoolean("CreativeSalaryMode",this.creativeSalaryMode);
		compound.putBoolean("ExtraAdminSalary",this.seperateAdminSalary);
		compound.put("MemberSalary",this.memberSalary.save());
		compound.put("AdminSalary",this.adminSalary.save());
		compound.putBoolean("FailedLastSalary",this.failedLastSalary);
		
		return compound;
	}
	
	public static Team load(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup)
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
				team.bankAccount = new BankAccount(team::markDirty, compound.getCompound("BankAccount"), lookup);
				if(compound.contains("BankLimit", Tag.TAG_INT))
					team.bankAccountLimit = compound.getInt("BankLimit");
				team.bankAccount.updateOwnersName(team.teamName);
				team.bankAccount.setNotificationConsumer(team::notificationSender);
			}

			if(compound.contains("Stats"))
				team.statTracker.load(compound.getCompound("Stats"), lookup);

			if(compound.contains("LastSalaryTime"))
				team.lastSalaryTime = compound.getLong("LastSalaryTime");
			if(compound.contains("SalaryNotification"))
				team.salaryNotification = compound.getBoolean("SalaryNotification");
			if(compound.contains("SalaryDelay"))
				team.salaryDelay = compound.getLong("SalaryDelay");
			if(compound.contains("CreativeSalaryMode"))
				team.creativeSalaryMode = compound.getBoolean("CreativeSalaryMode");
			if(compound.contains("ExtraAdminSalary"))
				team.seperateAdminSalary = compound.getBoolean("ExtraAdminSalary");
			if(compound.contains("MemberSalary"))
				team.memberSalary = MoneyValue.load(compound.getCompound("MemberSalary"));
			if(compound.contains("AdminSalary"))
				team.adminSalary = MoneyValue.load(compound.getCompound("AdminSalary"));
			if(compound.contains("FailedLastSalary"))
				team.failedLastSalary = compound.getBoolean("FailedLastSalary");

			return team;
			
		}
		return null;
	}
	
	public static Team of(long id, @Nonnull PlayerReference owner, @Nonnull String name) { return new Team(id, owner, name); }
	
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

	public void tick()
	{
		if(this.lastSalaryTime > 0 && this.salaryDelay > 0)
		{
			if(!TimeUtil.compareTime(this.salaryDelay,this.lastSalaryTime))
			{
				this.lastSalaryTime = TimeUtil.getCurrentTime();
				this.forcePaySalaries();
			}
		}
	}

	@Override
	public void forcePaySalaries() {
		if(!this.hasBankAccount())
			return;
		//Comfirm that we can afford to pay everyone
		if(!this.canAffordNextSalary())
		{
			this.failedLastSalary = true;
			this.markDirty();
			return;
		}
		this.failedLastSalary = false;
		this.statTracker.incrementStat(StatKeys.Generic.SALARY_TRIGGERS,1);
		for(MoneyValue payment : this.getTotalSalaryCost())
		{
			if(!this.creativeSalaryMode)
			{
				this.bankAccount.pushNotification(() -> new DepositWithdrawNotification.Trader(this.teamName,this.bankAccount.getName(),false,payment),this.salaryNotification);
				this.bankAccount.withdrawMoney(payment);
			}
			//Still track the total salary paid even if it's not actually taken from our bank account
			this.statTracker.incrementStat(StatKeys.Generic.MONEY_PAID,payment);
		}
		if(!this.memberSalary.isEmpty())
		{
			List<PlayerReference> membersToPay = this.seperateAdminSalary ? this.members : this.getAllMembers();
			for(PlayerReference member : membersToPay)
				this.payMember(member,this.memberSalary);
		}
		if(this.seperateAdminSalary && !this.adminSalary.isEmpty())
		{
			for(PlayerReference admin : this.getAdminsAndOwner())
				this.payMember(admin,this.adminSalary);
		}
		this.markDirty();
	}

	private void payMember(@Nonnull PlayerReference member, @Nonnull final MoneyValue value)
	{
		final IBankAccount memberAccount = PlayerBankReference.of(member).get();
		if(memberAccount != null)
		{
			memberAccount.pushNotification(() -> new DepositWithdrawNotification.Trader(this.teamName,memberAccount.getName(),true,value),this.salaryNotification);
			memberAccount.depositMoney(value);
		}
	}

}
