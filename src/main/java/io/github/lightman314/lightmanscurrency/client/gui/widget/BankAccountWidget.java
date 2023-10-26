package io.github.lightman314.lightmanscurrency.client.gui.widget;

import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.easy.IEasyTickable;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.network.message.bank.CPacketBankInteraction;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class BankAccountWidget implements IEasyTickable {

	public static final int MIN_WIDTH = 100;
	public static final int HEIGHT = CoinValueInput.HEIGHT + 40;
	public static final int BUTTON_WIDTH = 70;
	
	private final IBankAccountWidget parent;
	
	private final CoinValueInput amountSelection;
	public CoinValueInput getAmountSelection() { return this.amountSelection; }
	private final EasyButton buttonDeposit;
	private final EasyButton buttonWithdraw;
	
	int y;
	int spacing;
	
	public boolean allowEmptyDeposits = true;
	
	public BankAccountWidget(int y, IBankAccountWidget parent, Consumer<Object> addWidget) { this(y, parent, 0, addWidget); }
	
	public BankAccountWidget(int y, IBankAccountWidget parent, int spacing, Consumer<Object> addWidget) {
		this.parent = parent;
		
		this.y = y;
		this.spacing = spacing;

		int screenMiddle = this.parent.getScreen().width / 2;

		this.amountSelection = new CoinValueInput(screenMiddle - CoinValueInput.DISPLAY_WIDTH / 2, this.y, EasyText.translatable("gui.lightmanscurrency.bank.amounttip"), CoinValue.EMPTY, this.parent.getFont(), value -> {});
		this.amountSelection.allowFreeToggle = false;
		addWidget.accept(this.amountSelection);

		this.buttonDeposit = new EasyTextButton(screenMiddle - 5 - BUTTON_WIDTH, this.y + CoinValueInput.HEIGHT + 5 + spacing, BUTTON_WIDTH, 20, EasyText.translatable("gui.button.bank.deposit"), this::OnDeposit);
		addWidget.accept(this.buttonDeposit);
		this.buttonWithdraw = new EasyTextButton(screenMiddle + 5, this.y + CoinValueInput.HEIGHT + 5 + spacing, BUTTON_WIDTH, 20, EasyText.translatable("gui.button.bank.withdraw"), this::OnWithdraw);
		addWidget.accept(this.buttonWithdraw);
		this.buttonDeposit.active = this.buttonWithdraw.active = false;
		
	}

	public void renderInfo(@Nonnull EasyGuiGraphics gui) { this.renderInfo(gui, 0); }
	
	public void renderInfo(@Nonnull EasyGuiGraphics gui, int yOffset)
	{

		int screenMiddle = this.parent.getScreen().width / 2;
		Component balanceComponent = this.parent.getBankAccount() == null ? EasyText.translatable("gui.lightmanscurrency.bank.null") : EasyText.translatable("gui.lightmanscurrency.bank.balance", this.parent.getBankAccount().getCoinStorage().getComponent("0"));
		int offset = gui.font.width(balanceComponent.getString()) / 2;
		gui.pushOffsetZero().drawString(balanceComponent, screenMiddle - offset, this.y + CoinValueInput.HEIGHT + 30 + spacing + yOffset, 0x404040);
		gui.popOffset();
	}

	@Override
	public void tick()
	{
		this.amountSelection.tick();
		
		if(this.parent.getBankAccount() == null)
		{
			this.buttonDeposit.active = this.buttonWithdraw.active = false;
		}
		else
		{
			this.buttonDeposit.active = MoneyUtil.getValue(this.parent.getCoinAccess()) > 0 && (this.allowEmptyDeposits || this.amountSelection.getCoinValue().getValueNumber() > 0);
			this.buttonWithdraw.active = this.amountSelection.getCoinValue().getValueNumber() > 0;
		}
		
	}
	
	private void OnDeposit(EasyButton button)
	{
		new CPacketBankInteraction(true, this.amountSelection.getCoinValue()).send();
		this.amountSelection.setCoinValue(CoinValue.EMPTY);
	}
	
	private void OnWithdraw(EasyButton button)
	{
		new CPacketBankInteraction(false, this.amountSelection.getCoinValue()).send();
		this.amountSelection.setCoinValue(CoinValue.EMPTY);
	}

	public interface IBankAccountWidget
	{
		Font getFont();
		Screen getScreen();
		BankAccount getBankAccount();
		Container getCoinAccess();
	}
	
}
