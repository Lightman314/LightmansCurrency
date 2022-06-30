package io.github.lightman314.lightmanscurrency.menus.wallet;

import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.IBankAccountMenu;
import io.github.lightman314.lightmanscurrency.core.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class WalletBankMenu extends WalletMenuBase implements IBankAccountMenu {

	public static final int BANK_WIDGET_SPACING = 128;
	
	public WalletBankMenu(int windowId, Inventory inventory, int walletStackIndex) {
		
		super(ModMenus.WALLET_BANK, windowId, inventory, walletStackIndex);
		
		this.addCoinSlots(BANK_WIDGET_SPACING + 1);
		this.addDummySlots(WalletMenuBase.getMaxWalletSlots());
		
	}

	@Override
	public Container getCoinInput() { return this.coinInput; }

	@Override
	public boolean isClient() { return this.player.level.isClientSide; }

	@Override
	public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
	
	@Override
	public boolean stillValid(Player player) {
		this.getBankAccountReference();
		return super.stillValid(player) && this.hasBankAccess();
	}
	
	@Override
	public void onDepositOrWithdraw() {
		if(this.getAutoConvert()) //Don't need to save if converting, as the ConvertCoins function auto-saves.
			this.ConvertCoins();
		else //Save the wallet contents on bank interaction.
			this.saveWalletContents();
		
	}
	
}