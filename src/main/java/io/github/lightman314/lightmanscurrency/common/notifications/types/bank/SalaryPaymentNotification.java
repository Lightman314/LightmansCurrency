package io.github.lightman314.lightmanscurrency.common.notifications.types.bank;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.salary.SalaryData;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.CommonData;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.BankCategory;
import io.github.lightman314.lightmanscurrency.common.util.TagUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.function.Supplier;

public class SalaryPaymentNotification extends Notification {

    public static final NotificationType<SalaryPaymentNotification> TYPE = new Type();

    private Component accountName = EasyText.empty();
    private Component salaryName = EasyText.empty();
    private MoneyValue salaryAmount = MoneyValue.empty();
    private MoneyValue totalAmount = MoneyValue.empty();
    private List<Component> targetAccounts = ImmutableList.of();
    private SalaryPaymentNotification() { }
    private SalaryPaymentNotification(Component accountName, Component salaryName, MoneyValue salaryAmount, MoneyValue totalAmount, List<Component> targetAccounts, CommonData data) {
        super(data);
        this.accountName = accountName;
        this.salaryName = salaryName;
        this.salaryAmount = salaryAmount;
        this.totalAmount = totalAmount;
        this.targetAccounts = ImmutableList.copyOf(targetAccounts);
    }
    private SalaryPaymentNotification(Component accountName,Component salaryName,MoneyValue salaryAmount,MoneyValue totalAmount,List<Component> targetAccounts)
    {
        this.accountName = accountName;
        this.salaryName = salaryName;
        this.salaryAmount = salaryAmount;
        this.totalAmount = totalAmount;
        this.targetAccounts = ImmutableList.copyOf(targetAccounts);
    }
    public static Supplier<Notification> create(IBankAccount account, SalaryData salary, MoneyValue totalAmount, List<BankReference> targets)
    {
        Component accountName = account.getName();
        Component salaryName = salary.getName();
        MoneyValue salaryAmount = salary.getSalary();
        List<Component> targetAccounts = targets.stream().map(SalaryPaymentNotification::safeGetName).toList();
        return () -> new SalaryPaymentNotification(accountName,salaryName,salaryAmount,totalAmount,targetAccounts);
    }

    private static Component safeGetName(BankReference br)
    {
        IBankAccount account = br.get();
        if(account == null)
            return EasyText.literal("ERROR");
        return account.getName();
    }

    @Override
    public NotificationType<?> getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return new BankCategory(this.accountName); }

    @Override
    public List<Component> getMessageLines() {
        List<Component> lines = LCText.NOTIFICATION_BANK_SALARY_PAYMENT.get(this.salaryName,this.salaryAmount.getText(),this.totalAmount.getText(),this.targetAccounts.size());
        MutableComponent entry = EasyText.empty();
        boolean first = true;
        for(Component target : this.targetAccounts)
        {
            if(!first)
                entry = entry.append(LCText.GUI_SEPERATOR.get());
            entry.append(target);
            if(!first)
            {
                first = true;
                lines.add(entry);
                entry = EasyText.empty();
            }
            else
                first = false;
        }
        if(!first)
            lines.add(entry);
        return lines;
    }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
        this.accountName = Component.Serializer.fromJson(compound.getString("Name"),lookup);
        this.salaryName = Component.Serializer.fromJson(compound.getString("SalaryName"),lookup);
        this.salaryAmount = MoneyValue.load(compound.getCompound("Salary"));
        this.totalAmount = MoneyValue.load(compound.getCompound("Total"));
        this.targetAccounts = TagUtil.loadStringList(compound.getList("Targets", Tag.TAG_STRING)).stream().map(s -> (Component)Component.Serializer.fromJson(s,lookup)).toList();
    }

    @Override
    protected boolean canMerge(Notification other) { return false; }

    private static class Type extends NotificationType<SalaryPaymentNotification>
    {
        private static final MapCodec<SalaryPaymentNotification> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ComponentSerialization.CODEC.fieldOf("account").forGetter(n -> n.accountName),
                ComponentSerialization.CODEC.fieldOf("salary").forGetter(n -> n.salaryName),
                MoneyValue.CODEC.fieldOf("payment").forGetter(n -> n.salaryAmount),
                MoneyValue.CODEC.fieldOf("tota'").forGetter(n -> n.totalAmount),
                ComponentSerialization.CODEC.listOf().fieldOf("targets").forGetter(n -> n.targetAccounts),
                baseFields()
        ).apply(builder,SalaryPaymentNotification::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,SalaryPaymentNotification> STREAM_CODEC = StreamHelper.combine(baseStreamFields(),
                ComponentSerialization.STREAM_CODEC,n -> n.accountName,
                ComponentSerialization.STREAM_CODEC,n -> n.salaryName,
                MoneyValue.STREAM_CODEC,n -> n.salaryAmount,
                MoneyValue.STREAM_CODEC,n -> n.totalAmount,
                ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list()),n -> n.targetAccounts,
                SalaryPaymentNotification::new);

        @Override
        protected SalaryPaymentNotification createNew() { return new SalaryPaymentNotification(); }
        @Override
        public MapCodec<SalaryPaymentNotification> codec() { return CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SalaryPaymentNotification> streamCodec() { return STREAM_CODEC; }
    }

}
