package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMConversionButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.atm.ATMConversionButtonData;
import io.github.lightman314.lightmanscurrency.common.atm.ATMData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageATMConversion;
import net.minecraft.util.text.IFormattableTextComponent;

import javax.annotation.Nonnull;

public class ConversionTab extends ATMTab{

	public ConversionTab(ATMScreen screen) { super(screen); }
	
	@Override
	public void init() {
		
		List<ATMConversionButtonData> buttonData = ATMData.get().getConversionButtons();
		int left = this.screen.getGuiLeft();
		int top = this.screen.getGuiTop();
		for(ATMConversionButtonData data : buttonData)
		{
			this.screen.addRenderableTabWidget(new ATMConversionButton(left, top, data, this::RunConversionCommand));
		}
		
	}

	@Override
	public void preRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		this.screen.getFont().draw(pose, this.getTooltip(), this.screen.getGuiLeft() + 8f, this.screen.getGuiTop() + 6f, 0x404040);
	}

	@Override
	public void postRender(MatrixStack pose, int mouseX, int mouseY) { }
	
	@Override
	public void tick() { }
	
	@Override
	public void onClose() { }
	
	private void RunConversionCommand(String command) {
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageATMConversion(command));
	}

	@Nonnull
    @Override
	public IconData getIcon() { return IconData.of(ModBlocks.MACHINE_ATM.get()); }

	@Override
	public IFormattableTextComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.atm.conversion"); }
	
	

}
