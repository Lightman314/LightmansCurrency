package io.github.lightman314.lightmanscurrency.common.items.data;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.LCItemStackHandler;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.capability.MoneyViewer;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;

@Deprecated(forRemoval = true)
public final class WalletDataWrapper extends MoneyViewer {

    private final ItemStack wallet;
    public boolean isForStack(ItemStack stack) {
        if(this.wallet == null && !WalletItem.isWallet(stack))
            return true;
        return this.wallet == stack;
    }
    private final WalletItem item;
    public int getContainerSize() { return WalletItem.InventorySize(this.wallet); }
    public int getBonusSlots() { return this.getData().getBonusSlots(this.wallet.getOrDefault(ModDataComponents.WALLET_UPGRADE_LIMIT,0)); }

    private WalletDataWrapper() {
        this.wallet = null;
        this.item = null;
    }
    public WalletDataWrapper(ItemStack wallet)
    {
        this.wallet = wallet;
        this.item = (WalletItem)this.wallet.getItem();
        //Validate that list matches capacity
        LCItemStackHandler container = this.getContents();
        if(container.getSlots() != WalletItem.InventorySize(this.wallet))
            this.forceContainerSize(container);
    }

    public boolean valid() { return this.wallet != null; }
    private WalletData getData() {
        if(this.wallet == null)
            return WalletData.EMPTY;
        if(!this.wallet.has(ModDataComponents.WALLET_DATA))
            this.wallet.set(ModDataComponents.WALLET_DATA, WalletData.createFor(this.wallet));
        return this.wallet.get(ModDataComponents.WALLET_DATA);
    }

    public boolean getAutoExchange() { return WalletItem.CanExchange(this.item) && this.getData().autoExchange(); }
    public void setAutoExchange(boolean autoExchange) {
        if(!this.valid())
        {
            LightmansCurrency.LogError("WalletDataWrapper#setAutoExchange was called on a wallet wrapper that has not been intialized properly!");
            return;
        }
        this.wallet.set(ModDataComponents.WALLET_DATA, this.getData().withAutoExchange(autoExchange));
    }

    public LCItemStackHandler getContents() { return new LCItemStackHandler(this.getData().items()); }

    public void setContents(IItemHandler contents, @Nullable LivingEntity owner)
    {
        if(!this.valid() && !contents.isEmpty())
        {
            LightmansCurrency.LogError("WalletDataWrapper#setContents was called on a wallet wrapper that is not an actual wallet!");
            return;
        }
        if(contents.getContainerSize() != WalletItem.InventorySize(this.wallet))
        {
            LightmansCurrency.LogWarning("WalletDataWrapper#setContents container size does not match the expected container size for this wallet.\nForcing container to match the wallets actual size!");
            contents = this.forceContainerSize(contents);
        }
        this.wallet.set(ModDataComponents.WALLET_DATA, this.getData().withItems(contents));

        if(owner != null)
            WalletMenuBase.OnWalletUpdated(owner);
    }

    private LCItemStackHandler forceContainerSize(LCItemStackHandler container)
    {
        LCItemStackHandler newContainer = new LCItemStackHandler(WalletItem.InventorySize(this.wallet));
        for(int i = 0; i < container.getSlots(); ++i)
        {
            if(i < newContainer.getSlots() && i < container.getSlots())
                newContainer.setStackInSlot(i,container.getStackInSlot(i).copy());
            else if(i < container.getSlots())
            {
                ItemStack leftovers = ItemHandlerHelper.insertItem(newContainer,container.getStackInSlot(i),false);
                if(!leftovers.isEmpty())
                    LightmansCurrency.LogWarning("Lost " + leftovers.getCount() + "x " + BuiltInRegistries.ITEM.getKey(leftovers.getItem()) + " when shrinking Wallet Contents to fit size limits!");
            }
        }
        return newContainer;
    }

    @Override
    protected void collectStoredMoney(MoneyView.Builder builder) {
        IMoneyHandler handler = MoneyAPI.getApi().GetContainersMoneyHandler(this.getContents(),s -> {}, IClientTracker.forClient());
        builder.merge(handler.getStoredMoney());
    }

}
