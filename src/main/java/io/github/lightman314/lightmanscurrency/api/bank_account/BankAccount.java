package io.github.lightman314.lightmanscurrency.api.bank_account;

import java.util.*;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Products;
import com.mojang.datafixers.util.Function6;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.bank_account.notifications.BankInterestNotification;
import io.github.lightman314.lightmanscurrency.api.bank_account.notifications.LowBalanceNotification;
import io.github.lightman314.lightmanscurrency.api.bank_account.salary.SalaryData;
import io.github.lightman314.lightmanscurrency.api.codecs.partial.SPart6;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.IRegistryAccess;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerServer;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.SortableMoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.builtin.UnlimitedMoneyStorage;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationStack;
import io.github.lightman314.lightmanscurrency.api.notifications.holder.NotificationConsumer;
import io.github.lightman314.lightmanscurrency.api.notifications.holder.NotificationHolder;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;

public class BankAccount extends IRegistryAccess.WithHolder implements SortableMoneyResourceHandler.Deferred, ISidedContext.Mutable<BankAccount>, ITickerServer, NotificationConsumer.ForChat {

    public static final int SALARY_LIMIT = 100;

    public static final TextEntry GUI_BANK_ACCOUNT_NAME = TextEntry.gui(LCApi.MODID,"bank_account");
    public static final TextEntry TOOLTIP_MONEY_SOURCE_BANK = TextEntry.tooltip(LCApi.MODID,"money_source.bank");

    public static final Codec<BankAccount> CODEC = buildCodec(BankAccount::new);

    public static final StreamCodec<RegistryFriendlyByteBuf,BankAccount> STREAM_CODEC = buildStreamCodec(BankAccount.class,BankAccount::new);

    protected static <T extends BankAccount> Products.P6<RecordCodecBuilder.Mu<T>,UnlimitedMoneyStorage,NotificationHolder,String,Map<MoneyKey,MoneyValue>,Integer,List<SalaryData>> baseFields(RecordCodecBuilder.Instance<T> builder) {
        return builder.group(
                UnlimitedMoneyStorage.CODEC.fieldOf("money").forGetter(BankAccount::getMoneyStorage),
                NotificationHolder.CODEC.fieldOf("logs").forGetter(a -> a.notifications),
                Codec.STRING.fieldOf("ownerName").forGetter(BankAccount::getOwnersName),
                MoneyValue.SET_CODEC.fieldOf("notificationLevels").forGetter(BankAccount::getNotificationLevels),
                Codec.INT.fieldOf("atmCardCode").forGetter(BankAccount::getCardValidation),
                SalaryData.CODEC.listOf().fieldOf("salaries").forGetter(BankAccount::getSalaries)
        );
    }

    protected static <T extends BankAccount> Codec<T> buildCodec(Function6<UnlimitedMoneyStorage,NotificationHolder,String,Map<MoneyKey,MoneyValue>,Integer,List<SalaryData>,T> factory) { return RecordCodecBuilder.create(builder -> baseFields(builder).apply(builder,factory)); }

    protected static <T extends BankAccount> SPart6<RegistryFriendlyByteBuf,T,UnlimitedMoneyStorage,NotificationHolder,String,Map<MoneyKey,MoneyValue>,Integer,List<SalaryData>> baseStreamFields(Class<T> clazz) {
        return new SPart6<>(
                UnlimitedMoneyStorage.STREAM_CODEC,BankAccount::getMoneyStorage,
                NotificationHolder.STREAM_CODEC,a -> a.notifications,
                ByteBufCodecs.STRING_UTF8,BankAccount::getOwnersName,
                MoneyValue.SET_STREAM_CODEC,BankAccount::getNotificationLevels,
                ByteBufCodecs.INT,BankAccount::getCardValidation,
                SalaryData.STREAM_CODEC.apply(ByteBufCodecs.list()),BankAccount::getSalaries);
    }

    protected static <T extends BankAccount> StreamCodec<RegistryFriendlyByteBuf,T> buildStreamCodec(Class<T> clazz,Function6<UnlimitedMoneyStorage,NotificationHolder,String,Map<MoneyKey,MoneyValue>,Integer,List<SalaryData>,T> factory) { return baseStreamFields(clazz).assemble(factory); }



    @Nullable
    private FancyPacketMap.Mutable changedData = null;
    protected final void setChanged(Consumer<FancyPacketMap.Mutable> dataWriter)
    {
        if(this.changedData == null)
            this.changedData = FancyPacketMap.map();
        dataWriter.accept(this.changedData);
        this.listener.run();
    }

    public void setSalaryChanged(SalaryData salary,Consumer<FancyPacketMap.Mutable> dataWriter) {
        int index = this.salaryData.indexOf(salary);
        if(index >= 0)
            this.setChanged(builder -> builder.modifyMap("salary_" + index,dataWriter));
    }

    public FancyPacketMap getAndCleanPacket()
    {
        FancyPacketMap.Mutable result = this.changedData;
        this.changedData = null;
        return result.immutable();
    }

    private ISidedContext context = ISidedContext.LOGICAL_CLIENT;
    @Override
    public boolean isClient() { return this.context.isClient(); }

    @Override
    public BankAccount setSidedContext(ISidedContext context) {
        this.context = context;
        return this;
    }

    private Runnable listener;
    public void setListener(Runnable listener) { this.listener = listener; }

    private final UnlimitedMoneyStorage storage = new UnlimitedMoneyStorage().withListener(() ->
            this.setChanged(builder -> builder.setList("money",LCFancyPacketTypes.MONEY,this.storage.getAllResources())));

    public UnlimitedMoneyStorage getMoneyStorage() { return this.storage; }


    public List<SalaryData> salaryData = new ArrayList<>();
    public List<SalaryData> getSalaries() { return Collections.unmodifiableList(this.salaryData); }
    public SalaryData createNewSalary() {
        if(this.salaryData.size() >= SALARY_LIMIT)
            return null;
        SalaryData newSalary = new SalaryData().init(this,this.salaryData::indexOf);
        this.salaryData.add(newSalary);
        this.setChanged(builder -> {
            if(builder.contains("SetSalaries"))
                builder.setList("SetSalaries",LCFancyPacketTypes.BANK_SALARY,this.salaryData);
            else
                builder.setInt("AddSalary",builder.getInt("AddSalary") + 1);
        });
        return newSalary;
    }

    public void deleteSalary(SalaryData salary) {
        if(this.salaryData.contains(salary))
        {
            int index = this.salaryData.indexOf(salary);
            this.salaryData.remove(salary);
            this.setChanged(builder -> builder
                    .remove("AddSalary")
                    .setList("SetSalaries",LCFancyPacketTypes.BANK_SALARY,this.salaryData));
        }
    }

    int cardValidation;
    public int getCardValidation() { return this.cardValidation; }
    public boolean isCardValid(int validationLevel) { return validationLevel >= this.cardValidation; }
    public void resetCards() {
        this.cardValidation++;
        this.setChanged(builder -> builder.setInt("CardValidation",this.cardValidation));
    }

    @Override
    public MoneyResourceHandler getMoneyResourceHandler() { return this.storage; }

    private final Map<MoneyKey,MoneyValue> notificationLevels = new HashMap<>();

    public Map<MoneyKey,MoneyValue> getNotificationLevels() { return ImmutableMap.copyOf(this.notificationLevels); }

    public MoneyValue getNotificationLevelFor(MoneyKey type) { return this.notificationLevels.getOrDefault(type, MoneyValue.empty()); }

    public void setNotificationLevel(MoneyKey type,MoneyValue value) {
        if(value.isEmpty())
            this.notificationLevels.remove(type);
        else
            this.notificationLevels.put(type, value);
        this.setChanged(builder -> builder.setList("NotificationLevels",LCFancyPacketTypes.MONEY,new ArrayList<>(this.notificationLevels.values())));
    }

    private Consumer<Notification> notificationSender;
    public void setNotificationConsumer(Consumer<Notification> notificationSender) { this.notificationSender = notificationSender; }

    public void pushLocalNotification(Notification notification) {
        this.notifications.postNotification(notification);
        this.setChanged(builder -> builder.addToList("AddNotification",LCFancyPacketTypes.NOTIFICATION,notification));
    }
    public void postNotification(Notification notification,boolean notifyPlayers) {
        this.pushLocalNotification(notification);
        if(notifyPlayers && this.notificationSender != null)
            this.notificationSender.accept(notification);
    }

    public static Consumer<Notification> generateNotificationAcceptor(UUID playerID) {
        return (notification) -> LCApi.getNotificationAPI().pushPlayerNotification(playerID,notification);
    }

    protected final NotificationHolder notifications = new NotificationHolder();

    public List<NotificationStack> getNotifications() { return this.notifications.getNotifications(); }

    private String ownerName;
    public String getOwnersName() { return this.ownerName; }
    public void updateOwnersName(String ownerName) { this.ownerName = ownerName; }
    public Component getName() { return GUI_BANK_ACCOUNT_NAME.get(this.ownerName); }
    public Component getOwnerName() { return Component.literal(this.ownerName); }

    public void depositMoney(MoneyValue depositAmount,@Nullable Transaction transaction) {
        try(Transaction tx = Transaction.open(transaction)) {
            this.storage.insert(depositAmount,tx);
            tx.commit();
        }
    }

    public MoneyValue withdrawMoney(MoneyValue amount,@Nullable Transaction transaction) {
        try(Transaction tx = Transaction.open(transaction)) {
            MoneyKey type = amount.getKey();
            //Cache the previously stored amount
            long oldValue = this.storage.getResource(type).getInternalValue();
            MoneyValue result = this.storage.extract(amount,tx);
            //Cannot withdraw if none is in storage
            if(result.isEmpty())
                return MoneyValue.empty();
            tx.commit();
            //Check if this interaction triggered a
            MoneyValue notificationLevel = this.getNotificationLevelFor(type);
            long newLevel = notificationLevel.getInternalValue();
            if(oldValue >= newLevel && this.storage.getResource(type).getInternalValue() < newLevel)
                this.postNotification(new LowBalanceNotification(this.getName(),notificationLevel));
            return result;
        }
    }

    public BankAccount() {
        this(new UnlimitedMoneyStorage(),new NotificationHolder(),"Unknown",new HashMap<>(),0,new ArrayList<>());
    }

    protected BankAccount(UnlimitedMoneyStorage storage,NotificationHolder logger,String ownerName,Map<MoneyKey,MoneyValue> notificationLevels,int cardValidation,List<SalaryData> salaries)
    {
        this.storage.copyFrom(storage);
        this.notifications.loadFrom(logger.getNotifications());
        this.ownerName = ownerName;
        this.notificationLevels.putAll(notificationLevels);
        this.cardValidation = cardValidation;
        this.salaryData.addAll(salaries);
        SalaryData.init(this.salaryData,this);
    }

    public void copyFrom(BankAccount other)
    {
        this.storage.copyFrom(other.storage);
        this.notifications.loadFrom(other.getNotifications());
        this.ownerName = other.ownerName;
        this.notificationLevels.clear();
        this.notificationLevels.putAll(other.notificationLevels);
        this.cardValidation = other.cardValidation;
        this.salaryData.clear();
        this.salaryData.addAll(other.salaryData);
        SalaryData.init(this.salaryData,this);
    }

    public final void handlePacket(FancyPacketMap data)
    {
        //Handle "Add Salary" methods
        int addCount = data.getInt("AddSalary");
        for(int i = 0; i < addCount; ++i)
            this.createNewSalary();
        //Handle "Remove Salary" arguments
        if(data.contains("SetSalaries"))
        {
            this.salaryData.clear();
            this.salaryData.addAll(data.getList("SetSalaries",LCFancyPacketTypes.BANK_SALARY));
            SalaryData.init(this.salaryData,this);
        }
        for(int i = 0; i < this.salaryData.size(); ++i)
        {
            String key = "salary_" + i;
            if(data.contains(key))
                this.salaryData.get(i).handlePacket(data.getMap(key));
        }
        if(data.contains("CardValidation"))
            this.cardValidation = data.getInt("CardValidation");
        if(data.contains("NotificationLevels"))
        {
            this.notificationLevels.clear();
            for(MoneyValue value : data.getList("NotificationLevels",LCFancyPacketTypes.MONEY))
                this.notificationLevels.put(value.getKey(),value);
        }
        if(data.contains("AddNotification"))
        {
            for(Notification n : data.getList("AddNotification", LCFancyPacketTypes.NOTIFICATION))
                this.notifications.postNotification(n);
        }
    }

    @Override
    public Component getMoneyCategoryTitle() { return TOOLTIP_MONEY_SOURCE_BANK.get(); }

    public final void applyInterest(double interestMultiplier, Map<MoneyKey,MoneyValue> limits, List<String> blacklist, boolean forceInterest, boolean notifyPlayers) {
        for(MoneyValue value : this.storage.getAllResources())
        {
            //Don't calculate interest if the value has decided to opt out
            if(!value.allowInterest() || isBlacklisted(blacklist,value))
                continue;
            MoneyValue interest = value.multiplyValue(interestMultiplier);
            if(interest.isEmpty() && forceInterest)
                interest = value.getSmallestValue();
            if(!interest.isEmpty())
            {
                //Check for limits
                if(limits.containsKey(value.getKey())) {
                    MoneyValue limit = limits.get(value.getKey());
                    if(interest.containsValue(limit))
                        interest = limit;
                }
                if(!interest.isEmpty())
                {
                    this.depositMoney(interest,null);
                    this.postNotification(new BankInterestNotification(this.getName(),interest), notifyPlayers);
                }
            }
        }
    }

    @Override
    public void serverTick() {
        //Tick the salaries
        for(SalaryData salary : new ArrayList<>(this.salaryData))
            salary.tick();
    }

    //public Map<String,CustomTarget> extraSalaryTargets() { return Map.of(); }

    //@Nullable
    //public StatTracker getStatTracker() { return null; }

    private static boolean isBlacklisted(List<String> blacklist, MoneyValue value)
    {
        MoneyKey id = value.getKey();
        return blacklist.stream().anyMatch(entry -> {
            if(entry.endsWith("*"))
                return id.toString().startsWith(entry.substring(0, entry.length() - 1));
            return entry.equals(id.toString());
        });
    }

}