package io.github.lightman314.lightmanscurrency.client.gui.settings.core;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

public class LoggerTab extends SettingsTab{

	public static final LoggerTab INSTANCE = new LoggerTab();
	
	int scroll = 0;
	
	ScrollTextDisplay display;
	
	private LoggerTab() { }
	
	@Override
	public int getColor() { return 0xFFFFFF; }

	@Override
	public IconData getIcon() { return IconData.of(Items.PAPER); }
	
	@Override
	public MutableComponent getTooltip() { return Component.translatable("tooltip.lightmanscurrency.settings.log"); }
	
	@Override
	public boolean canOpen() { return true; }
	
	@Override
	public void initTab() {
		//TraderSettingsScreen screen = this.getScreen();
		//this.display = screen.addRenderableTabWidget(new ScrollTextDisplay(screen.guiLeft() + 5, screen.guiTop() + 5, 190, 190, screen.getFont(), () -> getLogger().logText));
		//this.display.invertText = true;
		//TODO rework logger display
	}
	
	//private TextLogger getLogger() { return this.getSetting(CoreTraderSettings.class).getLogger(); }

	@Override
	public void preRender(PoseStack matrix, int mouseX, int mouseY, float partialTicks) { }
	
	@Override
	public void postRender(PoseStack matrix, int mouseX, int mouseY, float partialTicks) { }

	@Override
	public void tick() {
		
	}

	@Override
	public void closeTab() {
		this.display = null;
	}

}
