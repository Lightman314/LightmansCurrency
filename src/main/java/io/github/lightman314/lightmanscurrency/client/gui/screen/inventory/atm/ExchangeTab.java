package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import java.util.List;

import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMExchangeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.atm.ATMExchangeButtonData;
import io.github.lightman314.lightmanscurrency.common.atm.ATMData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class ExchangeTab extends ATMTab{

	public ExchangeTab(ATMScreen screen) { super(screen); }
	
	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {
		
		List<ATMExchangeButtonData> buttonData = ATMData.get().getConversionButtons();
		for(ATMExchangeButtonData data : buttonData)
			this.addChild(new ATMExchangeButton(screenArea.pos, data, this::RunExchangeCommand));
		
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {
		gui.drawString(this.getTooltip(), 8, 6, 0x404040);
	}
	
	private void RunExchangeCommand(String command) {
		this.screen.getMenu().SendCoinExchangeMessage(command);
		//LightmansCurrencyPacketHandler.instance.sendToServer(new MessageATMConversion(command));
	}

	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(ModBlocks.ATM); }

	@Override
	public MutableComponent getTooltip() { return Component.translatable("tooltip.lightmanscurrency.atm.conversion"); }
	
	

}
