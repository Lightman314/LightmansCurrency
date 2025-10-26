package io.github.lightman314.lightmanscurrency.integration.create.attributes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CoinAttributeType implements ItemAttributeType {

    private static final CoinAttribute IS_COIN_ATTRIBUTE = new NormalCoinAttribute("");

    public static final MapCodec<CoinAttribute> CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    Codec.BOOL.fieldOf("ancient").forGetter(CoinAttribute::isAncient),
                    Codec.STRING.fieldOf("chain").forGetter(CoinAttribute::getChain)
            ).apply(builder,CoinAttributeType::parse));

    public static final StreamCodec<RegistryFriendlyByteBuf,CoinAttribute> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL,CoinAttribute::isAncient,ByteBufCodecs.STRING_UTF8,CoinAttribute::getChain,CoinAttributeType::parse);

    @Override
    public ItemAttribute createAttribute() { return IS_COIN_ATTRIBUTE; }

    @Override
    public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
        ChainData chain = CoinAPI.getApi().ChainDataOfCoin(stack);
        if(chain != null)
            return List.of(IS_COIN_ATTRIBUTE,new NormalCoinAttribute(chain.chain));
        else if(stack.getItem() == ModItems.COIN_ANCIENT.get())
            return List.of(IS_COIN_ATTRIBUTE,AncientCoinAttribute.INSTANCE);
        return List.of();
    }

    @Override
    public MapCodec<? extends ItemAttribute> codec() { return CODEC; }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ? extends ItemAttribute> streamCodec() { return STREAM_CODEC; }

    public static CoinAttribute parse(boolean ancient,String chain)
    {
        if(ancient)
            return AncientCoinAttribute.INSTANCE;
        if(chain.isEmpty())
            return IS_COIN_ATTRIBUTE;
        return new NormalCoinAttribute(chain);
    }

    public static abstract class CoinAttribute implements ItemAttribute
    {
        public boolean isAncient() { return false; }
        public abstract String getChain();
    }

    public static class AncientCoinAttribute extends CoinAttribute
    {

        public static final CoinAttribute INSTANCE = new AncientCoinAttribute();

        private AncientCoinAttribute() {}
        @Override
        public boolean isAncient() { return true; }
        @Override
        public String getChain() { return ""; }
        @Override
        public boolean appliesTo(ItemStack stack, Level world) { return stack.getItem() == ModItems.COIN_ANCIENT.get(); }
        @Override
        public ItemAttributeType getType() { return LCItemAttributes.COIN_ATTRIBUTE.get(); }
        @Override
        public String getTranslationKey() { return "lightmanscurrency.coin.chain"; }
        @Override
        public Object[] getTranslationParameters() { return new Object[] { LCText.ANCIENT_COIN_VALUE_NAME.get() }; }
    }

    public static class NormalCoinAttribute extends CoinAttribute
    {

        private final String chain;
        public NormalCoinAttribute(String chain) { this.chain = chain; }

        @Override
        public String getChain() { return this.chain; }

        @Override
        public boolean appliesTo(ItemStack stack, Level world) {
            ChainData data = CoinAPI.getApi().ChainDataOfCoin(stack);
            if(data == null)
                return false;
            if(this.isNullType())
                return stack.getItem() == ModItems.COIN_ANCIENT.get();
            else
                return data.chain.equals(this.chain);
        }
        @Override
        public ItemAttributeType getType() { return LCItemAttributes.COIN_ATTRIBUTE.get(); }

        private boolean isNullType() { return this.chain.isEmpty(); }

        private Component getChainName()
        {
            ChainData data = CoinAPI.getApi().ChainData(this.chain);
            return data == null ? EasyText.literal(this.chain) : data.getDisplayName();
        }
        @Override
        public String getTranslationKey() { return this.chain.isEmpty() ? "lightmanscurrency.coin.any" : "lightmanscurrency.coin.chain"; }
        @Override
        public Object[] getTranslationParameters() { return this.isNullType() ? new Object[] {} : new Object[] { this.getChainName() }; }
    }

}
