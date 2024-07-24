package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.walletbank;

import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.WalletBankScreen;

public abstract class WalletBankTab extends EasyTab
{
	protected final WalletBankScreen screen;
	
	protected WalletBankTab(WalletBankScreen screen) { super(screen); this.screen = screen; }
	
}
