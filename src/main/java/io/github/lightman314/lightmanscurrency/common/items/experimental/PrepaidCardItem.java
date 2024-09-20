package io.github.lightman314.lightmanscurrency.common.items.experimental;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.capability.money.CapabilityMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.common.items.colored.ColoredItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class PrepaidCardItem extends Item implements ColoredItem {

    public PrepaidCardItem(Properties properties) { super(properties); }

    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return CapabilityMoneyHandler.createProvider(new PrepaidCardMoneyHandler(stack));
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {

        tooltip.add(LCText.TOOLTIP_BETA.getWithStyle(ChatFormatting.RED));

        //Tooltip with balance
        CompoundTag tag = stack.getOrCreateTag();
        MoneyValue value = tag.contains("StoredMoney") ? MoneyValue.load(tag.getCompound("StoredMoney")) : MoneyValue.empty();
        tooltip.add(LCText.GUI_BANK_BALANCE.get(value.getText(LCText.GUI_MONEY_STORAGE_EMPTY.get())).withStyle(ChatFormatting.GRAY));
        //Deletion Warning
        tooltip.add(LCText.TOOLTIP_PREPAID_CARD_DELETE_WARNING.getWithStyle(ChatFormatting.GRAY));

    }

    @Override
    public void inventoryTick(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull Entity player, int slot, boolean isSelected) {
        CompoundTag tag = stack.getOrCreateTag();
        if(!tag.contains("StoredMoney") || MoneyValue.load(tag.getCompound("StoredMoney")).isEmpty())
        {
            //Remove the empty pre-paid card from the players inventory
            stack.setCount(0);
        }
    }
}