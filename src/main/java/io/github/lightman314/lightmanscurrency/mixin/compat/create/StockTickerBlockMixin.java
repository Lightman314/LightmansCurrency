package io.github.lightman314.lightmanscurrency.mixin.compat.create;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlock;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.foundation.item.SmartInventory;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletCapability;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//Leave refmap=true on this one as well, since we're injecting into a vanilla method
@Mixin(value = StockTickerBlock.class)
public class StockTickerBlockMixin {

    @Unique
    private StockTickerBlock lightmanscurrency$self() { return (StockTickerBlock)(Object)this;}

    @Inject(at = @At("HEAD"),method = "use",cancellable = true)
    private void useItemOn(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir)
    {
        if(player == null)
            return;
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof LogisticallyLinkedBlockItem)
            return;
        StockTickerBlock self = this.lightmanscurrency$self();
        if(level.getBlockEntity(pos) instanceof StockTickerBlockEntity be)
        {
            if(!be.behaviour.mayInteract(player))
                return;
            if(!level.isClientSide && be instanceof StockTickerBlockEntityAccessor accessor && !accessor.getReceivedPayments().isEmpty())
            {
                IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(player);
                if(walletHandler == null)
                    return;
                SmartInventory payments = accessor.getReceivedPayments();
                for (int i = 0; i < payments.getSlots(); i++)
                {
                    ItemStack item = payments.getItem(i);
                    if(CoinAPI.API.IsCoin(payments.getItem(i),false))
                    {
                        ItemStack wallet = walletHandler.getWallet();
                        ItemStack leftovers = WalletItem.PickupCoin(wallet,item);
                        int removedAmount = item.getCount() - leftovers.getCount();
                        if(removedAmount == item.getCount())
                            payments.setItem(i,ItemStack.EMPTY);
                        else
                            payments.setItem(i,item.split(removedAmount));
                    }
                }
                //If we emptied all payment items into the wallet, play the item pickup sounds and cancel the original interaction
                if(payments.isEmpty())
                {
                    AllSoundEvents.playItemPickup(player);
                    cir.setReturnValue(InteractionResult.SUCCESS);
                }
            }
        }
    }


}