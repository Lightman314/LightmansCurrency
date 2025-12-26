package io.github.lightman314.lightmanscurrency.integration.impactor.money;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.types.IPlayerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.impactdev.impactor.api.economy.currency.Currency;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ImpactorCurrencyType extends CurrencyType {

    public static final ResourceLocation TYPE = VersionUtil.lcResource("impactor_compat");
    public static final CurrencyType INSTANCE = new ImpactorCurrencyType();

    private ImpactorCurrencyType() { super(TYPE); }

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
    @Override
    public IMoneyHandler createMoneyHandlerForContainer(Container container, Consumer<ItemStack> overflowHandler, IClientTracker tracker) { return null; }

    @Override
    public MoneyValue loadMoneyValue(CompoundTag valueTag) { return ImpactorMoneyValue.load(valueTag); }

    @Override
    public MoneyValue loadMoneyValueJson(JsonObject json) { return ImpactorMoneyValue.load(json); }

    @Override
    public MoneyValueParser getValueParser() { return ImpactorValueParser.INSTANCE; }

}
