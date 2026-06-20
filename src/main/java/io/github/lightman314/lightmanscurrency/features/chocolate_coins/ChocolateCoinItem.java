package io.github.lightman314.lightmanscurrency.features.chocolate_coins;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ChocolateCoinItem extends Item {

    public ChocolateCoinItem(Properties properties, MobEffectInstance... effects) { this(properties,0f,effects); }
    public ChocolateCoinItem(Properties properties, float healing,MobEffectInstance... effects) {
        super(properties
                .food(new FoodProperties.Builder().alwaysEdible().nutrition(2).saturationModifier(0.1f).build())
                .component(LCDataComponents.CHOCOLATE_HEALING,healing)
                .component(LCDataComponents.CHOCOLATE_EFFECTS,ImmutableList.copyOf(effects)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        //If registries aren't loaded, assume the configs aren't either
        if(context.registries() == null)
            return;
        if(LCConfig.SERVER.chocolateCoinEffects.get())
        {
            float healing = stack.getOrDefault(LCDataComponents.CHOCOLATE_HEALING,0f);
            if(healing > 0f)
                builder.accept(LCText.Items.TOOLTIP_HEALING.get((int)healing).withStyle(ChatFormatting.BLUE));
            List<MobEffectInstance> effects = stack.getOrDefault(LCDataComponents.CHOCOLATE_EFFECTS,ImmutableList.of());
            if(!effects.isEmpty()) //Add effects tooltips as though this was a potion
                new PotionContents(Optional.empty(),Optional.empty(),effects,Optional.empty()).addToTooltip(context,builder,flag,stack);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if(LCConfig.SERVER.chocolateCoinEffects.get())
        {
            float healing = stack.getOrDefault(LCDataComponents.CHOCOLATE_HEALING,0f);
            if(healing > 0f)
                entity.heal(healing);
            List<MobEffectInstance> effects = stack.getOrDefault(LCDataComponents.CHOCOLATE_EFFECTS,ImmutableList.of());
            for(MobEffectInstance effect : effects)
                entity.addEffect(new MobEffectInstance(effect));
        }
        return super.finishUsingItem(stack,level,entity);
    }
}
