package io.github.lightman314.lightmanscurrency.api.coins.money;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.money.OverflowHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.player.PlayerMoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValueHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nullable;
import java.util.function.UnaryOperator;

public class CoinValueHelper extends MoneyValueHelper {

    public static final CoinValueHelper INSTANCE = new CoinValueHelper();

    protected CoinValueHelper() {}

    @Override
    @Nullable
    public PlayerMoneyResourceHandler createMoneyHandlerForPlayer(Player player,OverflowHandler.Modifier overflowModifier) { return new PlayerWalletHandler(player,overflowModifier); }
    @Override
    @Nullable
    public MoneyResourceHandler wrapContainer(ResourceHandler<ItemResource> itemResource,OverflowHandler overflowHandler, ISidedContext context) { return new CoinContainerWrapper(itemResource,overflowHandler); }
    @Override
    public boolean allowInMoneySlot(ItemStack stack) { return LCApi.getCoinAPI().isCoin(stack,true); }

}
