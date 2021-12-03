package io.github.lightman314.lightmanscurrency.containers.providers;

import io.github.lightman314.lightmanscurrency.containers.WalletContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class WalletContainerProvider implements INamedContainerProvider{

	int walletItemIndex;
	
	public WalletContainerProvider(int walletItemIndex)
	{
		this.walletItemIndex = walletItemIndex;
	}
	
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("");
	}
	
	public Container createMenu(int id, PlayerInventory inventory, PlayerEntity entity)
	{
		return new WalletContainer(id, inventory, this.walletItemIndex);
	}
	
}
