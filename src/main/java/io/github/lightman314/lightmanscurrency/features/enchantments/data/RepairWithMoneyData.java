package io.github.lightman314.lightmanscurrency.features.enchantments.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.values.source.MoneyValueSource;
import io.github.lightman314.lightmanscurrency.api.money.values.source.builtin.DirectSource;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public final class RepairWithMoneyData {

    public static final Codec<RepairWithMoneyData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            MoneyValueSource.CODEC.fieldOf("baseCost").forGetter(d -> d.baseCost),
            BonusForEnchantment.CODEC.listOf().optionalFieldOf("extraEnchantmentCost",List.of()).forGetter(d -> d.enchantmentExtras),
            ItemOverride.CODEC.listOf().optionalFieldOf("itemOverrides",List.of()).forGetter(d -> d.itemOverrides)
    ).apply(builder,RepairWithMoneyData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf,RepairWithMoneyData> STREAM_CODEC = StreamCodec.composite(
            MoneyValueSource.STREAM_CODEC,d -> d.baseCost,
            BonusForEnchantment.STREAM_CODEC.apply(ByteBufCodecs.list()),d -> d.enchantmentExtras,
            ItemOverride.STREAM_CODEC.apply(ByteBufCodecs.list()),d -> d.itemOverrides,
            RepairWithMoneyData::new);

    private final MoneyValueSource baseCost;
    public MoneyValue getBaseCost() { return this.baseCost.getMoneyValue(); }
    private final List<BonusForEnchantment> enchantmentExtras;
    private final List<ItemOverride> itemOverrides;

    public RepairWithMoneyData(MoneyValueSource baseCost,List<BonusForEnchantment> enchantmentExtras,List<ItemOverride> itemOverrides) {
        this.baseCost = baseCost;
        this.enchantmentExtras = enchantmentExtras;
        this.itemOverrides = itemOverrides;
    }

    public MoneyValue getRepairCost(ItemStack item, ItemEnchantments enchantments) {

        MoneyValue base = this.baseCost.getMoneyValue();
        for(ItemOverride override : this.itemOverrides) {
            if(override.matches(item))
                base = override.getBaseCost();
        }
        MoneyValue total = base;
        for(Holder<Enchantment> holder : enchantments.keySet()) {
            for(BonusForEnchantment b : this.enchantmentExtras) {
                if(holder.is(b.enchantment)) {
                    int level = Math.min(enchantments.getLevel(holder),b.maxLevelCalculation <= 0 ? Integer.MAX_VALUE : b.maxLevelCalculation);
                    MoneyValue bonusCost = b.getBonusCost();
                    MoneyValue toAdd = level == 1 ? bonusCost : bonusCost.fromInternalValue(bonusCost.getInternalValue() * level);
                    MoneyValue newTotal = total.addValue(toAdd);
                    if(newTotal != null)
                        total = newTotal;
                }
            }
        }
        return total;
    }

    @Override
    public int hashCode() { return Objects.hash(this.baseCost,this.enchantmentExtras,this.itemOverrides); }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        return obj instanceof RepairWithMoneyData other && this.baseCost.equals(other.baseCost) && this.enchantmentExtras.equals(other.enchantmentExtras) && this.itemOverrides.equals(other.itemOverrides);
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Builder() {}

        private MoneyValueSource baseCost = new DirectSource(MoneyValue.empty());
        private final List<BonusForEnchantment> enchantmentExtras = new ArrayList<>();
        private final List<ItemOverride> itemOverrides = new ArrayList<>();

        public Builder baseCost(MoneyValue value) { return this.baseCost(new DirectSource(value)); }
        public Builder baseCost(MoneyValueSource value) { this.baseCost = value; return this; }

        public Builder bonusForEnchantment(ResourceKey<Enchantment> enchantment,MoneyValue bonusCost,int maxLevelCalculation) { return this.bonusForEnchantment(enchantment,new DirectSource(bonusCost),maxLevelCalculation); }
        public Builder bonusForEnchantment(ResourceKey<Enchantment> enchantment,MoneyValueSource bonusCost,int maxLevelCalculation) { return this.bonusForEnchantment(enchantment.identifier(),bonusCost,maxLevelCalculation); }
        public Builder bonusForEnchantment(Identifier enchantment, MoneyValue bonusCost, int maxLevelCalculation) { return this.bonusForEnchantment(enchantment,new DirectSource(bonusCost),maxLevelCalculation); }
        public Builder bonusForEnchantment(Identifier enchantment, MoneyValueSource bonusCost, int maxLevelCalculation) {
            this.enchantmentExtras.add(new BonusForEnchantment(bonusCost,enchantment,maxLevelCalculation));
            return this;
        }

        public Builder itemOverride(ItemOverride override) { this.itemOverrides.add(override); return this; }
        public ItemOverride.Builder<Builder> itemOverride() { return ItemOverride.builder(this,this::itemOverride); }

        public RepairWithMoneyData build() { return new RepairWithMoneyData(this.baseCost,this.enchantmentExtras,this.itemOverrides); }


    }

}
