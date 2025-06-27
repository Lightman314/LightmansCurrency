package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.LCConfig;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CoinItem extends Item {

    public CoinItem(Properties properties) { super(properties); }

    @Override
    public boolean isPiglinCurrency(ItemStack stack) { return LCConfig.COMMON.piglinsBarterCoins.get(); }

}