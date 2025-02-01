package io.github.lightman314.lightmanscurrency.common.enchantments.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.MoneyValueOption;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import javax.annotation.Nonnull;
import java.util.*;

public class RepairWithMoneyData
{

    public static final Codec<RepairWithMoneyData> CODEC =
            RecordCodecBuilder.create(builder ->
                    builder.group(ValueInput.CODEC.optionalFieldOf("baseCost",new ValueInput("")).forGetter(d -> d.baseCost),
                                    BonusForEnchantment.CODEC.listOf().fieldOf("enchantmentExtraCost").forGetter(d -> d.enchantmentExtras),
                                    ItemOverride.CODEC.listOf().fieldOf("itemOverrides").forGetter(d -> d.itemOverrides))
                            .apply(builder,RepairWithMoneyData::new));


    private boolean forceCache = true;
    private boolean updatingCache = false;
    private long lastCacheTime = 0;
    private final ValueInput baseCost;
    public MoneyValue getBaseCost() { this.checkCache(); return this.baseCost.getCost(); }
    private final List<BonusForEnchantment> enchantmentExtras;
    private final List<ItemOverride> itemOverrides;
    public MoneyValue getRepairCost(@Nonnull ItemStack item, @Nonnull ItemEnchantments enchantments)
    {
        this.checkCache();
        MoneyValue base = this.baseCost.getCost();
        for(ItemOverride io : this.itemOverrides)
        {
            if(io.matches(item))
                base = io.getCost();
        }
        MoneyValue total = base;
        for(Holder<Enchantment> holder : enchantments.keySet())
        {
            for(BonusForEnchantment b : this.enchantmentExtras)
            {
                if(holder.is(b.enchantment))
                {
                    int level = Math.min(enchantments.getLevel(holder),Math.max(1,b.maxLevelCalculation <= 0 ? Integer.MAX_VALUE : b.maxLevelCalculation));
                    MoneyValue toAdd = b.getCost().percentageOfValue(level * 100);
                    MoneyValue newTotal = total.addValue(toAdd);
                    if(newTotal != null)
                        total = newTotal;
                }
            }
        }
        return total == null ? base : total;
    }

    private RepairWithMoneyData(@Nonnull ValueInput baseCost, @Nonnull List<BonusForEnchantment> enchantmentExtras, @Nonnull List<ItemOverride> itemOverrides) {
        this.baseCost = baseCost;
        this.enchantmentExtras = ImmutableList.copyOf(enchantmentExtras);
        this.enchantmentExtras.forEach(e -> e.init(this));
        this.itemOverrides = ImmutableList.copyOf(itemOverrides);
    }

    private void checkCache()
    {
        if(this.updatingCache)
            return;
        //Re-parse values every 2 minutes
        if(this.forceCache || System.currentTimeMillis() - this.lastCacheTime >= 1000 * 120)
        {
            this.updatingCache = true;
            this.forceCache = false;
            this.lastCacheTime = System.currentTimeMillis();
            this.baseCost.updateCache();
            this.itemOverrides.forEach(ValueInput::updateCache);
            this.enchantmentExtras.forEach(ValueInput::updateCache);
            this.updatingCache = false;
        }
    }

    @Override
    public int hashCode() { return Objects.hash(this.baseCost,this.enchantmentExtras,this.itemOverrides); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof RepairWithMoneyData other)
            return this.baseCost.equals(other.baseCost) && this.itemOverrides.equals(other.itemOverrides) && this.enchantmentExtras.equals(other.enchantmentExtras);
        return false;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder
    {
        private ValueInput baseCost = new ValueInput("");
        private final Map<ResourceLocation,BonusForEnchantment> enchantmentExtras = new HashMap<>();
        private final List<ItemOverride> itemOverrides = new ArrayList<>();

        public Builder baseCost(String baseCostInput) { this.baseCost = new ValueInput(baseCostInput); return this; }
        public Builder baseCost(MoneyValueOption configOption) { return this.baseCost(ValueInput.writeConfig(configOption)); }

        public Builder bonusForEnchantment(ResourceKey<Enchantment> enchantment, MoneyValueOption bonusCost, int maxLevelCalculation) { return this.bonusForEnchantment(enchantment.location(),bonusCost,maxLevelCalculation); }
        public Builder bonusForEnchantment(ResourceKey<Enchantment> enchantment, String bonusCost, int maxLevelCalculation) { return this.bonusForEnchantment(enchantment.location(),bonusCost,maxLevelCalculation); }
        public Builder bonusForEnchantment(ResourceLocation enchantmentID, MoneyValueOption bonusCost, int maxLevelCalculation) { return this.bonusForEnchantment(enchantmentID,ValueInput.writeConfig(bonusCost),maxLevelCalculation); }
        public Builder bonusForEnchantment(ResourceLocation enchantmentID, String bonusCost, int maxLevelCalculation)
        {
            this.enchantmentExtras.put(enchantmentID,new BonusForEnchantment(bonusCost,enchantmentID,maxLevelCalculation));
            return this;
        }

        public Builder itemOverride(ItemOverride override) { this.itemOverrides.add(override); return this; }
        public ItemOverride.Builder itemOverride(String baseCost) { return ItemOverride.builder(baseCost,this); }

        public RepairWithMoneyData build() { return new RepairWithMoneyData(this.baseCost,this.enchantmentExtras.values().stream().toList(),this.itemOverrides); }

    }

}
