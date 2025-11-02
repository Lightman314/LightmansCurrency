package io.github.lightman314.lightmanscurrency.integration.create.attributes;

import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CoinAttributeType implements ItemAttributeType {

    private static final CoinAttribute IS_COIN_ATTRIBUTE = new CoinAttribute("");
    private static final CoinAttribute ANCIENT_COIN_ATTRIBUTE = new CoinAttribute(true);

    @Override
    public ItemAttribute createAttribute() { return new CoinAttribute(""); }

    @Override
    public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
        ChainData chain = CoinAPI.getApi().ChainDataOfCoin(stack);
        if(chain != null)
            return List.of(IS_COIN_ATTRIBUTE,new CoinAttribute(chain.chain));
        else if(stack.getItem() == ModItems.COIN_ANCIENT.get())
            return List.of(IS_COIN_ATTRIBUTE,ANCIENT_COIN_ATTRIBUTE);
        return List.of();
    }

    public static class CoinAttribute implements ItemAttribute
    {
        private boolean ancientCoin;
        private String chain;
        private CoinAttribute(boolean ancient) { this.ancientCoin = true; this.chain = ""; }
        private CoinAttribute(String chain) { this.ancientCoin = false; this.chain = chain; }

        @Override
        public boolean appliesTo(ItemStack stack, Level world) {
            if(this.ancientCoin)
                return stack.getItem() == ModItems.COIN_ANCIENT.get();
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

        @Override
        public void save(CompoundTag tag) {
            if(this.ancientCoin)
                tag.putBoolean("Ancient",true);
            else
                tag.putString("Chain",this.chain);
        }

        @Override
        public void load(CompoundTag tag) {
            if(tag.getBoolean("Ancient"))
            {
                this.ancientCoin = true;
                this.chain = "";
            }
            else
            {
                this.ancientCoin = false;
                this.chain = tag.getString("Chain");
            }
        }

        private boolean isNullType() { return this.chain.isEmpty(); }

        private Component getChainName()
        {
            ChainData data = CoinAPI.getApi().ChainData(this.chain);
            return data == null ? EasyText.literal(this.chain) : data.getDisplayName();
        }
        @Override
        public String getTranslationKey() { return this.chain.isEmpty() && !this.ancientCoin ? "lightmanscurrency.coin.any" : "lightmanscurrency.coin.chain"; }
        @Override
        public Object[] getTranslationParameters() {
            if(this.ancientCoin)
                return new Object[] { LCText.ANCIENT_COIN_VALUE_NAME.get() };
            return this.isNullType() ? new Object[] {} : new Object[] { this.getChainName() };
        }

    }

}