package io.github.lightman314.lightmanscurrency.api.money.types.builtin;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.types.IPlayerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.coins.CoinContainerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.coins.CoinPlayerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValueParser;
import io.github.lightman314.lightmanscurrency.common.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CoinCurrencyType extends CurrencyType {

    public static final ResourceLocation TYPE = VersionUtil.lcResource("coins");
    public static final CoinCurrencyType INSTANCE = new CoinCurrencyType();

    protected CoinCurrencyType() { super(TYPE); }

    public static String getUniqueName(String chain) { return TYPE.toString() + "_" + chain; }
    
    @Override
    protected MoneyValue sumValuesInternal(List<MoneyValue> values) {
        long totalValue = 0;
        ChainData chain = null;
        for(MoneyValue val : values)
        {
            if(val instanceof CoinValue cv)
            {
                //Coin value's will be included for stored money
                if(chain == null)
                    chain = CoinAPI.getApi().ChainData(cv.getChain());
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
    public IPlayerMoneyHandler createMoneyHandlerForPlayer(Player player) { return new CoinPlayerMoneyHandler(player); }

    @Nullable
    @Override
    public IMoneyHandler createMoneyHandlerForContainer(Container container, Consumer<ItemStack> overflowHandler, IClientTracker tracker) { return new CoinContainerMoneyHandler(container, overflowHandler); }

    @Override
    public MoneyValue loadMoneyValue(CompoundTag valueTag) { return CoinValue.loadCoinValue(valueTag); }

    @Override
    public MoneyValue loadMoneyValueJson(JsonObject json) { return CoinValue.loadCoinValue(json); }

    
    @Override
    public MoneyValueParser getValueParser() { return CoinValueParser.INSTANCE; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Object> getInputHandlers(@Nullable Player player) {
        List<Object> results = new ArrayList<>();
        for(ChainData chain : CoinAPI.getApi().AllChainData())
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

    @Override
    public boolean allowItemInMoneySlot(Player player, ItemStack item) { return CoinAPI.getApi().IsCoin(item,true); }

    @Override
    public void addMoneySlotBackground(Consumer<Pair<ResourceLocation, ResourceLocation>> consumer, Consumer<ResourceLocation> lazyConsumer) { lazyConsumer.accept(CoinSlot.EMPTY_COIN_SLOT); }

}
