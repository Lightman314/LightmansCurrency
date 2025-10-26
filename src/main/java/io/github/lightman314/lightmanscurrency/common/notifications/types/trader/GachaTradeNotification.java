package io.github.lightman314.lightmanscurrency.common.notifications.types.trader;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.taxes.notifications.SingleLineTaxableNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.data.ItemData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GachaTradeNotification extends SingleLineTaxableNotification {

    public static final NotificationType<GachaTradeNotification> TYPE = new NotificationType<>(VersionUtil.lcResource("gacha_trade"), GachaTradeNotification::new);

    private TraderCategory traderData;

    private ItemData item;
    private MoneyValue cost;

    private String customer;

    private GachaTradeNotification() {}
    private GachaTradeNotification(ItemStack item, MoneyValue cost, PlayerReference customer, TraderCategory traderData, MoneyValue taxes)
    {
        super(taxes);
        this.traderData = traderData;
        this.item = new ItemData(item);
        this.cost = cost;

        this.customer = customer.getName(false);
    }

    public static Supplier<Notification> create(ItemStack item, MoneyValue cost, PlayerReference customer, TraderCategory trader, MoneyValue taxesPaid) { return () -> new GachaTradeNotification(item,cost,customer,trader,taxesPaid); }

    
    @Override
    protected NotificationType<?> getType() { return TYPE; }

    
    @Override
    public NotificationCategory getCategory() { return this.traderData; }

    @Override
    protected Component getNormalMessage() { return LCText.NOTIFICATION_TRADE_GACHA.get(this.customer,this.cost.getText("NULL"),this.item.format()); }

    @Override
    protected void saveNormal(CompoundTag compound, HolderLookup.Provider lookup) {
        compound.put("TraderInfo", this.traderData.save(lookup));
        compound.put("Item",this.item.save(lookup));
        compound.put("Money",this.cost.save());
        compound.putString("Customer",this.customer);
    }

    @Override
    protected void loadNormal(CompoundTag compound, HolderLookup.Provider lookup) {
        this.traderData = new TraderCategory(compound.getCompound("TraderInfo"),lookup);
        this.item = ItemData.load(compound.getCompound("Item"),lookup);
        this.cost = MoneyValue.safeLoad(compound,"Money");
        this.customer = compound.getString("Customer");
    }

    @Override
    protected boolean canMerge(Notification other) {
        if(other instanceof GachaTradeNotification gtn)
            return gtn.traderData.matches(this.traderData) && gtn.item.matches(this.item) && gtn.cost.equals(this.cost) && gtn.customer.equals(this.customer) && gtn.TaxesMatch(this);
        return false;
    }
}
