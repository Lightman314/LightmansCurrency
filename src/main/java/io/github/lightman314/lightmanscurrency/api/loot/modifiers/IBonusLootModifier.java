package io.github.lightman314.lightmanscurrency.api.loot.modifiers;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.function.Predicate;

public interface IBonusLootModifier {

    boolean tryModifyLoot(RandomSource random,List<ItemStack> loot);

    static void replaceItems(List<ItemStack> loot,Item toReplace,ItemLike replacement) { replaceItems(loot,s -> s.is(toReplace),replacement); }
    static void replaceItems(List<ItemStack> loot, Predicate<ItemStack> filter, ItemLike replacement) {
        loot.replaceAll(stack -> {
            if(filter.test(stack))
                return stack.transmuteCopy(replacement);
            return stack;
        });
    }

}