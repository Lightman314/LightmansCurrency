package io.github.lightman314.lightmanscurrency.integration.create.attributes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WalletAttributeType implements ItemAttributeType {

    public enum AbilityType
    {
        EXCHANGE,
        PICKUP,
        BANK
    }

    public static final MapCodec<WalletAttribute> CODEC = Codec.STRING.xmap(WalletAttributeType::parse,a -> a.ability.name()).fieldOf("ability");
    public static final StreamCodec<ByteBuf,WalletAttribute> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(WalletAttributeType::parse, a -> a.ability.name());

    private static final WalletAttribute PICKUP_ABILITY = new WalletAttribute(AbilityType.PICKUP);
    private static final WalletAttribute EXCHANGE_ABILITY = new WalletAttribute(AbilityType.EXCHANGE);
    private static final WalletAttribute BANK_ABILITY = new WalletAttribute(AbilityType.BANK);

    @Override
    public ItemAttribute createAttribute() { return new WalletAttribute(AbilityType.PICKUP); }
    @Override
    public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
        List<ItemAttribute> result = new ArrayList<>();
        if(stack.getItem() instanceof WalletItem wallet)
        {
            if(WalletItem.CanExchange(wallet))
                result.add(EXCHANGE_ABILITY);
            if(WalletItem.CanPickup(wallet))
                result.add(PICKUP_ABILITY);
            if(WalletItem.HasBankAccess(wallet))
                result.add(BANK_ABILITY);
        }
        return result;
    }

    @Override
    public MapCodec<? extends ItemAttribute> codec() { return CODEC; }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ? extends ItemAttribute> streamCodec() { return STREAM_CODEC; }

    private static WalletAttribute parse(String abilityType)
    {
        return switch (EnumUtil.enumFromString(abilityType,AbilityType.values(),AbilityType.PICKUP)) {
            case PICKUP -> PICKUP_ABILITY;
            case EXCHANGE -> EXCHANGE_ABILITY;
            case BANK -> BANK_ABILITY;
        };
    }

    public record WalletAttribute(AbilityType ability) implements ItemAttribute
    {
        @Override
        public boolean appliesTo(ItemStack stack, Level world) {
            if(stack.getItem() instanceof WalletItem wallet)
            {
                return switch (this.ability) {
                    case PICKUP -> WalletItem.CanPickup(wallet);
                    case EXCHANGE -> WalletItem.CanExchange(wallet);
                    case BANK -> WalletItem.HasBankAccess(wallet);
                };
            }
            return false;
        }
        @Override
        public ItemAttributeType getType() { return LCItemAttributes.WALLET_ATTRIBUTE.get(); }
        @Override
        public String getTranslationKey() { return "lightmanscurrency.wallet_ability." + this.ability.name().toLowerCase(Locale.ENGLISH); }

    }

}
