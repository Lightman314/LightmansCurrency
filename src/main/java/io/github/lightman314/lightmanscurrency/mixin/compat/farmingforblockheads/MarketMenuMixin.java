package io.github.lightman314.lightmanscurrency.mixin.compat.farmingforblockheads;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletCapability;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.blay09.mods.balm.api.container.DefaultContainer;
import net.blay09.mods.farmingforblockheads.menu.MarketMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(MarketMenu.class)
public abstract class MarketMenuMixin {

    @Accessor(value = "player",remap = false)
    protected abstract Player getPlayer();

    @Accessor(value = "marketInputBuffer",remap = false)
    protected abstract DefaultContainer getInputContainer();

    @Unique
    private boolean selectingEntry = false;

    @Unique
    private boolean hasCoinInInputSlot() { return CoinAPI.API.IsCoin(this.getInputContainer().getItem(0),false); }

    @Inject(at = @At("HEAD"),method = "removed")
    private void removed(Player player, CallbackInfo callback)
    {
        if(this.hasCoinInInputSlot())
            this.storeCoins(true);
    }

    @Inject(at = @At("HEAD"),method = "selectMarketEntry",remap = false)
    private void selectMarketEntryEarly(UUID entryID, boolean stack, CallbackInfo callback) { this.selectingEntry = true; }

    @Inject(at = @At("TAIL"),method = "selectMarketEntry",remap = false)
    private void selectMarketEntryLate(UUID entryID, boolean stack, CallbackInfo callback) { this.selectingEntry = false; }

    @Inject(at = @At("HEAD"), method = "quickMoveStack",cancellable = true)
    private void quickMoveStack(Player player, int slot, CallbackInfoReturnable<ItemStack> callback)
    {
        if(this.selectingEntry && slot == 0 && this.hasCoinInInputSlot())
        {
            this.storeCoins(false);
            callback.setReturnValue(ItemStack.EMPTY);
            callback.cancel();
        }
    }

    @Inject(at = @At("HEAD"),method = "quickMoveCost",cancellable = true,remap = false)
    private void quickMoveCost(ItemStack costItem, int desiredCount, CallbackInfo callback)
    {
        if(CoinAPI.API.IsCoin(costItem,false))
        {

            //Confirm that the input slot is empty
            DefaultContainer container = this.getInputContainer();
            if(!container.getItem(0).isEmpty())
                return;

            //Attempt to collect money from wallet
            MoneyView availableFunds = WalletCapability.getWalletMoney(this.getPlayer());

            ChainData chain = CoinAPI.API.ChainDataOfCoin(costItem);

            long value = chain.getCoreValue(costItem);

            int coinToAdd = 0;

            for(boolean keepLooping = true; keepLooping;)
            {
                int tempCoinToAdd = desiredCount > coinToAdd ? MathUtil.clamp(coinToAdd + 1, 0, desiredCount) : coinToAdd;

                if(!availableFunds.containsValue(CoinValue.fromNumber(chain.chain,value * tempCoinToAdd)))
                    keepLooping = false;
                else
                {
                    coinToAdd = tempCoinToAdd;
                    if(coinToAdd >= desiredCount)
                        keepLooping = false;
                }
            }

            if(coinToAdd > 0)
            {
                MoneyValue fundsToExtract = CoinValue.fromNumber(chain.chain, value * coinToAdd);

                ItemStack coin = costItem.copy();
                coin.setCount(coinToAdd);

                IMoneyHolder handler = MoneyAPI.API.GetPlayersMoneyHandler(this.getPlayer());

                if(handler.extractMoney(fundsToExtract,true).isEmpty())
                {
                    handler.extractMoney(fundsToExtract,false);
                    container.setItem(0,coin);
                    LightmansCurrency.LogDebug("Moved " + fundsToExtract.getString() + " worth of coins into the Market Menu!");
                    callback.cancel();
                }
            }

        }
    }

    @Unique
    private void storeCoins(boolean noUpdate)
    {
        DefaultContainer container = this.getInputContainer();
        ItemStack stack = noUpdate ? container.removeItemNoUpdate(0) : container.removeItem(0,Integer.MAX_VALUE);
        IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(this.getPlayer());
        if(walletHandler != null)
        {
            ItemStack wallet = walletHandler.getWallet();
            stack = WalletItem.PickupCoin(wallet, stack);
        }
        if(!stack.isEmpty())
            ItemHandlerHelper.giveItemToPlayer(this.getPlayer(),stack);
    }


}