package io.github.lightman314.lightmanscurrency.common.menus.providers;

import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

public class WalletMenuProvider implements MenuProvider{

	int walletItemIndex;
	
	public WalletMenuProvider(int walletItemIndex) { this.walletItemIndex = walletItemIndex; }
	
	public @NotNull Component getDisplayName() { return EasyText.empty(); }
	
	public AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player entity) { return new WalletMenu(id, inventory, this.walletItemIndex); }
	
}
