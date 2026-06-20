package io.github.lightman314.lightmanscurrency.api.coins.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import javax.annotation.Nullable;

public class CoinValuePair {

    public static final Codec<CoinValuePair> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("coin").forGetter(p -> p.coin),
            Codec.LONG.fieldOf("amount").forGetter(p -> p.amount)
    ).apply(builder,CoinValuePair::new));
    public static final StreamCodec<RegistryFriendlyByteBuf,CoinValuePair> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(BuiltInRegistries.ITEM.key()),p -> p.coin,
            ByteBufCodecs.LONG,p -> p.amount,
            CoinValuePair::new);

    public final Item coin;
    public final long amount;

    public CoinValuePair(Item coin,long amount) {
        this.coin = coin;
        this.amount = amount;
    }

    public ItemStackTemplate asTemplate() { return new ItemStackTemplate(this.coin,(int)Math.min(this.amount,Integer.MAX_VALUE)); }
    public ItemStack asStack() { return new ItemStack(this.coin,(int)Math.min(this.amount,Integer.MAX_VALUE)); }

    public CoinValuePair addAmount(long amount) { return new CoinValuePair(this.coin,this.amount + amount); }
    @Nullable
    public CoinValuePair removeAmount(long amount) {
        long newAmount = this.amount - amount;
        if(newAmount <= 0)
            return null;
        return new CoinValuePair(this.coin,newAmount);
    }

}
