package io.github.lightman314.lightmanscurrency.common.items;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class ChocolateCoinItem extends Item {

    private final List<MobEffectInstance> effects;
    private final float healing;


    public ChocolateCoinItem(MobEffectInstance... effects) { this(0f, effects); }
    public ChocolateCoinItem(float healing, MobEffectInstance... effects) { this(new Properties(), healing, effects); }
    public ChocolateCoinItem(Properties properties, MobEffectInstance... effects) { this(properties, 0f, effects); }
    public ChocolateCoinItem(Properties properties, float healing, MobEffectInstance... effects) {
        //Same food properties as the vanilla cookie, but with AlwaysEat flag
        super(properties.food(new FoodProperties.Builder().alwaysEat().nutrition(2).saturationMod(0.1f).build()));
        this.effects = ImmutableList.copyOf(effects);
        this.healing = healing;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if(!Config.serverSpec.isLoaded())
            return;
        if(Config.SERVER.chocolateCoinEffects.get())
        {
            if(this.healing > 0)
                tooltip.add(EasyText.translatable("tooltip.lightmanscurrency.chocolate_coin.healing", (int)this.healing).withStyle(ChatFormatting.BLUE));
            if(this.effects.size() > 0)
                PotionUtils.addPotionTooltip(this.effects, tooltip, 1f);
        }
    }

    @Nonnull
    @Override
    public ItemStack finishUsingItem(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull LivingEntity entity) {
        if(Config.SERVER.chocolateCoinEffects.get())
        {
            if(this.healing > 0f)
                entity.heal(this.healing);
            for(MobEffectInstance effect : this.effects)
                entity.addEffect(new MobEffectInstance(effect));
        }
        return super.finishUsingItem(stack, level, entity);
    }

}
