package io.github.lightman314.lightmanscurrency.api.money.capability.implementations;

import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyViewer;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import net.minecraft.world.Container;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

/**
 * A safe way to make view-only {@link IMoneyViewer} instances for anything that also implements {@link IMoneyHandler}<br>
 * Since it's wrapped, nobody can cast this to a Money Handler and attempt to make illegal alterations.<br>
 * Primarily intended for viewing money within a container without the need to implement an item overflow condition
 */
public class MoneyViewWrapper implements IMoneyViewer {

    private final IMoneyHandler handler;
    public MoneyViewWrapper(IMoneyHandler handler) { this.handler = handler; }

    public static IMoneyViewer forContainer(Container container,IClientTracker tracker) { return forInventory(new InvWrapper(container),tracker); }
    public static IMoneyViewer forInventory(IItemHandler itemHandler,IClientTracker tracker) {
        IMoneyHandler handler = MoneyAPI.getApi().GetContainersMoneyHandler(itemHandler,i -> {},tracker);
        return new MoneyViewWrapper(handler);
    }

    @Override
    public MoneyView getStoredMoney() { return this.handler.getStoredMoney(); }
}
