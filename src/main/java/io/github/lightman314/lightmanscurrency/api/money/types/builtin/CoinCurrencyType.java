package io.github.lightman314.lightmanscurrency.api.money.types.builtin;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.types.IPlayerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.coins.CoinContainerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.coins.PlayerWalletMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValueParser;
import io.github.lightman314.lightmanscurrency.api.misc.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class CoinCurrencyType extends CurrencyType<CoinValue> {

    public static final CoinCurrencyType INSTANCE = new CoinCurrencyType();

    protected CoinCurrencyType() {  }

    public static String getUniqueName(ChainData chain) { return getUniqueName(chain.chain); }
    public static String getUniqueName(String chain) { return MoneyValue.generateCustomUniqueName(INSTANCE,chain); }

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
    public IPlayerMoneyHandler createMoneyHandlerForPlayer(Player player) { return new PlayerWalletMoneyHandler(player,false); }
    @Nullable
    @Override
    public IPlayerMoneyHandler createUnsafeMoneyHandlerForPlayer(Player player) { return new PlayerWalletMoneyHandler(player,true); }

    @Nullable
    @Override
    public IMoneyHandler createMoneyHandlerForContainer(IItemHandler container, Consumer<ItemStack> overflowHandler, IClientTracker tracker) { return new CoinContainerMoneyHandler(container, overflowHandler); }

    @Override
    public MoneyValue loadOldMoneyValue(CompoundTag valueTag) { return CoinValue.loadOldCoinValue(valueTag); }

    @Override
    public MoneyValueParser getValueParser() { return CoinValueParser.INSTANCE; }

    @Override
    public boolean allowItemInMoneySlot(Player player, ItemStack item) { return CoinAPI.getApi().IsCoin(item,true); }

    @Override
    public void addMoneySlotBackground(Consumer<Pair<ResourceLocation, ResourceLocation>> consumer, Consumer<ResourceLocation> lazyConsumer) { lazyConsumer.accept(CoinSlot.EMPTY_COIN_SLOT); }

    @Override
    public MapCodec<CoinValue> moneyValueCodec() { return CoinValue.MAP_CODEC; }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf,CoinValue> moneyValueStreamCodec() { return CoinValue.STREAM_CODEC; }

}
