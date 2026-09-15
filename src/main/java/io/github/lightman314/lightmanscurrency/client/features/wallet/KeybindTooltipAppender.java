package io.github.lightman314.lightmanscurrency.client.features.wallet;

import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.common.tooltip.TooltipAppender;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public record KeybindTooltipAppender(Predicate<ItemStack> filter, TextEntry text, KeyMapping key) implements TooltipAppender {
    public KeybindTooltipAppender(Class<? extends Item> item,TextEntry text,KeyMapping key) { this(s -> item.isInstance(s.getItem()),text,key); }

    @Override
    public void append(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, @Nullable Player player, TooltipFlag tooltipFlag, Consumer<Component> builder) {
        if(this.filter.test(stack))
            builder.accept(text.get(key.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.YELLOW)));
    }

}
