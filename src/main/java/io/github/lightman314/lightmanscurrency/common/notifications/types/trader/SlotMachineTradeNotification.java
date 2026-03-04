package io.github.lightman314.lightmanscurrency.common.notifications.types.trader;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.CommonData;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.taxes.notifications.SingleLineTaxableNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.data.ItemData;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SlotMachineTradeNotification extends SingleLineTaxableNotification {

    public static final NotificationType<SlotMachineTradeNotification> TYPE = new Type();

    TraderCategory traderData = TraderCategory.NULL;

    List<ItemData> items = new ArrayList<>();
    MoneyValue money = MoneyValue.empty();

    private Either<List<ItemData>,MoneyValue> getRewardData() {
        if(this.money.isEmpty())
            return Either.left(this.items);
        return Either.right(this.money);
    }

    MoneyValue cost = MoneyValue.empty();

    String customer = "";

    private SlotMachineTradeNotification() {}
    private SlotMachineTradeNotification(TraderCategory trader, Either<List<ItemData>,MoneyValue> reward, MoneyValue cost, String customer, MoneyValue taxes, CommonData data) {
        super(taxes,data);
        this.traderData = trader;
        reward.ifLeft(items -> this.items = items)
                .ifRight(money -> this.money = money);
        this.cost = cost;
        this.customer = customer;
    }
    protected SlotMachineTradeNotification(SlotMachineEntry entry, MoneyValue cost, PlayerReference customer, TraderCategory traderData, MoneyValue taxesPaid)
    {
        super(taxesPaid);
        this.traderData = traderData;
        this.cost = cost;
        this.items = new ArrayList<>();
        if(entry != null)
        {
            if(entry.isMoney())
                this.money = entry.getMoneyValue();
            else
            {
                for(ItemStack item : InventoryUtil.combineQueryItems(entry.items))
                    this.items.add(new ItemData(item));
            }
        }
        this.customer = customer.getName(false);
    }

    public static Supplier<Notification> create(SlotMachineEntry entry, MoneyValue cost, PlayerReference customer, TraderCategory traderData, MoneyValue taxesPaid) { return () -> new SlotMachineTradeNotification(entry, cost, customer, traderData, taxesPaid); }


    @Override
    public NotificationType<SlotMachineTradeNotification> getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return this.traderData; }

    @Override
    public Component getNormalMessage() {
        Component rewardText;
        if(!this.money.isEmpty())
            rewardText = this.money.getText();
        else if(!this.items.isEmpty())
            rewardText = ItemData.getItemNames(this.items);
        else
            rewardText = LCText.NOTIFICATION_TRADE_SLOT_MACHINE_FAIL.get();

        return LCText.NOTIFICATION_TRADE_SLOT_MACHINE.get(this.customer, this.cost.getText(), rewardText);
    }

    @Override
    protected void loadNormal(CompoundTag compound, HolderLookup.Provider lookup) {

        this.traderData = new TraderCategory(compound.getCompound("TraderInfo"),lookup);
        ListTag itemList = compound.getList("Items", Tag.TAG_COMPOUND);
        this.items = new ArrayList<>();
        for(int i = 0; i < itemList.size(); ++i)
            this.items.add(ItemData.load(itemList.getCompound(i),lookup));
        this.money = MoneyValue.safeLoad(compound,"Money");
        this.cost = MoneyValue.safeLoad(compound, "Price");
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

    private static class Type extends NotificationType<SlotMachineTradeNotification>
    {
        private static final MapCodec<SlotMachineTradeNotification> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                TraderCategory.TYPE.codec().codec().fieldOf("trader").forGetter(n -> n.traderData),
                Codec.either(ItemData.LIST_CODEC,MoneyValue.CODEC).fieldOf("reward").forGetter(SlotMachineTradeNotification::getRewardData),
                MoneyValue.CODEC.fieldOf("cost").forGetter(n -> n.cost),
                Codec.STRING.fieldOf("customer").forGetter(n -> n.customer)
        ).and(taxableFields(builder)).apply(builder,SlotMachineTradeNotification::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,SlotMachineTradeNotification> STREAM_CODEC = StreamHelper.combine(taxableStreamFields(),
                TraderCategory.TYPE.streamCodec(),n -> n.traderData,
                ByteBufCodecs.either(ItemData.STREAM_CODEC_LIST,MoneyValue.STREAM_CODEC),SlotMachineTradeNotification::getRewardData,
                MoneyValue.STREAM_CODEC,n -> n.cost,
                ByteBufCodecs.STRING_UTF8,n -> n.customer,
                SlotMachineTradeNotification::new);

        @Override
        protected SlotMachineTradeNotification createNew() { return new SlotMachineTradeNotification(); }
        @Override
        public MapCodec<SlotMachineTradeNotification> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SlotMachineTradeNotification> streamCodec() { return STREAM_CODEC; }
    }


}
