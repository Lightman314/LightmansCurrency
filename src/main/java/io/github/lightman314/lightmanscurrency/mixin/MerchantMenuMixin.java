package io.github.lightman314.lightmanscurrency.mixin;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin {

    @Unique
    protected MerchantMenu self() { return (MerchantMenu)(Object)this; }

    @Accessor("trader")
    public abstract Merchant getTrader();
    @Accessor("tradeContainer")
    public abstract MerchantContainer getTradeContainer();
    public Player getPlayer() { Merchant m = this.getTrader(); if(m != null) return m.getTradingPlayer(); return null; }

    @Inject(at = @At("HEAD"), method = "tryMoveItems")
    private void tryMoveItemsEarly(int trade, CallbackInfo info)
    {
        //Clear coin items into the wallet instead of their inventory
        try {
            MerchantMenu self = this.self();
            if(trade >= 0 && trade < self.getOffers().size())
                this.EjectMoneyIntoWallet(this.getPlayer(), false);
        } catch (Throwable ignored) {}
    }

    @Inject(at = @At("TAIL"), method = "tryMoveItems")
    private void tryMoveItems(int trade, CallbackInfo info)
    {
        try {
            MerchantMenu self = this.self();
            if(trade >= 0 && trade < self.getOffers().size())
            {
                MerchantContainer tradeContainer = this.getTradeContainer();
                if(tradeContainer.getItem(0).isEmpty() && tradeContainer.getItem(1).isEmpty())
                {
                    MerchantOffer offer = self.getOffers().get(trade);
                    if(MoneyUtil.isCoin(offer.getCostA(), false) && isCoinOrEmpty(offer.getCostB()))
                    {
                        ItemStack coinA = offer.getCostA();
                        ItemStack coinB = offer.getCostB();

                        CoinValue tradeValue = MoneyUtil.getCoinValue(ImmutableList.of(coinA, coinB));
                        LightmansCurrency.LogDebug("Coin Value of the selected trade is " + tradeValue.getString());
                        Player player = this.getPlayer();

                        CoinValue availableFunds = WalletCapability.getWalletMoney(player);

                        CoinValue fundsToExtract = CoinValue.EMPTY;
                        int coinACount = coinA.getCount();
                        int coinBCount = coinB.isEmpty() ? 0 : coinB.getCount();
                        int coinAMaxCount = coinA.getMaxStackSize();
                        int coinBMaxCount = coinB.isEmpty() ? 0 : coinB.getMaxStackSize();
                        int coinToAddA = 0;
                        int coinToAddB = 0;

                        for(boolean keepLooping = true; keepLooping;)
                        {
                            int tempC2AA = coinAMaxCount > coinToAddA ? MathUtil.clamp(coinToAddA + coinACount, 0, coinAMaxCount) : coinToAddA;
                            int tempC2AB = coinBMaxCount > coinToAddB ? MathUtil.clamp(coinToAddB + coinBCount, 0, coinBMaxCount) : coinToAddB;

                            coinA.setCount(tempC2AA);
                            coinB.setCount(tempC2AB);

                            CoinValue thisValue = MoneyUtil.getCoinValue(ImmutableList.of(coinA, coinB));
                            if(availableFunds.getValueNumber() < thisValue.getValueNumber())
                                keepLooping = false;
                            else
                            {
                                fundsToExtract = thisValue;
                                coinToAddA = tempC2AA;
                                coinToAddB = tempC2AB;
                                if(coinToAddA >= coinAMaxCount && coinToAddB >= coinBMaxCount)
                                    keepLooping = false;
                            }
                        }

                        if((coinToAddA > 0 || coinToAddB > 0) && fundsToExtract.hasAny())
                        {
                            coinA.setCount(coinToAddA);
                            coinB.setCount(coinToAddB);
                            if(MoneyUtil.ProcessPayment(null, player, fundsToExtract))
                            {
                                tradeContainer.setItem(0, coinA.copy());
                                tradeContainer.setItem(1, coinB.copy());
                                LightmansCurrency.LogDebug("Moved " + fundsToExtract.getString() + " worth of coins into the Merchant Menu!");
                            }
                        }
                    }
                }
            }
        } catch(Throwable ignored) {}
    }

    @Inject(at = @At("HEAD"), method = "removed")
    private void removed(Player player, CallbackInfo info) {
        if(this.isPlayerAliveAndValid(player))
            this.EjectMoneyIntoWallet(player, true);
    }

    protected boolean isPlayerAliveAndValid(Player player)
    {
        if(player.isAlive())
        {
            if(player instanceof ServerPlayer sp)
                return !sp.hasDisconnected();
            return true;
        }
        return false;
    }

    private void EjectMoneyIntoWallet(Player player, boolean noUpdate)
    {
        MerchantContainer tradeContainer = this.getTradeContainer();
        ItemStack item = tradeContainer.getItem(0);
        if (!item.isEmpty() && MoneyUtil.isCoin(item, false)) {
            MoneyUtil.ProcessChange(null, player, MoneyUtil.getCoinValue(item));
            if(noUpdate)
                tradeContainer.removeItemNoUpdate(0);
            else
                tradeContainer.setItem(0, ItemStack.EMPTY);
        }
        item = tradeContainer.getItem(1);
        if (!item.isEmpty() && MoneyUtil.isCoin(item, false)) {
            MoneyUtil.ProcessChange(null, player, MoneyUtil.getCoinValue(item));
            if(noUpdate)
                tradeContainer.removeItemNoUpdate(1);
            else
                tradeContainer.setItem(1, ItemStack.EMPTY);
        }
    }

    private static boolean isCoinOrEmpty(ItemStack item) { return MoneyUtil.isCoin(item, false) || item.isEmpty(); }

}
