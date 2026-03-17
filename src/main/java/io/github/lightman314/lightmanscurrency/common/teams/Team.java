package io.github.lightman314.lightmanscurrency.common.teams;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.data.IRegistryAccess;
import io.github.lightman314.lightmanscurrency.api.misc.ISidedObject;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.TeamBankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.salary.SalaryData;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.api.stats.StatTracker;
import io.github.lightman314.lightmanscurrency.api.stats.StatType;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.data.types.TeamDataCache;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import io.github.lightman314.lightmanscurrency.common.util.TagUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

public class Team extends IRegistryAccess.Holder implements ITeam, ISidedObject {

	public static final int MAX_NAME_LENGTH = 32;

    public static final Codec<Team> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.LONG.fieldOf("id").forGetter(Team::getID),
            PlayerReference.CODEC.fieldOf("owner").forGetter(Team::getOwner),
            Codec.STRING.fieldOf("name").forGetter(Team::getName),
            PlayerReference.CODEC.listOf().fieldOf("admins").forGetter(Team::getAdmins),
            PlayerReference.CODEC.listOf().fieldOf("members").forGetter(Team::getMembers),
            Codec.INT.fieldOf("bankPermissions").forGetter(Team::getBankLimit),
            Codec.INT.fieldOf("salaryPermissions").forGetter(Team::getBankSalaryEdit),
            TeamBankAccount.CODEC.optionalFieldOf("bankAccount").forGetter(t -> Optional.ofNullable(t.bankAccount)),
            StatTracker.CODEC.fieldOf("stats").forGetter(t -> t.statTracker.getStatMap())
    ).apply(builder,Team::new));

    public static final StreamCodec<RegistryFriendlyByteBuf,Team> STREAM_CODEC = StreamHelper.composite(
            ByteBufCodecs.VAR_LONG,Team::getID,
            PlayerReference.STREAM_CODEC,Team::getOwner,
            ByteBufCodecs.STRING_UTF8,Team::getName,
            PlayerReference.LIST_STREAM_CODEC,Team::getAdmins,
            PlayerReference.LIST_STREAM_CODEC,Team::getMembers,
            ByteBufCodecs.INT,Team::getBankLimit,
            ByteBufCodecs.INT,Team::getBankSalaryEdit,
            ByteBufCodecs.optional(TeamBankAccount.STREAM_CODEC),t -> Optional.ofNullable(t.bankAccount),
            StatTracker.STREAM_CODEC,t -> t.statTracker.getStatMap(),
            Team::new);

    @Nullable
    private LazyPacketData.Builder changedData = null;

    private boolean locked = true;
    public Team initialize() { this.locked = false; return this; }

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
	public Team flagAsClient() { return this.flagAsClient(true); }
	@Override
	public Team flagAsClient(boolean isClient) { this.isClient = isClient; if(this.bankAccount != null) this.bankAccount.flagAsClient(this); return this; }
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
	private final StatTracker statTracker = new StatTracker(this::setStatsChanged,this);
	
	@Override
	public StatTracker getStats() { return this.statTracker; }

    private void setStatsChanged() {
        this.setChanged(builder -> builder.setCustom("Stats",this.statTracker.getStatMap(),ModLazyPackets.STAT_TRACKER));
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
            this.setChanged(builder -> builder.addToList("MemberChanges",this.builder()
                            .setInt("Level",2)
                            .setCustom("Player",player,ModLazyPackets.PLAYER_REFERENCE),
                    LazyPacketData.BUILDER_FACTORY));
		}
		else
		{
			this.members.add(player);
            //Check for online players only when adding the member for the first time
            //Promoting them is redundant as they're already being checked for
            if(this.bankAccount != null)
                this.bankAccount.checkForOnlinePlayers();
            this.setChanged(builder -> builder.addToList("MemberChanges",this.builder()
                    .setInt("Level",1)
                    .setCustom("Player",player,ModLazyPackets.PLAYER_REFERENCE),
                    LazyPacketData.BUILDER_FACTORY));
		}
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
            this.setChanged(builder -> builder.addToList("MemberChanges",this.builder()
                    .setInt("Level",1)
                    .setCustom("Player",player,ModLazyPackets.PLAYER_REFERENCE),
                    LazyPacketData.BUILDER_FACTORY));
		}
		else if(this.isMember(player))
		{
			//We've already checked if this is an admin or self requesting, so simply remove from the member list
			PlayerReference.removeFromList(this.members,player);
            this.setChanged(builder -> builder.addToList("MemberChanges",this.builder()
                            .setInt("Level",0)
                            .setCustom("Player",player,ModLazyPackets.PLAYER_REFERENCE),
                    LazyPacketData.BUILDER_FACTORY));
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
        PlayerReference oldOwner = this.owner;
		//Set the previous owner as an admin
		this.admins.add(oldOwner);
		//Set the new owner
		this.owner = player;
		//Check if the new owner is an admin or a member, and if so remove them.
		PlayerReference.removeFromList(this.admins, player);
		PlayerReference.removeFromList(this.members, player);
        this.setChanged(builder -> builder.addToList("MemberChanges",this.builder()
                        .setInt("Level",3)
                        .setCustom("Player",player,ModLazyPackets.PLAYER_REFERENCE),
                        LazyPacketData.BUILDER_FACTORY)
                .addToList("MemberChanges",this.builder()
                        .setInt("Level",2)
                        .setCustom("Player",oldOwner,ModLazyPackets.PLAYER_REFERENCE),
                        LazyPacketData.BUILDER_FACTORY));
	}

    protected final void forcePlayerLevel(int level,PlayerReference player)
    {
        if(level == 0)
        {
            PlayerReference.removeFromList(this.admins,player);
            PlayerReference.removeFromList(this.members,player);
            if(this.owner.is(player))
                this.owner = PlayerReference.NULL;
            return;
        }
        //Clear from selected lists
        if(level != 1)
            PlayerReference.removeFromList(this.members,player);
        if(level != 2)
            PlayerReference.removeFromList(this.admins,player);
        if(level != 3 && this.owner.is(player))
            this.owner = PlayerReference.NULL;
        //Add to targeted list
        if(level == 1)
            PlayerReference.addToList(this.members,player);
        if(level == 2)
            PlayerReference.addToList(this.admins,player);
        if(level == 3)
            this.owner = player;
    }
	
	public void changeName(Player requestor, String newName)
	{
		if(this.isAdmin(requestor))
		{
			this.teamName = newName;
			if(this.bankAccount != null)
				this.bankAccount.updateOwnersName(this.teamName);
            this.setChanged(builder -> builder.setString("Name",this.teamName));
		}
	}
	
	public void createBankAccount(Player requestor)
	{
		if(this.hasBankAccount() || !isOwner(requestor))
			return;
        this.createBankAccountInternal();
        this.setChanged(builder -> builder.setFlag("CreateBA"));
	}
    private void createBankAccountInternal()
    {
        this.bankAccount = new TeamBankAccount().forTeam(this);
        this.bankAccount.setRegistryAccess(this);
        this.bankAccount.setListener(this::setChangedNoPacket);
        this.bankAccount.updateOwnersName(this.teamName);
        this.bankAccount.setNotificationConsumer(this::notificationSender);
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
			this.setChanged(builder -> builder.setInt("BankLimit",this.bankAccountLimit));
		}
	}

    public void changeSalaryLimit(Player requestor, int newLimit)
    {
        if(isOwner(requestor) && this.bankSalaryEdit != newLimit)
        {
            this.bankSalaryEdit = newLimit;
            this.setChanged(builder -> builder.setInt("SalaryLimit",this.bankSalaryEdit));
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
    private Team(long id, PlayerReference owner, String name, List<PlayerReference> admins, List<PlayerReference> members, int bankAccountLimit, int bankSalaryEdit, Optional<TeamBankAccount> bankAccount, Map<String, StatType.Instance<?,?>> stats)
    {
        this.id = id;
        this.owner = owner;
        this.teamName = name;
        this.admins.addAll(admins);
        this.members.addAll(members);
        this.bankAccountLimit = bankAccountLimit;
        this.bankSalaryEdit = bankSalaryEdit;
        this.bankAccount = bankAccount.orElse(null);
        if(this.bankAccount != null)
            this.bankAccount.forTeam(this);
        this.statTracker.load(stats);
    }

    protected final void setChangedNoPacket() {
        if(this.locked)
            return;
        if(!this.isClient)
            TeamDataCache.TYPE.get(this).markTeamDirty(this.id);
    }
	
	public void setChanged(Consumer<LazyPacketData.Builder> dataWriter)
	{
        if(this.locked)
            return;
		if(!this.isClient)
        {
            TeamDataCache.TYPE.get(this).markTeamDirty(this.id);
            if(this.changedData == null)
                this.changedData = this.builder();
            dataWriter.accept(this.changedData);
        }
	}

    public LazyPacketData getAndCleanPacket() {
        if(this.changedData != null)
        {
            if(this.bankAccount != null)
            {
                LazyPacketData bankPacket = this.bankAccount.getAndCleanPacket();
                if(!bankPacket.isEmpty())
                    this.changedData.setMap("BankAccount",bankPacket);
            }
            LazyPacketData result = this.changedData.build();
            this.changedData = null;
            return result;
        }
        return this.builder().build();
    }

    public void handlePacket(LazyPacketData data)
    {
        if(data.contains("CreateBA"))
            this.createBankAccountInternal();
        if(data.contains("BankAccount") && this.bankAccount != null)
            this.bankAccount.handlePacket(data.getMap("BankAccount"));
        if(data.contains("Stats"))
            this.statTracker.load(data.getCustom("Stats",ModLazyPackets.STAT_TRACKER));
        if(data.contains("MemberChanges"))
        {
            for(LazyPacketData entry : data.getList("MemberChanges", LazyPacketData.class))
            {
                int level = entry.getInt("Level");
                PlayerReference player = entry.getCustom("Player",ModLazyPackets.PLAYER_REFERENCE);
                if(player != null)
                    this.forcePlayerLevel(level,player);
            }
        }
        if(data.contains("Name"))
            this.teamName = data.getString("Name");
        if(data.contains("BankLimit"))
            this.bankAccountLimit = data.getInt("BankLimit");
        if(data.contains("SalaryLimit"))
            this.bankSalaryEdit = data.getInt("SalaryLimit");
    }

    @Deprecated
	private static Team loadOldData(CompoundTag compound, HolderLookup.Provider lookup)
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
                team.bankAccount = new TeamBankAccount().forTeam(team);
                BankAccount ba = BankAccount.loadOldData(compound,lookup);
                team.bankAccount.copyFrom(ba);
				if(compound.contains("BankLimit", Tag.TAG_INT))
					team.bankAccountLimit = compound.getInt("BankLimit");
                if(compound.contains("SalaryLimit",Tag.TAG_INT))
                    team.bankSalaryEdit = compound.getInt("SalaryLimit");
				team.bankAccount.updateOwnersName(team.teamName);
				team.bankAccount.setNotificationConsumer(team::notificationSender);
			}

			if(compound.contains("Stats"))
				team.statTracker.load(compound.getCompound("Stats"),DataContext.createNBT(lookup));

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
                            salary.setSalary(adminSalary);
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
