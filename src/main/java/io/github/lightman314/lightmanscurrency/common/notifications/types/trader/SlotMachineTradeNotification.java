package io.github.lightman314.lightmanscurrency.common.notifications.types.trader;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.data.ItemWriteData;
import io.github.lightman314.lightmanscurrency.common.notifications.types.TaxableNotification;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SlotMachineTradeNotification extends TaxableNotification {

    public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "slot_machine_trade");

    TraderCategory traderData;

    List<ItemWriteData> items;
    CoinValue cost = CoinValue.EMPTY;
    CoinValue money = CoinValue.EMPTY;

    String customer;

    protected SlotMachineTradeNotification(SlotMachineEntry entry, CoinValue cost, PlayerReference customer, TraderCategory traderData, CoinValue taxesPaid)
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
                this.items.add(new ItemWriteData(item));
        }

        this.customer = customer.getName(false);
    }

    public static NonNullSupplier<Notification> create(SlotMachineEntry entry, CoinValue cost, PlayerReference customer, TraderCategory traderData, CoinValue taxesPaid) { return () -> new SlotMachineTradeNotification(entry, cost, customer, traderData, taxesPaid); }

    public SlotMachineTradeNotification(CompoundTag compound) { super(); this.load(compound); }


    @Override
    protected ResourceLocation getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return this.traderData; }

    @Nonnull
    @Override
    public MutableComponent getNormalMessage() {
        Component rewardText;
        if(this.money.hasAny())
            rewardText = this.money.getComponent("0");
        else
            rewardText = ItemWriteData.getItemNames(this.items);

        return EasyText.translatable("notifications.message.slot_machine_trade", this.customer, this.cost.getString("0"), rewardText);
    }

    @Override
    protected void saveNormal(CompoundTag compound) {

        compound.put("TraderInfo", this.traderData.save());
        ListTag itemList = new ListTag();
        for(ItemWriteData item : this.items)
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
            this.items.add(new ItemWriteData(itemList.getCompound(i)));
        this.money = CoinValue.safeLoad(compound,"Money");
        this.cost = CoinValue.safeLoad(compound, "Price");
        this.customer = compound.getString("Customer");

    }

    @Override
    protected boolean canMerge(Notification other) {
        if(other instanceof SlotMachineTradeNotification smtn)
        {
            if(!smtn.traderData.matches(this.traderData))
                return false;
            if(smtn.items.size() != this.items.size())
                return false;
            for(int i = 0; i < this.items.size(); ++i)
            {
                ItemWriteData i1 = this.items.get(i);
                ItemWriteData i2 = smtn.items.get(i);
                if(!i1.itemName.getString().equals(i2.itemName.getString()))
                    return false;
                if(i1.count != i2.count)
                    return false;
            }
            if(smtn.money.getValueNumber() != this.money.getValueNumber())
                return false;
            if(smtn.cost.getValueNumber() != this.cost.getValueNumber())
                return false;
            if(!smtn.customer.equals(this.customer))
                return false;
            //Passed all checks. Allow merging.
            return this.TaxesMatch(smtn);
        }
        return false;
    }
}