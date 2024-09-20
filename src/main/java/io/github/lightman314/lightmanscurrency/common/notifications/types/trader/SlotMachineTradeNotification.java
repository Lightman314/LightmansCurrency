package io.github.lightman314.lightmanscurrency.common.notifications.types.trader;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.data.ItemData;
import io.github.lightman314.lightmanscurrency.common.notifications.types.TaxableNotification;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SlotMachineTradeNotification extends TaxableNotification {

    public static final NotificationType<SlotMachineTradeNotification> TYPE = new NotificationType<>(new ResourceLocation(LightmansCurrency.MODID, "slot_machine_trade"),SlotMachineTradeNotification::new);

    TraderCategory traderData;

    List<ItemData> items;
    MoneyValue cost = MoneyValue.empty();
    MoneyValue money = MoneyValue.empty();

    String customer;

    private SlotMachineTradeNotification() {}

    protected SlotMachineTradeNotification(SlotMachineEntry entry, MoneyValue cost, PlayerReference customer, TraderCategory traderData, MoneyValue taxesPaid)
    {
        super(taxesPaid);
        this.traderData = traderData;
        this.cost = cost;
        this.items = new ArrayList<>();
        if(entry.isMoney())
            this.money = entry.getMoneyValue();
        else
        {
            for(ItemStack item : InventoryUtil.combineQueryItems(entry.items))
                this.items.add(new ItemData(item));
        }

        this.customer = customer.getName(false);
    }

    public static Supplier<Notification> create(SlotMachineEntry entry, MoneyValue cost, PlayerReference customer, TraderCategory traderData, MoneyValue taxesPaid) { return () -> new SlotMachineTradeNotification(entry, cost, customer, traderData, taxesPaid); }


    @Nonnull
    @Override
    protected NotificationType<SlotMachineTradeNotification> getType() { return TYPE; }

    @Nonnull
    @Override
    public NotificationCategory getCategory() { return this.traderData; }

    @Nonnull
    @Override
    public MutableComponent getNormalMessage() {
        Component rewardText;
        if(!this.money.isEmpty())
            rewardText = this.money.getText();
        else
            rewardText = ItemData.getItemNames(this.items);

        return LCText.NOTIFICATION_TRADE_SLOT_MACHINE.get(this.customer, this.cost.getString(), rewardText);
    }

    @Override
    protected void saveNormal(CompoundTag compound) {

        compound.put("TraderInfo", this.traderData.save());
        ListTag itemList = new ListTag();
        for(ItemData item : this.items)
            itemList.add(item.save());
        compound.put("Items", itemList);
        compound.put("Money", this.money.save());
        compound.put("Price", this.cost.save());
        compound.putString("Customer", this.customer);

    }

    @Override
    protected void loadNormal(CompoundTag compound) {

        this.traderData = new TraderCategory(compound.getCompound("TraderInfo"));
        ListTag itemList = compound.getList("Items", Tag.TAG_COMPOUND);
        this.items = new ArrayList<>();
        for(int i = 0; i < itemList.size(); ++i)
            this.items.add(ItemData.load(itemList.getCompound(i)));
        this.money = MoneyValue.safeLoad(compound,"Money");
        this.cost = MoneyValue.safeLoad(compound, "Price");
        this.customer = compound.getString("Customer");

    }

    @Override
    protected boolean canMerge(@Nonnull Notification other) {
        if(other instanceof SlotMachineTradeNotification smtn)
        {
            if(!smtn.traderData.matches(this.traderData))
                return false;
            if(smtn.items.size() != this.items.size())
                return false;
            for(int i = 0; i < this.items.size(); ++i)
            {
                if(!this.items.get(i).matches(smtn.items.get(i)))
                    return false;
            }
            if(!smtn.money.equals(this.money))
                return false;
            if(!smtn.cost.equals(this.cost))
                return false;
            if(!smtn.customer.equals(this.customer))
                return false;
            //Passed all checks. Allow merging.
            return this.TaxesMatch(smtn);
        }
        return false;
    }
}
