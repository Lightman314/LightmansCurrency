package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.ownership.OwnerHolder;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public interface IBlockDropProvider {

    void addBlockDrops(Consumer<ItemStack> builder,OwnerHolder owner);

}