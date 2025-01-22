package io.github.lightman314.lightmanscurrency.common.enchantments.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;

public final class BonusForEnchantment extends ValueInput {

    public static final Codec<BonusForEnchantment> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(Codec.STRING.fieldOf("bonusCost").forGetter(v -> v.costInput),
                    ResourceLocation.CODEC.fieldOf("enchantment").forGetter(v -> v.enchantment),
                    Codec.INT.optionalFieldOf("maxLevelCalculation",1).forGetter(v -> v.maxLevelCalculation))
                    .apply(builder,BonusForEnchantment::new));

    public final ResourceLocation enchantment;
    public final int maxLevelCalculation;
    private RepairWithMoneyData parent = null;

    public BonusForEnchantment(String costInput, ResourceLocation enchantment, int maxLevelCalculation) {
        super(costInput);
        this.enchantment = enchantment;
        this.maxLevelCalculation = maxLevelCalculation;
    }

    @Nullable
    @Override
    protected MoneyValue getMatch() { return this.parent == null ? null : this.parent.getBaseCost(); }

    public void init(RepairWithMoneyData parent) { this.parent = parent; }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof BonusForEnchantment other)
            return other.costInput.equals(this.costInput) && this.enchantment.equals(other.enchantment) && this.maxLevelCalculation == other.maxLevelCalculation;
        return false;
    }

    @Override
    public int hashCode() { return Objects.hash(this.costInput,this.enchantment,this.maxLevelCalculation); }
}
