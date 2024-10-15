package io.github.lightman314.lightmanscurrency.common.items;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public class ChocolateCoinItem extends Item {


    public ChocolateCoinItem(MobEffectInstance... effects) { this(0f, effects); }
    public ChocolateCoinItem(float healing, MobEffectInstance... effects) { this(new Properties(), healing, effects); }
    public ChocolateCoinItem(Properties properties, MobEffectInstance... effects) { this(properties, 0f, effects); }
    public ChocolateCoinItem(Properties properties, float healing, MobEffectInstance... effects) {
        //Same food properties as the vanilla cookie, but with AlwaysEat flag
        super(properties
                .food(new FoodProperties.Builder().alwaysEdible().nutrition(2).saturationModifier(0.1f).build())
                .component(ModDataComponents.CHOCOLATE_HEALING,healing)
                .component(ModDataComponents.CHOCOLATE_EFFECTS,ImmutableList.copyOf(effects)));
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nonnull TooltipContext context, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        //If registries aren't loaded, assume the configs aren't either
        if(context.registries() == null)
            return;
        if(LCConfig.SERVER.chocolateCoinEffects.get())
        {
            float healing = stack.getOrDefault(ModDataComponents.CHOCOLATE_HEALING,0f);
            if(healing > 0f)
                tooltip.add(LCText.TOOLTIP_HEALING.get((int)healing).withStyle(ChatFormatting.BLUE));
            List<MobEffectInstance> effects = stack.getOrDefault(ModDataComponents.CHOCOLATE_EFFECTS,ImmutableList.of());
            if(!effects.isEmpty())
                new PotionContents(Optional.empty(),Optional.empty(), effects).addPotionTooltip(tooltip::add, 1f, context.tickRate());
        }
    }

    @Nonnull
    @Override
    public ItemStack finishUsingItem(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull LivingEntity entity) {
        if(LCConfig.SERVER.chocolateCoinEffects.get())
        {
            float healing = stack.getOrDefault(ModDataComponents.CHOCOLATE_HEALING,0f);
            if(healing > 0f)
                entity.heal(healing);
            List<MobEffectInstance> effects = stack.getOrDefault(ModDataComponents.CHOCOLATE_EFFECTS,ImmutableList.of());
            for(MobEffectInstance effect : effects)
                entity.addEffect(new MobEffectInstance(effect));
        }
        return super.finishUsingItem(stack, level, entity);
    }

}
