package io.github.lightman314.lightmanscurrency.integration.backpacked;

import io.github.lightman314.lightmanscurrency.integration.backpacked.container.SuperExtendedPlayerContainer;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.PlayerContainer;

public class SuperExtensionHolder {

	private SuperExtendedPlayerInventory superInventory;
	private SuperExtendedPlayerContainer superContainer;
	
	public SuperExtensionHolder(PlayerEntity player, boolean localWorld)  throws ClassNotFoundException
	{
		if(!setupExtension(player, localWorld))
			throw new ClassNotFoundException();
	}
	
	private boolean setupExtension(PlayerEntity player, boolean localWorld)
	{
		if(!LightmansCurrency.isBackpackedLoaded())
			return false;
		
		superInventory = new SuperExtendedPlayerInventory(player);
		superContainer = new SuperExtendedPlayerContainer(this.superInventory, localWorld, player);
		
		return true;
	}
	
	public PlayerInventory getInventory()
	{
		return this.superInventory;
	}
	
	public PlayerContainer getContainer()
	{
		return this.superContainer;
	}
	
	
	
}
