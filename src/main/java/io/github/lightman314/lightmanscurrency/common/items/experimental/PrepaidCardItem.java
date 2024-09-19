package io.github.lightman314.lightmanscurrency.common.items.experimental;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.List;

public class PrepaidCardItem extends Item {

    public PrepaidCardItem(Properties properties) { super(properties); }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nonnull TooltipContext context, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {

        tooltip.add(LCText.TOOLTIP_BETA.getWithStyle(ChatFormatting.RED));

        //Tooltip with balance
        MoneyValue value = stack.getOrDefault(ModDataComponents.MONEY_VALUE,MoneyValue.empty());
        tooltip.add(LCText.GUI_BANK_BALANCE.get(value.getText(LCText.GUI_MONEY_STORAGE_EMPTY.get())).withStyle(ChatFormatting.GRAY));
        //Deletion Warning
        tooltip.add(LCText.TOOLTIP_PREPAID_CARD_DELETE_WARNING.getWithStyle(ChatFormatting.GRAY));

    }

    @Override
    public void inventoryTick(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull Entity player, int slot, boolean isSelected) {
        if(stack.getOrDefault(ModDataComponents.MONEY_VALUE,MoneyValue.empty()).isEmpty())
        {
            //Remove the empty pre-paid card from the players inventory
            stack.setCount(0);
        }
    }
}
