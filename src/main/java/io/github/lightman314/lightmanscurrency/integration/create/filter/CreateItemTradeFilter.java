package io.github.lightman314.lightmanscurrency.integration.create.filter;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.filter.IItemTradeFilter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CreateItemTradeFilter implements IItemTradeFilter {

    public static final IItemTradeFilter INSTANCE = new CreateItemTradeFilter();

    private CreateItemTradeFilter() {}

    @Nullable
    @Override
    public Predicate<ItemStack> getFilter(ItemStack stack) {
        if(stack.getItem() instanceof FilterItem)
        {
            Level level = LightmansCurrency.getProxy().safeGetDummyLevel();
            if(level == null) //Create filters use the level apparently, so we can't provide a filter without a level
                return null;
            //Confirm that the item has a valid filter defined
            if(stack.getItem() == AllItems.FILTER.get() || stack.getItem() == AllItems.ATTRIBUTE_FILTER.get())
            {
                if(!hasCustomFilterStack(stack))
                    return null;
            }
            FilterItemStack filter = FilterItemStack.of(stack);
            return s -> filter.test(level,s);
        }
        return null;
    }

    private static boolean hasCustomFilterStack(ItemStack stack)
    {
        FilterItemStack filter = FilterItemStack.of(stack);
        return filter.getClass() != FilterItemStack.class;
    }

    @Nullable
    @Override
    public List<Component> getCustomTooltip(ItemStack stack) {
        if(stack.getItem() instanceof FilterItem filter)
        {
            List<Component> result = new ArrayList<>();
            //Filter Items appendHoverText method only adds the summary apparently, which works out well for me :)
            filter.appendHoverText(stack, null,result,TooltipFlag.NORMAL);
            //Remove the first line as they put a blank space there for some odd reason
            if(!result.isEmpty())
                result.remove(0);
            return result;
        }
        return null;
    }

}