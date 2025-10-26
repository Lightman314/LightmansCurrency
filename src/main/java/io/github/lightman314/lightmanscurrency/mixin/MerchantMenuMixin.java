package io.github.lightman314.lightmanscurrency.mixin;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import io.github.lightman314.lightmanscurrency.common.attachments.wallet.WalletHelpers;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenu;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin {

    @Unique
    private MerchantMenu lightmanscurrency$self() { return (MerchantMenu)(Object)this; }

    @Accessor("trader")
    public abstract Merchant getTrader();
    @Accessor("tradeContainer")
    public abstract MerchantContainer getTradeContainer();
    @Unique
    private Player lightmanscurrency$getPlayer() { Merchant m = this.getTrader(); if(m != null) return m.getTradingPlayer(); return null; }

    @Inject(at = @At("HEAD"), method = "tryMoveItems")
    private void tryMoveItemsEarly(int trade, CallbackInfo info)
    {
        //Clear coin items into the wallet instead of their inventory
        try {
            MerchantMenu self = this.lightmanscurrency$self();
            if(trade >= 0 && trade < self.getOffers().size())
                this.lightmanscurrency$EjectMoneyIntoWallet(this.lightmanscurrency$getPlayer(), false);
        } catch (Throwable ignored) {}
    }

    @Inject(at = @At("TAIL"), method = "tryMoveItems")
    private void tryMoveItems(int trade, CallbackInfo info)
    {
        try {
            MerchantMenu self = this.lightmanscurrency$self();
            if(trade >= 0 && trade < self.getOffers().size())
            {
                MerchantContainer tradeContainer = this.getTradeContainer();
                if(tradeContainer.getItem(0).isEmpty() || tradeContainer.getItem(1).isEmpty())
                {
                    MerchantOffer offer = self.getOffers().get(trade);
                    if(CoinAPI.getApi().IsCoin(offer.getCostA(), false) || CoinAPI.getApi().IsCoin(offer.getCostB(), false))
                    {

                        ItemStack coinA = offer.getCostA().copy();
                        ItemStack coinB = offer.getCostB().copy();

                        ChainData chainA = CoinAPI.getApi().ChainDataOfCoin(coinA);
                        ChainData chainB = CoinAPI.getApi().ChainDataOfCoin(coinB);

                        //Don't calculate values of non-empty slots to avoid overriding an existing item.
                        long valueA;
                        if(chainA != null && tradeContainer.getItem(0).isEmpty())
                            valueA = chainA.getCoreValue(coinA);
                        else valueA = 0;
                        //Don't calculate values of non-empty slots to avoid overriding an existing item.
                        long valueB;
                        if(chainB != null && tradeContainer.getItem(1).isEmpty())
                            valueB = chainB.getCoreValue(coinB);
                        else valueB = 0;

                        //LightmansCurrency.LogDebug("Coin Value of the selected trade is " + tradeValue.getString());
                        Player player = this.lightmanscurrency$getPlayer();

                        MoneyView availableFunds = WalletHelpers.getWalletMoney(player);

                        MoneyValue fundsToExtractA = MoneyValue.empty();
                        MoneyValue fundsToExtractB = MoneyValue.empty();
                        int coinACount = valueA > 0 ? coinA.getCount() : 0;
                        int coinBCount = valueB > 0 ? coinB.getCount() : 0;
                        int coinAMaxCount = valueA > 0 ? coinA.getMaxStackSize() : 0;
                        int coinBMaxCount = valueB > 0 ? coinB.getMaxStackSize() : 0;
                        int coinToAddA = 0;
                        int coinToAddB = 0;

                        for(boolean keepLooping = true; keepLooping;)
                        {
                            int tempC2AA = coinAMaxCount > coinToAddA ? MathUtil.clamp(coinToAddA + coinACount, 0, coinAMaxCount) : coinToAddA;
                            int tempC2AB = coinBMaxCount > coinToAddB ? MathUtil.clamp(coinToAddB + coinBCount, 0, coinBMaxCount) : coinToAddB;

                            coinA.setCount(tempC2AA);
                            coinB.setCount(tempC2AB);

                            if(!lightmanscurrency$containsValueFor(availableFunds, chainA, valueA, tempC2AA, chainB, valueB, tempC2AB))
                                keepLooping = false;
                            else
                            {
                                fundsToExtractA = CoinValue.fromNumber(chainA.chain, valueA * tempC2AA);
                                if(chainB != null)
                                    fundsToExtractB = CoinValue.fromNumber(chainB.chain, valueB * tempC2AB);
                                coinToAddA = tempC2AA;
                                coinToAddB = tempC2AB;
                                if(coinToAddA >= coinAMaxCount && coinToAddB >= coinBMaxCount)
                                    keepLooping = false;
                            }
                        }

                        if((coinToAddA > 0 || coinToAddB > 0) && !fundsToExtractA.isEmpty())
                        {
                            coinA.setCount(coinToAddA);
                            coinB.setCount(coinToAddB);

                            //Combine the funds if they're the same type so that the simulations will be accurate
                            if(fundsToExtractA.sameType(fundsToExtractB))
                            {
                                fundsToExtractA = fundsToExtractA.addValue(fundsToExtractB);
                                fundsToExtractB = MoneyValue.empty();
                            }

                            IMoneyHolder handler = MoneyAPI.getApi().GetPlayersMoneyHandler(player);
                            if(handler.extractMoney(fundsToExtractA,true).isEmpty() && handler.extractMoney(fundsToExtractB,true).isEmpty())
                            {
                                handler.extractMoney(fundsToExtractA,false);
                                handler.extractMoney(fundsToExtractB,false);
                                if(coinToAddA > 0)
                                    tradeContainer.setItem(0, coinA.copy());
                                if(coinToAddB > 0)
                                    tradeContainer.setItem(1, coinB.copy());
                                LightmansCurrency.LogDebug("Moved " + fundsToExtractA.getString() + " & " + fundsToExtractB.getString() + " worth of coins into the Merchant Menu!");
                            }
                        }
                    }
                }
            }
        } catch(Throwable ignored) {}
    }

    @Unique
    private static boolean lightmanscurrency$containsValueFor(@Nonnull MoneyView query, @Nonnull ChainData chainA, long valueA, int countA, @Nullable ChainData chainB, long valueB, int countB)
    {
        MoneyValue cvA = CoinValue.fromNumber(chainA.chain, valueA * countA);
        MoneyValue cvB = chainB == null ? MoneyValue.empty() : CoinValue.fromNumber(chainB.chain, valueB * countB);
        if(cvA.sameType(cvB))
        {
            cvA = cvA.addValue(cvB);
            cvB = MoneyValue.empty();
        }
        return query.containsValue(cvA) && query.containsValue(cvB);
    }

    @Inject(at = @At("HEAD"), method = "removed")
    private void removed(Player player, CallbackInfo info) {
        if(this.lightmanscurrency$isPlayerAliveAndValid(player))
            this.lightmanscurrency$EjectMoneyIntoWallet(player, true);
    }

    @Unique
    private boolean lightmanscurrency$isPlayerAliveAndValid(Player player)
    {
        if(player.isAlive())
        {
            if(player instanceof ServerPlayer sp)
                return !sp.hasDisconnected();
            return true;
        }
        return false;
    }

    @Unique
    private void lightmanscurrency$EjectMoneyIntoWallet(Player player, boolean noUpdate)
    {
        MerchantContainer tradeContainer = this.getTradeContainer();
        ItemStack item = tradeContainer.getItem(0);
        if (!item.isEmpty() && CoinAPI.getApi().IsCoin(item, false)) {
            WalletHandler walletHandler = WalletHandler.get(player);
            if(walletHandler != null)
            {
                ItemStack wallet = walletHandler.getWallet();
                if(WalletItem.isWallet(wallet))
                {
                    ItemStack leftovers = WalletItem.PickupCoin(wallet, item);
                    //Shouldn't be needed as the player *should* be in the MerchantMenu at this point, but I'm leaving it here just to be safe.
                    WalletMenu.OnWalletUpdated(player);
                    if(!leftovers.isEmpty())
                        ItemHandlerHelper.giveItemToPlayer(player, leftovers);
                    if(noUpdate)
                        tradeContainer.removeItemNoUpdate(0);
                    else
                        tradeContainer.setItem(0, ItemStack.EMPTY);
                }
            }
        }
        item = tradeContainer.getItem(1);
        if (!item.isEmpty() && CoinAPI.getApi().IsCoin(item, false)) {
            WalletHandler walletHandler = WalletHandler.get(player);
            if(walletHandler != null)
            {
                ItemStack wallet = walletHandler.getWallet();
                if(WalletItem.isWallet(wallet))
                {
                    ItemStack leftovers = WalletItem.PickupCoin(wallet, item);
                    WalletMenu.OnWalletUpdated(player);
                    if(!leftovers.isEmpty())
                        ItemHandlerHelper.giveItemToPlayer(player, leftovers);
                    if(noUpdate)
                        tradeContainer.removeItemNoUpdate(1);
                    else
                        tradeContainer.setItem(1, ItemStack.EMPTY);
                }
            }
        }
    }

}
