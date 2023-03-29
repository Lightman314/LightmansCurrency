package io.github.lightman314.lightmanscurrency.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderInterfaceBlockEntity.ActiveMode;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderInterfaceBlockEntity.InteractionType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ITooltipRenderable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.util.NonNullFunction;
import net.minecraftforge.common.util.NonNullSupplier;

public class IconAndButtonUtil {

	/**
	 * Texture file used for miscellaneous icons, most of which are refferred to in the IconData constants below.
	 */
	public static final ResourceLocation ICON_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/icons.png");
	/**
	 * Texture file formerly used for the Trader Settings screen, but with the screen portion removed leaving only the widget textures.
	 */
	public static final ResourceLocation WIDGET_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/widgets.png");

	public static final IconData ICON_TRADER = IconData.of(ModItems.TRADING_CORE);
	public static final IconData ICON_STORAGE = IconData.of(Items.CHEST);
	
	public static final IconData ICON_COLLECT_COINS = IconData.of(ICON_TEXTURE, 0, 0);
	public static final IconData ICON_STORE_COINS = IconData.of(ICON_TEXTURE, 16, 0);
	public static final IconData ICON_TRADE_RULES = IconData.of(Items.BOOK);
	public static final IconData ICON_SETTINGS = IconData.of(ICON_TEXTURE, 32, 0);
	
	public static final IconData ICON_BACK = IconData.of(ICON_TEXTURE, 0, 16);
	public static final IconData ICON_LEFT = IconData.of(ICON_TEXTURE, 16, 16);
	public static final IconData ICON_RIGHT = IconData.of(ICON_TEXTURE, 32, 16);
	public static final IconData ICON_UP = IconData.of(ICON_TEXTURE, 112, 16);
	public static final IconData ICON_DOWN = IconData.of(ICON_TEXTURE, 128, 16);

	public static final IconData ICON_SHOW_LOGGER = IconData.of(Items.WRITABLE_BOOK);
	public static final IconData ICON_CLEAR_LOGGER = IconData.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER));
	
	public static NonNullFunction<IconButton,IconData> ICON_CREATIVE(NonNullSupplier<Boolean> isCreative) {
		return b -> {
			boolean creative = b.isHoveredOrFocused() != isCreative.get();
			return creative ? ICON_CREATIVE_ON : ICON_CREATIVE_OFF;
		};
	}
	private static final IconData ICON_CREATIVE_ON = IconData.of(ICON_TEXTURE, 48, 16);
	private static final IconData ICON_CREATIVE_OFF = IconData.of(ICON_TEXTURE, 64, 16);
	
	public static final IconData ICON_PERSISTENT_DATA = IconData.of(ICON_TEXTURE, 80, 16);
	
	public static NonNullSupplier<IconData> ICON_INTERFACE_ACTIVE(NonNullSupplier<Boolean> isActive) {
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
	public static final IconData ICON_PRICE_FLUCTUATION = IconData.of(ICON_TEXTURE, 128, 32);
	
	public static final IconData ICON_TRADELIST = IconData.of(ICON_TEXTURE, 48, 0);
	
	public static final IconData ICON_MODE_DISABLED = IconData.of(Items.BARRIER);
	public static final IconData ICON_MODE_REDSTONE_OFF = IconData.of(ICON_TEXTURE, 64, 0);
	public static final IconData ICON_MODE_REDSTONE = IconData.of(Items.REDSTONE_TORCH);
	public static final IconData ICON_MODE_ALWAYS_ON = IconData.of(Items.REDSTONE_BLOCK);
	
	public static IconData GetIcon(ActiveMode mode) {
		return switch (mode) {
			case DISABLED -> ICON_MODE_DISABLED;
			case REDSTONE_OFF -> ICON_MODE_REDSTONE_OFF;
			case REDSTONE_ONLY -> ICON_MODE_REDSTONE;
			case ALWAYS_ON -> ICON_MODE_ALWAYS_ON;
		};
	}
	
	public static final IconData ICON_CHECKMARK = IconData.of(ICON_TEXTURE, 0, 48);
	
	public static final IconData ICON_ONLINEMODE_TRUE = IconData.of(Items.PLAYER_HEAD);
	public static final IconData ICON_ONLINEMODE_FALSE = ICON_CHECKMARK;
	
	public static final SimpleTooltip TOOLTIP_TRADER = new SimpleTooltip(Component.translatable("tooltip.lightmanscurrency.trader.opentrades"));
	public static final SimpleTooltip TOOLTIP_STORAGE = new SimpleTooltip(Component.translatable("tooltip.lightmanscurrency.trader.openstorage"));
	
	public static final String TOOLTIP_COLLECT_COINS = "tooltip.lightmanscurrency.trader.collectcoins";
	public static final SimpleTooltip TOOLTIP_STORE_COINS = new SimpleTooltip(Component.translatable("tooltip.lightmanscurrency.trader.storecoins"));
	
	public static final SimpleTooltip TOOLTIP_BACK_TO_TERMINAL = new SimpleTooltip(Component.translatable("tooltip.lightmanscurrency.trader.universaltrader.back"));

	public static final SimpleTooltip TOOLTIP_TRADE_RULES = new SimpleTooltip(Component.translatable("tooltip.lightmanscurrency.trader.traderules"));
	public static final MutableComponent TOOLTIP_TRADE_RULES_TRADER = Component.translatable("tooltip.lightmanscurrency.trader.traderules.trader");
	public static final MutableComponent TOOLTIP_TRADE_RULES_TRADE = Component.translatable("tooltip.lightmanscurrency.trader.traderules.trade");
	public static final SimpleTooltip TOOLTIP_OPEN_SETTINGS = new SimpleTooltip(Component.translatable("tooltip.lightmanscurrency.trader.settings"));
	
	public static final Component TOOLTIP_CREATIVE_ENABLE = Component.translatable("tooltip.lightmanscurrency.trader.creative.enable");
	public static final Component TOOLTIP_CREATIVE_DISABLE = Component.translatable("tooltip.lightmanscurrency.trader.creative.disable");
	
	public static final Component TOOLTIP_INTERFACE_ENABLE = Component.translatable("tooltip.lightmanscurrency.interface.enable");
	public static final Component TOOLTIP_INTERFACE_DISABLE = Component.translatable("tooltip.lightmanscurrency.interface.disable");
	
	public static final SimpleTooltip TOOLTIP_PERSISTENT_TRADER = new SimpleTooltip(Component.translatable("tooltip.lightmanscurrency.persistent.add.trader"));
	public static final SimpleTooltip TOOLTIP_PERSISTENT_AUCTION = new SimpleTooltip(Component.translatable("tooltip.lightmanscurrency.persistent.add.auction"));

	public static IconButton traderButton(int x, int y, Button.OnPress pressable) { return new IconButton(x, y, pressable, ICON_TRADER, TOOLTIP_TRADER); }
	public static IconButton storageButton(int x, int y, Button.OnPress pressable) { return new IconButton(x, y, pressable, ICON_STORAGE, TOOLTIP_STORAGE); }
	public static IconButton storageButton(int x, int y, Button.OnPress pressable, NonNullSupplier<Boolean> visiblityCheck) {
		IconButton button = storageButton(x,y,pressable);
		button.setVisiblityCheck(visiblityCheck);
		return button;
	}
	
	public static IconButton collectCoinButton(int x, int y, Button.OnPress pressable, Player player, Supplier<TraderData> traderSource) {
		IconButton button = new IconButton(x, y, pressable, ICON_COLLECT_COINS,
				new ToggleTooltip2(() -> {
					TraderData trader = traderSource.get();
					return trader == null || trader.getStoredMoney().getRawValue() <= 0;
				},
				new SimpleTooltip(EasyText.empty()),
				new AdditiveTooltip(TOOLTIP_COLLECT_COINS, () -> {
					TraderData trader = traderSource.get();
					if(trader != null)
						return new Object[] { trader.getStoredMoney().getString() };
					else
						return new Object[] {};
				}))
		);
		button.setVisiblityCheck(() -> {
			TraderData trader = traderSource.get();
			if(trader == null)
				return false;
			return trader.hasPermission(player, Permissions.COLLECT_COINS) && !trader.hasBankAccount();
		});
		button.setActiveCheck(() -> {
			TraderData trader = traderSource.get();
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
	
	public static IconButton tradeRuleButton(int x, int y, Button.OnPress pressable) { return new IconButton(x, y, pressable, ICON_TRADE_RULES, TOOLTIP_TRADE_RULES); }
	public static IconButton tradeRuleButton(int x, int y, Button.OnPress pressable, NonNullSupplier<Boolean> isTradeSelected) { return new IconButton(x, y, pressable, ICON_TRADE_RULES, new SimpleTooltip(TOOLTIP_TRADE_RULES_TRADE)); }
	public static IconButton openSettingsButton(int x, int y, Button.OnPress pressable) { return new IconButton(x, y, pressable, ICON_SETTINGS, TOOLTIP_OPEN_SETTINGS); }
	
	public static IconButton creativeToggleButton(int x, int y, Button.OnPress pressable, NonNullSupplier<Boolean> isCreative) { return new IconButton(x, y, pressable, ICON_CREATIVE(isCreative), new ToggleTooltip(isCreative, TOOLTIP_CREATIVE_DISABLE, TOOLTIP_CREATIVE_ENABLE)); }
	
	public static IconButton interfaceActiveToggleButton(int x, int y, Button.OnPress pressable, NonNullSupplier<Boolean> isActive) { return new IconButton(x, y, pressable, ICON_INTERFACE_ACTIVE(isActive), new ToggleTooltip(isActive, TOOLTIP_INTERFACE_DISABLE, TOOLTIP_INTERFACE_ENABLE)); }
	
	public static PlainButton quickInsertButton(int x, int y, Button.OnPress pressable) { return new PlainButton(x, y, 10, 10, pressable, TraderScreen.GUI_TEXTURE, TraderScreen.WIDTH + 18, 0); }
	public static PlainButton quickExtractButton(int x, int y, Button.OnPress pressable) { return new PlainButton(x, y, 10, 10, pressable, TraderScreen.GUI_TEXTURE, TraderScreen.WIDTH + 28, 0); }
	
	public static PlainButton checkmarkButton(int x, int y, Button.OnPress pressable, NonNullSupplier<Boolean> isActive) {
		return new PlainButton(x, y, 10, 10, pressable, WIDGET_TEXTURE, () -> Pair.of(10, isActive.get() ? 200 : 220));
	}
	
	//public static TextLogWindow traderLogWindow(AbstractContainerScreen<?> screen, Supplier<ILoggerSupport<?>> loggerSource) { return new TextLogWindow(screen, () -> loggerSource.get().getLogger()); }
	
	public static DropdownWidget interactionTypeDropdown(int x, int y, int width, Font font, InteractionType currentlySelected, Consumer<Integer> onSelect, Function<Button,Button> addButton, List<InteractionType> blacklist) {
		List<Component> options = new ArrayList<>();
		for(int i = 0; i < InteractionType.size(); ++i)
			options.add(InteractionType.fromIndex(i).getDisplayText());
		return new DropdownWidget(x, y, width, font, currentlySelected.index, onSelect, (index) ->  !blacklist.contains(InteractionType.fromIndex(index)), addButton, options);
	}

	private static abstract class BaseTooltip implements Supplier<Tooltip> {

		protected abstract Component getTooltip();

		@Override
		public Tooltip get() { return Tooltip.create(this.getTooltip()); }

	}

	public static class SimpleTooltip extends BaseTooltip
	{
		private final Component tooltip;
		public SimpleTooltip(Component tooltip) { this.tooltip = tooltip; }
		@Override
		protected Component getTooltip() { return this.tooltip; }
	}

	public static class SuppliedTooltip extends BaseTooltip
	{
		private final NonNullSupplier<Component> tooltipSource;
		public SuppliedTooltip(NonNullSupplier<Component> tooltipSource) { this.tooltipSource = tooltipSource; }
		@Override
		protected Component getTooltip() { return this.tooltipSource.get(); }
	}

	public static class AdditiveTooltip extends BaseTooltip
	{
		private final String translationKey;
		private final Supplier<Object[]> inputSource;
		public AdditiveTooltip(String translationKey, Supplier<Object[]> inputSource) { this.translationKey = translationKey; this.inputSource = inputSource; }
		@Override
		protected Component getTooltip() { return Component.translatable(translationKey, inputSource.get()); }
	}

	public static class ToggleTooltip extends BaseTooltip
	{
		private final NonNullSupplier<Boolean> toggleSource;
		private final Component trueTooltip;
		private final Component falseTooltip;
		public ToggleTooltip(NonNullSupplier<Boolean> toggleSource, Component trueTooltip, Component falseTooltip) {
			this.toggleSource = toggleSource;
			this.trueTooltip = trueTooltip;
			this.falseTooltip = falseTooltip;
		}
		@Override
		protected Component getTooltip() { return this.toggleSource.get() ? this.trueTooltip : this.falseTooltip; }
	}

	public static class ToggleTooltip2 implements Supplier<Tooltip>
	{
		private final NonNullSupplier<Boolean> toggleSource;
		private final Supplier<Tooltip> trueTooltip;
		private final Supplier<Tooltip> falseTooltip;
		public ToggleTooltip2(NonNullSupplier<Boolean> toggleSource, Supplier<Tooltip> trueTooltip, Supplier<Tooltip>  falseTooltip) {
			this.toggleSource = toggleSource;
			this.trueTooltip = trueTooltip;
			this.falseTooltip = falseTooltip;
		}
		@Override
		public Tooltip get() { return this.toggleSource.get() ? this.trueTooltip.get() : this.falseTooltip.get(); }
	}

	public static class ChangingTooltip extends BaseTooltip
	{
		private final Supplier<Integer> indicator;
		private final List<Component> tooltips;
		public ChangingTooltip(Supplier<Integer> indicator, Component... tooltips)
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

	public static void ManuallyRenderTooltips(Screen screen, PoseStack pose, int mouseX, int mouseY, List<Renderable> widgets) {
		for(Renderable widget : widgets)
		{
			if(widget instanceof AbstractWidget w && widget instanceof ITooltipRenderable tr && w.isMouseOver(mouseX, mouseY)) {
				Tooltip tooltip = tr.getTooltip();
				screen.renderTooltip(pose, tooltip.toCharSequence(screen.getMinecraft()), mouseX, mouseY);
			}
		}
	}
}
