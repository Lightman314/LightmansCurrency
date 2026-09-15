package io.github.lightman314.lightmanscurrency.features.coin_mint;

import io.github.lightman314.lightmanscurrency.api.helpers.resource.NormalItemStorage;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class CoinMintStorage extends NormalItemStorage {

    public final ExternalAccess externalAccess = new ExternalAccess(this);
    public CoinMintStorage() { super(2); }

    public static final class ExternalAccess extends DelegatingResourceHandler<ItemResource> {
        private ExternalAccess(CoinMintStorage storage) { super(storage); }

        @Override
        public int insert(ItemResource resource, int amount, TransactionContext transaction) { return this.insert(0,resource, amount, transaction); }
        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if(index != 0)
                return 0;
            return super.insert(index, resource, amount, transaction);
        }

        @Override
        public int extract(ItemResource resource, int amount, TransactionContext transaction) { return this.extract(1,resource, amount, transaction); }
        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if(index != 1)
                return 0;
            return super.extract(index,resource,amount,transaction);
        }
    }

}
