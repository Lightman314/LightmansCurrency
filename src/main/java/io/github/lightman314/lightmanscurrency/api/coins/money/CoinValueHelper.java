package io.github.lightman314.lightmanscurrency.api.coins.money;

import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.money.resource.player.PlayerMoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValueHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

public class CoinValueHelper extends MoneyValueHelper {

    public static final CoinValueHelper INSTANCE = new CoinValueHelper();

    protected CoinValueHelper() {}

    @Override
    @Nullable
    public PlayerMoneyResourceHandler createMoneyHandlerForPlayer(Player player,boolean allowOverflow) { return new PlayerWalletHandler(player,allowOverflow); }
    @Override
    @Nullable
    public MoneyResourceHandler wrapContainer(ResourceHandler<ItemResource> itemResource,BiConsumer<ItemStack,TransactionContext> overflowHandler, ISidedContext context) { return new CoinContainerWrapper(itemResource,overflowHandler); }

}
