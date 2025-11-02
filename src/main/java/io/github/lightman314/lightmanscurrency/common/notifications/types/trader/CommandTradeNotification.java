package io.github.lightman314.lightmanscurrency.common.notifications.types.trader;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.taxes.notifications.SingleLineTaxableNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.traders.commands.tradedata.CommandTrade;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandTradeNotification extends SingleLineTaxableNotification {

    public static final NotificationType<CommandTradeNotification> TYPE = new NotificationType<>(VersionUtil.lcResource("command_trade"),CommandTradeNotification::new);

    TraderCategory traderData;
    String command;
    MoneyValue cost = MoneyValue.empty();

    String customer;

    private CommandTradeNotification() {}
    private CommandTradeNotification(CommandTrade trade, MoneyValue cost, PlayerReference customer, TraderCategory traderData, MoneyValue taxesPaid)
    {
        super(taxesPaid);
        this.traderData = traderData;
        this.command = trade.getCommand();
        this.cost = cost;
        this.customer = customer.getName(false);
    }

    public static Supplier<Notification> create(CommandTrade trade, MoneyValue cost, PlayerReference customer, TraderCategory traderData, MoneyValue taxesPaid) { return () -> new CommandTradeNotification(trade,cost,customer,traderData,taxesPaid); }

    @Override
    protected NotificationType<?> getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return this.traderData; }

    @Override
    protected Component getNormalMessage() { return LCText.NOTIFICATION_TRADE_COMMAND.get(this.customer,this.cost.getText("NULL"),this.command); }

    @Override
    protected void saveNormal(CompoundTag compound) {
        compound.put("TraderInfo", this.traderData.save());
        compound.putString("Command",this.command);
        compound.put("Price",this.cost.save());
        compound.putString("Customer",this.customer);
    }

    @Override
    protected void loadNormal(CompoundTag compound) {

        this.traderData = new TraderCategory(compound.getCompound("TraderInfo"));
        this.command = compound.getString("Command");
        this.cost = MoneyValue.load(compound.getCompound("Price"));
        this.customer = compound.getString("Customer");

    }

    @Override
    protected boolean canMerge(Notification other) {
        if(other instanceof CommandTradeNotification ctn)
        {
            if(!ctn.traderData.matches(this.traderData))
                return false;
            if(!ctn.command.equals(this.command))
                return false;
            if(!ctn.cost.equals(this.cost))
                return false;
            if(!ctn.customer.equals(this.customer))
                return false;
            //Passed all checks. Allow merging.
            return this.TaxesMatch(ctn);
        }
        return false;
    }
}