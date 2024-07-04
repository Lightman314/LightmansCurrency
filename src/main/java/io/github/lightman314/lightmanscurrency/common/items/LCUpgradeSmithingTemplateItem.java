package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.common.text.MultiLineTextEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nonnull;
import java.util.List;

public class LCUpgradeSmithingTemplateItem extends Item {

    private final MultiLineTextEntry tooltip;

    public LCUpgradeSmithingTemplateItem(@Nonnull MultiLineTextEntry tooltip, Properties properties) { super(properties); this.tooltip = tooltip; }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nonnull TooltipContext context, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.addAll(this.tooltip.getWithStyle(ChatFormatting.GRAY));
    }

}
