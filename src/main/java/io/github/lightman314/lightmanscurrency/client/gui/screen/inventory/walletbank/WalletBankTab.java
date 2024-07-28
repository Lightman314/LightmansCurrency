package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.walletbank;

import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.WalletBankScreen;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletBankMenu;

public abstract class WalletBankTab extends EasyTab
{
	protected final WalletBankScreen screen;
	protected final WalletBankMenu menu;
	
	protected WalletBankTab(WalletBankScreen screen) { super(screen); this.screen = screen; this.menu = this.screen.getMenu(); }
	
}
