package io.github.lightman314.lightmanscurrency.api.money.types.builtin;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.types.IPlayerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.coins.CoinContainerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.coins.CoinPlayerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValueParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class CoinCurrencyType extends CurrencyType {

    public static final ResourceLocation TYPE = new ResourceLocation(MoneyAPI.MODID, "coins");
    public static final CoinCurrencyType INSTANCE = new CoinCurrencyType();

    protected CoinCurrencyType() { super(TYPE); }

    @Nonnull
    public static String getUniqueName(@Nonnull String chain) { return TYPE.toString() + "_" + chain; }
    @Nonnull
    @Override
    protected MoneyValue sumValuesInternal(@Nonnull List<MoneyValue> values) {
        long totalValue = 0;
        ChainData chain = null;
        for(MoneyValue val : values)
        {
            if(val instanceof CoinValue cv)
            {
                //Coin value's will be included for stored money
                if(chain == null)
                    chain = CoinAPI.API.ChainData(cv.getChain());
                if(chain != null && chain.chain.equals(cv.getChain()))
                    totalValue += cv.getCoreValue();
            }
        }
        if(chain != null)
            return CoinValue.fromNumber(chain.chain, totalValue);
        return MoneyValue.empty();
    }

    @Nullable
    @Override
    public IPlayerMoneyHandler createMoneyHandlerForPlayer(@Nonnull Player player) { return new CoinPlayerMoneyHandler(player); }

    @Nullable
    @Override
    public IMoneyHandler createMoneyHandlerForContainer(@Nonnull Container container, @Nonnull Consumer<ItemStack> overflowHandler) { return new CoinContainerMoneyHandler(container, overflowHandler); }

    @Override
    public MoneyValue loadMoneyValue(@Nonnull CompoundTag valueTag) { return CoinValue.loadCoinValue(valueTag); }

    @Override
    public MoneyValue loadMoneyValueJson(@Nonnull JsonObject json) { return CoinValue.loadCoinValue(json); }

    @Nonnull
    @Override
    public MoneyValueParser getValueParser() { return CoinValueParser.INSTANCE; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Object> getInputHandlers(@Nullable Player player) {
        List<Object> results = new ArrayList<>();
        for(ChainData chain : CoinAPI.API.AllChainData())
        {
            //Only add input handler if the chain is visible to the player
            if(player == null || chain.isVisibleTo(player))
            {
                Object i = chain.getInputHandler();
                if(i != null)
                    results.add(i);
            }
        }
        return results;
    }

}
