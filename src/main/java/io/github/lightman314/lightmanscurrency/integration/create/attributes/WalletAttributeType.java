package io.github.lightman314.lightmanscurrency.integration.create.attributes;

import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
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

    public static class WalletAttribute implements ItemAttribute
    {

        private AbilityType ability;
        public WalletAttribute(AbilityType ability) { this.ability = ability; }

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
        public void save(CompoundTag tag) {
            tag.putString("ability",this.ability.name());
        }
        @Override
        public void load(CompoundTag tag) {
            this.ability = EnumUtil.enumFromString(tag.getString("ability"),AbilityType.values(),AbilityType.PICKUP);
        }

        @Override
        public String getTranslationKey() { return "lightmanscurrency.wallet_ability." + this.ability.name().toLowerCase(Locale.ENGLISH); }

    }

}