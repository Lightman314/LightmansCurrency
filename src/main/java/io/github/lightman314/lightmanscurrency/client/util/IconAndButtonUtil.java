package io.github.lightman314.lightmanscurrency.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity.InteractionType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.dropdown.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.NonNullSupplier;

public class IconAndButtonUtil {

	/**
	 * Texture file formerly used for the Trader Settings screen, but with the screen portion removed leaving only the widget textures.
	 */
	public static final ResourceLocation WIDGET_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/widgets.png");

	public static final Sprite SPRITE_PLUS = Sprite.SimpleSprite(TraderScreen.GUI_TEXTURE, TraderScreen.WIDTH + 18, 20, 10, 10);
	public static final Sprite SPRITE_MINUS = Sprite.SimpleSprite(TraderScreen.GUI_TEXTURE, TraderScreen.WIDTH + 28, 20, 10, 10);

	public static final Sprite SPRITE_CHECK_ACTIVE = Sprite.SimpleSprite(WIDGET_TEXTURE, 10, 200, 10, 10);
	public static final Sprite SPRITE_CHECK_INACTIVE = Sprite.SimpleSprite(WIDGET_TEXTURE, 10, 220, 10, 10);

	public static final Sprite SPRITE_TOGGLE_ACTIVE = Sprite.SimpleSprite(WIDGET_TEXTURE,0,20,8,18);
	public static final Sprite SPRITE_TOGGLE_INACTIVE = Sprite.SimpleSprite(WIDGET_TEXTURE,8,20,8,18);

	public static IconButton traderButton(int x, int y, Consumer<EasyButton> pressable) { return new IconButton(x, y, pressable, IconUtil.ICON_TRADER).withAddons(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADER_OPEN_TRADES)); }
	public static IconButton storageButton(int x, int y, Consumer<EasyButton> pressable) { return new IconButton(x, y, pressable, IconUtil.ICON_STORAGE).withAddons(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADER_OPEN_STORAGE)); }
	public static IconButton storageButton(int x, int y, Consumer<EasyButton> pressable, NonNullSupplier<Boolean> visiblityCheck) { return storageButton(x,y,pressable).withAddons(EasyAddonHelper.visibleCheck(visiblityCheck)); }
	
	public static IconButton collectCoinButton(int x, int y, Consumer<EasyButton> pressable, Player player, Supplier<TraderData> traderSource) {
		return new IconButton(x, y, pressable, IconUtil.ICON_COLLECT_COINS)
				.withAddons(EasyAddonHelper.tooltips(() -> {
					TraderData trader = traderSource.get();
					if(trader != null && !trader.hasBankAccount() && !trader.getStoredMoney().getStoredMoney().isEmpty())
					{
						List<Component> result = new ArrayList<>();
						result.add(LCText.TOOLTIP_TRADER_COLLECT_COINS.get());
						IMoneyHolder storage = trader.getStoredMoney();
						for(MoneyValue value : storage.getStoredMoney().allValues())
							result.add(value.getText());
						return result;
					}
					return new ArrayList<>();
				}),
				EasyAddonHelper.visibleCheck(() -> {
					TraderData trader = traderSource.get();
					if(trader == null)
						return false;
					return trader.hasPermission(player, Permissions.COLLECT_COINS) && !trader.hasBankAccount();
				}),
				EasyAddonHelper.activeCheck(() -> {
					TraderData trader = traderSource.get();
					if(trader == null)
						return false;
					return !trader.getInternalStoredMoney().isEmpty();
				}));
	}
	public static IconButton collectCoinButtonAlt(int x, int y, Consumer<EasyButton> pressable, Supplier<MoneyStorage> moneyStorageSource) { return collectCoinButtonAlt(ScreenPosition.of(x,y), pressable, moneyStorageSource); }
	public static IconButton collectCoinButtonAlt(ScreenPosition pos, Consumer<EasyButton> pressable, Supplier<MoneyStorage> moneyStorageSource) {
		return new IconButton(pos.x, pos.y, pressable, IconUtil.ICON_COLLECT_COINS)
				.withAddons(EasyAddonHelper.tooltips(() -> {
					MoneyStorage storage = moneyStorageSource.get();
					if(storage != null && !storage.isEmpty())
					{
						List<Component> result = new ArrayList<>();
						result.add(LCText.TOOLTIP_TRADER_COLLECT_COINS.get());
						for(MoneyValue value : storage.allValues())
							result.add(value.getText());
						return result;
					}
					return new ArrayList<>();
		}));
	}
	public static IconButton storeCoinButton(int x, int y, Consumer<EasyButton> pressable) { return new IconButton(x, y, pressable, IconUtil.ICON_STORE_COINS).withAddons(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADER_STORE_COINS)); }
	
	public static IconButton leftButton(int x, int y, Consumer<EasyButton> pressable) { return new IconButton(x, y, pressable, IconUtil.ICON_LEFT); }
	public static IconButton rightButton(int x, int y, Consumer<EasyButton> pressable) { return new IconButton(x, y, pressable, IconUtil.ICON_RIGHT); }

	public static PlainButton plusButton(ScreenPosition pos, Consumer<EasyButton> pressable) { return plusButton(pos.x, pos.y, pressable); }
	public static PlainButton plusButton(int x, int y, Consumer<EasyButton> pressable) { return new PlainButton(x, y, pressable, SPRITE_PLUS); }
	public static PlainButton minusButton(ScreenPosition pos, Consumer<EasyButton> pressable) { return minusButton(pos.x, pos.y, pressable); }
	public static PlainButton minusButton(int x, int y, Consumer<EasyButton> pressable) { return new PlainButton(x, y, pressable, SPRITE_MINUS); }

	public static IconButton backToTerminalButton(int x, int y, Consumer<EasyButton> pressable) { return new IconButton(x,y, pressable, IconUtil.ICON_BACK).withAddons(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADER_NETWORK_BACK)); }
	public static IconButton backToTerminalButton(int x, int y, Consumer<EasyButton> pressable, NonNullSupplier<Boolean> visibilityCheck) { return new IconButton(x,y, pressable, IconUtil.ICON_BACK).withAddons(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADER_NETWORK_BACK), EasyAddonHelper.visibleCheck(visibilityCheck)); }

	public static IconButton tradeRuleButton(int x, int y, Consumer<EasyButton> pressable) { return new IconButton(x, y, pressable, IconUtil.ICON_TRADE_RULES).withAddons(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADER_TRADE_RULES_TRADE)); }

	public static IconButton creativeToggleButton(ScreenPosition pos, Consumer<EasyButton> pressable, NonNullSupplier<Boolean> isCreative) { return creativeToggleButton(pos.x, pos.y, pressable, isCreative); }
	public static IconButton creativeToggleButton(int x, int y, Consumer<EasyButton> pressable, NonNullSupplier<Boolean> isCreative) { return new IconButton(x, y, pressable, IconUtil.ICON_CREATIVE(isCreative)).withAddons(EasyAddonHelper.toggleTooltip(isCreative, LCText.TOOLTIP_TRADER_SETTINGS_CREATIVE_DISABLE.get(), LCText.TOOLTIP_TRADER_SETTINGS_CREATIVE_ENABLE.get())); }

	public static PlainButton quickInsertButton(ScreenPosition pos, Consumer<EasyButton> pressable) { return new PlainButton(pos, pressable, Sprite.SimpleSprite(TraderScreen.GUI_TEXTURE, TraderScreen.WIDTH + 18, 0, 10, 10)); }
	public static PlainButton quickExtractButton(ScreenPosition pos, Consumer<EasyButton> pressable) { return new PlainButton(pos, pressable,  Sprite.SimpleSprite(TraderScreen.GUI_TEXTURE, TraderScreen.WIDTH + 28, 0, 10, 10)); }
	
	public static PlainButton checkmarkButton(ScreenPosition pos, Consumer<EasyButton> pressable, NonNullSupplier<Boolean> isActive) { return checkmarkButton(pos.x, pos.y, pressable, isActive); }
	public static PlainButton checkmarkButton(int x, int y, Consumer<EasyButton> pressable, NonNullSupplier<Boolean> isActive) { return new PlainButton(x, y, pressable, () -> isActive.get() ? SPRITE_CHECK_ACTIVE : SPRITE_CHECK_INACTIVE); }

	public static PlainButton toggleButton(ScreenPosition pos, Consumer<EasyButton> pressable, Supplier<Boolean> isActive) { return new PlainButton(pos, pressable, () -> isActive.get() ? SPRITE_TOGGLE_ACTIVE : SPRITE_TOGGLE_INACTIVE); }

	//public static TextLogWindow traderLogWindow(AbstractContainerScreen<?> screen, Supplier<ILoggerSupport<?>> loggerSource) { return new TextLogWindow(screen, () -> loggerSource.get().getLogger()); }
	
	public static DropdownWidget interactionTypeDropdown(ScreenPosition pos, int width, InteractionType currentlySelected, Consumer<Integer> onSelect, List<InteractionType> blacklist) { return interactionTypeDropdown(pos.x, pos.y, width, currentlySelected, onSelect, blacklist); }
	public static DropdownWidget interactionTypeDropdown(int x, int y, int width, InteractionType currentlySelected, Consumer<Integer> onSelect, List<InteractionType> blacklist) {
		List<Component> options = new ArrayList<>();
		for(int i = 0; i < InteractionType.size(); ++i)
			options.add(InteractionType.fromIndex(i).getDisplayText());
		return new DropdownWidget(x, y, width, currentlySelected.index, onSelect, (index) ->  !blacklist.contains(InteractionType.fromIndex(index)), options);
	}

}
