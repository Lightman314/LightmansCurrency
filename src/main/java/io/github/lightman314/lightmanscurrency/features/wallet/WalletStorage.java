package io.github.lightman314.lightmanscurrency.features.wallet;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.resource.NormalItemStorage;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class WalletStorage extends NormalItemStorage {

    private final ItemAccess walletAccess;
    public WalletStorage(ItemAccess walletAccess) {
        super(WalletItem.getWalletSlots(walletAccess.getResource().toStack()));
        this.walletAccess = walletAccess;
    }

    @Override
    protected boolean isValid(int index, ItemStack stack) { return LCApi.getCoinAPI().isAllowedInCoinContainer(stack,true); }

    @Override
    protected void afterChangeBeforeCommit(TransactionContext transaction) {
        //Update the wallet stack with the transaction as context so that it can be rolled back if the transaction doesn't go through
        this.walletAccess.exchange(this.walletAccess.getResource().with(LCDataComponents.WALLET_CONTENTS,new WalletStorageData(this)),1,transaction);
    }

}
