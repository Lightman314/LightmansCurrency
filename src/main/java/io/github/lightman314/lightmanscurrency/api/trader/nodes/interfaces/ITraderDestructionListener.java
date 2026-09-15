package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.ownership.interfaces.IOwnerHolder;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Consumer;

public interface ITraderDestructionListener {

    void onTraderDestroyed(IOwnerHolder owner, Consumer<ItemStack> itemSpawner, Optional<MoneyResourceHandler> playerMoney);

}
