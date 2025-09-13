package io.github.lightman314.lightmanscurrency.api.filter;

import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.IItemTradeFilter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FilterAPI {

    private static final List<Function<ItemStack,IItemTradeFilter>> filterProviders;

    static {
        filterProviders = new ArrayList<>();
        filterProviders.add(s -> {
            if(s.getItem() instanceof IItemTradeFilter filter)
                return filter;
            return null;
        });
    }

    public static void registerFilterProvider(Function<ItemStack,IItemTradeFilter> provider)
    {
        if(filterProviders.contains(provider))
            return;
        filterProviders.add(provider);
    }

    @Nullable
    public static IItemTradeFilter tryGetFilter(ItemStack stack)
    {
        for(var provider : filterProviders)
        {
            IItemTradeFilter result = provider.apply(stack);
            if(result != null)
                return result;
        }
        return null;
    }

}
