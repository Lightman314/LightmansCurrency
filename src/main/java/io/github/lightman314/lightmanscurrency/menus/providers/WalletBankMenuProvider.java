package io.github.lightman314.lightmanscurrency.menus.providers;

import io.github.lightman314.lightmanscurrency.menus.wallet.WalletBankMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class WalletBankMenuProvider implements MenuProvider{

	int walletItemIndex;
	
	public WalletBankMenuProvider(int walletItemIndex)
	{
		this.walletItemIndex = walletItemIndex;
	}
	
	public Component getDisplayName()
	{
		return new TranslatableComponent("");
	}
	
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player entity)
	{
		return new WalletBankMenu(id, inventory, this.walletItemIndex);
	}
	
}
