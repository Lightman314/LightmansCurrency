package io.github.lightman314.lightmanscurrency.client.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageBankInteraction;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;

public class BankAccountWidget {

	public static final int MIN_WIDTH = 100;
	public static final int HEIGHT = CoinValueInput.HEIGHT + 40;
	public static final int BUTTON_WIDTH = 70;
	
	private final IBankAccountWidget parent;
	
	private CoinValueInput amountSelection;
	public CoinValueInput getAmountSelection() { return this.amountSelection; }
	Button buttonDeposit;
	Button buttonWithdraw;
	
	int y;
	int spacing;
	
	public boolean allowEmptyDeposits = true;
	
	public BankAccountWidget(int y, IBankAccountWidget parent) { this(y, parent, 0); }
	
	public BankAccountWidget(int y, IBankAccountWidget parent, int spacing) {
		this.parent = parent;
		
		this.y = y;
		this.spacing = spacing;
		
		int screenMiddle = this.parent.getScreen().width / 2;
		
		this.amountSelection = this.parent.addCustomWidget(new CoinValueInput(screenMiddle - CoinValueInput.DISPLAY_WIDTH / 2, this.y, new TranslatableComponent("gui.lightmanscurrency.bank.amounttip"), CoinValue.EMPTY, this.parent.getFont(), value -> {}, this.parent::addCustomWidget));
		this.amountSelection.allowFreeToggle = false;
		this.amountSelection.init();
		
		this.buttonDeposit = this.parent.addCustomWidget(new Button(screenMiddle - 5 - BUTTON_WIDTH, this.y + CoinValueInput.HEIGHT + 5 + spacing, BUTTON_WIDTH, 20, new TranslatableComponent("gui.button.bank.deposit"), this::OnDeposit));
		this.buttonWithdraw = this.parent.addCustomWidget(new Button(screenMiddle + 5, this.y + CoinValueInput.HEIGHT + 5 + spacing, BUTTON_WIDTH, 20, new TranslatableComponent("gui.button.bank.withdraw"), this::OnWithdraw));
		this.buttonDeposit.active = this.buttonWithdraw.active = false;
		
	}
	
	public void renderInfo(PoseStack pose) { this.renderInfo(pose, 0); }
	
	public void renderInfo(PoseStack pose, int yOffset)
	{
		
		int screenMiddle = this.parent.getScreen().width / 2;
		Font font = this.parent.getFont();
		Component balanceComponent = this.parent.getAccount() == null ? new TranslatableComponent("gui.lightmanscurrency.bank.null") : new TranslatableComponent("gui.lightmanscurrency.bank.balance", this.parent.getAccount().getCoinStorage().getString("0"));
		int offset = font.width(balanceComponent.getString()) / 2;
		this.parent.getFont().draw(pose, balanceComponent, screenMiddle - offset, this.y + CoinValueInput.HEIGHT + 30 + spacing + yOffset, 0x404040);
		
	}
	
	public void tick()
	{
		this.amountSelection.tick();
		
		if(this.parent.getAccount() == null)
		{
			this.buttonDeposit.active = this.buttonWithdraw.active = false;
		}
		else
		{
			this.buttonDeposit.active = MoneyUtil.getValue(this.parent.getCoinAccess()) > 0 && (this.allowEmptyDeposits || this.amountSelection.getCoinValue().getRawValue() > 0);
			this.buttonWithdraw.active = this.amountSelection.getCoinValue().getRawValue() > 0;
		}
		
	}
	
	private void OnDeposit(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageBankInteraction(true, this.amountSelection.getCoinValue()));
		this.amountSelection.setCoinValue(CoinValue.EMPTY);
	}
	
	private void OnWithdraw(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageBankInteraction(false, this.amountSelection.getCoinValue()));
		this.amountSelection.setCoinValue(CoinValue.EMPTY);
	}

	public interface IBankAccountWidget
	{
		public <T extends GuiEventListener & Widget & NarratableEntry> T addCustomWidget(T widget);
		public Font getFont();
		public Screen getScreen();
		public BankAccount getAccount();
		public Container getCoinAccess();
	}
	
}
