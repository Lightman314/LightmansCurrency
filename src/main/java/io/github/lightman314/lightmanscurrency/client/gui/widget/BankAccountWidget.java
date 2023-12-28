package io.github.lightman314.lightmanscurrency.client.gui.widget;

import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.MoneyContainer;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.easy.IEasyTickable;
import io.github.lightman314.lightmanscurrency.network.message.bank.CPacketBankInteraction;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class BankAccountWidget implements IEasyTickable {

	public static final int HEIGHT = MoneyValueWidget.HEIGHT + 40;
	public static final int BUTTON_WIDTH = 70;
	
	private final IBankAccountWidget parent;
	
	private final MoneyValueWidget amountSelection;
	public MoneyValueWidget getAmountSelection() { return this.amountSelection; }
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

		this.amountSelection = new MoneyValueWidget(screenMiddle - MoneyValueWidget.WIDTH / 2, this.y, null, MoneyValue.empty(), value -> {});
		this.amountSelection.allowFreeInput = false;
		addWidget.accept(this.amountSelection);

		this.buttonDeposit = new EasyTextButton(screenMiddle - 5 - BUTTON_WIDTH, this.y + MoneyValueWidget.HEIGHT + 5 + spacing, BUTTON_WIDTH, 20, EasyText.translatable("gui.button.bank.deposit"), this::OnDeposit);
		addWidget.accept(this.buttonDeposit);
		this.buttonWithdraw = new EasyTextButton(screenMiddle + 5, this.y + MoneyValueWidget.HEIGHT + 5 + spacing, BUTTON_WIDTH, 20, EasyText.translatable("gui.button.bank.withdraw"), this::OnWithdraw);
		addWidget.accept(this.buttonWithdraw);
		this.buttonDeposit.active = this.buttonWithdraw.active = false;
		
	}

	public void renderInfo(@Nonnull EasyGuiGraphics gui) { this.renderInfo(gui, 0); }
	
	public void renderInfo(@Nonnull EasyGuiGraphics gui, int yOffset)
	{

		int screenMiddle = this.parent.getScreen().width / 2;
		BankAccount ba = this.parent.getBankAccount();
		Component balanceComponent = ba == null ? EasyText.translatable("gui.lightmanscurrency.bank.null") : EasyText.translatable("gui.lightmanscurrency.bank.balance", ba.getMoneyStorage().getRandomValueText());
		int offset = gui.font.width(balanceComponent.getString()) / 2;
		gui.pushOffsetZero().drawString(balanceComponent, screenMiddle - offset, this.y + MoneyValueWidget.HEIGHT + 30 + spacing + yOffset, 0x404040);
		gui.popOffset();

	}

	@Override
	public void tick()
	{
		
		if(this.parent.getBankAccount() == null)
		{
			this.buttonDeposit.active = this.buttonWithdraw.active = false;
		}
		else
		{
			this.buttonDeposit.active = !this.parent.getCoinAccess().getStoredMoney().isEmpty() && (this.allowEmptyDeposits || !this.amountSelection.getCurrentValue().isEmpty());
			this.buttonWithdraw.active = !this.amountSelection.getCurrentValue().isEmpty();
		}
		
	}
	
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

	public interface IBankAccountWidget
	{
		Font getFont();
		Screen getScreen();
		BankAccount getBankAccount();
		MoneyContainer getCoinAccess();
	}
	
}
