package io.github.lightman314.lightmanscurrency.api.money.resource;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.resource.TransactionCommitListener;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.resource.builtin.MoneyResourceHandlerCommitListenerWrapper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.BiConsumer;

public interface MoneyHoldingItemResourceHandler extends ResourceHandler<ItemResource> {

    default boolean shouldWrapCapabilities() { return true; }

    default MoneyResourceHandler getMoneyResourceHandler(Player player) { return this.getMoneyResourceHandler(MoneyAPI.playerOverflow(player),ISidedContext.wrap(player),this.shouldWrapCapabilities()); }
    default MoneyResourceHandler getMoneyResourceHandler(Player player,boolean wrapCapabilities) { return this.getMoneyResourceHandler(MoneyAPI.playerOverflow(player), ISidedContext.wrap(player),wrapCapabilities); }
    default MoneyResourceHandler getMoneyResourceHandler(BiConsumer<ItemStack,TransactionContext> overflowHandler,ISidedContext context) { return this.getMoneyResourceHandler(overflowHandler,context,this.shouldWrapCapabilities()); }
    default MoneyResourceHandler getMoneyResourceHandler(BiConsumer<ItemStack,TransactionContext> overflowHandler,ISidedContext context,boolean wrapCapabilities) {
        return new MoneyResourceHandlerCommitListenerWrapper(LCApi.getMoneyAPI().getContainersMoneyHandler(this,overflowHandler,context,wrapCapabilities),this::afterMoneyCommit);
    }

    default MoneyResourceHandler getMoneyResourceViewer(ISidedContext context) { return this.getMoneyResourceViewer(context,this.shouldWrapCapabilities()); }
    default MoneyResourceHandler getMoneyResourceViewer(ISidedContext context,boolean wrapCapabilities) {
        return LCApi.getMoneyAPI().getContainersMoneyViewer(this,context,wrapCapabilities);
    }

    default void afterMoneyCommit() {}

}
