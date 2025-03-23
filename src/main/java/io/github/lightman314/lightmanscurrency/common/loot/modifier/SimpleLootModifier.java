package io.github.lightman314.lightmanscurrency.common.loot.modifier;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class SimpleLootModifier implements ILootModifier {

    @Override
    public final boolean tryModifyLoot(@Nonnull RandomSource random, @Nonnull List<ItemStack> loot) {
        if(this.isEnabled())
            this.replaceLoot(random, loot);
        return false;
    }

    protected void replaceRandomItems(@Nonnull RandomSource random, @Nonnull List<ItemStack> loot, @Nonnull Item toReplace, @Nonnull ItemLike replacement) {
        replaceRandomItems(random, loot, this.getSuccessChance(), toReplace, replacement);
    }

    public static void replaceRandomItems(@Nonnull RandomSource random, @Nonnull List<ItemStack> loot, double chance, @Nonnull Item toReplace, @Nonnull ItemLike replacement)
    {
        List<ItemStack> toAdd = new ArrayList<>();
        for(int i = 0; i < loot.size(); ++i)
        {
            ItemStack stack = loot.get(i);
            if(!stack.isEmpty() && stack.getItem() == toReplace)
            {
                int replaceCount = randomCount(random, chance, stack.getCount());
                if(replaceCount > 0)
                {
                    if(replaceCount >= stack.getCount())
                    {
                        loot.remove(i);
                        i--;
                    }
                    else
                    {
                        ItemStack split = stack.split(replaceCount);
                        toAdd.add(ILootModifier.replaceItem(split, replacement));
                    }
                }
            }
        }
        loot.addAll(toAdd);
    }

    public static boolean randomCheck(@Nonnull RandomSource random, double chance) { return random.nextDouble() < chance; }

    public static int randomCount(@Nonnull RandomSource random, double chance, int attempts)
    {
        int result = 0;
        for(int i = 0; i < attempts; ++i)
        {
            if(randomCheck(random, chance))
                result++;
        }
        return result;
    }

    protected abstract void replaceLoot(@Nonnull RandomSource random, @Nonnull List<ItemStack> loot);

    public abstract boolean isEnabled();
    protected abstract double getSuccessChance();

}
