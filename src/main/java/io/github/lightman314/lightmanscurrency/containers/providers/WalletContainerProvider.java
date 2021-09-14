package io.github.lightman314.lightmanscurrency.containers.providers;

import io.github.lightman314.lightmanscurrency.containers.WalletContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class WalletContainerProvider implements MenuProvider{

	int walletItemIndex;
	
	public WalletContainerProvider(int walletItemIndex)
	{
		this.walletItemIndex = walletItemIndex;
	}
	
	public Component getDisplayName()
	{
		return new TextComponent("");
	}
	
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player entity)
	{
		return new WalletContainer(id, inventory, this.walletItemIndex);
	}
	
}
