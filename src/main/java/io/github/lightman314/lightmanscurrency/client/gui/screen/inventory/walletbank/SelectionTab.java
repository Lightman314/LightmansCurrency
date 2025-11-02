package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.walletbank;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.WalletBankScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountSelectionWidget;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.network.message.bank.CPacketSelectBankAccount;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;

public class SelectionTab extends WalletBankTab {

	public SelectionTab(WalletBankScreen screen) { super(screen); }

	BankAccountSelectionWidget bankAccountSelection;

	@Nonnull
	@Override
	public IconData getIcon() { return ItemIcon.ofItem(Items.PAPER); }

	@Override
	public MutableComponent getTooltip() { return LCText.TOOLTIP_ATM_SELECTION.get(); }

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {

		this.bankAccountSelection = this.addChild(BankAccountSelectionWidget.builder()
				.position(screenArea.pos.offset(20,15))
				.width(screenArea.width - 40)
				.rows(4)
				.filter(this::allowedAccess)
				.selected(this.menu::getBankAccountReference)
				.handler(this::selectAccount)
				.build());

	}

	@Override
	public boolean blockInventoryClosing() { return true; }

	private boolean allowedAccess(@Nonnull BankReference reference)
	{
		//Only allow selection of accounts the player can access without ADMIN MODE for the wallet screen
		return reference.allowedAccess(PlayerReference.of(this.menu.player));
	}

	private void selectAccount(@Nonnull BankReference reference)
	{
		new CPacketSelectBankAccount(reference).send();
	}

	private void PressPersonalAccount(EasyButton button)
	{
		BankReference account = PlayerBankReference.of(this.screen.getMenu().getPlayer());
		new CPacketSelectBankAccount(account).send();
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {

		gui.drawString(this.getTooltip(), 8, 6, 0x404040);

	}

}