package io.github.lightman314.lightmanscurrency.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletCapability;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemHandlerHelper.class)
public class ItemHandlerHelperMixin {

    @WrapMethod(method = "giveItemToPlayer(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V",remap = false)
    private static void giveItemToPlayerIntercept(Player player, ItemStack item, Operation<Void> original)
    {
        if(!LCConfig.COMMON.interceptInventoryHelper.get())
        {
            original.call(player,item);
            return;
        }
        if(CoinAPI.API.IsAllowedInCoinContainer(item,false))
        {
            IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(player);
            if(walletHandler == null)
            {
                original.call(player,item);
                return;
            }
            ItemStack wallet = walletHandler.getWallet();
            if(WalletItem.isWallet(wallet))
                item = WalletItem.PickupCoin(wallet,item);
        }
        original.call(player,item);
    }

}