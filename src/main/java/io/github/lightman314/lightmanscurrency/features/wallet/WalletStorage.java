package io.github.lightman314.lightmanscurrency.features.wallet;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.data.ItemContents;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyHoldingItemResourceHandler;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class WalletStorage extends SnapshotJournal<List<ItemStack>> implements ResourceHandler<ItemResource>, IndexModifier<ItemResource>, MoneyHoldingItemResourceHandler {

    private final ItemAccess walletAccess;
    private ItemContents lastKnownData;
    private NonNullList<ItemStack> lastKnownContents = null;
    boolean hasWallet() { return WalletItem.isWallet(this.walletAccess.getResource()); }
    NonNullList<ItemStack> getContents() {
        ItemResource currentWallet = this.walletAccess.getResource();
        ItemContents currentData = currentWallet.getOrDefault(LCDataComponents.WALLET_CONTENTS,ItemContents.EMPTY);
        if(this.lastKnownContents == null || !Objects.equals(this.lastKnownData,currentData))
        {
            //If the contents have not yet been loaded from the wallet,
            // or the wallet we have access to has different items than we last knew of
            // reload the contents from the wallet stack
            this.lastKnownData = currentData;
            if(!WalletItem.isWallet(currentWallet))
                this.lastKnownContents = NonNullList.of(ItemStack.EMPTY);
            else
            {
                int size = WalletItem.getWalletSlots(currentWallet);
                this.lastKnownContents = NonNullList.withSize(size,ItemStack.EMPTY);
                List<ItemStack> overflow = new ArrayList<>();
                List<ItemStack> temp = currentData.asItems(size,overflow);
                for(int i = 0; i < temp.size() && i < this.lastKnownData.size(); ++i)
                    this.lastKnownContents.set(i,temp.get(i));
                for(ItemStack over : overflow)
                    this.forceAdd(over);
            }
        }
        return this.lastKnownContents;
    }
    boolean shouldAutoExchange() { return WalletItem.shouldAutoExchange(this.walletAccess.getResource().toStack()); }
    boolean setChanged(@Nullable TransactionContext transaction) {
        ItemContents newData = ItemContents.fromItems(this.lastKnownContents);
        ItemResource newWallet = this.walletAccess.getResource().with(LCDataComponents.WALLET_CONTENTS,newData);
        if(this.walletAccess.exchange(newWallet,1,transaction) == 1)
        {
            this.lastKnownData = newData;
            return true;
        }
        return false;
    }
    ItemStack getItem(int slot) {
        NonNullList<ItemStack> contents = this.getContents();
        if(slot < 0 || slot >= contents.size())
            return ItemStack.EMPTY;
        return contents.get(slot);
    }
    boolean setItem(int slot,ItemStack stack,@Nullable TransactionContext transaction) {
        List<ItemStack> contents = this.getContents();
        if(slot < 0 || slot >= contents.size())
            return false;
        if(stack.isEmpty())
            stack = ItemStack.EMPTY;
        ItemStack oldStack = contents.set(slot,stack.copy());
        boolean success = this.setChanged(transaction);
        if(!success) //If we failed to update the wallets data, then revert stack back to its old state
            contents.set(slot,oldStack);
        return success;
    }
    public WalletStorage(ItemAccess walletAccess) {
        this.walletAccess = walletAccess;
        this.lastKnownData = this.walletAccess.getResource().getOrDefault(LCDataComponents.WALLET_CONTENTS,ItemContents.EMPTY);
    }

    private void forceAdd(ItemStack item) {
        for(int i = 0; i < this.lastKnownContents.size(); ++i)
        {
            ItemStack stack = this.lastKnownContents.get(i);
            if(stack.isEmpty())
            {
                int fittable = Math.min(item.getCount(),item.getMaxStackSize());
                this.lastKnownContents.add(item.split(fittable));
                if(item.isEmpty())
                    return;
            }
            else if(ItemStack.isSameItemSameComponents(stack,item)) {
                int fittable = Math.min(item.getCount(),item.getMaxStackSize()) - stack.getCount();
                if(fittable > 0)
                {
                    stack.grow(fittable);
                    item.shrink(fittable);
                    if(item.isEmpty())
                        return;
                }
            }
        }
    }

    @Override
    public int size() { return this.getContents().size(); }

    @Override
    public ItemResource getResource(int index) { return ItemResource.of(this.getContents().get(index)); }

    @Override
    public long getAmountAsLong(int index) { return this.getContents().get(index).getCount(); }

    @Override
    public long getCapacityAsLong(int index,ItemResource resource) { return Math.min(64,resource.getMaxStackSize()); }

    @Override
    public boolean isValid(int index, ItemResource resource) { return WalletItem.isWallet(this.walletAccess.getResource()) && LCApi.getCoinAPI().isAllowedInCoinContainer(resource.toStack(),true); }

    /**
     * Wallet-exclusive version of the {@link ResourceHandler#insert(Resource, int, TransactionContext)} method,
     * however this also triggers the coin exchange automatically on a sucessful insertion <i>if</i> the wallet
     * in question should in-fact automatically exchange coins on pickup.
     * @param resource – The resource to insert. Must be non-empty.
     * @param amount – The maximum amount of the resource to insert. Must be non-negative.
     * @param transaction – The transaction that this operation is part of.
     * @return The amount that was inserted. Between 0 (inclusive, nothing was inserted) and amount (inclusive, everything was inserted).
     */
    public int pickup(ItemResource resource,int amount,TransactionContext transaction) {
        int result = this.insert(resource,amount,transaction);
        if(result > 0 && this.shouldAutoExchange())
            LCApi.getCoinAPI().exchangeCoinsAllUp(this,transaction);
        return result;
    }

    @Override
    public int insert(int index,ItemResource resource,int amount,TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        if(!this.hasWallet())
            return 0;
        ItemStack stack = this.getItem(index);
        if(stack.isEmpty() || resource.matches(stack))
        {
            this.updateSnapshots(transaction);
            //Insert up to the limit
            int space = this.getCapacityAsInt(index,resource) - stack.getCount();
            int inserted = Math.min(amount,space);
            stack = resource.toStack(inserted + stack.getCount());
            //Failed to update the wallet, so nothing was inserted
            if(!this.setItem(index,stack,transaction))
                return 0;
            return inserted;
        }
        return 0;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        if(!this.hasWallet())
            return 0;
        ItemStack stack = this.getItem(index).copy();
        if(resource.matches(stack))
        {
            this.updateSnapshots(transaction);
            int extracted = Math.min(amount,stack.getCount());
            stack.shrink(extracted);
            //Failed to update the wallet, so nothing was extracted
            if(!this.setItem(index,stack,transaction))
                return 0;
            return extracted;
        }
        return 0;
    }

    @Override
    public void set(int index,ItemResource resource,int amount) { this.setItem(index,resource.toStack(amount),null); }

    @Override
    protected List<ItemStack> createSnapshot() { return ItemHelper.copyList(this.getContents()); }

    @Override
    protected void revertToSnapshot(List<ItemStack> snapshot) {
        //Shouldn't actually need to do anything here I think,
        // since it'll just reload the contents from the wallet stack when the item access snapshot is reverted
    }

    @Override
    public boolean shouldWrapCapabilities() { return false; }

}
