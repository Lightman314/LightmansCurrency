package io.github.lightman314.lightmanscurrency.api.variants.item.data;

import io.github.lightman314.lightmanscurrency.LCText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.function.Consumer;

public class VariantLock implements TooltipProvider {

    public static final VariantLock INSTANCE = new VariantLock();
    private VariantLock() {}

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> adder, TooltipFlag flag) {
        adder.accept(LCText.TOOLTIP_MODEL_VARIANT_LOCKED.getWithStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean equals(Object obj) { return obj instanceof VariantLock; }
    @Override
    public int hashCode() { return 0; }

}
