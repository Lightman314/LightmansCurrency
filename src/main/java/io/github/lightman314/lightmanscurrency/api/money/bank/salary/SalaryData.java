package io.github.lightman314.lightmanscurrency.api.money.bank.salary;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.stats.StatKey;
import io.github.lightman314.lightmanscurrency.api.stats.StatKeys;
import io.github.lightman314.lightmanscurrency.api.stats.StatTracker;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.DepositWithdrawNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.SalaryPaymentNotification;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.util.TagUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class SalaryData {

    public static final int PERM_VIEW = 1;
    public static final int PERM_EDIT = 2;

    private final IBankAccount account;
    private final Function<SalaryData,Integer> index;
    public SalaryData(IBankAccount account, Function<SalaryData,Integer> index) { this.account = account; this.index = index; }

    //Old Team Salary Settings
    public boolean isAutoSalaryEnabled() { return this.lastSalaryTime > 0; }

    private final List<UUID> onlineDuringSalary = new ArrayList<>();
    public void forceOnlinePlayerList(List<UUID> onlineDuringSalary)
    {
        this.onlineDuringSalary.clear();
        this.onlineDuringSalary.addAll(onlineDuringSalary);
        this.markDirty();
    }
    private boolean requireLoginForSalary = false;
    public boolean getLoginRequiredForSalary() { return this.requireLoginForSalary; }
    public void setLoginRequiredForSalary(boolean requireLoginForSalary)
    {
        if(this.requireLoginForSalary == requireLoginForSalary)
            return;
        this.requireLoginForSalary = requireLoginForSalary;
        this.checkForOnlinePlayers(false);
        this.validateSalaryCache();
        this.markDirty();
    }
    long lastSalaryTime = 0;
    public long getLastSalaryTime() { return this.lastSalaryTime; }
    //Used by Teams when converting old salary data into new salary data
    public void forceLastSalaryTime(long lastSalaryTime) {
        if(this.lastSalaryTime == lastSalaryTime)
            return;
        this.lastSalaryTime = lastSalaryTime;
        if(this.lastSalaryTime > 0)
            this.checkForOnlinePlayers(false);
        this.validateSalaryCache();
        this.markDirty();
    }
    public void setAutoSalaryEnabled(boolean enabled)
    {
        if(enabled == this.isAutoSalaryEnabled())
            return;
        if(enabled)
        {
            this.lastSalaryTime = TimeUtil.getCurrentTime();
            this.onlineDuringSalary.clear();
            this.checkForOnlinePlayers(false);
        }
        else
            this.lastSalaryTime = 0;
        this.validateSalaryCache();
        this.markDirty();
    }
    boolean salaryNotification = true;
    public boolean getSalaryNotification() { return this.salaryNotification; }
    public void setSalaryNotification(boolean salaryNotification) {
        if(this.salaryNotification == salaryNotification)
            return;
        this.salaryNotification = salaryNotification;
        this.markDirty();
    }
    long salaryDelay = 0;
    public long getSalaryDelay() { return this.salaryDelay; }
    public void setSalaryDelay(long salaryDelay) {
        if(this.salaryDelay == salaryDelay)
            return;
        this.salaryDelay = salaryDelay;
        this.markDirty();
    }
    boolean creativeSalaryMode = false;
    public boolean isSalaryCreative() { return this.creativeSalaryMode; }
    public void setSalaryCreative(@Nullable Player player, boolean creative)  { if(player != null && creative && !LCAdminMode.isAdminPlayer(player)) return; this.creativeSalaryMode = creative; this.markDirty(); }

    private MoneyValue salary = MoneyValue.empty();
    public MoneyValue getSalary() { return this.salary; }
    public void setSalary(MoneyValue salary) {
        this.salary = salary;
        this.validateSalaryCache();
        this.markDirty();
    }

    private boolean cacheInvalid = false;
    private MoneyValue quickSalaryCache = MoneyValue.empty();
    private MoneyValue totalSalaryCache = MoneyValue.empty();

    //Custom Bank Salary Settings
    String name = "";
    public String getInternalName() { return this.name; }
    public void setName(String name) { this.name = name; this.markDirty(); }
    public Component getName() {
        if(this.name.isBlank())
            return LCText.GUI_BANK_SALARY_NAME.get(this.account.getOwnerName(),this.index.apply(this) + 1);
        return EasyText.literal(this.name);
    }

    List<BankReference> directTargets = new ArrayList<>();
    public List<BankReference> getDirectTargets() { return ImmutableList.copyOf(this.directTargets); }
    public void addTarget(BankReference target) {
        if(target.get() == this.account)
            return;
        for(BankReference t : this.directTargets)
        {
            if(t.equals(target))
                return;
        }
        this.directTargets.add(target);
        if(this.requireLoginForSalary)
            this.checkForOnlinePlayers(false);
        this.validateSalaryCache();
        this.markDirty();
    }
    public void removeTarget(BankReference target) {
        if(this.directTargets.remove(target))
        {
            this.validateSalaryCache();
            this.markDirty();
        }
    }

    Set<String> customTargets = new HashSet<>();
    public Set<String> getCustomTargetSelections() { return ImmutableSet.copyOf(this.customTargets); }
    public List<CustomTarget> getCustomTargets()
    {
        List<CustomTarget> options = new ArrayList<>();
        Map<String,CustomTarget> data = this.account.extraSalaryTargets();
        for(String key : this.customTargets)
        {
            CustomTarget entry = data.get(key);
            if(entry != null)
                options.add(entry);
        }
        return options;
    }
    public List<BankReference> getCustomTargetAccounts()
    {
        List<BankReference> list = new ArrayList<>();
        for(CustomTarget t : this.getCustomTargets())
        {
            for(BankReference br : t.getTargets())
                this.addToBankList(list,br);
        }
        return list;
    }
    public void addCustomTarget(String key) {
        if(this.account.extraSalaryTargets().containsKey(key) && !this.customTargets.contains(key))
        {
            this.customTargets.add(key);
            this.validateSalaryCache();
            this.markDirty();
        }
    }
    public void removeCustomTarget(String key)
    {
        if(this.customTargets.contains(key))
        {
            this.customTargets.remove(key);
            this.validateSalaryCache();
            this.markDirty();
        }
    }

    boolean failedLastSalary = false;
    public boolean failedLastSalaryAttempt() { return this.failedLastSalary; }
    public void forceFailedLastSalary(boolean failedLast)
    {
        if(this.failedLastSalary == failedLast)
            return;
        this.failedLastSalary = failedLast;
        this.markDirty();
    }

    public void HandleEditMessage(Player player,LazyPacketData message)
    {
        if(message.contains("EnableAutoSalary"))
            this.setAutoSalaryEnabled(message.getBoolean("EnableAutoSalary"));
        if(message.contains("LoginRequirement"))
            this.setLoginRequiredForSalary(message.getBoolean("LoginRequirement"));
        if(message.contains("SalaryNotification"))
            this.setSalaryNotification(message.getBoolean("SalaryNotification"));
        if(message.contains("SalaryDelay"))
            this.setSalaryDelay(message.getLong("SalaryDelay"));
        if(message.contains("TriggerSalary"))
            this.forcePaySalaries(false);
        if(message.contains("CreativeSalary"))
            this.setSalaryCreative(player,message.getBoolean("CreativeSalary"));
        if(message.contains("ChangeName"))
            this.setName(message.getString("ChangeName"));
        if(message.contains("Salary"))
            this.setSalary(message.getMoneyValue("Salary"));
        if(message.contains("CustomTarget"))
        {
            String target = message.getString("CustomTarget");
            if(message.getBoolean("NewState"))
                this.addCustomTarget(target);
            else
                this.removeCustomTarget(target);
        }
        if(message.contains("DirectTarget"))
        {
            BankReference target = BankReference.load(message.getNBT("DirectTarget")).flagAsClient(this.account);
            if(target != null)
            {
                if(message.getBoolean("NewState"))
                    this.addTarget(target);
                else
                    this.removeTarget(target);
            }
        }
        if(message.contains("DeleteSalary"))
            this.account.deleteSalary(this);

    }

    public List<BankReference> getAllTargets()
    {
        List<BankReference> results = new ArrayList<>(this.directTargets);
        for(CustomTarget bonus : this.getCustomTargets())
        {
            for(BankReference target : bonus.getTargets())
                this.addToBankList(results,target);
        }
        return results.stream().filter(BankReference::isValid).toList();
    }
    private void validateTargetsExist()
    {
        boolean changed = false;
        for(BankReference target : new ArrayList<>(this.directTargets))
        {
            if(!target.isValid())
            {
                this.directTargets.remove(target);
                changed = true;
            }
        }
        if(changed)
        {
            this.validateSalaryCache();
            this.markDirty();
        }
    }
    private void addToBankList(List<BankReference> list, BankReference toAdd)
    {
        if(list.stream().anyMatch(br -> br.equals(toAdd)))
            return;
        list.add(toAdd.flagAsClient(this.account));
    }
    public boolean isTarget(Player player) { return this.getAllTargets().stream().anyMatch(br -> br.isSalaryTarget(player)); }

    @Deprecated(since = "2.3.0.2")
    public MoneyValue getTotalSalaryCost(boolean validateOnlinePlayers) { return this.getTotalSalaryCost(validateOnlinePlayers,true); }
    public MoneyValue getTotalSalaryCost(boolean validateOnlinePlayers, boolean performCalculation) {
        if(performCalculation)
        {
            if(this.salary.isEmpty())
                return MoneyValue.empty();
            int validMemberCount;
            if(validateOnlinePlayers && this.requireLoginForSalary)
                validMemberCount = (int)this.getAllTargets().stream().filter(this::wasOnline).count();
            else
                validMemberCount = this.getAllTargets().size();
            return this.salary.fromCoreValue(this.salary.getCoreValue() * validMemberCount);
        }
        else
        {
            if(this.cacheInvalid)
            {
                this.validateSalaryCache();
                this.markDirty();
            }
            if(validateOnlinePlayers && this.requireLoginForSalary)
                return this.totalSalaryCache;
            return this.quickSalaryCache;
        }
    }

    private boolean wasOnline(BankReference account)
    {
        for(UUID online : this.onlineDuringSalary)
        {
            if(account.isSalaryTarget(PlayerReference.of(online,"")))
                return true;
        }
        return false;
    }

    @Deprecated(since = "2.3.0.2")
    public boolean canAffordNextSalary(boolean validateOnlinePlayers) { return this.canAffordNextSalary(validateOnlinePlayers,true); }
    public boolean canAffordNextSalary(boolean validateOnlinePlayers, boolean performCalculation) {
        if(this.creativeSalaryMode)
            return true;
        if(this.salary.isEmpty())
            return false;
        return this.account.getMoneyStorage().containsValue(this.getTotalSalaryCost(validateOnlinePlayers,performCalculation));
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
        if(this.cacheInvalid)
            this.validateSalaryCache();
    }

    public void onPlayerJoin(ServerPlayer player)
    {
        if(this.isTarget(player))
            this.flagPlayerAsOnline(player,true);
    }

    public void forcePaySalaries(boolean validateOnlinePlayers) {
        //Confirm that all current targets still actually exist
        this.validateTargetsExist();
        //Comfirm that we can afford to pay everyone
        if(!this.canAffordNextSalary(validateOnlinePlayers,true))
        {
            this.failedLastSalary = true;
            this.markDirty();
            return;
        }
        this.failedLastSalary = false;
        MoneyValue payment = this.getTotalSalaryCost(validateOnlinePlayers,true);
        if(payment.isEmpty())
            return;
        this.incrementStat(StatKeys.Generic.SALARY_TRIGGERS,1);
        //Still track the total salary paid even if it's not actually taken from our bank account
        this.incrementStat(StatKeys.Generic.MONEY_PAID,payment);
        List<BankReference> targetsToPay = this.getAllTargets();
        if(this.requireLoginForSalary)
            targetsToPay = targetsToPay.stream().filter(this::wasOnline).toList();
        for(BankReference target : targetsToPay)
            this.payMember(target,this.salary);
        if(!this.creativeSalaryMode)
        {
            this.account.pushNotification(SalaryPaymentNotification.create(this.account,this,payment,targetsToPay),this.salaryNotification);
            this.account.withdrawMoney(payment);
        }
        if(validateOnlinePlayers)
        {
            this.onlineDuringSalary.clear();
            this.checkForOnlinePlayers(false);
            this.validateSalaryCache();
        }
        this.markDirty();
    }

    private <T> void incrementStat(StatKey<?,T> key,T value)
    {
        StatTracker stats = this.account.getStatTracker();
        if(stats != null)
            stats.incrementStat(key,value);
    }

    @Deprecated(since = "2.3.0.2",forRemoval = true)
    public void checkForOnlinePlayers() { this.checkForOnlinePlayers(true); }

    public void checkForOnlinePlayers(boolean updateCache)
    {
        if(!this.requireLoginForSalary || !this.isAutoSalaryEnabled())
            return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server == null)
            return;
        List<BankReference> targets = this.getAllTargets();
        for(ServerPlayer player : server.getPlayerList().getPlayers())
        {
            for(BankReference target : targets)
            {
                if(target.isSalaryTarget(player))
                    this.flagPlayerAsOnline(player,updateCache);
            }
        }
    }

    private void flagPlayerAsOnline(ServerPlayer player, boolean updateCache)
    {
        //Online state is not relevant if no auto-salary is enabled, or if the login requirement is not required
        if(!this.requireLoginForSalary || !this.isAutoSalaryEnabled())
            return;
        UUID playerID = player.getUUID();
        if(!this.onlineDuringSalary.contains(playerID))
        {
            this.onlineDuringSalary.add(playerID);
            if(updateCache)
                this.validateSalaryCache();
            this.markDirty();
        }
    }

    private void payMember(BankReference member, final MoneyValue value)
    {
        final IBankAccount memberAccount = member.get();
        if(memberAccount != null)
        {
            memberAccount.pushNotification(() -> new DepositWithdrawNotification.Custom(this.getName(),memberAccount.getName(),true,value),this.salaryNotification);
            memberAccount.depositMoney(value);
        }
    }

    public CompoundTag save()
    {
        //Online List
        CompoundTag tag = new CompoundTag();
        tag.put("OnlinePlayers",TagUtil.writeUUIDList(this.onlineDuringSalary));
        tag.putBoolean("LoginRequired",this.requireLoginForSalary);
        tag.putLong("LastSalaryTime",this.lastSalaryTime);
        tag.putBoolean("SalaryNotification",this.salaryNotification);
        tag.putLong("SalaryDelay",this.salaryDelay);
        tag.putBoolean("CreativeSalary",this.creativeSalaryMode);
        tag.put("Salary",this.salary.save());
        tag.putString("Name",this.name);
        ListTag targets = new ListTag();
        for(BankReference br : this.directTargets)
            targets.add(br.save());
        tag.put("Targets",targets);
        ListTag customTargets = new ListTag();
        for(String ct : this.customTargets)
            customTargets.add(StringTag.valueOf(ct));
        tag.put("CustomTargets",customTargets);
        tag.putBoolean("FailedLast",this.failedLastSalary);
        //Salary Cache
        CompoundTag cache = new CompoundTag();
        cache.put("QuickTotal",this.quickSalaryCache.save());
        if(this.requireLoginForSalary)
            cache.put("FullTotal",this.totalSalaryCache.save());
        tag.put("Cache",cache);
        return tag;
    }

    public void load(CompoundTag tag)
    {
        this.onlineDuringSalary.clear();
        this.onlineDuringSalary.addAll(TagUtil.readUUIDList(tag.getList("OnlinePlayers",Tag.TAG_INT_ARRAY)));
        this.requireLoginForSalary = tag.getBoolean("LoginRequired");
        this.lastSalaryTime = tag.getLong("LastSalaryTime");
        this.salaryNotification = tag.getBoolean("SalaryNotification");
        this.salaryDelay = tag.getLong("SalaryDelay");
        this.creativeSalaryMode = tag.getBoolean("CreativeSalary");
        this.salary = MoneyValue.load(tag.getCompound("Salary"));
        this.name = tag.getString("Name");
        ListTag targets = tag.getList("Targets",Tag.TAG_COMPOUND);
        this.directTargets.clear();
        for(int i = 0; i < targets.size(); ++i)
        {
            BankReference br = BankReference.load(targets.getCompound(i));
            if(br != null)
                this.directTargets.add(br.flagAsClient(this.account));
        }
        ListTag customTargets = tag.getList("CustomTargets",Tag.TAG_STRING);
        this.customTargets.clear();
        for(int i = 0; i < customTargets.size(); ++i)
            this.customTargets.add(customTargets.getString(i));
        this.failedLastSalary = tag.getBoolean("FailedLast");
        //Load or invalidate cache
        if(tag.contains("Cache"))
        {
            CompoundTag cache = tag.getCompound("Cache");
            this.quickSalaryCache = MoneyValue.load(cache.getCompound("QuickSalary"));
            if(cache.contains("TotalSalary"))
                this.totalSalaryCache = MoneyValue.load(cache.getCompound("TotalSalary"));
            else
                this.totalSalaryCache = MoneyValue.empty();
        }
        else
            this.cacheInvalid = true;
    }

    protected final void validateSalaryCache()
    {
        int totalTargets = this.getAllTargets().size();
        this.quickSalaryCache = this.salary.fromCoreValue(this.salary.getCoreValue() * totalTargets);
        if(this.requireLoginForSalary)
        {
            int loggedInTargets = (int)this.getAllTargets().stream().filter(this::wasOnline).count();
            this.totalSalaryCache = this.salary.fromCoreValue(this.salary.getCoreValue() * loggedInTargets);
        }
        else
            this.totalSalaryCache = MoneyValue.empty();
        //Remove invalid flag
        this.cacheInvalid = false;
    }

    protected final void markDirty() { this.account.markDirty(); }

}