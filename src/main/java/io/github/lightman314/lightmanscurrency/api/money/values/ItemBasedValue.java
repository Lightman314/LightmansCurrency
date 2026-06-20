package io.github.lightman314.lightmanscurrency.api.money.values;

import io.github.lightman314.lightmanscurrency.api.money.values.interfaces.IItemBasedValue;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerHolder;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public abstract class ItemBasedValue extends MoneyValue implements IItemBasedValue {

    @Override
    public void spawnInWorld(Consumer<ItemStack> itemSpawner,OwnerHolder owner) {
        for(ItemStack item : this.getAsSeperatedItemList())
            itemSpawner.accept(item);
    }

}
