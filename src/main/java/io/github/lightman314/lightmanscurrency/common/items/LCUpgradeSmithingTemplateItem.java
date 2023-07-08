package io.github.lightman314.lightmanscurrency.common.items;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class LCUpgradeSmithingTemplateItem extends Item {

    private final MutableComponent tooltip;

    public LCUpgradeSmithingTemplateItem(MutableComponent tooltip, Properties properties) { super(properties); this.tooltip = tooltip; }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(this.tooltip);
    }

}
