package io.github.lightman314.lightmanscurrency.common.menus.providers;

import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import javax.annotation.Nonnull;

public class WalletMenuProvider implements MenuProvider{

	private final int walletItemIndex;
	
	public WalletMenuProvider(int walletItemIndex) { this.walletItemIndex = walletItemIndex; }
	
	@Nonnull
	public Component getDisplayName() { return Component.empty(); }
	
	public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inventory, @Nonnull Player entity) { return new WalletMenu(id, inventory, this.walletItemIndex); }
	
}
