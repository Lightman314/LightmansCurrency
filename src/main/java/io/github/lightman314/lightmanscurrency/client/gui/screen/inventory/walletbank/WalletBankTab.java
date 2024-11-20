package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.walletbank;

import io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed.EasyClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.WalletBankScreen;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletBankMenu;

public abstract class WalletBankTab extends EasyClientTab<WalletBankMenu,WalletBankScreen,WalletBankTab>
{

	protected WalletBankTab(WalletBankScreen screen) { super(screen);  }

}