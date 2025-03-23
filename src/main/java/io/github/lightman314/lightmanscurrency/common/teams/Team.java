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
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.TeamBankReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.api.stats.StatKeys;
import io.github.lightman314.lightmanscurrency.api.stats.StatTracker;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.data.types.TeamDataCache;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.DepositWithdrawNotification;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.common.util.TagUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

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
	BankAccount bankAccount = null;
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
	@Override
	@Nullable
	public BankReference getBankReference() { if(this.hasBankAccount()) return TeamBankReference.of(this.id).flagAsClient(this.isClient); return null; }

	private final StatTracker statTracker = new StatTracker(this::markDirty,this);
	
	@Override
	public StatTracker getStats() { return this.statTracker; }

	//Salary Settings
	private final List<UUID> onlineDuringSalary = new ArrayList<>();
	private boolean requireLoginForSalary = false;
	@Override
	public boolean getLoginRequiredForSalary() { return this.requireLoginForSalary; }
	public void setLoginRequiredForSalary(Player player, boolean requireLoginForSalary)
	{
		if(!this.isAdmin(player))
			return;
		this.requireLoginForSalary = requireLoginForSalary;
		this.checkForOnlinePlayers();
		this.markDirty();
	}
	long lastSalaryTime = 0;
	@Override
	public long getLastSalaryTime() { return this.lastSalaryTime; }
	public void setAutoSalaryEnabled(Player player, boolean enabled)
	{
		if(!this.isAdmin(player))
			return;
		if(enabled)
		{
			this.lastSalaryTime = TimeUtil.getCurrentTime();
			this.onlineDuringSalary.clear();
			this.checkForOnlinePlayers();
		}
		else
			this.lastSalaryTime = 0;
		this.markDirty();
	}
	boolean salaryNotification = true;
	@Override
	public boolean getSalaryNotification() { return this.salaryNotification; }
	public void setSalaryNotification(Player player, boolean salaryNotification) { if(!this.isAdmin(player)) return; this.salaryNotification = salaryNotification; this.markDirty(); }
	long salaryDelay = 0;
	@Override
	public long getSalaryDelay() { return this.salaryDelay; }
	public void setSalaryDelay(Player player, long salaryDelay) { if(!this.isAdmin(player)) return; this.salaryDelay = salaryDelay; this.markDirty(); }
	boolean creativeSalaryMode = false;
	@Override
	public boolean isSalaryCreative() { return this.creativeSalaryMode; }
	public void setSalaryMoneyCreative(Player player, boolean creative)  { if(creative && !LCAdminMode.isAdminPlayer(player)) return; this.creativeSalaryMode = creative; this.markDirty(); }
	boolean seperateAdminSalary = false;
	@Override
	public boolean isAdminSalarySeperate() { return this.seperateAdminSalary; }
	public void setAdminSalarySeperate(Player player, boolean seperateAdminSalary) { if(!this.isOwner(player)) return; this.seperateAdminSalary = seperateAdminSalary; this.markDirty(); }
	MoneyValue memberSalary = MoneyValue.empty();
	
	@Override
	public MoneyValue getMemberSalary() { return this.memberSalary; }
	public void setMemberSalary(Player player, MoneyValue memberSalary) { if(!this.isAdmin(player)) return; this.memberSalary = memberSalary; this.markDirty(); }
	MoneyValue adminSalary = MoneyValue.empty();
	
	@Override
	public MoneyValue getAdminSalary() { return this.adminSalary; }
	public void setAdminSalary(Player player, MoneyValue adminSalary) { if(!this.isOwner(player)) return; this.adminSalary = adminSalary; this.markDirty(); }
	boolean failedLastSalary = false;
	@Override
	public boolean failedLastSalaryAttempt() { return this.failedLastSalary; }

	
	@Override
	public List<MoneyValue> getTotalSalaryCost(boolean validateOnlinePlayers) {
		if(this.seperateAdminSalary)
		{
			List<MoneyValue> result = new ArrayList<>();
			int validMemberCount;
			int validAdminCount;
			if(validateOnlinePlayers && this.requireLoginForSalary)
			{
				validMemberCount = (int)this.members.stream().filter(m -> this.onlineDuringSalary.contains(m.id)).count();
				validAdminCount = (int)this.getAdminsAndOwner().stream().filter(m -> this.onlineDuringSalary.contains(m.id)).count();
			}
			else
			{
				validMemberCount = this.members.size();
				validAdminCount = this.getAdminsAndOwner().size();
			}
			MoneyValue memberCost = this.memberSalary.fromCoreValue(this.memberSalary.getCoreValue() * validMemberCount);
			MoneyValue adminCost = this.adminSalary.fromCoreValue(this.adminSalary.getCoreValue() * validAdminCount);
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
		{
			int validMemberCount;
			if(validateOnlinePlayers && this.requireLoginForSalary)
				validMemberCount = (int)this.getAllMembers().stream().filter(m -> this.onlineDuringSalary.contains(m.id)).count();
			else
				validMemberCount = this.getMemberCount();
			return ImmutableList.of(this.memberSalary.fromCoreValue(this.memberSalary.getCoreValue() * validMemberCount));
		}
	}

	@Override
	public boolean canAffordNextSalary(boolean validateOnlinePlayers) {
		if(this.creativeSalaryMode)
			return true;
		IBankAccount account = this.getBankAccount();
		if(account == null)
			return false;
		for(MoneyValue cost : this.getTotalSalaryCost(validateOnlinePlayers))
		{
			if(!account.getMoneyStorage().containsValue(cost))
				return false;
		}
		return true;
	}

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
			this.checkForOnlinePlayers();
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
				NotificationAPI.API.PushPlayerNotification(player.id, notification.get());
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
		}

		compound.put("Stats", this.statTracker.save(lookup));

		compound.putBoolean("SalaryLoginCheck",this.requireLoginForSalary);
		compound.putLong("LastSalaryTime", this.lastSalaryTime);
		compound.putBoolean("SalaryNotification", this.salaryNotification);
		compound.putLong("SalaryDelay",this.salaryDelay);
		compound.putBoolean("CreativeSalaryMode",this.creativeSalaryMode);
		compound.putBoolean("ExtraAdminSalary",this.seperateAdminSalary);
		compound.put("MemberSalary",this.memberSalary.save());
		compound.put("AdminSalary",this.adminSalary.save());
		compound.putBoolean("FailedLastSalary",this.failedLastSalary);

		compound.put("SalaryLogins", TagUtil.writeUUIDList(this.onlineDuringSalary));
		
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
			if(compound.contains("SalaryLoginCheck"))
				team.requireLoginForSalary = compound.getBoolean("SalaryLoginCheck");
			if(compound.contains("SalaryLogins"))
				team.onlineDuringSalary.addAll(TagUtil.readUUIDList(compound.getList("SalaryLogins",Tag.TAG_INT_ARRAY)));

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

	public void tick()
	{
		if(this.lastSalaryTime > 0 && this.salaryDelay > 0)
		{
			if(!TimeUtil.compareTime(this.salaryDelay,this.lastSalaryTime))
			{
				this.lastSalaryTime = TimeUtil.getCurrentTime();
				this.forcePaySalaries(true);
			}
		}
	}

	public void onPlayerJoin(ServerPlayer player)
	{
		if(this.isMember(PlayerReference.of(player)))
			this.flagPlayerAsOnline(player);
	}

	@Override
	public void forcePaySalaries(boolean validateOnlinePlayers) {
		if(!this.hasBankAccount())
			return;
		//Comfirm that we can afford to pay everyone
		if(!this.canAffordNextSalary(validateOnlinePlayers))
		{
			this.failedLastSalary = true;
			this.markDirty();
			return;
		}
		this.failedLastSalary = false;
		this.statTracker.incrementStat(StatKeys.Generic.SALARY_TRIGGERS,1);
		for(MoneyValue payment : this.getTotalSalaryCost(validateOnlinePlayers))
		{
			if(!this.creativeSalaryMode)
			{
				this.bankAccount.pushNotification(() -> new DepositWithdrawNotification.Custom(this.teamName,this.bankAccount.getName(),false,payment),this.salaryNotification);
				this.bankAccount.withdrawMoney(payment);
			}
			//Still track the total salary paid even if it's not actually taken from our bank account
			this.statTracker.incrementStat(StatKeys.Generic.MONEY_PAID,payment);
		}
		if(!this.memberSalary.isEmpty())
		{
			List<PlayerReference> membersToPay = this.seperateAdminSalary ? this.members : this.getAllMembers();
			if(this.requireLoginForSalary)
				membersToPay = membersToPay.stream().filter(m -> this.onlineDuringSalary.contains(m.id)).toList();
			for(PlayerReference member : membersToPay)
				this.payMember(member,this.memberSalary);
		}
		if(this.seperateAdminSalary && !this.adminSalary.isEmpty())
		{
			List<PlayerReference> adminsToPay = this.getAdminsAndOwner();
			if(this.requireLoginForSalary)
				adminsToPay = adminsToPay.stream().filter(m -> this.onlineDuringSalary.contains(m.id)).toList();
			for(PlayerReference admin : adminsToPay)
				this.payMember(admin,this.adminSalary);
		}
		if(validateOnlinePlayers)
		{
			this.onlineDuringSalary.clear();
			this.checkForOnlinePlayers();
		}
		this.markDirty();
	}

	private void checkForOnlinePlayers()
	{
		if(!this.requireLoginForSalary || !this.isAutoSalaryEnabled())
			return;
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server == null)
			return;
		List<PlayerReference> members = this.getAllMembers();
		for(ServerPlayer player : server.getPlayerList().getPlayers())
		{
			if(this.isMember(PlayerReference.of(player)))
				this.flagPlayerAsOnline(player);
		}
	}

	private void flagPlayerAsOnline(ServerPlayer player)
	{
		//Online state is not relevant if no auto-salary is enabled, or if the login requirement is not required
		if(!this.requireLoginForSalary || !this.isAutoSalaryEnabled())
			return;
		UUID playerID = player.getUUID();
		if(!this.onlineDuringSalary.contains(playerID))
		{
			this.onlineDuringSalary.add(playerID);
			this.markDirty();
		}
	}

	private void payMember(PlayerReference member, final MoneyValue value)
	{
		final IBankAccount memberAccount = PlayerBankReference.of(member).get();
		if(memberAccount != null)
		{
			memberAccount.pushNotification(() -> new DepositWithdrawNotification.Custom(this.teamName,memberAccount.getName(),true,value),this.salaryNotification);
			memberAccount.depositMoney(value);
		}
	}

}
