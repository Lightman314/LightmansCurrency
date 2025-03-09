package io.github.lightman314.lightmanscurrency.common.items.cards;

import io.github.lightman314.lightmanscurrency.api.capability.money.MoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class PrepaidCardMoneyHandler extends MoneyHandler {

    private final ItemStack card;
    public PrepaidCardMoneyHandler(@Nonnull ItemStack card) { this.card = card; }

    @Nonnull
    private MoneyValue getCardMoney() {
        CompoundTag tag = this.card.getOrCreateTag();
        return tag.contains("StoredMoney") ? MoneyValue.load(tag.getCompound("StoredMoney")) : MoneyValue.empty();
    }
    private void setCardMoney(@Nonnull MoneyValue value) {
        CompoundTag tag = this.card.getOrCreateTag();
        tag.put("StoredMoney",value.save());
        if(value.isEmpty())
            this.card.setCount(0);
    }

    @Nonnull
    @Override
    public MoneyValue insertMoney(@Nonnull MoneyValue insertAmount, boolean simulation) { return insertAmount; }

    @Nonnull
    @Override
    public MoneyValue extractMoney(@Nonnull MoneyValue extractAmount, boolean simulation) {
        MoneyValue value = this.getCardMoney();
        if(value.sameType(extractAmount))
        {
            MoneyValue amountToTake = value.containsValue(extractAmount) ? extractAmount : value;
            if(!simulation)
                this.setCardMoney(value.subtractValue(amountToTake));
            return extractAmount.subtractValue(amountToTake);
        }
        return extractAmount;
    }

    @Override
    public boolean isMoneyTypeValid(@Nonnull MoneyValue value) { return this.getCardMoney().sameType(value); }

    @Override
    protected void collectStoredMoney(@Nonnull MoneyView.Builder builder) {
        builder.add(this.getCardMoney());
    }

}