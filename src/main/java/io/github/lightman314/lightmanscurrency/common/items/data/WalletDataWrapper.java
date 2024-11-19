package io.github.lightman314.lightmanscurrency.common.items.data;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.coins.CoinContainerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.MoneyViewer;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class WalletDataWrapper extends MoneyViewer {

    public static final WalletDataWrapper EMPTY = new WalletDataWrapper();

    private final ItemStack wallet;
    public boolean isForStack(@Nonnull ItemStack stack) {
        if(this.wallet == null && !WalletItem.isWallet(stack))
            return true;
        return this.wallet == stack;
    }
    private final WalletItem item;
    public int getContainerSize() { return WalletItem.InventorySize(this.wallet); }
    public int getBonusSlots() { return this.getData().getBonusSlots(this.wallet.getOrDefault(ModDataComponents.WALLET_UPGRADE_LIMIT,0)); }
    Container contentCache = null;

    private WalletDataWrapper() {
        this.wallet = null;
        this.item = null;
    }
    public WalletDataWrapper(@Nonnull ItemStack wallet)
    {
        this.wallet = wallet;
        this.item = (WalletItem)this.wallet.getItem();
        //Validate that list matches capacity
        Container container = this.getContents();
        if(container.getContainerSize() != WalletItem.InventorySize(this.wallet))
            this.forceContainerSize(container);
        //Make local cache, but empty
        this.contentCache = new SimpleContainer(WalletItem.InventorySize(this.wallet));
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

    public Container getContents() { return InventoryUtil.buildInventory(this.getData().items()); }

    public void setContents(@Nonnull Container contents, @Nullable LivingEntity owner)
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

    private Container forceContainerSize(@Nonnull Container container)
    {
        Container newContainer = new SimpleContainer(WalletItem.InventorySize(this.wallet));
        for(int i = 0; i < container.getContainerSize(); ++i)
        {
            if(i < newContainer.getContainerSize())
                newContainer.setItem(i,container.getItem(i).copy());
            else
            {
                ItemStack leftovers = InventoryUtil.TryPutItemStack(newContainer,container.getItem(i));
                if(!leftovers.isEmpty())
                    LightmansCurrency.LogWarning("Lost " + leftovers.getCount() + "x " + BuiltInRegistries.ITEM.getKey(leftovers.getItem()) + " when shrinking Wallet Contents to fit size limits!");
            }
        }
        return newContainer;
    }

    @Override
    protected void collectStoredMoney(@Nonnull MoneyView.Builder builder) {
        this.contentCache = this.getContents();
        CoinContainerMoneyHandler.queryContainerContents(this.contentCache, builder);
    }

}
