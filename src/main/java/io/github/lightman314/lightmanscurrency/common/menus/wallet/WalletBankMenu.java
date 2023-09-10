package io.github.lightman314.lightmanscurrency.common.menus.wallet;

import io.github.lightman314.lightmanscurrency.common.bank.interfaces.IBankAccountMenu;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class WalletBankMenu extends WalletMenuBase implements IBankAccountMenu {

	public static final int BANK_WIDGET_SPACING = 128;
	
	public WalletBankMenu(int windowId, Inventory inventory, int walletStackIndex) {
		
		super(ModMenus.WALLET_BANK.get(), windowId, inventory, walletStackIndex);

		this.addValidator(this::hasBankAccess);
		
		this.addCoinSlots(BANK_WIDGET_SPACING + 1);
		this.addDummySlots(WalletMenuBase.getMaxWalletSlots());
		
	}

	@Override
	public Container getCoinInput() { return this.coinInput; }

	@Override
	public boolean isClient() { return this.player.level.isClientSide; }

	@Nonnull
	@Override
	public ItemStack quickMoveStack(@Nonnull Player player, int slot) { return ItemStack.EMPTY; }
	
	@Override
	protected void onValidationTick(@Nonnull Player player) {
		super.onValidationTick(player);
		this.getBankAccountReference();
	}
	
	@Override
	public void onDepositOrWithdraw() {
		if(this.getAutoExchange()) //Don't need to save if converting, as the ExchangeCoins function auto-saves.
			this.ConvertCoins();
		else //Save the wallet contents on bank interaction.
			this.saveWalletContents();
		
	}
	
}
