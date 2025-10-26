package io.github.lightman314.lightmanscurrency.integration.create.filter;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.filter.IItemTradeFilter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;

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
            if(stack.getItem() == AllItems.FILTER.get())
            {
                //Check if contents are empty
                ItemStackHandler contents = FilterItem.getFilterItems(stack);
                boolean empty = true;
                for(int i = 0; i < contents.getSlots() && empty; ++i)
                {
                    if(!contents.getStackInSlot(i).isEmpty())
                        empty = false;
                }
                if(empty)
                    return null;
            }
            else if(stack.getItem() == AllItems.ATTRIBUTE_FILTER.get())
            {
                //Check if no attributes are present
                List<ItemAttribute.ItemAttributeEntry> entries = stack.getOrDefault(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES,new ArrayList<>());
                if(entries.isEmpty())
                    return null;
            }
            else //Unsupported third filter type (package filter or they added a new one)
                return null;
            FilterItemStack filter = FilterItemStack.of(stack);
            return s -> filter.test(level,s);
        }
        return null;
    }

    @Nullable
    @Override
    public List<Component> getCustomTooltip(ItemStack stack) {
        if(stack.getItem() instanceof FilterItem filter)
        {
            List<Component> result = new ArrayList<>();
            //Filter Items appendHoverText method only adds the summary apparently, which works out well for me :)
            filter.appendHoverText(stack, Item.TooltipContext.EMPTY,result,TooltipFlag.NORMAL);
            //Remove the first line as they put a blank space there for some odd reason
            if(!result.isEmpty())
                result.removeFirst();
            return result;
        }
        return null;
    }

}
