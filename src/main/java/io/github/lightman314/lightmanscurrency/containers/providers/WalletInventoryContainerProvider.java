package io.github.lightman314.lightmanscurrency.containers.providers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.containers.PlayerInventoryWalletContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class WalletInventoryContainerProvider implements MenuProvider {

	@Nonnull
	@Override
	public Component getDisplayName() {
		return new TranslatableComponent("container.crafting");
	}
	
	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int windowId, @Nonnull Inventory playerInventory, @Nonnull Player playerEntity)
	{
		return new PlayerInventoryWalletContainer(windowId, playerInventory);
	}
	
}
