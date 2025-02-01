package io.github.lightman314.lightmanscurrency.common.enchantments.data;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import net.minecraft.resources.ResourceLocation;

public class BonusForEnchantment {

    public final MoneyValue bonusCost;
    public final ResourceLocation enchantment;
    public final int maxLevelCalculation;
    public BonusForEnchantment(MoneyValue bonusCost, ResourceLocation enchantment, int maxLevelCalculation)
    {
        this.bonusCost = bonusCost;
        this.enchantment = enchantment;
        this.maxLevelCalculation = maxLevelCalculation;
    }

}
