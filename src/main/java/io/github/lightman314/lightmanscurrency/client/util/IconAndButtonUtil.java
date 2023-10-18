package io.github.lightman314.lightmanscurrency.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderInterfaceBlockEntity.ActiveMode;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderInterfaceBlockEntity.InteractionType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.dropdown.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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

	public static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(new ResourceLocation("widget/button"), new ResourceLocation("widget/button_disabled"), new ResourceLocation("widget/button_highlighted"));


	/**
	 * Texture file used for miscellaneous icons, most of which are refferred to in the IconData constants below.
	 */
	public static final ResourceLocation ICON_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/icons.png");
	/**
	 * Texture file formerly used for the Trader Settings screen, but with the screen portion removed leaving only the widget textures.
	 */
	public static final ResourceLocation WIDGET_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/widgets.png");

	public static final IconData ICON_TRADER = IconData.of(ModItems.TRADING_CORE);
	public static final IconData ICON_TRADER_ALT = IconData.of(ICON_TEXTURE, 80, 0);
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

	public static final IconData ICON_ALEX_HEAD;

	public static final IconData ICON_TAXES = IconData.of(ICON_TEXTURE, 96, 0);

	public static final Sprite SPRITE_PLUS = Sprite.SimpleSprite(TraderScreen.GUI_TEXTURE, TraderScreen.WIDTH + 18, 20, 10, 10);
	public static final Sprite SPRITE_MINUS = Sprite.SimpleSprite(TraderScreen.GUI_TEXTURE, TraderScreen.WIDTH + 28, 20, 10, 10);

	public static final Sprite SPRITE_CHECK_ACTIVE = Sprite.SimpleSprite(WIDGET_TEXTURE, 10, 200, 10, 10);
	public static final Sprite SPRITE_CHECK_INACTIVE = Sprite.SimpleSprite(WIDGET_TEXTURE, 10, 220, 10, 10);

	public static final IconData ICON_ONLINEMODE_TRUE = IconData.of(Items.PLAYER_HEAD);
	public static final IconData ICON_ONLINEMODE_FALSE = ICON_CHECKMARK;

	public static final Component TOOLTIP_TRADER = EasyText.translatable("tooltip.lightmanscurrency.trader.opentrades");
	public static final Component TOOLTIP_STORAGE = EasyText.translatable("tooltip.lightmanscurrency.trader.openstorage");
	
	public static final String TOOLTIP_COLLECT_COINS = "tooltip.lightmanscurrency.trader.collectcoins";
	public static final Component TOOLTIP_STORE_COINS = EasyText.translatable("tooltip.lightmanscurrency.trader.storecoins");
	
	public static final Component TOOLTIP_BACK_TO_TERMINAL = EasyText.translatable("tooltip.lightmanscurrency.trader.universaltrader.back");

	public static final Component TOOLTIP_TRADE_RULES = EasyText.translatable("tooltip.lightmanscurrency.trader.traderules");
	public static final MutableComponent TOOLTIP_TRADE_RULES_TRADER = EasyText.translatable("tooltip.lightmanscurrency.trader.traderules.trader");
	public static final MutableComponent TOOLTIP_TRADE_RULES_TRADE = EasyText.translatable("tooltip.lightmanscurrency.trader.traderules.trade");
	public static final Component TOOLTIP_OPEN_SETTINGS = EasyText.translatable("tooltip.lightmanscurrency.trader.settings");

	public static final Component TOOLTIP_CANNOT_BE_UNDONE = EasyText.translatable("tooltip.lightmanscurrency.warning").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW);

	public static final Component TOOLTIP_CREATIVE_ENABLE = EasyText.translatable("tooltip.lightmanscurrency.trader.creative.enable");
	public static final Component TOOLTIP_CREATIVE_DISABLE = EasyText.translatable("tooltip.lightmanscurrency.trader.creative.disable");
	
	public static final Component TOOLTIP_INTERFACE_ENABLE = EasyText.translatable("tooltip.lightmanscurrency.interface.enable");
	public static final Component TOOLTIP_INTERFACE_DISABLE = EasyText.translatable("tooltip.lightmanscurrency.interface.disable");
	
	public static final Component TOOLTIP_PERSISTENT_TRADER = EasyText.translatable("tooltip.lightmanscurrency.persistent.add.trader");
	public static final Component TOOLTIP_PERSISTENT_AUCTION = EasyText.translatable("tooltip.lightmanscurrency.persistent.add.auction");

	public static IconButton traderButton(int x, int y, Consumer<EasyButton> pressable) { return new IconButton(x, y, pressable, ICON_TRADER).withAddons(EasyAddonHelper.tooltip(TOOLTIP_TRADER)); }
	public static IconButton storageButton(int x, int y, Consumer<EasyButton> pressable) { return new IconButton(x, y, pressable, ICON_STORAGE).withAddons(EasyAddonHelper.tooltip(TOOLTIP_STORAGE)); }
	public static IconButton storageButton(int x, int y, Consumer<EasyButton> pressable, NonNullSupplier<Boolean> visiblityCheck) { return storageButton(x,y,pressable).withAddons(EasyAddonHelper.visibleCheck(visiblityCheck)); }
	
	public static IconButton collectCoinButton(int x, int y, Consumer<EasyButton> pressable, Player player, Supplier<TraderData> traderSource) {
		return new IconButton(x, y, pressable, ICON_COLLECT_COINS)
				.withAddons(EasyAddonHelper.toggleTooltip(() -> {
					TraderData trader = traderSource.get();
					return trader == null || trader.getStoredMoney().getValueNumber() <= 0;
				},EasyText::empty,
				() -> {
					TraderData trader = traderSource.get();
					if(trader != null)
						return EasyText.translatable(TOOLTIP_COLLECT_COINS, trader.getStoredMoney().getComponent());
					return EasyText.empty();
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
					return trader.getInternalStoredMoney().getValueNumber() > 0;
				}));
	}
	public static IconButton collectCoinButtonAlt(int x, int y, Consumer<EasyButton> pressable, Supplier<Object> storedCoinTextSource) { return new IconButton(x, y, pressable, ICON_COLLECT_COINS).withAddons(EasyAddonHelper.additiveTooltip2(TOOLTIP_COLLECT_COINS, storedCoinTextSource)); }
	public static IconButton collectCoinButtonAlt(ScreenPosition pos, Consumer<EasyButton> pressable, Supplier<Object> storedCoinTextSource) { return new IconButton(pos.x, pos.y, pressable, ICON_COLLECT_COINS).withAddons(EasyAddonHelper.additiveTooltip2(TOOLTIP_COLLECT_COINS, storedCoinTextSource)); }
	public static IconButton storeCoinButton(int x, int y, Consumer<EasyButton> pressable) { return new IconButton(x, y, pressable, ICON_STORE_COINS).withAddons(EasyAddonHelper.tooltip(TOOLTIP_STORE_COINS)); }
	
	public static IconButton leftButton(int x, int y, Consumer<EasyButton> pressable) { return new IconButton(x, y, pressable, ICON_LEFT); }
	public static IconButton rightButton(int x, int y, Consumer<EasyButton> pressable) { return new IconButton(x, y, pressable, ICON_RIGHT); }

	public static PlainButton plusButton(ScreenPosition pos, Consumer<EasyButton> pressable) { return plusButton(pos.x, pos.y, pressable); }
	public static PlainButton plusButton(int x, int y, Consumer<EasyButton> pressable) { return new PlainButton(x, y, pressable, SPRITE_PLUS); }
	public static PlainButton minusButton(ScreenPosition pos, Consumer<EasyButton> pressable) { return minusButton(pos.x, pos.y, pressable); }
	public static PlainButton minusButton(int x, int y, Consumer<EasyButton> pressable) { return new PlainButton(x, y, pressable, SPRITE_MINUS); }

	public static IconButton backToTerminalButton(int x, int y, Consumer<EasyButton> pressable) { return new IconButton(x,y, pressable, ICON_BACK).withAddons(EasyAddonHelper.tooltip(TOOLTIP_BACK_TO_TERMINAL)); }
	public static IconButton backToTerminalButton(int x, int y, Consumer<EasyButton> pressable, NonNullSupplier<Boolean> visibilityCheck) { return new IconButton(x,y, pressable, ICON_BACK).withAddons(EasyAddonHelper.tooltip(TOOLTIP_BACK_TO_TERMINAL), EasyAddonHelper.visibleCheck(visibilityCheck)); }

	public static IconButton tradeRuleButton(int x, int y, Consumer<EasyButton> pressable) { return new IconButton(x, y, pressable, ICON_TRADE_RULES).withAddons(EasyAddonHelper.tooltip(TOOLTIP_TRADE_RULES_TRADE)); }

	public static IconButton creativeToggleButton(ScreenPosition pos, Consumer<EasyButton> pressable, NonNullSupplier<Boolean> isCreative) { return creativeToggleButton(pos.x, pos.y, pressable, isCreative); }
	public static IconButton creativeToggleButton(int x, int y, Consumer<EasyButton> pressable, NonNullSupplier<Boolean> isCreative) { return new IconButton(x, y, pressable, ICON_CREATIVE(isCreative)).withAddons(EasyAddonHelper.toggleTooltip(isCreative, TOOLTIP_CREATIVE_DISABLE, TOOLTIP_CREATIVE_ENABLE)); }

	public static IconButton interfaceActiveToggleButton(int x, int y, Consumer<EasyButton> pressable, NonNullSupplier<Boolean> isActive) { return new IconButton(x, y, pressable, ICON_INTERFACE_ACTIVE(isActive)).withAddons(EasyAddonHelper.toggleTooltip(isActive, TOOLTIP_INTERFACE_DISABLE, TOOLTIP_INTERFACE_ENABLE)); }
	
	public static PlainButton quickInsertButton(ScreenPosition pos, Consumer<EasyButton> pressable) { return new PlainButton(pos, pressable, Sprite.SimpleSprite(TraderScreen.GUI_TEXTURE, TraderScreen.WIDTH + 18, 0, 10, 10)); }
	public static PlainButton quickExtractButton(ScreenPosition pos, Consumer<EasyButton> pressable) { return new PlainButton(pos, pressable,  Sprite.SimpleSprite(TraderScreen.GUI_TEXTURE, TraderScreen.WIDTH + 28, 0, 10, 10)); }
	
	public static PlainButton checkmarkButton(ScreenPosition pos, Consumer<EasyButton> pressable, NonNullSupplier<Boolean> isActive) { return checkmarkButton(pos.x, pos.y, pressable, isActive); }
	public static PlainButton checkmarkButton(int x, int y, Consumer<EasyButton> pressable, NonNullSupplier<Boolean> isActive) { return new PlainButton(x, y, pressable, () -> isActive.get() ? SPRITE_CHECK_ACTIVE : SPRITE_CHECK_INACTIVE); }

	//public static TextLogWindow traderLogWindow(AbstractContainerScreen<?> screen, Supplier<ILoggerSupport<?>> loggerSource) { return new TextLogWindow(screen, () -> loggerSource.get().getLogger()); }
	
	public static DropdownWidget interactionTypeDropdown(ScreenPosition pos, int width, InteractionType currentlySelected, Consumer<Integer> onSelect, List<InteractionType> blacklist) { return interactionTypeDropdown(pos.x, pos.y, width, currentlySelected, onSelect, blacklist); }
	public static DropdownWidget interactionTypeDropdown(int x, int y, int width, InteractionType currentlySelected, Consumer<Integer> onSelect, List<InteractionType> blacklist) {
		List<Component> options = new ArrayList<>();
		for(int i = 0; i < InteractionType.size(); ++i)
			options.add(InteractionType.fromIndex(i).getDisplayText());
		return new DropdownWidget(x, y, width, currentlySelected.index, onSelect, (index) ->  !blacklist.contains(InteractionType.fromIndex(index)), options);
	}

	static {
		ItemStack alexHead = new ItemStack(Items.PLAYER_HEAD);
		CompoundTag headData = new CompoundTag();
		CompoundTag skullOwner = new CompoundTag();
		skullOwner.putIntArray("Id", new int[] {-731408145, -304985227, -1778597514, 158507129 });
		CompoundTag properties = new CompoundTag();
		ListTag textureList = new ListTag();
		CompoundTag texture = new CompoundTag();
		texture.putString("Value", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjNiMDk4OTY3MzQwZGFhYzUyOTI5M2MyNGUwNDkxMDUwOWIyMDhlN2I5NDU2M2MzZWYzMWRlYzdiMzc1MCJ9fX0=");
		textureList.add(texture);
		properties.put("textures", textureList);
		skullOwner.put("Properties", properties);
		headData.put("SkullOwner", skullOwner);
		alexHead.setTag(headData);
		ICON_ALEX_HEAD = IconData.of(alexHead);
	}

}
