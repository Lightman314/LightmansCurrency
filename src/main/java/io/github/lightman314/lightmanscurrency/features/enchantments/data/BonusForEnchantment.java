package io.github.lightman314.lightmanscurrency.features.enchantments.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.values.source.MoneyValueSource;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.Objects;

public final class BonusForEnchantment {

    public static final Codec<BonusForEnchantment> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            MoneyValueSource.CODEC.fieldOf("bonusCost").forGetter(v -> v.bonusCost),
            Identifier.CODEC.fieldOf("enchantment").forGetter(v -> v.enchantment),
            Codec.intRange(0,Integer.MAX_VALUE).optionalFieldOf("maxLevelCalculation",1).forGetter(v -> v.maxLevelCalculation)
    ).apply(builder,BonusForEnchantment::new));
    public static final StreamCodec<RegistryFriendlyByteBuf,BonusForEnchantment> STREAM_CODEC = StreamCodec.composite(
            MoneyValueSource.STREAM_CODEC,b -> b.bonusCost,
            Identifier.STREAM_CODEC,b -> b.enchantment,
            ByteBufCodecs.VAR_INT,b -> b.maxLevelCalculation,
            BonusForEnchantment::new);

    private final MoneyValueSource bonusCost;
    public MoneyValue getBonusCost() { return this.bonusCost.getMoneyValue(); }
    public final Identifier enchantment;
    public final int maxLevelCalculation;

    public BonusForEnchantment(MoneyValueSource bonusCost,Identifier enchantment) { this(bonusCost,enchantment,1); }
    public BonusForEnchantment(MoneyValueSource bonusCost,Identifier enchantment,int maxLevelCalculation) {
        this.bonusCost = bonusCost;
        this.enchantment = enchantment;
        this.maxLevelCalculation = maxLevelCalculation;
    }

    @Override
    public int hashCode() { return Objects.hash(this.bonusCost,this.enchantment,this.maxLevelCalculation); }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        return obj instanceof BonusForEnchantment bfe && bfe.bonusCost.equals(this.bonusCost) && bfe.enchantment.equals(this.enchantment) && bfe.maxLevelCalculation == this.maxLevelCalculation;
    }
}
