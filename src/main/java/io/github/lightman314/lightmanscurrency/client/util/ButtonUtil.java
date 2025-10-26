package io.github.lightman314.lightmanscurrency.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class ButtonUtil {

	public static IconButton finishCollectCoinButton(IconButton.Builder builder, Player player, Supplier<TraderData> traderSource)
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
                    if(trader.isCreative() && trader.getInternalStoredMoney().isEmpty())
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
	
	public static IconButton finishCollectCoinButton(IconButton.Builder builder, Supplier<MoneyStorage> moneyStorageSource)
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
}
