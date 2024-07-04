package io.github.lightman314.lightmanscurrency.common.loot.modifier;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import java.util.List;

public interface ILootModifier {

    boolean tryModifyLoot(@Nonnull RandomSource random, @Nonnull List<ItemStack> loot);

    @Nonnull
    static ItemStack replaceItem(@Nonnull ItemStack stack, @Nonnull ItemLike replacement)
    {
        return stack.transmuteCopy(replacement);
    }

    static void replaceItems(@Nonnull List<ItemStack> loot, @Nonnull Item toReplace, @Nonnull ItemLike replacement)
    {
        for(int i = 0; i < loot.size(); ++i)
        {
            ItemStack stack = loot.get(i);
            if(stack.getItem() == toReplace)
                loot.set(i,replaceItem(stack, replacement));
        }
    }

}
