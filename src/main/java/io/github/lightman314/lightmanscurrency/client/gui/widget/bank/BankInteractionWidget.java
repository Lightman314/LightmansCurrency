package io.github.lightman314.lightmanscurrency.client.gui.widget.bank;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.network.message.bank.CPacketBankInteraction;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

public class BankInteractionWidget extends EasyWidgetWithChildren {

	public static final int HEIGHT = MoneyValueWidget.HEIGHT + 40;
	public static final int BUTTON_WIDTH = 70;

	private final IBankInteractionHandler parent;

	private MoneyValueWidget amountSelection;
	public MoneyValueWidget getAmountSelection() { return this.amountSelection; }

	private final int spacing;

	private final boolean allowEmptyDeposits;
	private final boolean drawMoneyBG;

	private BankInteractionWidget(@Nonnull Builder builder)
	{
		super(builder);
		this.parent = Objects.requireNonNull(builder.handler);
		this.spacing = builder.spacing;
		this.allowEmptyDeposits = builder.allowEmptyDeposits;
		this.drawMoneyBG = builder.drawMoneyBG;
	}

	@Override
	public void addChildren(@Nonnull ScreenArea area) {

		this.amountSelection = this.addChild(MoneyValueWidget.builder()
				.position(area.pos)
				.blockFreeInputs()
				.drawBG(this.drawMoneyBG)
				.build());

		this.addChild(EasyTextButton.builder()
				.position(area.pos.offset(13,MoneyValueWidget.HEIGHT + this.spacing))
				.width(BUTTON_WIDTH)
				.text(LCText.BUTTON_BANK_DEPOSIT)
				.pressAction(this::OnDeposit)
				.addon(EasyAddonHelper.activeCheck(this::canDeposit))
				.build());
		this.addChild(EasyTextButton.builder()
				.position(area.pos.offset(23 + BUTTON_WIDTH,MoneyValueWidget.HEIGHT + this.spacing))
				.width(BUTTON_WIDTH)
				.text(LCText.BUTTON_BANK_WITHDRAW)
				.pressAction(this::OnWithdraw)
				.addon(EasyAddonHelper.activeCheck(this::canWithdraw))
				.build());

	}

	@Override
	protected void renderWidget(@Nonnull EasyGuiGraphics gui) {
		//int screenMiddle = this.parent.getScreen().width / 2;
		IBankAccount ba = this.parent.getBankAccount();
		Component balanceComponent = ba == null ? LCText.GUI_BANK_NO_SELECTED_ACCOUNT.get() : ba.getBalanceText();
		TextRenderUtil.drawCenteredText(gui,balanceComponent, this.width / 2, MoneyValueWidget.HEIGHT + 25 + this.spacing, 0x404040);
	}

	private boolean canDeposit() {
		IBankAccount account = this.parent.getBankAccount();
		return account != null && (this.allowEmptyDeposits || !this.amountSelection.getCurrentValue().isEmpty());
	}

	private boolean canWithdraw() { return this.parent.getBankAccount() != null && !this.amountSelection.getCurrentValue().isEmpty(); }
	
	private void OnDeposit(EasyButton button)
	{
		new CPacketBankInteraction(true, this.amountSelection.getCurrentValue()).send();
		this.amountSelection.changeValue(MoneyValue.empty());
	}
	
	private void OnWithdraw(EasyButton button)
	{
		new CPacketBankInteraction(false, this.amountSelection.getCurrentValue()).send();
		this.amountSelection.changeValue(MoneyValue.empty());
	}

	@Nonnull
	public static Builder builder() { return new Builder(); }

	@MethodsReturnNonnullByDefault
	@FieldsAreNonnullByDefault
	@ParametersAreNonnullByDefault
	public static class Builder extends EasyBuilder<Builder>
	{
		private Builder() { super(176,MoneyValueWidget.HEIGHT + 40); }
		@Override
		protected Builder getSelf() { return this; }

		@Nullable
		private IBankInteractionHandler handler = null;
		private int spacing = 5;
		private boolean allowEmptyDeposits = true;
		private boolean drawMoneyBG = false;

		public Builder handler(IBankInteractionHandler handler) { this.handler = handler; return this; }
		public Builder spacing(int spacing) { this.spacing = spacing; this.changeHeight(MoneyValueWidget.HEIGHT + this.spacing + 35); return this; }
		public Builder blockEmptyDeposits() { this.allowEmptyDeposits = false; return this; }
		public Builder drawMoneyBG() { this.drawMoneyBG = true; return this; }

		public BankInteractionWidget build() { return new BankInteractionWidget(this); }

	}
	
}
