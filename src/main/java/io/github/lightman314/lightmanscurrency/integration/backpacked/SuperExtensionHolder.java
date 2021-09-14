package io.github.lightman314.lightmanscurrency.integration.backpacked;

import io.github.lightman314.lightmanscurrency.integration.backpacked.container.SuperExtendedPlayerContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;

public class SuperExtensionHolder {

	private SuperExtendedPlayerInventory superInventory;
	private SuperExtendedPlayerContainer superContainer;
	
	public SuperExtensionHolder(Player player, boolean localWorld)  throws ClassNotFoundException
	{
		if(!setupExtension(player, localWorld))
			throw new ClassNotFoundException();
	}
	
	private boolean setupExtension(Player player, boolean localWorld)
	{
		if(!LightmansCurrency.isBackpackedLoaded())
			return false;
		
		superInventory = new SuperExtendedPlayerInventory(player);
		superContainer = new SuperExtendedPlayerContainer(this.superInventory, localWorld, player);
		
		return true;
	}
	
	public Inventory getInventory()
	{
		return this.superInventory;
	}
	
	public InventoryMenu getContainer()
	{
		return this.superContainer;
	}
	
	
	
}
