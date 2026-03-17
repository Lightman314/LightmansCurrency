package io.github.lightman314.lightmanscurrency.common.bank;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Products;
import com.mojang.datafixers.util.Function6;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.partial.SPart6;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.data.IRegistryAccess;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.salary.CustomTarget;
import io.github.lightman314.lightmanscurrency.api.money.bank.salary.SalaryData;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.builtin.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.api.money.capability.MoneyHolder;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.api.stats.StatTracker;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.BankInterestNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.LowBalanceNotification;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nullable;

public class BankAccount extends MoneyHolder.Slave implements IBankAccount {

    private static final Codec<Map<String,MoneyValue>> NOTIFICATION_LEVEL_CODEC = MoneyValue.NON_EMPTY_OR_FREE_CODEC.listOf()
            .xmap(list -> {
                Map<String,MoneyValue> map = new HashMap<>();
                for(MoneyValue v : list)
                    map.put(v.getUniqueName(),v);
                return map;
            },map -> new ArrayList<>(map.values()));
    private static final StreamCodec<RegistryFriendlyByteBuf,Map<String,MoneyValue>> NOTIFICATION_LEVEL_STREAM = MoneyValue.STREAM_CODEC.apply(ByteBufCodecs.list())
            .map(list -> {
                Map<String,MoneyValue> map = new HashMap<>();
                for(MoneyValue v : list)
                    map.put(v.getUniqueName(),v);
                return map;
            },map -> new ArrayList<>(map.values()));

    public static final Codec<BankAccount> CODEC = buildCodec(BankAccount::new);

    public static final StreamCodec<RegistryFriendlyByteBuf,BankAccount> STREAM_CODEC = buildStreamCodec(BankAccount.class,BankAccount::new);

    protected static <T extends BankAccount> Products.P6<RecordCodecBuilder.Mu<T>,MoneyStorage,NotificationData,String,Map<String,MoneyValue>,Integer,List<SalaryData>> baseFields(RecordCodecBuilder.Instance<T> builder) {
        return builder.group(
                MoneyStorage.CODEC.fieldOf("money").forGetter(BankAccount::getMoneyStorage),
                NotificationData.CODEC.fieldOf("logs").forGetter(a -> a.logger),
                Codec.STRING.fieldOf("ownerName").forGetter(BankAccount::getOwnersName),
                NOTIFICATION_LEVEL_CODEC.fieldOf("notificationLevels").forGetter(BankAccount::getNotificationLevels),
                Codec.INT.fieldOf("atmCardCode").forGetter(BankAccount::getCardValidation),
                SalaryData.CODEC.listOf().fieldOf("salaries").forGetter(BankAccount::getSalaries)
        );
    }

    protected static <T extends BankAccount> Codec<T> buildCodec(Function6<MoneyStorage,NotificationData,String,Map<String,MoneyValue>,Integer,List<SalaryData>,T> factory) { return RecordCodecBuilder.create(builder -> baseFields(builder).apply(builder,factory)); }

    protected static <T extends BankAccount> SPart6<RegistryFriendlyByteBuf,T,MoneyStorage,NotificationData,String,Map<String,MoneyValue>,Integer,List<SalaryData>> baseStreamFields(Class<T> clazz) {
        return new SPart6<>(
                MoneyStorage.STREAM_CODEC,BankAccount::getMoneyStorage,
                NotificationData.STREAM_CODEC,a -> a.logger,
                ByteBufCodecs.STRING_UTF8,BankAccount::getOwnersName,
                NOTIFICATION_LEVEL_STREAM,BankAccount::getNotificationLevels,
                ByteBufCodecs.INT,BankAccount::getCardValidation,
                SalaryData.STREAM_CODEC.apply(ByteBufCodecs.list()),BankAccount::getSalaries);
    }

    protected static <T extends BankAccount> StreamCodec<RegistryFriendlyByteBuf,T> buildStreamCodec(Class<T> clazz,Function6<MoneyStorage,NotificationData,String,Map<String,MoneyValue>,Integer,List<SalaryData>,T> factory) { return baseStreamFields(clazz).assemble(factory); }

    private HolderLookup.Provider registryAccess = null;
    @Override
    public final HolderLookup.Provider registryAccess() { return Objects.requireNonNull(this.registryAccess); }
    public final void setRegistryAccess(HolderLookup.Provider registryAccess) { this.registryAccess = registryAccess; }
    public final void setRegistryAccess(IRegistryAccess parent) { this.registryAccess = parent.registryAccess(); }

    @Nullable
    private LazyPacketData.Builder changedData = null;
    protected final void setChanged(Consumer<LazyPacketData.Builder> dataWriter)
    {
        if(this.changedData == null)
            this.changedData = this.builder();
        dataWriter.accept(this.changedData);
        this.listener.run();
    }

    @Override
    public void setSalaryChanged(SalaryData salary,Consumer<LazyPacketData.Builder> dataWriter) {
        int index = this.salaryData.indexOf(salary);
        if(index >= 0)
            this.setChanged(builder -> builder.modifyMap("Salary_" + index,dataWriter));
    }

    public LazyPacketData getAndCleanPacket()
    {
        LazyPacketData.Builder result = this.changedData;
        this.changedData = null;
        return result.build();
    }

	private boolean isClient = false;
	@Override
	public boolean isClient() { return this.isClient; }

	public BankAccount flagAsClient() { return this.flagAsClient(true); }
	public BankAccount flagAsClient(boolean isClient) { this.isClient = isClient; if(this.isClient) this.logger.flagAsClient(); return this; }
	public BankAccount flagAsClient(IClientTracker parent) { return this.flagAsClient(parent.isClient()); }

	private Runnable listener;
    public void setListener(Runnable listener) { this.listener = listener; }
	
	private final MoneyStorage coinStorage = new MoneyStorage().withListener(() ->
            this.setChanged(builder -> builder.setList("Money",this.coinStorage.allValues(),ModLazyPackets.MONEY_VALUE)));
	
	public MoneyStorage getMoneyStorage() { return this.coinStorage; }

    public List<SalaryData> salaryData = new ArrayList<>();
    @Override
    public List<SalaryData> getSalaries() { return ImmutableList.copyOf(this.salaryData); }
    @Nullable
    public SalaryData createNewSalary() {
        if(this.salaryData.size() >= SALARY_LIMIT)
            return null;
        SalaryData newSalary = new SalaryData().init(this,this.salaryData::indexOf);
        this.salaryData.add(newSalary);
        this.setChanged(builder -> {
            if(builder.has("SetSalaries"))
                builder.setList("SetSalaries",this.salaryData,ModLazyPackets.SALARY_DATA);
            else
                builder.addToList("AddSalary",null,LazyPacketData.FLAG_FACTORY);
        });
        return newSalary;
    }

    @Override
    public void deleteSalary(SalaryData salary) {
        if(this.salaryData.contains(salary))
        {
            int index = this.salaryData.indexOf(salary);
            this.salaryData.remove(salary);
            this.setChanged(builder -> builder
                    .remove("AddSalary")
                    .setList("SetSalaries",this.salaryData,ModLazyPackets.SALARY_DATA));
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
	@Nullable
	protected IMoneyHolder getParent() { return this.coinStorage; }

	private final Map<String,MoneyValue> notificationLevels = new HashMap<>();
	
	@Override
	public Map<String,MoneyValue> getNotificationLevels() { return ImmutableMap.copyOf(this.notificationLevels); }
	
	@Override
	public MoneyValue getNotificationLevelFor(String type) { return this.notificationLevels.getOrDefault(type, MoneyValue.empty()); }

	@Override
	public void setNotificationLevel(String type,MoneyValue value) {
		if(value.isEmpty())
			this.notificationLevels.remove(type);
		else
			this.notificationLevels.put(type, value);
		this.setChanged(builder -> builder.setList("NotificationLevels",new ArrayList<>(this.notificationLevels.values()),ModLazyPackets.MONEY_VALUE));
	}
	
	private Consumer<Supplier<Notification>> notificationSender;
	public void setNotificationConsumer(Consumer<Supplier<Notification>> notificationSender) { this.notificationSender = notificationSender; }

	@Override
	public void pushLocalNotification(Notification notification) {
		this.logger.addNotification(notification);
		this.setChanged(builder -> builder.addToList("AddNotification",notification,ModLazyPackets.NOTIFICATION));
	}
	@Override
	public void pushNotification(Supplier<Notification> notification, boolean notifyPlayers) {
		this.pushLocalNotification(notification.get());
		if(notifyPlayers && this.notificationSender != null)
			this.notificationSender.accept(notification);
	}
	
	public static Consumer<Supplier<Notification>> generateNotificationAcceptor(UUID playerID) {
		return (notification) -> NotificationAPI.getApi().PushPlayerNotification(playerID, notification.get());
	}
	
	protected final NotificationData logger = new NotificationData();

	@Override
	public List<Notification> getNotifications() { return this.logger.getNotifications(); }
	
	private String ownerName;
	public String getOwnersName() { return this.ownerName; }
	public void updateOwnersName(String ownerName) { this.ownerName = ownerName; }
	@Override
	public MutableComponent getName() { return LCText.GUI_BANK_ACCOUNT_NAME.get(this.ownerName); }
    public Component getOwnerName() { return EasyText.literal(this.ownerName); }

	@Override
	public void depositMoney(MoneyValue depositAmount) { this.coinStorage.addValue(depositAmount); }
	
	@Override
	public MoneyValue withdrawMoney(MoneyValue withdrawAmount) {
		String type = withdrawAmount.getUniqueName();
		withdrawAmount = this.coinStorage.capValue(withdrawAmount);
		//Cannot withdraw if none is in storage
		if(withdrawAmount.isEmpty())
			return MoneyValue.empty();
		long oldValue = this.coinStorage.valueOf(type).getCoreValue();
		this.coinStorage.removeValue(withdrawAmount);
		//Check if we should push the notification
		MoneyValue notificationLevel = this.getNotificationLevelFor(withdrawAmount.getUniqueName());
		long nl = notificationLevel.getCoreValue();
		if(oldValue >= nl && this.coinStorage.valueOf(type).getCoreValue() < nl)
			this.pushNotification(LowBalanceNotification.create(this.getName(), notificationLevel));
		return withdrawAmount;
	}
	
	public BankAccount() {
        this(new MoneyStorage(),new NotificationData(),"Unknown",new HashMap<>(),0,new ArrayList<>());
    }

    protected BankAccount(MoneyStorage storage,NotificationData logger,String ownerName,Map<String,MoneyValue> notificationLevels,int cardValidation,List<SalaryData> salaries)
    {
        this.coinStorage.load(storage.allValues());
        this.logger.copyFrom(logger);
        this.ownerName = ownerName;
        this.notificationLevels.putAll(notificationLevels);
        this.cardValidation = cardValidation;
        this.salaryData.addAll(salaries);
        SalaryData.init(this.salaryData,this);
    }

    public void copyFrom(BankAccount other)
    {
        this.coinStorage.load(other.coinStorage.allValues());
        this.logger.copyFrom(other.logger);
        this.ownerName = other.ownerName;
        this.notificationLevels.clear();
        this.notificationLevels.putAll(other.notificationLevels);
        this.cardValidation = other.cardValidation;
        this.salaryData.clear();
        this.salaryData.addAll(other.salaryData);
        SalaryData.init(this.salaryData,this);
    }

    public final void handlePacket(LazyPacketData data)
    {
        //Handle "Add Salary" methods
        for(var dummy : data.getList("AddSalary",Void.class))
            this.createNewSalary();
        //Handle "Remove Salary" arguments
        if(data.contains("SetSalaries"))
        {
            this.salaryData.clear();
            this.salaryData.addAll(data.getList("SetSalaries",ModLazyPackets.SALARY_DATA));
            SalaryData.init(this.salaryData,this);
        }
        for(int i = 0; i < this.salaryData.size(); ++i)
        {
            String key = "Salary_" + i;
            if(data.contains(key))
                this.salaryData.get(i).handlePacket(data.getMap(key));
        }
        if(data.contains("CardValidation"))
            this.cardValidation = data.getInt("CardValidation");
        if(data.contains("NotificationLevels"))
        {
            this.notificationLevels.clear();
            for(MoneyValue value : data.getList("NotificationLevels",ModLazyPackets.MONEY_VALUE))
                this.notificationLevels.put(value.getUniqueName(),value);
        }
        if(data.contains("AddNotification"))
        {
            for(Notification n : data.getList("AddNotification",ModLazyPackets.NOTIFICATION))
                this.logger.addNotification(n);
        }
    }

    @Deprecated
    public static BankAccount loadOldData(CompoundTag compound,HolderLookup.Provider lookup)
    {
        MoneyStorage storage = new MoneyStorage();
        storage.load(compound.getList("CoinStorage",Tag.TAG_COMPOUND));
        NotificationData logger = DataContext.createNBT(lookup)
                .read(compound.get("AccountLogs"),NotificationData.CODEC);
        String ownerName = compound.getString("OwnerName");
        Map<String,MoneyValue> notificationLevels = new HashMap<>();
        if(compound.contains("NotificationLevel"))
        {
            MoneyValue level = MoneyValue.safeLoad(compound, "NotificationLevel");
            if(!level.isEmpty() && !level.isFree())
                notificationLevels.put(level.getUniqueName(), level);
        }
        else if(compound.contains("NotificationLevels"))
        {
            ListTag list = compound.getList("NotificationLevels", Tag.TAG_COMPOUND);
            for(int i = 0; i < list.size(); ++i)
            {
                MoneyValue level = MoneyValue.load(list.getCompound(i));
                if(level.isInvalid() || (!level.isFree() && !level.isEmpty()))
                    notificationLevels.put(level.getUniqueName(), level);
            }
        }
        int cardValidation = compound.getInt("CardValidation");
        List<SalaryData> salaryData = new ArrayList<>();
        if(compound.contains("Salaries"))
        {
            ListTag list = compound.getList("Salaries",Tag.TAG_COMPOUND);
            for(int i = 0; i < list.size(); ++i)
            {
                SalaryData newSalary = SalaryData.loadOldData(list.getCompound(i));
                salaryData.add(newSalary);
            }
        }
        return new BankAccount(storage,logger,ownerName,notificationLevels,cardValidation,salaryData);
    }

    @Override
	public void formatTooltip(List<Component> tooltip) {
		IMoneyHolder.defaultTooltipFormat(tooltip, this.getTooltipTitle(), this.getStoredMoney());
	}

	@Override
	public Component getTooltipTitle() { return LCText.TOOLTIP_MONEY_SOURCE_BANK.get(); }

	@Override
	public void applyInterest(double interestMultiplier, List<MoneyValue> limits, List<String> blacklist, boolean forceInterest, boolean notifyPlayers) {
		for(MoneyValue value : this.coinStorage.allValues())
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
				for(MoneyValue limit : limits)
				{
					if(!limit.isEmpty() && limit.sameType(interest))
					{
						if(interest.containsValue(limit))
							interest = limit;
					}
				}
				if(!interest.isEmpty())
				{
					this.depositMoney(interest);
					this.pushNotification(BankInterestNotification.create(this.getName(), interest), notifyPlayers);
				}
			}
		}
	}

    @Override
    public void tick() {
        for(SalaryData salary : new ArrayList<>(this.salaryData))
            salary.tick();
    }

    @Override
    public Map<String, CustomTarget> extraSalaryTargets() { return Map.of(); }

    @Nullable
    @Override
    public StatTracker getStatTracker() { return null; }

    private static boolean isBlacklisted(List<String> blacklist, MoneyValue value)
	{
		String id = value.getUniqueName();
		return blacklist.stream().anyMatch(entry -> {
			if(entry.endsWith("*"))
                return id.startsWith(entry.substring(0, entry.length() - 1));
			return entry.equals(id);
		});
	}

}
