package io.github.lightman314.lightmanscurrency.common.menus.wallet;

import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.IBankAccountMenu;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class WalletBankMenu extends WalletMenuBase implements IBankAccountMenu {

	public static final int BANK_WIDGET_SPACING = 128;
	
	public WalletBankMenu(int windowId, PlayerInventory inventory, int walletStackIndex) {
		
		super(ModMenus.WALLET_BANK.get(), windowId, inventory, walletStackIndex);
		
		this.addCoinSlots(BANK_WIDGET_SPACING + 1);
		this.addDummySlots(WalletMenuBase.getMaxWalletSlots());
		
	}

	@Override
	public IInventory getCoinInput() { return this.coinInput; }

	@Override
	public boolean isClient() { return this.player.level.isClientSide; }

	@Nonnull
	@Override
	public ItemStack quickMoveStack(@Nonnull PlayerEntity player, int slot) { return ItemStack.EMPTY; }
	
	@Override
	public boolean stillValid(@Nonnull PlayerEntity player) {
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