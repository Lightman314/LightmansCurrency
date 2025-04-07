package io.github.lightman314.lightmanscurrency.api.money.coins;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Interface that can be applied to an item to make it appear as a coin in {@link CoinAPI#IsAllowedInCoinContainer(Item, boolean)}
 */
public interface ICoinLike {

    default boolean isCoin(ItemStack stack) { return true; }
    default boolean isFromSideChain(ItemStack stack) { return false; }

}
