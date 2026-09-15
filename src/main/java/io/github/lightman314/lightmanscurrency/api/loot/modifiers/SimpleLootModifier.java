package io.github.lightman314.lightmanscurrency.api.loot.modifiers;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

public abstract class SimpleLootModifier implements IBonusLootModifier {

    @Override
    public final boolean tryModifyLoot(RandomSource random, List<ItemStack> loot) {
        if(this.isEnabled())
            this.replaceLoot(random,loot);
        return false;
    }

    protected void replaceRandomItems(RandomSource random,List<ItemStack> loot,Item toReplace,ItemLike replacement) { replaceRandomItems(random,loot,this.getSuccessChance(),toReplace,replacement); }

    public static void replaceRandomItems(RandomSource random,List<ItemStack> loot,double chance,Item toReplace,ItemLike replacement) {
        List<ItemStack> toAdd = new ArrayList<>();
        for(int i = 0; i < loot.size(); ++i) {
            ItemStack stack = loot.get(i);
            if(!stack.isEmpty() && stack.is(toReplace)) {
                int replacementCount = randomCount(random,chance,stack.getCount());
                if(replacementCount > 0) {
                    if(replacementCount >= stack.getCount())
                        loot.remove(i--);
                    else {
                        ItemStack split = stack.split(replacementCount);
                        toAdd.add(split.transmuteCopy(replacement));
                    }
                }
            }
        }
        loot.addAll(toAdd);
    }

    public static boolean randomCheck(RandomSource random,double chance) { return random.nextDouble() < chance; }

    public static int randomCount(RandomSource random,double chance,int attempts) {
        int result = 0;
        while(attempts-- > 0) {
            if(randomCheck(random,chance))
                result++;
        }
        return result;
    }

    protected abstract void replaceLoot(RandomSource random,List<ItemStack> loot);

    public abstract boolean isEnabled();
    protected abstract double getSuccessChance();


}
