package io.github.lightman314.lightmanscurrency.api.money;

import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.resource.TransactionCommitListener;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface MoneyAPI {

    /**
     * Assembles a {@link MoneyResourceHandler} that can be used to directly give or take money from the player<br>
     * Default implementations will take money from the players equipped wallet, etc.
     * but custom money types that don't involve physical items can have said money stored in something akin to a
     * bank account while still allowing direct payments through this handler.
     * @param player The player whom we wish to query, give, or take money from
     * @return A Money Resource Handler through which monetary interactions can be taken.
     */
    default MoneyResourceHandler getPlayersMoneyHandler(Player player) { return this.getPlayersMoneyHandler(player,true); }
    /**
     * Assembles a {@link MoneyResourceHandler} that can be used to directly give or take money from the player<br>
     * Default implementations will take money from the players equipped wallet, etc.
     * but custom money types that don't involve physical items can have said money stored in something akin to a
     * bank account while still allowing direct payments through this handler.
     * @param player The player whom we wish to query, give, or take money from
     * @param allowOverflow Whether the players inventory is a valid item overflow location. If {@code false} item overflow will be left to be output elsewhere if needed.
     * @return A Money Resource Handler through which monetary interactions can be taken.
     */
    MoneyResourceHandler getPlayersMoneyHandler(Player player,boolean allowOverflow);

    /**
     * Assembles a {@link MoneyResourceHandler} that can be used to insert or extract money from the given Container<br>
     * May not support interactions with Money Types that don't involve items, however by default it will allow interactions with any items that have a Money Capability.<br>
     * This version uses the Player's Inventory as an item overflow handler.
     * @param container The vanilla container from which to insert or take money from.
     * @param player The Player for context. Used to obtain both a {@link Consumer overflow handler}, and the {@link ISidedContext logical side}.
     * @return A Money Resource Handler through which you can interact with the container in a monetary fashion.
     */
    default MoneyResourceHandler getContainersMoneyHandler(Container container,Player player) { return this.getContainersMoneyHandler(VanillaContainerWrapper.of(container), TransactionCommitListener.wrapAction(stack -> player.getInventory().placeItemBackInInventory(stack)),ISidedContext.wrap(player)); }
    /**
     * Assembles a {@link MoneyResourceHandler} that can be used to insert or extract money from the given Container<br>
     * May not support interactions with Money Types that don't involve items, however by default it will allow interactions with any items that have a Money Capability.
     * @param container The vanilla container from which to insert or take money from.
     * @param overflowHandler A consumer
     * @return A Money Resource Handler through which you can interact with the container in a monetary fashion.
     */
    default MoneyResourceHandler getContainersMoneyHandler(Container container, BiConsumer<ItemStack,TransactionContext> overflowHandler, ISidedContext context) { return this.getContainersMoneyHandler(VanillaContainerWrapper.of(container),overflowHandler,context); }
    default MoneyResourceHandler getContainersMoneyHandler(ResourceHandler<ItemResource> itemResource,Player player) { return this.getContainersMoneyHandler(itemResource,(stack,tx) -> player.getInventory().placeItemBackInInventory(stack),ISidedContext.wrap(player)); }
    MoneyResourceHandler getContainersMoneyHandler(ResourceHandler<ItemResource> itemResource,BiConsumer<ItemStack,TransactionContext> overflowHandler,ISidedContext context);

    default MoneyResourceHandler getContainersMoneyViewer(Container container,ISidedContext context) { return this.getContainersMoneyViewer(VanillaContainerWrapper.of(container),context); }
    default MoneyResourceHandler getContainersMoneyViewer(ResourceHandler<ItemResource> itemResource,ISidedContext context) { return MoneyResourceHandler.viewOnly(this.getContainersMoneyHandler(itemResource,(s,t) -> {},context)); }

}
