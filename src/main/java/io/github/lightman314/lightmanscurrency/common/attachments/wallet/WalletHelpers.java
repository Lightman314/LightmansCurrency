package io.github.lightman314.lightmanscurrency.common.attachments.wallet;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import io.github.lightman314.lightmanscurrency.common.core.ModAttachmentTypes;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menus.containers.SuppliedItemContainer;
import io.github.lightman314.lightmanscurrency.network.message.walletslot.CPacketCreativeWalletEdit;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class WalletHelpers {

    /**
     * Returns a single-slot container used for an interactable Wallet slot for menus.<br>
     * Automatically handles manually sending update packets to the server if the given player is in creative mode.
     */
    @Nonnull
    public static Container getWalletContainer(@Nonnull final LivingEntity entity) {
        return new SuppliedItemContainer(() -> {
            WalletHandler handler = WalletHandler.get(entity);
            if(handler != null)
                return new WalletInteractable(handler);
            return null;
        });
    }

    /**
     * Gets a snapshot of the wallets current monetary contents.<br>
     * Returns {@link MoneyView#empty()} if no wallet is equipped.
     */
    @Nonnull
    public static MoneyView getWalletMoney(@Nonnull final LivingEntity entity) {
        WalletHandler walletHandler = entity.getData(ModAttachmentTypes.WALLET_HANDLER);
        ItemStack wallet = walletHandler.getWallet();
        return WalletItem.getDataWrapper(wallet).getStoredMoney();
    }

    private static class WalletInteractable implements SuppliedItemContainer.IItemInteractable
    {

        private final WalletHandler walletHandler;
        WalletInteractable(@Nonnull WalletHandler handler) { this.walletHandler = handler; }
        @Nonnull
        @Override
        public ItemStack getItem() { return this.walletHandler.getWallet(); }
        @Override
        public void setItem(@Nonnull ItemStack item) {
            this.walletHandler.setWallet(item);
            if(this.walletHandler.entity().level().isClientSide && this.walletHandler.entity() instanceof Player player && player.isCreative())
            {
                //Send an update packet when edited on the client in creative mode
                new CPacketCreativeWalletEdit(this.getItem().copy()).send();
            }
        }
    }

}
