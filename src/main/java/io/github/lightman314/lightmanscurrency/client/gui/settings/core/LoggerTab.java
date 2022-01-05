package io.github.lightman314.lightmanscurrency.client.gui.settings.core;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.api.TextLogger;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.trader.settings.CoreTraderSettings;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class LoggerTab extends SettingsTab{

	public static final LoggerTab INSTANCE = new LoggerTab();
	
	int scroll = 0;
	
	ScrollTextDisplay display;
	
	private LoggerTab() { }
	
	@Override
	public int getColor() { return 0xFFFFFF; }

	@Override
	public IconData getIcon() { return IconData.of(new ItemStack(Items.WRITABLE_BOOK)); }
	
	@Override
	public ITextComponent getTooltip() { return new TranslationTextComponent("tooltip.lightmanscurrency.settings.log"); }
	
	@Override
	public ImmutableList<String> requiredPermissions() { return ImmutableList.of(); }
	
	@Override
	public void initTab() {
		TraderSettingsScreen screen = this.getScreen();
		this.display = screen.addRenderableTabWidget(new ScrollTextDisplay(screen.guiLeft() + 5, screen.guiTop() + 5, 190, 190, screen.getFont(), () -> getLogger().logText));
		this.display.invertText = true;
	}
	
	private TextLogger getLogger() { return this.getSetting(CoreTraderSettings.class).getLogger(); }

	@Override
	public void preRender(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
		
		TraderSettingsScreen screen = this.getScreen();
		
		TextLogger logger = this.getLogger();
		
		Screen.fill(matrix, screen.guiLeft() + 5, screen.guiTop() + 5, screen.guiLeft() + screen.xSize - 5, screen.guiTop() + screen.ySize - 5, 0xFF000000);
		
		int x = 7;
		int y = 7;
		int width = screen.xSize - 14;
		int height = screen.ySize - 6;
		for(int i = this.getLogger().logText.size() - 1 - this.scroll; y < height && i >= 0; --i)
		{
			ITextComponent text = logger.logText.get(i);
			int thisHeight = screen.getFont().getWordWrappedHeight(text.getString(), width);
			if(y + thisHeight < height)
			{
				screen.getFont().func_238418_a_(text, screen.guiLeft() + x, screen.guiTop() + y, width, 0xFFFFFF);
				y += thisHeight;
			}
			else
				y+= thisHeight;
		}
		
	}
	
	@Override
	public void postRender(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) { }

	@Override
	public void tick() {
		
	}

	@Override
	public void closeTab() {
		this.display = null;
	}

}
