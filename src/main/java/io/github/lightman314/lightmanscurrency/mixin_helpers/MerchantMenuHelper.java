package io.github.lightman314.lightmanscurrency.mixin_helpers;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.coins.value.CoinValue;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.core.neoforge.LCDataAttachments;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletAttachment;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletItem;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletStorage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;

/**
 * Helper class that contains methods used to move coins in the {@link io.github.lightman314.lightmanscurrency.mixin.MerchantMenuMixin},
 * so that they might be used in similar mixins for mods that contain custom merchant menus.
 */
public final class MerchantMenuHelper {
    private MerchantMenuHelper() {}

    public static void tryMoveItems(Player player,Container tradeContainer, MerchantOffer offer) {
        if(tradeContainer.getItem(0).isEmpty() || tradeContainer.getItem(1).isEmpty()) {
            if(LCApi.getCoinAPI().isCoin(offer.getCostA(),false) || LCApi.getCoinAPI().isCoin(offer.getCostB(),false)) {
                ItemStack coinA = offer.getCostA();
                ItemStack coinB = offer.getCostB();

                ChainData chainA = LCApi.getCoinAPI().lookupChain(coinA);
                ChainData chainB = LCApi.getCoinAPI().lookupChain(coinB);

                //Don't calculate values of non-empty slots to avoid overriding an existing item
                long valueA;
                if(chainA != null && tradeContainer.getItem(0).isEmpty())
                    valueA = chainA.getInternalValue(coinA);
                else
                    valueA = 0;
                long valueB;
                if(chainB != null && tradeContainer.getItem(1).isEmpty())
                    valueB = chainB.getInternalValue(coinB);
                else
                    valueB = 0;

                //If the only items that are coins are also the non-empty slots, stop now as there's nothing to do
                if(valueA == 0 && valueB == 0)
                    return;

                MoneyResourceHandler walletMoney = player.getData(LCDataAttachments.WALLET).getContentHandler(player);

                MoneyValue fundsToExtractA = MoneyValue.empty();
                MoneyValue fundsToExtractB = MoneyValue.empty();
                int coinACount = valueA > 0 ? coinA.getCount() : 0;
                int coinBCount = valueB > 0 ? coinB.getCount() : 0;
                int coinAMaxCount = valueA > 0 ? coinA.getMaxStackSize() : 0;
                int coinBMaxCount = valueB > 0 ? coinB.getMaxStackSize() : 0;
                int coinToAddA = 0;
                int coinToAddB = 0;

                boolean flag = true;
                while(flag) {
                    int tempC2AA = coinAMaxCount > coinToAddA ? Math.clamp(coinToAddA + coinACount,0,coinAMaxCount) : coinToAddA;
                    int tempC2AB = coinBMaxCount > coinToAddB ? Math.clamp(coinToAddB + coinBCount,0,coinBMaxCount) : coinToAddB;

                    coinA.setCount(tempC2AA);
                    coinB.setCount(tempC2AB);

                    //End the loop if we can't afford the next count
                    if(!containsMoneyFor(walletMoney,chainA,valueA,tempC2AA,chainB,valueB,tempC2AB))
                        flag = false;
                    else {
                        fundsToExtractA = quickCoinValue(chainA,valueA,tempC2AA);
                        fundsToExtractB = quickCoinValue(chainB,valueB,tempC2AB);
                        coinToAddA = tempC2AA;
                        coinToAddB = tempC2AB;
                        //End the loop if we hit the stack limit
                        if(coinToAddA >= coinAMaxCount && coinToAddB >= coinBMaxCount)
                            flag = false;
                    }
                }
                if((coinToAddA > 0 || coinToAddB > 0) && !(fundsToExtractA.isEmpty() && fundsToExtractB.isEmpty())) {
                    coinA.setCount(coinToAddA);
                    coinB.setCount(coinToAddB);

                    if(fundsToExtractA.compatibleTypes(fundsToExtractB)) {
                        fundsToExtractA = fundsToExtractA.addValue(fundsToExtractB);
                        fundsToExtractB = MoneyValue.empty();
                    }

                    try(Transaction transaction = Transaction.openRoot()) {
                        MoneyValue extractedA = walletMoney.extract(fundsToExtractA,transaction);
                        MoneyValue extractedB = walletMoney.extract(fundsToExtractB,transaction);
                        if(extractedA.equals(fundsToExtractA) && extractedB.equals(fundsToExtractB)) {
                            //Commit the transaction as the money we wanted was in-fact extracted
                            transaction.commit();
                            //Now place the coins in their appropriate slots
                            if(coinToAddA > 0)
                                tradeContainer.setItem(0,coinA.copy());
                            if(coinToAddB > 0)
                                tradeContainer.setItem(1,coinB.copy());
                        }
                    }
                }
            }
        }
    }

    public static boolean containsMoneyFor(MoneyResourceHandler moneyHandler,@Nullable ChainData chainA,long valueA,int countA,@Nullable ChainData chainB,long valueB,int countB) {
        MoneyValue cvA = quickCoinValue(chainA,valueA,countA);
        MoneyValue cvB = quickCoinValue(chainB,valueB,countB);
        if(cvA.compatibleTypes(cvB)) {
            cvA = cvA.addValue(cvB);
            cvB = MoneyValue.empty();
        }
        return moneyHandler.containsResource(cvA) && moneyHandler.containsResource(cvB);
    }

    @Unique
    public static MoneyValue quickCoinValue(@Nullable ChainData chain, long value, int count) {
        return chain == null ? MoneyValue.empty() : CoinValue.fromNumber(chain,value * count);
    }

    public static void ejectMoneyIntoWallet(Player player,Container tradeContainer,boolean noUpdate) {
        WalletAttachment attachment = player.getData(LCDataAttachments.WALLET);
        if(!WalletItem.isWallet(attachment.getWallet()))
            return;
        WalletStorage walletStorage = attachment.getWalletStorage();
        for(int i = 0; i < 2; ++i) {
            ItemStack item = tradeContainer.getItem(i).copy();
            if(!item.isEmpty() && LCApi.getCoinAPI().isCoin(item,false)) {
                try(Transaction transaction = Transaction.openRoot()) {
                    int inserted = walletStorage.pickup(item,transaction);
                    transaction.commit();
                    item.shrink(inserted);
                    //Insert leftovers into the players inventory
                    if(!item.isEmpty())
                        player.getInventory().placeItemBackInInventory(item);
                    if(noUpdate)
                        tradeContainer.removeItemNoUpdate(i);
                    else
                        tradeContainer.setItem(i,ItemStack.EMPTY);
                }
            }
        }
    }

    public static ItemStack tryPickupCoin(Player player,ItemStack item) {
        if(LCApi.getCoinAPI().isCoin(item,false)) {
            WalletStorage storage = player.getData(LCDataAttachments.WALLET).getWalletStorage();
            if(storage.hasWallet()) {
                try(Transaction tx = Transaction.openRoot()) {
                    int inserted = storage.pickup(item,tx);
                    if(inserted > 0 && inserted <= item.getCount()) {
                        tx.commit();
                        item.shrink(inserted);
                        if(item.isEmpty())
                            return ItemStack.EMPTY;
                    }
                }
            }
        }
        return item;
    }

    /**
     * Checks if the player is both alive and still connected.<br>
     * If false, items in the container should be dropped as originally handled
     * @deprecated Inject where the items are actually placed in the players inventory during removal instead
     * @param player The player to potentially return money to
     * @return Whether the player is both alive and still connected to the server. Also returns <code>false</code> if the player is a client-player.
     */
    @Deprecated
    public static boolean isAliveAndValid(Player player) {
        if(player.isAlive() && player instanceof ServerPlayer sp)
            return !sp.hasDisconnected();
        return false;
    }

}
