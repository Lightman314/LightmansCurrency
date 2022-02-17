package io.github.lightman314.lightmanscurrency.client.util;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ILoggerSupport;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TextLogWindow;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class IconAndButtonUtil {
	
	public static final ResourceLocation ICON_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/icons.png");

	public static final IconData ICON_TRADER = IconData.of(ModItems.TRADING_CORE);
	public static final IconData ICON_STORAGE = IconData.of(Items.CHEST);
	
	public static final IconData ICON_COLLECT_COINS = IconData.of(ICON_TEXTURE, 0, 0);
	public static final IconData ICON_STORE_COINS = IconData.of(ICON_TEXTURE, 16, 0);
	public static final IconData ICON_TRADE_RULES = IconData.of(Items.BOOK);
	public static final IconData ICON_SETTINGS = IconData.of(ICON_TEXTURE, 32, 0);
	
	public static final IconData ICON_BACK = IconData.of(ICON_TEXTURE, 0, 16);
	public static final IconData ICON_LEFT = IconData.of(ICON_TEXTURE, 16, 16);
	public static final IconData ICON_RIGHT = IconData.of(ICON_TEXTURE, 32, 16);
	
	public static final IconData ICON_SHOW_LOGGER = IconData.of(new TranslationTextComponent("gui.button.lightmanscurrency.showlog"));
	public static final IconData ICON_CLEAR_LOGGER = IconData.of(new TranslationTextComponent("gui.button.lightmanscurrency.clearlog"));
	
	public static final IconData ICON_CREATIVE = IconData.of(ICON_TEXTURE, 48, 16);
	public static final IconData ICON_CREATIVE_OFF = IconData.of(ICON_TEXTURE, 64, 16);
	
	public static final IconData ICON_TICKET = IconData.of(ModItems.TICKET_MASTER);
	public static final IconData ICON_PAYGATE_ACTIVATE = IconData.of(Items.REDSTONE);
	
	public static final IconData ICON_PLUS = IconData.of(ICON_TEXTURE, 0, 32);
	public static final IconData ICON_X = IconData.of(ICON_TEXTURE, 16, 32);
	
	public static final IconData ICON_WHITELIST = IconData.of(ICON_TEXTURE, 32, 32);
	public static final IconData ICON_BLACKLIST = IconData.of(ICON_TEXTURE, 48, 32);
	public static final IconData ICON_COUNT = IconData.of(ICON_TEXTURE, 64, 32);
	public static final IconData ICON_COUNT_PLAYER = IconData.of(ICON_TEXTURE, 80, 32);
	
	public static final SimpleTooltip TOOLTIP_TRADER = new SimpleTooltip(new TranslationTextComponent("tooltip.lightmanscurrency.trader.opentrades"));
	public static final SimpleTooltip TOOLTIP_STORAGE = new SimpleTooltip(new TranslationTextComponent("tooltip.lightmanscurrency.trader.openstorage"));
	
	public static final String TOOLTIP_COLLECT_COINS = "tooltip.lightmanscurrency.trader.collectcoins";
	public static final SimpleTooltip TOOLTIP_STORE_COINS = new SimpleTooltip(new TranslationTextComponent("tooltip.lightmanscurrency.trader.storecoins"));
	
	public static final SimpleTooltip TOOLTIP_BACK_TO_TERMINAL = new SimpleTooltip(new TranslationTextComponent("tooltip.lightmanscurrency.trader.universaltrader.back"));
	
	public static final ITextComponent TOOLTIP_SHOW_LOGGER = new TranslationTextComponent("tooltip.lightmanscurrency.trader.log.show");
	public static final ITextComponent TOOLTIP_HIDE_LOGGER = new TranslationTextComponent("tooltip.lightmanscurrency.trader.log.hide");
	public static final SimpleTooltip TOOLTIP_CLEAR_LOGGER = new SimpleTooltip(new TranslationTextComponent("tooltip.lightmanscurrency.trader.log.clear"));
	
	public static final SimpleTooltip TOOLTIP_TRADE_RULES = new SimpleTooltip(new TranslationTextComponent("tooltip.lightmanscurrency.trader.traderules"));
	public static final SimpleTooltip TOOLTIP_OPEN_SETTINGS = new SimpleTooltip(new TranslationTextComponent("tooltip.lightmanscurrency.trader.settings"));
	
	public static final ITextComponent TOOLTIP_CREATIVE_ENABLE = new TranslationTextComponent("tooltip.lightmanscurrency.trader.creative.enable");
	public static final ITextComponent TOOLTIP_CREATIVE_DISABLE = new TranslationTextComponent("tooltip.lightmanscurrency.trader.creative.disable");
	
	public static final SimpleTooltip TOOLTIP_PAIR_TICKET = new SimpleTooltip(new TranslationTextComponent("tooltip.lightmanscurrency.paygate.setticket"));
	
	public static final SimpleTooltip TOOLTIP_PAYGATE_ACTIVATE = new SimpleTooltip(new TranslationTextComponent("tooltip.lightmanscurrency.paygate.paybutton"));
	
	public static IconButton traderButton(int x, int y, Button.IPressable pressable) { return new IconButton(x, y, pressable, ICON_TRADER, TOOLTIP_TRADER); }
	public static IconButton storageButton(int x, int y, Button.IPressable pressable) { return new IconButton(x, y, pressable, ICON_STORAGE, TOOLTIP_STORAGE); }
	
	public static IconButton collectCoinButton(int x, int y, Button.IPressable pressable, Supplier<ITrader> traderSource) { return new IconButton(x, y, pressable, ICON_COLLECT_COINS, new AdditiveTooltip(TOOLTIP_COLLECT_COINS, () -> new Object[] { traderSource.get().getStoredMoney().getString() })); }
	public static IconButton collectCoinButtonAlt(int x, int y, Button.IPressable pressable, Supplier<Object> storedCoinTextSource) { return new IconButton(x, y, pressable, ICON_COLLECT_COINS, new AdditiveTooltip(TOOLTIP_COLLECT_COINS, () -> new Object[] { storedCoinTextSource.get() })); }
	public static IconButton storeCoinButton(int x, int y, Button.IPressable pressable) { return new IconButton(x, y, pressable, ICON_STORE_COINS, TOOLTIP_STORE_COINS); }
	
	public static IconButton leftButton(int x, int y, Button.IPressable pressable) { return new IconButton(x, y, pressable, ICON_LEFT); }
	public static IconButton rightButton(int x, int y, Button.IPressable pressable) { return new IconButton(x, y, pressable, ICON_RIGHT); }
	
	public static IconButton backToTerminalButton(int x, int y, Button.IPressable pressable) { return new IconButton(x,y, pressable, ICON_BACK, TOOLTIP_BACK_TO_TERMINAL); }
	
	public static IconButton showLoggerButton(int x, int y, Button.IPressable pressable, Supplier<Boolean> isLoggerVisible) { return new IconButton(x,y,pressable, ICON_SHOW_LOGGER, new ToggleTooltip(isLoggerVisible, TOOLTIP_HIDE_LOGGER, TOOLTIP_SHOW_LOGGER)); }
	public static IconButton clearLoggerButton(int x, int y, Button.IPressable pressable) { return new IconButton(x, y, pressable, ICON_CLEAR_LOGGER, TOOLTIP_CLEAR_LOGGER); }
	
	public static IconButton tradeRuleButton(int x, int y, Button.IPressable pressable) { return new IconButton(x, y, pressable, ICON_TRADE_RULES, TOOLTIP_TRADE_RULES); }
	public static IconButton openSettingsButton(int x, int y, Button.IPressable pressable) { return new IconButton(x, y, pressable, ICON_SETTINGS, TOOLTIP_OPEN_SETTINGS); }
	
	public static IconButton creativeToggleButton(int x, int y, Button.IPressable pressable, Supplier<Boolean> isCreative) { return new IconButton(x, y, pressable, isCreative.get() ? ICON_CREATIVE : ICON_CREATIVE_OFF, new ToggleTooltip(isCreative, TOOLTIP_CREATIVE_DISABLE, TOOLTIP_CREATIVE_ENABLE)); }
	public static void updateCreativeToggleButton(IconButton creativeButton, boolean isCreative) { creativeButton.setIcon(isCreative ? ICON_CREATIVE : ICON_CREATIVE_OFF); }
	
	public static TextLogWindow traderLogWindow(ContainerScreen<?> screen, Supplier<ILoggerSupport<?>> loggerSource) { return new TextLogWindow(screen, () -> loggerSource.get().getLogger()); }
	
	
	
	public static void renderButtonTooltips(MatrixStack pose, int mouseX, int mouseY, List<Widget> widgets)
	{
		for(Widget w : widgets)
		{
			if(w instanceof Button && ((Button) w).isMouseOver(mouseX, mouseY))
				((Button)w).renderToolTip(pose, mouseX, mouseY);
		}
	}
	
	private static abstract class BaseTooltip implements Button.ITooltip
	{
		
		protected abstract ITextComponent getTooltip();
		
		@Override
		public void onTooltip(Button button, MatrixStack pose, int mouseX, int mouseY) {
			if(!button.visible || !button.active)
				return;
			Minecraft mc = Minecraft.getInstance();
			mc.currentScreen.renderTooltip(pose, this.getTooltip(), mouseX, mouseY);
		}
	}
	
	public static class SimpleTooltip extends BaseTooltip
	{
		private final ITextComponent tooltip;
		public SimpleTooltip(ITextComponent tooltip) { this.tooltip = tooltip; }
		@Override
		protected ITextComponent getTooltip() { return this.tooltip; }
	}
	
	public static class AdditiveTooltip extends BaseTooltip
	{
		private final String translationKey;
		private final Supplier<Object[]> inputSource;
		public AdditiveTooltip(String translationKey, Supplier<Object[]> inputSource) { this.translationKey = translationKey; this.inputSource = inputSource; }
		@Override
		protected ITextComponent getTooltip() { return new TranslationTextComponent(translationKey, inputSource.get()); }
	}
	
	public static class ToggleTooltip extends BaseTooltip
	{
		private final Supplier<Boolean> toggleSource;
		private final ITextComponent trueTooltip;
		private final ITextComponent falseTooltip;
		public ToggleTooltip(Supplier<Boolean> toggleSource, ITextComponent trueTooltip, ITextComponent falseTooltip) {
			this.toggleSource = toggleSource;
			this.trueTooltip = trueTooltip;
			this.falseTooltip = falseTooltip;
		}
		@Override
		protected ITextComponent getTooltip() { return this.toggleSource.get() ? this.trueTooltip : this.falseTooltip; }
	}
	
	public static class ChangingTooltip extends BaseTooltip
	{
		private final Supplier<Integer> indicator;
		private final List<ITextComponent> tooltips;
		private ChangingTooltip(Supplier<Integer> indicator, ITextComponent... tooltips)
		{
			this.indicator = indicator;
			this.tooltips = Lists.newArrayList(tooltips);
		}
		
		@Override
		protected ITextComponent getTooltip() {
			int index = this.indicator.get();
			return this.tooltips.get(MathUtil.clamp(index, 0, this.tooltips.size() - 1));
		}
		
	}
}
