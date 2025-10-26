package io.github.lightman314.lightmanscurrency.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemHandlerHelper.class)
public class ItemHandlerHelperMixin {

    @WrapMethod(method = "giveItemToPlayer(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V")
    private static void giveItemToPlayerIntercept(Player player, ItemStack item, Operation<Void> original)
    {
        if(!LCConfig.COMMON.interceptInventoryHelper.get())
        {
            original.call(player,item);
            return;
        }
        if(CoinAPI.getApi().IsAllowedInCoinContainer(item,false))
        {
            WalletHandler walletHandler = WalletHandler.get(player);
            if(walletHandler == null)
            {
                original.call(player,item);
                return;
            }
            item = walletHandler.PickupCoins(item);
        }
        original.call(player,item);
    }

}
