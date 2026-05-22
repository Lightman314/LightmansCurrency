package io.github.lightman314.lightmanscurrency.common.menus.wallet;

import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.bank.menu.IBankAccountMenu;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyViewer;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class WalletBankMenu extends WalletMenuBase implements IBankAccountMenu {

	public static final int BANK_WIDGET_SPACING = 128;

	private final IMoneyViewer coinInputHandler;

	public WalletBankMenu(int windowId, Inventory inventory, int walletStackIndex) {
		
		super(ModMenus.WALLET_BANK.get(), windowId, inventory, walletStackIndex);
		this.addValidator(this::hasBankAccess);
		this.addValidator(() -> !QuarantineAPI.IsDimensionQuarantined(this.player));

		this.coinInputHandler = MoneyAPI.getApi().GetContainersMoneyHandler(this.walletInventory, this.getPlayer());
		
		this.addCoinSlots(BANK_WIDGET_SPACING + 1);
		
	}

	@Override
	public IItemHandler getCoinInput() { return this.walletInventory; }
	public IMoneyViewer getCoinInputHandler() { return this.coinInputHandler; }

	@Override
	public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }

	@Override
	protected void onValidationTick(Player player) {
		super.onValidationTick(player);
		this.getBankAccountReference();
	}

	@Override
	public void onDepositOrWithdraw() {
		if(this.getAutoExchange()) //Don't need to save if exchanging coins, as the ExchangeCoins function auto-saves.
			this.ExchangeCoins();
		else //Save the wallet items on bank interaction.
			this.saveWalletContents();
	}

    public void openWallet() {
        if(this.isClient())
        {
            this.SendMessage(this.builder().setFlag("OpenWallet"));
            return;
        }
        if(this.hasWallet())
            WalletMenuBase.SafeOpenWalletMenu(this.player,this.walletStackIndex);
    }

    @Override
    protected void processMessage(LazyPacketData message) {
        super.processMessage(message);
        if(message.contains("OpenWallet"))
            this.openWallet();
    }
}
