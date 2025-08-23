package io.github.lightman314.lightmanscurrency.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import net.minecraft.server.commands.GiveCommand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GiveCommand.class)
public class GiveCommandMixin {

    @WrapOperation(method = "giveItem",at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean giveItemWalletIntercept(Inventory instance, ItemStack stack, Operation<Boolean> original)
    {
        if(!LCConfig.COMMON.interceptGiveCommand.get())
            return original.call(instance,stack);
        Player player = instance.player;
        if(CoinAPI.API.IsAllowedInCoinContainer(stack,false))
        {
            WalletHandler walletHandler = WalletHandler.get(player);
            if(walletHandler == null)
                return original.call(instance,stack);
            stack = walletHandler.PickupCoins(stack);
            //Placed the entire item inside the players wallet, so the addition was a success
            if(stack.isEmpty())
                return true;
        }
        //Not a coin, or not all the items fit in the wallet. Return the base add method
        return original.call(instance,stack);
    }

}
