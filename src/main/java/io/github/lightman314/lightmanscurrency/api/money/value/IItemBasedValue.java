package io.github.lightman314.lightmanscurrency.api.money.value;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface that can be applied to an {@link MoneyValue} to make it representable via items<br>
 */
public interface IItemBasedValue {

    /**
     * Returns the Money Value as a list of Item Stacks<br>
     * Item Stacks <b>do not</b> need to obey the items max stack size,
     * which is useful for display purposes
     * @see #getAsSeperatedItemList()
     */
    @Nonnull
    List<ItemStack> getAsItemList();

    /**
     * Returns the Money Value as a list of Item Stacks<br>
     * Item Stacks will obey the items max stack size,
     * which is useful if spawning the items into the world and/or placing them in a container
     * @see #getAsItemList()
     */
    @Nonnull
    default List<ItemStack> getAsSeperatedItemList()
    {
        List<ItemStack> result = new ArrayList<>();
        for(ItemStack stack : getAsItemList())
        {
            ItemStack s = stack.copy();
            while(s.getCount() > s.getMaxStackSize())
                result.add(s.split(s.getMaxStackSize()));
            if(!s.isEmpty())
                result.add(s);
        }
        return result;
    }

}
