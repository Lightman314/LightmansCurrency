package io.github.lightman314.lightmanscurrency.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ILoggerSupport;
import io.github.lightman314.lightmanscurrency.blockentity.UniversalTraderInterfaceBlockEntity.InteractionType;
import io.github.lightman314.lightmanscurrency.client.gui.widget.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TextLogWindow;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.util.NonNullSupplier;

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
	
	public static final IconData ICON_SHOW_LOGGER = IconData.of(Items.WRITABLE_BOOK);
	public static final IconData ICON_CLEAR_LOGGER = IconData.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER));
	
	public static final NonNullSupplier<IconData> ICON_CREATIVE(Supplier<Boolean> isCreative) {
		return () -> isCreative.get() ? ICON_CREATIVE_OFF : ICON_CREATIVE_ON;
	}
	private static final IconData ICON_CREATIVE_ON = IconData.of(ICON_TEXTURE, 48, 16);
	private static final IconData ICON_CREATIVE_OFF = IconData.of(ICON_TEXTURE, 64, 16);
	
	public static final IconData ICON_PERSISTENT_DATA = IconData.of(ICON_TEXTURE, 80, 16);
	
	public static final IconData ICON_TICKET = IconData.of(ModItems.TICKET_MASTER);
	public static final IconData ICON_PAYGATE_ACTIVATE = IconData.of(Items.REDSTONE);
	
	public static final NonNullSupplier<IconData> ICON_INTERFACE_ACTIVE(Supplier<Boolean> isActive) {
		return () -> isActive.get() ? ICON_INTERFACE_ON : ICON_INTERFACE_OFF;
	}
	private static final IconData ICON_INTERFACE_ON = IconData.of(Items.REDSTONE_TORCH);
	private static final IconData ICON_INTERFACE_OFF = IconData.of(Items.TORCH);
	
	public static final IconData ICON_PLUS = IconData.of(ICON_TEXTURE, 0, 32);
	public static final IconData ICON_X = IconData.of(ICON_TEXTURE, 16, 32);
	
	public static final IconData ICON_WHITELIST = IconData.of(ICON_TEXTURE, 32, 32);
	public static final IconData ICON_BLACKLIST = IconData.of(ICON_TEXTURE, 48, 32);
	public static final IconData ICON_COUNT = IconData.of(ICON_TEXTURE, 64, 32);
	public static final IconData ICON_COUNT_PLAYER = IconData.of(ICON_TEXTURE, 80, 32);
	public static final IconData ICON_TIMED_SALE = IconData.of(Items.CLOCK);
	public static final IconData ICON_DISCOUNT_LIST = IconData.of(ICON_TEXTURE, 96, 32);
	public static final IconData ICON_FREE_SAMPLE = IconData.of(ICON_TEXTURE, 112, 32);
	
	public static final IconData ICON_TRADELIST = IconData.of(ICON_TEXTURE, 48, 0);
	
	public static final SimpleTooltip TOOLTIP_TRADER = new SimpleTooltip(new TranslatableComponent("tooltip.lightmanscurrency.trader.opentrades"));
	public static final SimpleTooltip TOOLTIP_STORAGE = new SimpleTooltip(new TranslatableComponent("tooltip.lightmanscurrency.trader.openstorage"));
	
	public static final String TOOLTIP_COLLECT_COINS = "tooltip.lightmanscurrency.trader.collectcoins";
	public static final SimpleTooltip TOOLTIP_STORE_COINS = new SimpleTooltip(new TranslatableComponent("tooltip.lightmanscurrency.trader.storecoins"));
	
	public static final SimpleTooltip TOOLTIP_BACK_TO_TERMINAL = new SimpleTooltip(new TranslatableComponent("tooltip.lightmanscurrency.trader.universaltrader.back"));
	
	public static final Component TOOLTIP_SHOW_LOGGER = new TranslatableComponent("tooltip.lightmanscurrency.trader.log.show");
	public static final Component TOOLTIP_HIDE_LOGGER = new TranslatableComponent("tooltip.lightmanscurrency.trader.log.hide");
	public static final SimpleTooltip TOOLTIP_CLEAR_LOGGER = new SimpleTooltip(new TranslatableComponent("tooltip.lightmanscurrency.trader.log.clear"));
	
	public static final SimpleTooltip TOOLTIP_TRADE_RULES = new SimpleTooltip(new TranslatableComponent("tooltip.lightmanscurrency.trader.traderules"));
	public static final SimpleTooltip TOOLTIP_OPEN_SETTINGS = new SimpleTooltip(new TranslatableComponent("tooltip.lightmanscurrency.trader.settings"));
	
	public static final Component TOOLTIP_CREATIVE_ENABLE = new TranslatableComponent("tooltip.lightmanscurrency.trader.creative.enable");
	public static final Component TOOLTIP_CREATIVE_DISABLE = new TranslatableComponent("tooltip.lightmanscurrency.trader.creative.disable");
	
	public static final Component TOOLTIP_INTERFACE_ENABLE = new TranslatableComponent("tooltip.lightmanscurrency.interface.enable");
	public static final Component TOOLTIP_INTERFACE_DISABLE = new TranslatableComponent("tooltip.lightmanscurrency.interface.disable");
	
	public static final SimpleTooltip TOOLTIP_PERSISTENT_DATA = new SimpleTooltip(new TranslatableComponent("tooltip.lightmanscurrency.persistenttrader.copy"));
	
	public static final SimpleTooltip TOOLTIP_PAIR_TICKET = new SimpleTooltip(new TranslatableComponent("tooltip.lightmanscurrency.paygate.setticket"));
	
	public static final SimpleTooltip TOOLTIP_PAYGATE_ACTIVATE = new SimpleTooltip(new TranslatableComponent("tooltip.lightmanscurrency.paygate.paybutton"));
	
	public static IconButton traderButton(int x, int y, Button.OnPress pressable) { return new IconButton(x, y, pressable, ICON_TRADER, TOOLTIP_TRADER); }
	public static IconButton storageButton(int x, int y, Button.OnPress pressable) { return new IconButton(x, y, pressable, ICON_STORAGE, TOOLTIP_STORAGE); }
	public static IconButton storageButton(int x, int y, Button.OnPress pressable, NonNullSupplier<Boolean> visiblityCheck) {
		IconButton button = storageButton(x,y,pressable);
		button.setVisiblityCheck(visiblityCheck);
		return button;
	}
	
	public static IconButton collectCoinButton(int x, int y, Button.OnPress pressable, Supplier<ITrader> traderSource) {
		IconButton button = new IconButton(x, y, pressable, ICON_COLLECT_COINS, new AdditiveTooltip(TOOLTIP_COLLECT_COINS, () -> new Object[] { traderSource.get().getStoredMoney().getString() }));
		button.setVisiblityCheck(() -> {
			ITrader trader = traderSource.get();
			if(trader == null)
				return false;
			return !trader.getCoreSettings().hasBankAccount();
		});
		button.setActiveCheck(() -> {
			ITrader trader = traderSource.get();
			if(trader == null)
				return false;
			return trader.getInternalStoredMoney().getRawValue() > 0;
		});
		return button;
	}
	public static IconButton collectCoinButtonAlt(int x, int y, Button.OnPress pressable, Supplier<Object> storedCoinTextSource) { return new IconButton(x, y, pressable, ICON_COLLECT_COINS, new AdditiveTooltip(TOOLTIP_COLLECT_COINS, () -> new Object[] { storedCoinTextSource.get() })); }
	public static IconButton storeCoinButton(int x, int y, Button.OnPress pressable) { return new IconButton(x, y, pressable, ICON_STORE_COINS, TOOLTIP_STORE_COINS); }
	
	public static IconButton leftButton(int x, int y, Button.OnPress pressable) { return new IconButton(x, y, pressable, ICON_LEFT); }
	public static IconButton rightButton(int x, int y, Button.OnPress pressable) { return new IconButton(x, y, pressable, ICON_RIGHT); }
	
	public static IconButton backToTerminalButton(int x, int y, Button.OnPress pressable) { return new IconButton(x,y, pressable, ICON_BACK, TOOLTIP_BACK_TO_TERMINAL); }
	public static IconButton backToTerminalButton(int x, int y, Button.OnPress pressable, NonNullSupplier<Boolean> visibilityCheck) {
		IconButton button = new IconButton(x,y, pressable, ICON_BACK, TOOLTIP_BACK_TO_TERMINAL);
		button.setVisiblityCheck(visibilityCheck);
		return button;
	}
	
	public static IconButton showLoggerButton(int x, int y, Button.OnPress pressable, Supplier<Boolean> isLoggerVisible) { return new IconButton(x,y,pressable, ICON_SHOW_LOGGER, new ToggleTooltip(isLoggerVisible, TOOLTIP_HIDE_LOGGER, TOOLTIP_SHOW_LOGGER)); }
	public static IconButton clearLoggerButton(int x, int y, Button.OnPress pressable) { return new IconButton(x, y, pressable, ICON_CLEAR_LOGGER, TOOLTIP_CLEAR_LOGGER); }
	
	public static IconButton tradeRuleButton(int x, int y, Button.OnPress pressable) { return new IconButton(x, y, pressable, ICON_TRADE_RULES, TOOLTIP_TRADE_RULES); }
	public static IconButton openSettingsButton(int x, int y, Button.OnPress pressable) { return new IconButton(x, y, pressable, ICON_SETTINGS, TOOLTIP_OPEN_SETTINGS); }
	
	public static IconButton creativeToggleButton(int x, int y, Button.OnPress pressable, Supplier<Boolean> isCreative) { return new IconButton(x, y, pressable, ICON_CREATIVE(isCreative), new ToggleTooltip(isCreative, TOOLTIP_CREATIVE_DISABLE, TOOLTIP_CREATIVE_ENABLE)); }
	
	public static IconButton interfaceActiveToggleButton(int x, int y, Button.OnPress pressable, Supplier<Boolean> isActive) { return new IconButton(x, y, pressable, ICON_INTERFACE_ACTIVE(isActive), new ToggleTooltip(isActive, TOOLTIP_INTERFACE_DISABLE, TOOLTIP_INTERFACE_ENABLE)); }
	
	
	public static TextLogWindow traderLogWindow(AbstractContainerScreen<?> screen, Supplier<ILoggerSupport<?>> loggerSource) { return new TextLogWindow(screen, () -> loggerSource.get().getLogger()); }
	
	public static DropdownWidget interactionTypeDropdown(int x, int y, int width, Font font, InteractionType currentlySelected, Consumer<Integer> onSelect, Function<Button,Button> addButton) {
		List<Component> options = new ArrayList<>();
		for(int i = 0; i < InteractionType.size(); ++i)
			options.add(InteractionType.fromIndex(i).getDisplayText());
		return new DropdownWidget(x, y, width, font, currentlySelected.index, onSelect, addButton, options);
	}
	
	
	public static void renderButtonTooltips(PoseStack pose, int mouseX, int mouseY, List<Widget> widgets)
	{
		for(Widget w : widgets)
		{
			if(w instanceof Button && ((Button) w).isMouseOver(mouseX, mouseY))
				((Button)w).renderToolTip(pose, mouseX, mouseY);
		}
	}
	
	private static abstract class BaseTooltip implements Button.OnTooltip
	{
		
		protected abstract Component getTooltip();
		
		@Override
		public void onTooltip(Button button, PoseStack pose, int mouseX, int mouseY) {
			if(!button.visible || !button.active)
				return;
			Minecraft mc = Minecraft.getInstance();
			mc.screen.renderTooltip(pose, this.getTooltip(), mouseX, mouseY);
		}
	}
	
	public static class SimpleTooltip extends BaseTooltip
	{
		private final Component tooltip;
		public SimpleTooltip(Component tooltip) { this.tooltip = tooltip; }
		@Override
		protected Component getTooltip() { return this.tooltip; }
	}
	
	public static class AdditiveTooltip extends BaseTooltip
	{
		private final String translationKey;
		private final Supplier<Object[]> inputSource;
		public AdditiveTooltip(String translationKey, Supplier<Object[]> inputSource) { this.translationKey = translationKey; this.inputSource = inputSource; }
		@Override
		protected Component getTooltip() { return new TranslatableComponent(translationKey, inputSource.get()); }
	}
	
	public static class ToggleTooltip extends BaseTooltip
	{
		private final Supplier<Boolean> toggleSource;
		private final Component trueTooltip;
		private final Component falseTooltip;
		public ToggleTooltip(Supplier<Boolean> toggleSource, Component trueTooltip, Component falseTooltip) {
			this.toggleSource = toggleSource;
			this.trueTooltip = trueTooltip;
			this.falseTooltip = falseTooltip;
		}
		@Override
		protected Component getTooltip() { return this.toggleSource.get() ? this.trueTooltip : this.falseTooltip; }
	}
	
	public static class ChangingTooltip extends BaseTooltip
	{
		private final Supplier<Integer> indicator;
		private final List<Component> tooltips;
		private ChangingTooltip(Supplier<Integer> indicator, Component... tooltips)
		{
			this.indicator = indicator;
			this.tooltips = Lists.newArrayList(tooltips);
		}
		
		@Override
		protected Component getTooltip() {
			int index = this.indicator.get();
			return this.tooltips.get(MathUtil.clamp(index, 0, this.tooltips.size() - 1));
		}
		
	}
}
