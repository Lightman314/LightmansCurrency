package io.github.lightman314.lightmanscurrency.integration.impactor.money;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.types.IPlayerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import net.impactdev.impactor.api.economy.currency.Currency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

public class ImpactorCurrencyType extends CurrencyType<ImpactorMoneyValue> {

    public static final ImpactorCurrencyType INSTANCE = new ImpactorCurrencyType();

    private ImpactorCurrencyType() {  }

    @Override
    protected MoneyValue sumValuesInternal(List<MoneyValue> values) {
        BigDecimal result = BigDecimal.ZERO;
        Currency currency = null;
        for(MoneyValue value :values)
        {
            if(value instanceof ImpactorMoneyValue iv)
            {
                if(currency == null)
                {
                    currency = iv.getImpactorCurrency();
                    result = result.add(iv.getValue());
                }
                else if(iv.getImpactorCurrency().key().equals(currency.key()))
                    result = result.add(iv.getValue());
            }
        }
        return ImpactorMoneyValue.of(currency,result);
    }

    @Nullable
    @Override
    public IPlayerMoneyHandler createMoneyHandlerForPlayer(Player player) { return new ImpactorPlayerMoneyProvider(player); }

    @Nullable
    public IMoneyHandler createMoneyHandlerForContainer(IItemHandler container, Consumer<ItemStack> overflowHandler, IClientTracker tracker) { return null; }

    @Override
    public MapCodec<ImpactorMoneyValue> moneyValueCodec() { return ImpactorMoneyValue.MAP_CODEC; }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf,ImpactorMoneyValue> moneyValueStreamCodec() { return ImpactorMoneyValue.STREAM_CODEC; }

    @Override
    public MoneyValue loadOldMoneyValue(CompoundTag valueTag) { return ImpactorMoneyValue.loadOldValue(valueTag); }

    @Override
    public MoneyValueParser getValueParser() { return ImpactorValueParser.INSTANCE; }

}
