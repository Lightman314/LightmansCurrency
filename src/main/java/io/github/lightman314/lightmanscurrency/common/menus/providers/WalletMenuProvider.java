package io.github.lightman314.lightmanscurrency.common.menus.providers;

import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenu;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;

import javax.annotation.Nonnull;

public class WalletMenuProvider extends NamelessMenuProvider {

	int walletItemIndex;
	
	public WalletMenuProvider(int walletItemIndex)
	{
		this.walletItemIndex = walletItemIndex;
	}
	
	public Container createMenu(int id, @Nonnull PlayerInventory inventory, @Nonnull PlayerEntity entity) { return new WalletMenu(id, inventory, this.walletItemIndex); }
	
}
