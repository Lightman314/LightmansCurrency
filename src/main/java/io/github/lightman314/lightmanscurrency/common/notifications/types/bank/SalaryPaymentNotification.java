package io.github.lightman314.lightmanscurrency.common.notifications.types.bank;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.salary.SalaryData;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.BankCategory;
import io.github.lightman314.lightmanscurrency.common.util.TagUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.function.Supplier;

public class SalaryPaymentNotification extends Notification {

    public static final NotificationType<SalaryPaymentNotification> TYPE = new NotificationType<>(VersionUtil.lcResource("bank_salary_payment"),SalaryPaymentNotification::new);

    private Component accountName;
    private Component salaryName;
    private MoneyValue salaryAmount;
    private MoneyValue totalAmount;
    private List<Component> targetAccounts;
    private SalaryPaymentNotification() { }
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
    protected NotificationType<?> getType() { return TYPE; }

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
    protected void saveAdditional(CompoundTag compound) {
        compound.putString("Name",Component.Serializer.toJson(this.accountName));
        compound.putString("SalaryName",Component.Serializer.toJson(this.salaryName));
        compound.put("Salary",this.salaryAmount.save());
        compound.put("Total",this.totalAmount.save());
        compound.put("Targets",TagUtil.writeStringList(this.targetAccounts.stream().map(Component.Serializer::toJson).toList()));
    }

    @Override
    protected void loadAdditional(CompoundTag compound) {
        this.accountName = Component.Serializer.fromJson(compound.getString("Name"));
        this.salaryName = Component.Serializer.fromJson(compound.getString("SalaryName"));
        this.salaryAmount = MoneyValue.load(compound.getCompound("Salary"));
        this.totalAmount = MoneyValue.load(compound.getCompound("Total"));
        this.targetAccounts = TagUtil.loadStringList(compound.getList("Targets", Tag.TAG_STRING)).stream().map(s -> (Component)Component.Serializer.fromJson(s)).toList();
    }

    @Override
    protected boolean canMerge(Notification other) { return false; }

}