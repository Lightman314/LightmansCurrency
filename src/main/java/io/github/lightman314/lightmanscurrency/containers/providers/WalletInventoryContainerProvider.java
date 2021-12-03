package io.github.lightman314.lightmanscurrency.containers.providers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.containers.PlayerInventoryWalletContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class WalletInventoryContainerProvider implements INamedContainerProvider {

	@Nonnull
	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent("container.crafting");
	}
	
	@Nullable
	@Override
	public Container createMenu(int windowId, @Nonnull PlayerInventory playerInventory, @Nonnull PlayerEntity playerEntity)
	{
		return new PlayerInventoryWalletContainer(windowId, playerInventory);
	}
	
}
