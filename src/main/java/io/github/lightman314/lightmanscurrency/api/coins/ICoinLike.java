package io.github.lightman314.lightmanscurrency.api.coins;

import net.minecraft.world.item.ItemInstance;

public interface ICoinLike {

    default boolean isCoin(ItemInstance instance) { return true; }
    default boolean isFromSideChain(ItemInstance instance) { return false; }

}
