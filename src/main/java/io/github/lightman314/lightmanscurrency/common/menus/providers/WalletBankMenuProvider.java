package io.github.lightman314.lightmanscurrency.common.menus.providers;

import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletBankMenu;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;

import javax.annotation.Nonnull;

public class WalletBankMenuProvider extends NamelessMenuProvider{

	int walletItemIndex;
	
	public WalletBankMenuProvider(int walletItemIndex) { this.walletItemIndex = walletItemIndex; }
	
	public Container createMenu(int id, @Nonnull PlayerInventory inventory, @Nonnull PlayerEntity entity) { return new WalletBankMenu(id, inventory, this.walletItemIndex); }
	
}
