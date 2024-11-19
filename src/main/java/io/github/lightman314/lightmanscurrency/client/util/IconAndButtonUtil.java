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
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class IconAndButtonUtil {

	/**
	 * Texture file formerly used for the Trader Settings screen, but with the screen portion removed leaving only the widget textures.
	 */
	public static final ResourceLocation WIDGET_TEXTURE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "textures/gui/widgets.png");

	public static final Sprite SPRITE_PLUS = Sprite.SimpleSprite(TraderScreen.GUI_TEXTURE, TraderScreen.WIDTH + 18, 20, 10, 10);
	public static final Sprite SPRITE_MINUS = Sprite.SimpleSprite(TraderScreen.GUI_TEXTURE, TraderScreen.WIDTH + 28, 20, 10, 10);

	public static final Sprite SPRITE_QUICK_INSERT = Sprite.SimpleSprite(TraderScreen.GUI_TEXTURE, TraderScreen.WIDTH + 18, 0, 10, 10);
	public static final Sprite SPRITE_QUICK_EXTRACT = Sprite.SimpleSprite(TraderScreen.GUI_TEXTURE, TraderScreen.WIDTH + 28, 0, 10, 10);

	public static final Sprite SPRITE_CHECK_ACTIVE = Sprite.SimpleSprite(WIDGET_TEXTURE, 10, 200, 10, 10);
	public static final Sprite SPRITE_CHECK_INACTIVE = Sprite.SimpleSprite(WIDGET_TEXTURE, 10, 220, 10, 10);

	public static Supplier<Sprite> SPRITE_CHECK(Supplier<Boolean> isActive) { return () -> isActive.get() ? SPRITE_CHECK_ACTIVE : SPRITE_CHECK_INACTIVE; }

	public static final Sprite SPRITE_TOGGLE_ACTIVE = Sprite.SimpleSprite(WIDGET_TEXTURE,0,20,8,18);
	public static final Sprite SPRITE_TOGGLE_INACTIVE = Sprite.SimpleSprite(WIDGET_TEXTURE,8,20,8,18);

	public static Supplier<Sprite> SPRITE_TOGGLE(Supplier<Boolean> isActive) { return () -> isActive.get() ? SPRITE_TOGGLE_ACTIVE : SPRITE_TOGGLE_INACTIVE; }

	@Nonnull
	public static IconButton finishCollectCoinButton(@Nonnull IconButton.Builder builder, @Nonnull Player player, @Nonnull Supplier<TraderData> traderSource)
	{
		return builder
				.icon(IconUtil.ICON_COLLECT_COINS)
				.addon(EasyAddonHelper.tooltips(() -> {
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
				}))
				.addon(EasyAddonHelper.visibleCheck(() -> {
					TraderData trader = traderSource.get();
					if(trader == null)
						return false;
					return trader.hasPermission(player, Permissions.COLLECT_COINS) && !trader.hasBankAccount();
				}))
				.addon(EasyAddonHelper.activeCheck(() -> {
					TraderData trader = traderSource.get();
					if(trader == null)
						return false;
					return !trader.getInternalStoredMoney().isEmpty();
				}))
				.build();
	}
	@Nonnull
	public static IconButton finishCollectCoinButton(@Nonnull IconButton.Builder builder, @Nonnull Supplier<MoneyStorage> moneyStorageSource)
	{
		return builder
				.icon(IconUtil.ICON_COLLECT_COINS)
				.addon(EasyAddonHelper.tooltips(() -> {
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
				}))
				.build();
	}

	@Deprecated
	public static PlainButton plusButton(ScreenPosition pos, Consumer<EasyButton> pressable) { return plusButton(pos.x, pos.y, pressable); }
	@Deprecated
	public static PlainButton plusButton(int x, int y, Consumer<EasyButton> pressable) { return PlainButton.builder().position(x,y).pressAction(pressable).sprite(SPRITE_PLUS).build(); }
	@Deprecated
	public static PlainButton minusButton(ScreenPosition pos, Consumer<EasyButton> pressable) { return minusButton(pos.x, pos.y, pressable); }
	@Deprecated
	public static PlainButton minusButton(int x, int y, Consumer<EasyButton> pressable) { return PlainButton.builder().position(x,y).pressAction(pressable).sprite(SPRITE_MINUS).build(); }

	@Deprecated
	public static PlainButton checkmarkButton(ScreenPosition pos, Consumer<EasyButton> pressable, Supplier<Boolean> isActive) { return checkmarkButton(pos.x, pos.y, pressable, isActive); }
	@Deprecated
	public static PlainButton checkmarkButton(int x, int y, Consumer<EasyButton> pressable, Supplier<Boolean> isActive) { return PlainButton.builder().position(x,y).pressAction(pressable).sprite(SPRITE_CHECK(isActive)).build(); }

	//public static TextLogWindow traderLogWindow(AbstractContainerScreen<?> screen, Supplier<ILoggerSupport<?>> loggerSource) { return new TextLogWindow(screen, () -> loggerSource.get().getLogger()); }

	public static DropdownWidget interactionTypeDropdown(ScreenPosition pos, int width, InteractionType currentlySelected, Consumer<Integer> onSelect, List<InteractionType> blacklist) { return interactionTypeDropdown(pos.x, pos.y, width, currentlySelected, onSelect, blacklist); }
	public static DropdownWidget interactionTypeDropdown(int x, int y, int width, InteractionType currentlySelected, Consumer<Integer> onSelect, List<InteractionType> blacklist) {
		DropdownWidget.Builder builder = DropdownWidget.builder()
				.position(x,y)
				.width(width)
				.selected(currentlySelected.index)
				.selectAction(onSelect)
				.activeCheck((i) ->  !blacklist.contains(InteractionType.fromIndex(i)));
		for(int i = 0; i < InteractionType.size(); ++i)
			builder.option(InteractionType.fromIndex(i).getDisplayText());
		return builder.build();
	}

}
