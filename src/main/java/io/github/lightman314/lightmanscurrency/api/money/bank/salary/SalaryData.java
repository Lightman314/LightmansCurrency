package io.github.lightman314.lightmanscurrency.api.money.bank.salary;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.IBuilderProvider;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.stats.StatKey;
import io.github.lightman314.lightmanscurrency.api.stats.StatKeys;
import io.github.lightman314.lightmanscurrency.api.stats.StatTracker;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.DepositWithdrawNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.SalaryPaymentNotification;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.util.TagUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class SalaryData implements IBuilderProvider {

    public static final int PERM_VIEW = 1;
    public static final int PERM_EDIT = 2;

    public static final Codec<SalaryData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            UUIDUtil.CODEC_SET.fieldOf("onlinePlayers").forGetter(d -> d.onlineDuringSalary),
            Codec.BOOL.fieldOf("requireLogin").forGetter(SalaryData::getLoginRequiredForSalary),
            Codec.LONG.fieldOf("lastSalary").forGetter(SalaryData::getLastSalaryTime),
            Codec.BOOL.fieldOf("notification").forGetter(SalaryData::getSalaryNotification),
            Codec.LONG.fieldOf("delay").forGetter(SalaryData::getSalaryDelay),
            Codec.BOOL.fieldOf("creative").forGetter(SalaryData::isSalaryCreative),
            MoneyValue.CODEC.fieldOf("salary").forGetter(SalaryData::getSalary),
            Codec.STRING.fieldOf("name").forGetter(SalaryData::getInternalName),
            BankReference.CODEC.listOf().fieldOf("directTargets").forGetter(SalaryData::getDirectTargets),
            CodecHelper.setCodec(Codec.STRING).fieldOf("customTargets").forGetter(SalaryData::getCustomTargetSelections),
            Codec.BOOL.fieldOf("failedLast").forGetter(SalaryData::failedLastSalaryAttempt)
    ).apply(builder,SalaryData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf,SalaryData> STREAM_CODEC = StreamHelper.composite(
            UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.collection(HashSet::new)),d -> d.onlineDuringSalary,
            ByteBufCodecs.BOOL,SalaryData::getLoginRequiredForSalary,
            ByteBufCodecs.VAR_LONG,SalaryData::getLastSalaryTime,
            ByteBufCodecs.BOOL,SalaryData::getSalaryNotification,
            ByteBufCodecs.VAR_LONG,SalaryData::getSalaryDelay,
            ByteBufCodecs.BOOL,SalaryData::isSalaryCreative,
            MoneyValue.STREAM_CODEC,SalaryData::getSalary,
            ByteBufCodecs.STRING_UTF8,SalaryData::getInternalName,
            BankReference.STREAM_CODEC.apply(ByteBufCodecs.list()),SalaryData::getDirectTargets,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.collection(HashSet::new)),SalaryData::getCustomTargetSelections,
            ByteBufCodecs.BOOL,SalaryData::failedLastSalaryAttempt,
            SalaryData::new);

    private IBankAccount account;
    private Function<SalaryData,Integer> index = s -> -1;
    public SalaryData() {}

    private SalaryData(Set<UUID> onlineDuringSalary,boolean requireLoginForSalary,long lastSalaryTime,boolean salaryNotification,
                       long salaryDelay,boolean creativeSalaryMode,MoneyValue salary,String name,List<BankReference> directTargets,
                       Set<String> customTargets,boolean failedLastSalary) {
        this.onlineDuringSalary.addAll(onlineDuringSalary);
        this.requireLoginForSalary = requireLoginForSalary;
        this.lastSalaryTime = lastSalaryTime;
        this.salaryNotification = salaryNotification;
        this.salaryDelay = salaryDelay;
        this.creativeSalaryMode = creativeSalaryMode;
        this.salary = salary;
        this.name = name;
        this.directTargets.addAll(directTargets);
        this.customTargets.addAll(customTargets);
        this.failedLastSalary = failedLastSalary;
    }

    public SalaryData init(IBankAccount account, Function<SalaryData,Integer> index)
    {
        this.account = account;
        this.index = index;
        for(BankReference br : this.directTargets)
            br.flagAsClient(this.account);
        return this;
    }

    public static void init(List<SalaryData> salaryData,IBankAccount account)
    {
        for(SalaryData s : salaryData)
            s.init(account,salaryData::indexOf);
    }

    //Old Team Salary Settings
    public boolean isAutoSalaryEnabled() { return this.lastSalaryTime > 0; }

    private final Set<UUID> onlineDuringSalary = new HashSet<>();
    public void forceOnlinePlayerList(Collection<UUID> onlineDuringSalary)
    {
        //If the set is identical, then don't bother flagging the data as changed
        if(this.onlineDuringSalary.equals(onlineDuringSalary))
            return;
        this.onlineDuringSalary.clear();
        this.onlineDuringSalary.addAll(onlineDuringSalary);
        this.onOnlinePlayersChanged();
    }
    private void onOnlinePlayersChanged()
    {
        this.setChanged(builder -> builder.setList("OnlinePlayers",this.onlineDuringSalary,LazyPacketData.UUID_FACTORY));
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
        this.setChanged(builder -> builder.setBoolean("RequireLogin",this.requireLoginForSalary));
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
        this.setChanged(builder -> builder.setLong("LastSalary",this.lastSalaryTime));
    }
    public void setAutoSalaryEnabled(boolean enabled)
    {
        if(enabled == this.isAutoSalaryEnabled())
            return;
        if(enabled)
        {
            this.forceLastSalaryTime(TimeUtil.getCurrentTime());
            this.forceOnlinePlayerList(new HashSet<>());
            this.checkForOnlinePlayers(false);
        }
        else
            this.forceLastSalaryTime(0);
    }
    boolean salaryNotification = true;
    public boolean getSalaryNotification() { return this.salaryNotification; }
    public void setSalaryNotification(boolean salaryNotification) {
        if(this.salaryNotification == salaryNotification)
            return;
        this.salaryNotification = salaryNotification;
        this.setChanged(builder -> builder.setBoolean("Notification",this.salaryNotification));
    }
    long salaryDelay = 0;
    public long getSalaryDelay() { return this.salaryDelay; }
    public void setSalaryDelay(long salaryDelay) {
        if(this.salaryDelay == salaryDelay)
            return;
        this.salaryDelay = salaryDelay;
        this.setChanged(builder -> builder.setLong("Delay",this.salaryDelay));
    }
    boolean creativeSalaryMode = false;
    public boolean isSalaryCreative() { return this.creativeSalaryMode; }
    public void setSalaryCreative(@Nullable Player player, boolean creative) {
        if(player != null && creative && !LCAdminMode.isAdminPlayer(player))
            return;
        this.creativeSalaryMode = creative;
        this.setChanged(builder -> builder.setBoolean("Creative",this.creativeSalaryMode));
    }

    private MoneyValue salary = MoneyValue.empty();
    public MoneyValue getSalary() { return this.salary; }
    public void setSalary(MoneyValue salary) {
        if(this.salary.equals(salary))
            return;
        this.salary = salary;
        this.validateSalaryCache();
        this.setChanged(builder -> builder.setMoneyValue("Salary",this.salary));
    }

    private boolean cacheInvalid = true;
    private MoneyValue quickSalaryCache = MoneyValue.empty();
    private MoneyValue totalSalaryCache = MoneyValue.empty();

    //Custom Bank Salary Settings
    String name = "";
    public String getInternalName() { return this.name; }
    public void setName(String name) {
        if(this.name.equals(name))
            return;
        this.name = name;
        this.setChanged(builder -> builder.setString("Name",this.name));
    }
    public Component getName() {
        if(this.name.isBlank() && this.account != null)
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
        this.setChanged(builder -> builder.addToList("DirectTargets",this.builder()
                .setBoolean("Add",true)
                .setCustom("Target",target,ModLazyPackets.BANK_REFERENCE),
                LazyPacketData.BUILDER_FACTORY));
    }
    public void removeTarget(BankReference target) {
        if(this.directTargets.remove(target))
        {
            this.validateSalaryCache();
            this.setChanged(builder -> builder.addToList("DirectTargets",this.builder()
                    .setBoolean("Add",false)
                    .setCustom("Target",target,ModLazyPackets.BANK_REFERENCE),
                    LazyPacketData.BUILDER_FACTORY));
        }
    }

    Set<String> customTargets = new HashSet<>();
    public Set<String> getCustomTargetSelections() { return ImmutableSet.copyOf(this.customTargets); }
    public List<CustomTarget> getCustomTargets()
    {
        List<CustomTarget> options = new ArrayList<>();
        Map<String,CustomTarget> data = this.account != null ? this.account.extraSalaryTargets() : new HashMap<>();
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
        if(this.account == null)
            return;
        if(this.account.extraSalaryTargets().containsKey(key) && !this.customTargets.contains(key))
        {
            this.customTargets.add(key);
            this.validateSalaryCache();
            this.setChanged(builder -> builder.addToList("CustomTargets",this.builder()
                    .setBoolean("Add",true)
                    .setString("Target",key),
                    LazyPacketData.BUILDER_FACTORY));
        }
    }
    public void removeCustomTarget(String key)
    {
        if(this.customTargets.contains(key))
        {
            this.customTargets.remove(key);
            this.validateSalaryCache();
            this.setChanged(builder -> builder.addToList("CustomTargets",this.builder()
                            .setBoolean("Add",false)
                            .setString("Target",key),
                    LazyPacketData.BUILDER_FACTORY));
        }
    }

    boolean failedLastSalary = false;
    public boolean failedLastSalaryAttempt() { return this.failedLastSalary; }
    public void forceFailedLastSalary(boolean failedLast)
    {
        if(this.failedLastSalary == failedLast)
            return;
        this.failedLastSalary = failedLast;
        this.setChanged(builder ->  builder.setBoolean("FailedLastSalary",this.failedLastSalary));
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
        if(message.contains("DirectTarget") && this.account != null)
        {
            BankReference target = message.getCustom("DirectTarget",ModLazyPackets.BANK_REFERENCE).flagAsClient(this.account);
            if(target != null)
            {
                if(message.getBoolean("NewState"))
                    this.addTarget(target);
                else
                    this.removeTarget(target);
            }
        }
        if(message.contains("DeleteSalary") && this.account != null)
            this.account.deleteSalary(this);
    }

    public void handlePacket(LazyPacketData message)
    {
        if(this.account == null || this.account.isServer())
            return;
        if(message.contains("OnlinePlayers"))
        {
            this.onlineDuringSalary.clear();
            this.onlineDuringSalary.addAll(message.getList("OnlinePlayers",UUID.class));
        }
        if(message.contains("RequireLogin"))
            this.requireLoginForSalary = message.getBoolean("RequireLogin");
        if(message.contains("LastSalary"))
            this.lastSalaryTime = message.getLong("LastSalary");
        if(message.contains("Notification"))
            this.salaryNotification = message.getBoolean("Notification");
        if(message.contains("Delay"))
            this.salaryDelay = message.getLong("Delay");
        if(message.contains("Creative"))
            this.creativeSalaryMode = message.getBoolean("Creative");
        if(message.contains("Salary"))
            this.salary = message.getMoneyValue("Salary");
        if(message.contains("Name"))
            this.name = message.getString("Name");
        //Add/remove targets in a single list to avoid order complications
        if(message.contains("DirectTargets"))
        {
            for(LazyPacketData entry : message.getList("DirectTargets",LazyPacketData.class))
            {
                if(entry.getBoolean("Add"))
                    this.addTarget(entry.getCustom("Target",ModLazyPackets.BANK_REFERENCE));
                else
                    this.removeTarget(entry.getCustom("Target",ModLazyPackets.BANK_REFERENCE));
            }
        }
        if(message.contains("CustomTargets"))
        {
            for(LazyPacketData entry : message.getList("CustomTargets",LazyPacketData.class))
            {
                if(entry.getBoolean("Add"))
                    this.addCustomTarget(entry.getString("Target"));
                else
                    this.removeCustomTarget(entry.getString("Target"));
            }
        }
        if(message.contains("FailedLastSalary"))
            this.failedLastSalary = message.getBoolean("FailedLastSalary");
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
        if(this.account == null)
            return;
        boolean changed = false;
        for(BankReference target : new ArrayList<>(this.directTargets))
        {
            if(!target.isValid())
            {
                this.directTargets.remove(target);
                this.setChanged(builder -> builder.addToList("DirectTargets",this.builder()
                        .setBoolean("Add",false)
                        .setCustom("Target",target,ModLazyPackets.BANK_REFERENCE),
                        LazyPacketData.BUILDER_FACTORY));
                changed = true;
            }
        }
        Set<String> keySet = this.account.extraSalaryTargets().keySet();
        for(String customTarget : new ArrayList<>(this.customTargets))
        {
            if(!keySet.contains(customTarget))
            {
                this.customTargets.remove(customTarget);
                this.setChanged(builder -> builder.addToList("CustomTargets",this.builder()
                        .setBoolean("Add",false)
                        .setString("Target",customTarget),
                        LazyPacketData.BUILDER_FACTORY));
                changed = true;
            }
        }
        if(changed)
            this.validateSalaryCache();
    }
    private void addToBankList(List<BankReference> list, BankReference toAdd)
    {
        if(list.stream().anyMatch(br -> br.equals(toAdd)) || this.account == null)
            return;
        list.add(toAdd.flagAsClient(this.account));
    }
    public boolean isTarget(Player player) { return this.getAllTargets().stream().anyMatch(br -> br.isSalaryTarget(player)); }

    public MoneyValue getTotalSalaryCost(boolean validateOnlinePlayers, boolean performCalculation)
    {
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
                this.validateSalaryCache();
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

    public boolean canAffordNextSalary(boolean validateOnlinePlayers, boolean performCalculation) {
        if(this.creativeSalaryMode || this.account == null)
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
        if(this.account == null)
            return;
        //Confirm that all current targets still actually exist
        this.validateTargetsExist();
        //Comfirm that we can afford to pay everyone
        if(!this.canAffordNextSalary(validateOnlinePlayers,true))
        {
            this.forceFailedLastSalary(true);
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
            this.account.pushNotification(SalaryPaymentNotification.create(this.account,this,payment,targetsToPay));
            this.account.withdrawMoney(payment);
        }
        if(validateOnlinePlayers)
        {
            this.forceOnlinePlayerList(new HashSet<>());
            this.checkForOnlinePlayers(false);
            this.validateSalaryCache();
        }
    }

    private <T> void incrementStat(StatKey<?,T> key,T value)
    {
        if(this.account == null)
            return;
        StatTracker stats = this.account.getStatTracker();
        if(stats != null)
            stats.incrementStat(key,value);
    }

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
            this.onOnlinePlayersChanged();
            if(updateCache)
                this.validateSalaryCache();
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

    @Deprecated
    public static SalaryData loadOldData(CompoundTag tag)
    {
        SalaryData data = new SalaryData();
        data.onlineDuringSalary.addAll(TagUtil.readUUIDList(tag.getList("OnlinePlayers",Tag.TAG_INT_ARRAY)));
        data.requireLoginForSalary = tag.getBoolean("LoginRequired");
        data.lastSalaryTime = tag.getLong("LastSalaryTime");
        data.salaryNotification = tag.getBoolean("SalaryNotification");
        data.salaryDelay = tag.getLong("SalaryDelay");
        data.creativeSalaryMode = tag.getBoolean("CreativeSalary");
        data.salary = MoneyValue.load(tag.getCompound("Salary"));
        data.name = tag.getString("Name");
        ListTag targets = tag.getList("Targets",Tag.TAG_COMPOUND);
        for(int i = 0; i < targets.size(); ++i)
        {
            BankReference br = BankReference.load(targets.getCompound(i));
            if(br != null)
                data.directTargets.add(br);
        }
        ListTag customTargets = tag.getList("CustomTargets",Tag.TAG_STRING);
        data.customTargets.clear();
        for(int i = 0; i < customTargets.size(); ++i)
            data.customTargets.add(customTargets.getString(i));
        data.failedLastSalary = tag.getBoolean("FailedLast");
        return data;
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

    protected final void setChanged(Consumer<LazyPacketData.Builder> dataWriter) {
        if(this.account != null)
            this.account.setSalaryChanged(this,dataWriter);
    }

    @Override
    public LazyPacketData.Builder builder() { return this.account.builder(); }

    @Override
    public HolderLookup.Provider registryAccess() { return this.account.registryAccess(); }

}
