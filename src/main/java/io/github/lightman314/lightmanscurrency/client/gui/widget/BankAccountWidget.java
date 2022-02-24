package io.github.lightman314.lightmanscurrency.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput.ICoinValueInput;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageBankInteraction;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class BankAccountWidget implements ICoinValueInput{

	public static final int MIN_WIDTH = 100;
	public static final int HEIGHT = CoinValueInput.HEIGHT + 40;
	public static final int BUTTON_WIDTH = 70;
	
	private final IBankAccountWidget parent;
	
	private CoinValueInput amountSelection;
	Button buttonDeposit;
	Button buttonWithdraw;
	
	private final int y;
	private final int spacing;
	
	public boolean allowEmptyDeposits = true;
	
	public BankAccountWidget(int y, IBankAccountWidget parent) { this(y, parent, 0); }
	
	public BankAccountWidget(int y, IBankAccountWidget parent, int spacing) {
		this.parent = parent;
		
		this.y = y;
		this.spacing = spacing;
		
		this.amountSelection = this.parent.addCustomListener(new CoinValueInput(this.y, new TranslationTextComponent("gui.lightmanscurrency.bank.amounttip"), CoinValue.EMPTY, this));
		this.amountSelection.allowFreeToggle = false;
		//this.amountSelection.init();
		
		int screenMiddle = this.parent.getScreen().width / 2;
		
		this.buttonDeposit = this.parent.addCustomWidget(new Button(screenMiddle - 5 - BUTTON_WIDTH, this.y + CoinValueInput.HEIGHT + 5 + spacing, BUTTON_WIDTH, 20, new TranslationTextComponent("gui.button.bank.deposit"), this::OnDeposit));
		this.buttonWithdraw = this.parent.addCustomWidget(new Button(screenMiddle + 5, this.y + CoinValueInput.HEIGHT + 5 + spacing, BUTTON_WIDTH, 20, new TranslationTextComponent("gui.button.bank.withdraw"), this::OnWithdraw));
		this.buttonDeposit.active = this.buttonWithdraw.active = false;
		
	}
	
	public void renderCoinValueWidget(MatrixStack matrix, int mouseX, int mouseY, float partialTicks)
	{
		this.amountSelection.render(matrix, mouseX, mouseY, partialTicks);
	}
	
	public void renderInfo(MatrixStack matrix) { this.renderInfo(matrix, 0); }
	
	public void renderInfo(MatrixStack matrix, int yOffset)
	{
		
		int screenMiddle = this.parent.getScreen().width / 2;
		FontRenderer font = this.parent.getFont();
		ITextComponent balanceComponent = this.parent.getAccount() == null ? new TranslationTextComponent("gui.lightmanscurrency.bank.null") : new TranslationTextComponent("gui.lightmanscurrency.bank.balance", this.parent.getAccount().getCoinStorage().getString("0"));
		int offset = font.getStringWidth(balanceComponent.getString()) / 2;
		this.parent.getFont().drawString(matrix, balanceComponent.getString(), screenMiddle - offset, this.y + CoinValueInput.HEIGHT + 30 + spacing + yOffset, 0x404040);
		
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
		public <T extends Button> T addCustomWidget(T widget);
		public <T extends IGuiEventListener> T addCustomListener(T widget);
		public FontRenderer getFont();
		public Screen getScreen();
		public int getWidth();
		public BankAccount getAccount();
		public IInventory getCoinAccess();
	}

	@Override
	public <T extends Button> T addCustomButton(T button) {
		return this.parent.addCustomWidget(button);
	}
	
	@Override
	public <T extends IGuiEventListener> T addCustomListener(T listener) {
		return this.parent.addCustomListener(listener);
	}

	@Override
	public FontRenderer getFont() {
		return this.parent.getFont();
	}

	@Override
	public void OnCoinValueChanged(CoinValueInput input) { }

	@Override
	public int getWidth() { return this.parent.getWidth(); }
	
}
